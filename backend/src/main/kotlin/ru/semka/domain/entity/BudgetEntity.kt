package ru.semka.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

/**
 * JPA-сущность таблицы budgets — лимит трат на период (обычно текущий месяц).
 * category_id = null — лимит на весь кошелёк; иначе — лимит по одной категории расходов.
 * факт «сколько потрачено» в БД не хранится — считается по transactions за period_start…period_end.
 */
// таблица бюджетов (лимиты на месяц)
@Entity
@Table(name = "budgets")
class BudgetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null, // первичный ключ записи бюджета

    @Column(name = "wallet_id", nullable = false)
    var walletId: Long = 0, // кошелёк

    @Column(name = "category_id")
    var categoryId: Long? = null, // null = общий лимит кошелька; иначе id категории EXPENSE

    @Column(name = "period_start", nullable = false)
    var periodStart: LocalDate = LocalDate.now(), // первый день периода (1-е число месяца)

    @Column(name = "period_end", nullable = false)
    var periodEnd: LocalDate = LocalDate.now(), // последний день периода

    @Column(name = "limit_amount", nullable = false)
    var limitAmount: BigDecimal = BigDecimal.ZERO, // максимум трат за период

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    var category: CategoryEntity? = null, // для отображения имени категории в списке бюджетов
)
