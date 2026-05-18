package ru.semka.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.semka.domain.entity.SplitExpenseEntity
import ru.semka.domain.entity.TransactionEntity
import ru.semka.domain.enums.OperationType
import ru.semka.dto.CreateTransactionRequest
import ru.semka.dto.SplitRequest
import ru.semka.dto.TransactionDto
import ru.semka.dto.UpdateTransactionRequest
import ru.semka.exception.ApiException
import ru.semka.repository.CategoryRepository
import ru.semka.repository.SplitExpenseRepository
import ru.semka.repository.TransactionRepository
import ru.semka.security.AppUserDetails
import java.math.BigDecimal

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val splitRepository: SplitExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val access: WalletAccessService,
    private val walletService: WalletService,
    private val auditAsync: AuditAsyncService,
) {
    fun get(id: Long, user: AppUserDetails): TransactionDto {
        val tx = transactionRepository.findDetailedById(id)
            .orElseThrow { ApiException("NOT_FOUND", "операция не найдена") }
        access.requireMember(tx.walletId, user)
        return tx.toDto()
    }

    fun list(walletId: Long, tip: OperationType?, user: AppUserDetails): List<TransactionDto> {
        access.requireMember(walletId, user)
        val list = if (tip != null) {
            transactionRepository.findByWalletIdAndTypeOrderByTransactionDateDescCreatedAtDesc(walletId, tip)
        } else {
            transactionRepository.findByWalletIdOrderByTransactionDateDescCreatedAtDesc(walletId)
        }
        return list.map { it.toDto() }
    }

    @Transactional
    fun create(req: CreateTransactionRequest, user: AppUserDetails): TransactionDto {
        access.requireMember(req.walletId, user)
        validateCategory(req.walletId, req.categoryId, req.type)
        val tx = transactionRepository.save(
            TransactionEntity(
                walletId = req.walletId,
                authorId = user.id,
                categoryId = req.categoryId,
                type = req.type,
                amount = req.amount.money(),
                transactionDate = req.transactionDate,
                comment = req.comment,
            ),
        )
        walletService.evictBalance(req.walletId)
        return transactionRepository.findById(tx.id!!).get().toDto()
    }

    @Transactional
    fun split(req: SplitRequest, user: AppUserDetails) {
        access.requireMember(req.walletId, user)
        validateCategory(req.walletId, req.categoryId, OperationType.EXPENSE)
        val sumShares = req.shares.sumOf { it.shareAmount }.money()
        if (sumShares.subtract(req.totalAmount).abs() > BigDecimal("0.02")) {
            throw ApiException("VALIDATION_ERROR", "сумма долей ($sumShares) должна быть ${req.totalAmount}")
        }
        val tx = transactionRepository.save(
            TransactionEntity(
                walletId = req.walletId,
                authorId = user.id,
                categoryId = req.categoryId,
                type = OperationType.EXPENSE,
                amount = req.totalAmount.money(),
                transactionDate = req.transactionDate,
                comment = req.comment ?: "разделили чек",
            ),
        )
        req.shares.forEach { s ->
            splitRepository.save(
                SplitExpenseEntity(
                    transactionId = tx.id!!,
                    userId = s.userId,
                    shareAmount = s.shareAmount.money(),
                ),
            )
        }
        walletService.evictBalance(req.walletId)
        auditAsync.logSplitCreated(tx.id!!, user.nick)
    }

    @Transactional
    fun update(id: Long, req: UpdateTransactionRequest, user: AppUserDetails): TransactionDto {
        val tx = transactionRepository.findById(id).orElseThrow { ApiException("NOT_FOUND", "операция не найдена") }
        access.requireMember(tx.walletId, user)
        if (!access.isOwner(tx.walletId, user) && tx.authorId != user.id) {
            throw ApiException("FORBIDDEN", "можно изменять только свои операции")
        }
        if (splitRepository.existsByTransactionId(id)) {
            throw ApiException("VALIDATION_ERROR", "операцию с разделением чека нельзя редактировать")
        }
        validateCategory(tx.walletId, req.categoryId, req.type)
        tx.categoryId = req.categoryId
        tx.type = req.type
        tx.amount = req.amount.money()
        tx.transactionDate = req.transactionDate
        tx.comment = req.comment
        transactionRepository.save(tx)
        walletService.evictBalance(tx.walletId)
        return transactionRepository.findDetailedById(id).get().toDto()
    }

    @Transactional
    fun delete(id: Long, user: AppUserDetails) {
        val tx = transactionRepository.findById(id).orElseThrow { ApiException("NOT_FOUND", "операция не найдена") }
        access.requireMember(tx.walletId, user)
        if (!access.isOwner(tx.walletId, user) && tx.authorId != user.id) {
            throw ApiException("FORBIDDEN", "можно удалять только свои операции")
        }
        splitRepository.deleteByTransactionId(id)
        transactionRepository.delete(tx)
        walletService.evictBalance(tx.walletId)
    }

    private fun validateCategory(walletId: Long, categoryId: Long, type: OperationType) {
        val cat = categoryRepository.findById(categoryId).orElseThrow { ApiException("NOT_FOUND", "категория не найдена") }
        if (cat.walletId != walletId || cat.tip != type) {
            throw ApiException("VALIDATION_ERROR", "неверная категория")
        }
    }
}
