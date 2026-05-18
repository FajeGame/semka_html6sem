package ru.semka.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "budgets")
class BudgetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "wallet_id", nullable = false)
    var walletId: Long = 0,

    @Column(name = "category_id")
    var categoryId: Long? = null,

    @Column(name = "period_start", nullable = false)
    var periodStart: LocalDate = LocalDate.now(),

    @Column(name = "period_end", nullable = false)
    var periodEnd: LocalDate = LocalDate.now(),

    @Column(name = "limit_amount", nullable = false)
    var limitAmount: BigDecimal = BigDecimal.ZERO,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    var category: CategoryEntity? = null,
)
