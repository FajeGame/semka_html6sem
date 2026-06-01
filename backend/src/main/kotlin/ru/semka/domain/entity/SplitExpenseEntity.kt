package ru.semka.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal

/**
 * JPA-сущность таблицы split_expenses — доля одного участника в общем чеке.
 * одна операция transaction (общий расход) + несколько split_expenses по участникам.
 * сумма share_amount по всем долям ≈ amount родительской transaction.
 */
// таблица долей при разделении чека
@Entity
@Table(name = "split_expenses")
class SplitExpenseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null, // первичный ключ доли

    @Column(name = "transaction_id", nullable = false)
    var transactionId: Long = 0, // родительская операция-расход

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0, // кому принадлежит эта доля

    @Column(name = "share_amount", nullable = false)
    var shareAmount: BigDecimal = BigDecimal.ZERO, // сколько рублей «на этом» участнике

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    var transaction: TransactionEntity? = null, // обратная связь к операции

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    var user: UserEntity? = null, // для отчётов: ник участника
)
