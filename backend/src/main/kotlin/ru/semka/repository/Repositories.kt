package ru.semka.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.semka.domain.entity.*
import ru.semka.domain.enums.OperationType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): Optional<UserEntity>
    fun findByNick(nick: String): Optional<UserEntity>
    fun existsByEmail(email: String): Boolean
    fun existsByNick(nick: String): Boolean
}

interface WalletRepository : JpaRepository<WalletEntity, Long> {
    fun findByOwnerId(ownerId: Long): List<WalletEntity>
}

interface WalletMemberRepository : JpaRepository<WalletMemberEntity, Long> {
    fun findByWalletIdAndUserId(walletId: Long, userId: Long): Optional<WalletMemberEntity>
    fun findByWalletId(walletId: Long): List<WalletMemberEntity>
    fun findByUserId(userId: Long): List<WalletMemberEntity>
}

interface CategoryRepository : JpaRepository<CategoryEntity, Long> {
    fun findByWalletId(walletId: Long): List<CategoryEntity>
    fun findByWalletIdAndTip(walletId: Long, tip: OperationType): List<CategoryEntity>
}

interface TransactionRepository : JpaRepository<TransactionEntity, Long> {
    @EntityGraph(attributePaths = ["category", "author", "splits", "splits.user"])
    fun findDetailedById(id: Long): Optional<TransactionEntity>

    @EntityGraph(attributePaths = ["category", "author", "splits", "splits.user"])
    fun findByWalletIdOrderByTransactionDateDescCreatedAtDesc(walletId: Long): List<TransactionEntity>

    @EntityGraph(attributePaths = ["category", "author", "splits", "splits.user"])
    fun findByWalletIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
        walletId: Long,
        from: LocalDate,
        to: LocalDate,
    ): List<TransactionEntity>

    @EntityGraph(attributePaths = ["category", "author", "splits", "splits.user"])
    fun findByWalletIdAndTypeOrderByTransactionDateDescCreatedAtDesc(
        walletId: Long,
        type: OperationType,
    ): List<TransactionEntity>

    @Query(
        """
        SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t
        WHERE t.walletId = :walletId AND t.type = :type
        """,
    )
    fun sumByWalletAndType(
        @Param("walletId") walletId: Long,
        @Param("type") type: OperationType,
    ): BigDecimal

    @Query(
        """
        SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t
        WHERE t.walletId = :walletId AND t.type = 'EXPENSE' AND t.categoryId = :categoryId
        AND t.transactionDate BETWEEN :from AND :to
        """,
    )
    fun sumExpenseByCategoryAndPeriod(
        @Param("walletId") walletId: Long,
        @Param("categoryId") categoryId: Long,
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate,
    ): BigDecimal

    @Query(
        """
        SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t
        WHERE t.walletId = :walletId AND t.type = 'EXPENSE'
        AND t.transactionDate BETWEEN :from AND :to
        """,
    )
    fun sumExpenseByWalletAndPeriod(
        @Param("walletId") walletId: Long,
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate,
    ): BigDecimal

    fun countByCategoryId(categoryId: Long): Long

    fun countByWalletId(walletId: Long): Long
}

interface SplitExpenseRepository : JpaRepository<SplitExpenseEntity, Long> {
    fun findByTransactionId(transactionId: Long): List<SplitExpenseEntity>
    fun deleteByTransactionId(transactionId: Long)
    fun existsByTransactionId(transactionId: Long): Boolean
}

interface BudgetRepository : JpaRepository<BudgetEntity, Long> {
    fun findByWalletIdAndPeriodStart(walletId: Long, periodStart: LocalDate): List<BudgetEntity>
    fun findByWalletIdAndCategoryIdAndPeriodStart(
        walletId: Long,
        categoryId: Long?,
        periodStart: LocalDate,
    ): Optional<BudgetEntity>
}

interface RecurringRuleRepository : JpaRepository<RecurringRuleEntity, Long> {
    fun findByWalletId(walletId: Long): List<RecurringRuleEntity>
    fun findByActiveTrueAndNextRunDateLessThanEqual(date: LocalDate): List<RecurringRuleEntity>
}
