package ru.semka.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.semka.domain.entity.RecurringRuleEntity
import ru.semka.domain.entity.TransactionEntity
import ru.semka.domain.enums.OperationType
import ru.semka.dto.CreateRecurringRequest
import ru.semka.dto.UpdateRecurringRequest
import ru.semka.dto.RecurringRuleDto
import ru.semka.exception.ApiException
import ru.semka.repository.CategoryRepository
import ru.semka.repository.RecurringRuleRepository
import ru.semka.repository.TransactionRepository
import ru.semka.repository.WalletRepository
import ru.semka.security.AppUserDetails
import java.time.LocalDate

@Service
class RecurringService(
    private val recurringRepository: RecurringRuleRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val access: WalletAccessService,
    private val walletService: WalletService,
) {
    fun get(ruleId: Long, walletId: Long, user: AppUserDetails): RecurringRuleDto {
        access.requireOwner(walletId, user)
        val rule = recurringRepository.findById(ruleId).orElseThrow { ApiException("NOT_FOUND", "правило не найдено") }
        if (rule.walletId != walletId) throw ApiException("NOT_FOUND", "правило не найдено")
        return rule.toDto()
    }

    fun list(walletId: Long, user: AppUserDetails): List<RecurringRuleDto> {
        access.requireOwner(walletId, user)
        return recurringRepository.findByWalletId(walletId).map { it.toDto() }
    }

    @Transactional
    fun create(req: CreateRecurringRequest, user: AppUserDetails): RecurringRuleDto {
        access.requireOwner(req.walletId, user)
        val cat = categoryRepository.findById(req.categoryId).orElseThrow { ApiException("NOT_FOUND", "категория не найдена") }
        if (cat.walletId != req.walletId) throw ApiException("VALIDATION_ERROR", "категория из другого кошелька")
        val rule = recurringRepository.save(
            RecurringRuleEntity(
                walletId = req.walletId,
                categoryId = req.categoryId,
                amount = req.amount.money(),
                dayOfMonth = req.dayOfMonth.coerceIn(1, 31),
                nextRunDate = req.nextRunDate,
                active = req.active,
                comment = req.comment,
            ),
        )
        return recurringRepository.findById(rule.id!!).get().toDto()
    }

    @Transactional
    fun update(ruleId: Long, req: UpdateRecurringRequest, user: AppUserDetails): RecurringRuleDto {
        access.requireOwner(req.walletId, user)
        val rule = recurringRepository.findById(ruleId).orElseThrow { ApiException("NOT_FOUND", "правило не найдено") }
        if (rule.walletId != req.walletId) throw ApiException("NOT_FOUND", "правило не найдено")
        val cat = categoryRepository.findById(req.categoryId).orElseThrow { ApiException("NOT_FOUND", "категория не найдена") }
        if (cat.walletId != req.walletId) throw ApiException("VALIDATION_ERROR", "категория из другого кошелька")
        rule.categoryId = req.categoryId
        rule.amount = req.amount.money()
        rule.dayOfMonth = req.dayOfMonth.coerceIn(1, 31)
        rule.nextRunDate = req.nextRunDate
        rule.active = req.active
        rule.comment = req.comment
        return recurringRepository.save(rule).toDto()
    }

    @Transactional
    fun delete(ruleId: Long, walletId: Long, user: AppUserDetails) {
        access.requireOwner(walletId, user)
        val rule = recurringRepository.findById(ruleId).orElseThrow { ApiException("NOT_FOUND", "правило не найдено") }
        if (rule.walletId != walletId) throw ApiException("NOT_FOUND", "правило не найдено")
        recurringRepository.delete(rule)
    }

    @Transactional
    fun runDueRules(today: LocalDate = LocalDate.now()) {
        val rules = recurringRepository.findByActiveTrueAndNextRunDateLessThanEqual(today)
        for (rule in rules) {
            if (today.isBefore(rule.nextRunDate)) continue
            val ownerId = walletRepository.findById(rule.walletId).map { it.ownerId }.orElse(1L)
            transactionRepository.save(
                TransactionEntity(
                    walletId = rule.walletId,
                    authorId = ownerId,
                    categoryId = rule.categoryId,
                    type = OperationType.EXPENSE,
                    amount = rule.amount,
                    transactionDate = today,
                    comment = rule.comment ?: "автоплатёж",
                ),
            )
            rule.nextRunDate = rule.nextRunDate.plusMonths(1)
            recurringRepository.save(rule)
            walletService.evictBalance(rule.walletId)
        }
    }
}
