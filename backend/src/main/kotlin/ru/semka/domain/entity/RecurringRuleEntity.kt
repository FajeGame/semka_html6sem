package ru.semka.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "recurring_rules")
class RecurringRuleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "wallet_id", nullable = false)
    var walletId: Long = 0,

    @Column(name = "category_id", nullable = false)
    var categoryId: Long = 0,

    @Column(nullable = false)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "day_of_month", nullable = false)
    var dayOfMonth: Int = 1,

    @Column(name = "next_run_date", nullable = false)
    var nextRunDate: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    var active: Boolean = true,

    var comment: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    var category: CategoryEntity? = null,
)
