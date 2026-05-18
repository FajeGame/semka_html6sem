package ru.semka.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.semka.domain.entity.BudgetEntity
import ru.semka.domain.enums.MemberRole
import ru.semka.dto.BudgetDto
import ru.semka.dto.UpsertBudgetRequest
import ru.semka.exception.ApiException
import ru.semka.repository.BudgetRepository
import ru.semka.repository.CategoryRepository
import ru.semka.repository.TransactionRepository
import ru.semka.security.AppUserDetails
import java.math.BigDecimal

@Service
class BudgetService(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val memberRepository: ru.semka.repository.WalletMemberRepository,
    private val access: WalletAccessService,
) {
    fun list(walletId: Long, user: AppUserDetails): List<BudgetDto> {
        val member = memberRepository.findByWalletIdAndUserId(walletId, user.id)
            .orElseThrow { ApiException("FORBIDDEN", "нет доступа") }
        if (member.memberRole != MemberRole.WALLET_OWNER && !member.canSeeBudget) {
            return emptyList()
        }
        val (from, _) = currentMonthPeriod()
        return budgetRepository.findByWalletIdAndPeriodStart(walletId, from).map { enrich(it) }
    }

    @Transactional
    fun upsert(req: UpsertBudgetRequest, user: AppUserDetails): BudgetDto {
        access.requireOwner(req.walletId, user)
        if (req.categoryId != null) {
            val cat = categoryRepository.findById(req.categoryId).orElseThrow()
            if (cat.walletId != req.walletId) throw ApiException("VALIDATION_ERROR", "категория из другого кошелька")
        }
        val (from, to) = currentMonthPeriod()
        val existing = budgetRepository.findByWalletIdAndCategoryIdAndPeriodStart(req.walletId, req.categoryId, from)
        val entity = if (existing.isPresent) {
            val b = existing.get()
            b.limitAmount = req.limitAmount.money()
            budgetRepository.save(b)
        } else {
            budgetRepository.save(
                BudgetEntity(
                    walletId = req.walletId,
                    categoryId = req.categoryId,
                    periodStart = from,
                    periodEnd = to,
                    limitAmount = req.limitAmount.money(),
                ),
            )
        }
        return enrich(entity)
    }

    @Transactional
    fun delete(walletId: Long, budgetId: Long, user: AppUserDetails) {
        access.requireOwner(walletId, user)
        val b = budgetRepository.findById(budgetId).orElseThrow { ApiException("NOT_FOUND", "бюджет не найден") }
        if (b.walletId != walletId) throw ApiException("NOT_FOUND", "бюджет не найден")
        if (b.categoryId == null) throw ApiException("VALIDATION_ERROR", "общий бюджет нельзя удалить — укажите лимит 0")
        budgetRepository.delete(b)
    }

    private fun enrich(b: BudgetEntity): BudgetDto {
        val spent = if (b.categoryId == null) {
            transactionRepository.sumExpenseByWalletAndPeriod(b.walletId, b.periodStart, b.periodEnd)
        } else {
            transactionRepository.sumExpenseByCategoryAndPeriod(b.walletId, b.categoryId!!, b.periodStart, b.periodEnd)
        }.money()
        val remaining = (b.limitAmount - spent).money()
        return BudgetDto(
            id = b.id!!,
            walletId = b.walletId,
            categoryId = b.categoryId,
            categoryName = b.category?.name ?: categoryRepository.findById(b.categoryId ?: 0).map { it.name }.orElse(null),
            periodStart = b.periodStart,
            periodEnd = b.periodEnd,
            limitAmount = b.limitAmount,
            spent = spent,
            remaining = remaining,
        )
    }

}
