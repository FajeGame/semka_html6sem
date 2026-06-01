package ru.semka.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

/**
 * JPA-сущность таблицы recurring_rules — правило автоплатежа «раз в месяц».
 * RecurringScheduler раз в сутки смотрит next_run_date и создаёт transaction, если день наступил.
 * только владелец кошелька может создавать и менять правила через API.
 */
// таблица правил автоплатежей
@Entity
@Table(name = "recurring_rules")
class RecurringRuleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null, // первичный ключ правила

    @Column(name = "wallet_id", nullable = false)
    var walletId: Long = 0, // кошелёк

    @Column(name = "category_id", nullable = false)
    var categoryId: Long = 0, // категория расхода (коммуналка, подписка, …)

    @Column(nullable = false)
    var amount: BigDecimal = BigDecimal.ZERO, // сумма каждого автоматического списания

    @Column(name = "day_of_month", nullable = false)
    var dayOfMonth: Int = 1, // в какой день месяца проводить (1–31)

    @Column(name = "next_run_date", nullable = false)
    var nextRunDate: LocalDate = LocalDate.now(), // когда следующий запуск планировщика

    @Column(nullable = false)
    var active: Boolean = true, // выключенное правило планировщик пропускает

    var comment: String? = null, // подпись к автоплатежу (попадёт в transaction.comment)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    var category: CategoryEntity? = null, // имя категории для списка правил в UI
)
