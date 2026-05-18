package ru.semka.service

import org.springframework.stereotype.Service
import ru.semka.domain.enums.OperationType
import ru.semka.dto.CategoryExpenseDto
import ru.semka.dto.MemberExpenseDto
import ru.semka.dto.PeriodReportDto
import ru.semka.repository.SplitExpenseRepository
import ru.semka.repository.TransactionRepository
import ru.semka.repository.UserRepository
import ru.semka.repository.WalletMemberRepository
import ru.semka.security.AppUserDetails
import java.math.BigDecimal

@Service
class ReportService(
    private val transactionRepository: TransactionRepository,
    private val splitRepository: SplitExpenseRepository,
    private val memberRepository: WalletMemberRepository,
    private val userRepository: UserRepository,
    private val access: WalletAccessService,
) {
    fun periodReport(walletId: Long, from: java.time.LocalDate, to: java.time.LocalDate, user: AppUserDetails): PeriodReportDto {
        access.requireMember(walletId, user)
        val txs = transactionRepository
            .findByWalletIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(walletId, from, to)
        val income = txs.filter { it.type == OperationType.INCOME }.sumOf { it.amount }.money()
        val expense = txs.filter { it.type == OperationType.EXPENSE }.sumOf { it.amount }.money()
        val byCat = txs.filter { it.type == OperationType.EXPENSE }
            .groupBy { it.categoryId }
            .map { (catId, list) ->
                CategoryExpenseDto(
                    categoryId = catId,
                    categoryName = list.first().category?.name ?: "?",
                    expense = list.sumOf { it.amount }.money(),
                )
            }
        return PeriodReportDto(walletId, from, to, income, expense, byCat)
    }

    fun expensesByMember(
        walletId: Long,
        from: java.time.LocalDate,
        to: java.time.LocalDate,
        user: AppUserDetails,
    ): List<MemberExpenseDto> {
        access.requireMember(walletId, user)
        val map = mutableMapOf<Long, BigDecimal>()
        val txs = transactionRepository
            .findByWalletIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(walletId, from, to)
            .filter { it.type == OperationType.EXPENSE }
        for (tx in txs) {
            val splits = splitRepository.findByTransactionId(tx.id!!)
            if (splits.isEmpty()) {
                map[tx.authorId] = (map[tx.authorId] ?: BigDecimal.ZERO) + tx.amount
            } else {
                splits.forEach { s ->
                    map[s.userId] = (map[s.userId] ?: BigDecimal.ZERO) + s.shareAmount
                }
            }
        }
        return memberRepository.findByWalletId(walletId)
            .mapNotNull { m ->
                val exp = map[m.userId] ?: return@mapNotNull null
                if (exp <= BigDecimal.ZERO) return@mapNotNull null
                val nick = userRepository.findById(m.userId).map { it.nick }.orElse("?")
                MemberExpenseDto(m.userId, nick, exp.money())
            }
            .sortedByDescending { it.expense }
    }
}
