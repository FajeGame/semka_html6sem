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

/**
 * Spring Data JPA: интерфейсы доступа к PostgreSQL.
 * Spring сам генерирует реализации по именам методов; агрегаты — через @Query JPQL.
 */

// ——— пользователи ———
interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): Optional<UserEntity> // вход по email
    fun findByNick(nick: String): Optional<UserEntity> // приглашение в кошелёк по нику
    fun existsByEmail(email: String): Boolean // проверка при регистрации
    fun existsByNick(nick: String): Boolean
}

// ——— кошельки ———
interface WalletRepository : JpaRepository<WalletEntity, Long> {
    fun findByOwnerId(ownerId: Long): List<WalletEntity> // все кошельки, где user — owner_id
}

// ——— участники кошелька ———
interface WalletMemberRepository : JpaRepository<WalletMemberEntity, Long> {
    fun findByWalletIdAndUserId(walletId: Long, userId: Long): Optional<WalletMemberEntity> // проверка доступа
    fun findByWalletId(walletId: Long): List<WalletMemberEntity> // список участников
    fun findByUserId(userId: Long): List<WalletMemberEntity> // все кошельки пользователя
}

// ——— категории ———
interface CategoryRepository : JpaRepository<CategoryEntity, Long> {
    fun findByWalletId(walletId: Long): List<CategoryEntity>
    fun findByWalletIdAndTip(walletId: Long, tip: OperationType): List<CategoryEntity> // фильтр INCOME/EXPENSE
}

// ——— операции ———
interface TransactionRepository : JpaRepository<TransactionEntity, Long> {

    // одна операция с подгрузкой category, author, splits (без N+1)
    @EntityGraph(attributePaths = ["category", "author", "splits", "splits.user"])
    fun findDetailedById(id: Long): Optional<TransactionEntity>

    @EntityGraph(attributePaths = ["category", "author", "splits", "splits.user"])
    fun findByWalletIdOrderByTransactionDateDescCreatedAtDesc(walletId: Long): List<TransactionEntity>

    @EntityGraph(attributePaths = ["category", "author", "splits", "splits.user"])
    fun findByWalletIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
        walletId: Long,
        from: LocalDate,
        to: LocalDate,
    ): List<TransactionEntity> // для отчётов за период

    @EntityGraph(attributePaths = ["category", "author", "splits", "splits.user"])
    fun findByWalletIdAndTypeOrderByTransactionDateDescCreatedAtDesc(
        walletId: Long,
        type: OperationType,
    ): List<TransactionEntity> // фильтр доход/расход в списке

    // сумма INCOME или EXPENSE по всему кошельку (баланс)
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

    // потрачено по одной категории за период (бюджет по категории)
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

    // все расходы кошелька за период (общий бюджет)
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

    fun countByCategoryId(categoryId: Long): Long // можно ли удалить категорию
    fun countByWalletId(walletId: Long): Long // пустой ли кошелёк (cleanup)
}

// ——— доли split ———
interface SplitExpenseRepository : JpaRepository<SplitExpenseEntity, Long> {
    fun findByTransactionId(transactionId: Long): List<SplitExpenseEntity>
    fun deleteByTransactionId(transactionId: Long) // при удалении операции
    fun existsByTransactionId(transactionId: Long): Boolean
}

// ——— бюджеты ———
interface BudgetRepository : JpaRepository<BudgetEntity, Long> {
    fun findByWalletIdAndPeriodStart(walletId: Long, periodStart: LocalDate): List<BudgetEntity> // лимиты месяца
    fun findByWalletIdAndCategoryIdAndPeriodStart(
        walletId: Long,
        categoryId: Long?,
        periodStart: LocalDate,
    ): Optional<BudgetEntity> // upsert: общий (categoryId=null) или по категории
}

// ——— автоплатежи ———
interface RecurringRuleRepository : JpaRepository<RecurringRuleEntity, Long> {
    fun findByWalletId(walletId: Long): List<RecurringRuleEntity>
    fun findByActiveTrueAndNextRunDateLessThanEqual(date: LocalDate): List<RecurringRuleEntity> // для планировщика
}
