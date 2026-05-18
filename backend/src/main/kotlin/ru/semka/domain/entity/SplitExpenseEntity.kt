package ru.semka.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "split_expenses")
class SplitExpenseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "transaction_id", nullable = false)
    var transactionId: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,

    @Column(name = "share_amount", nullable = false)
    var shareAmount: BigDecimal = BigDecimal.ZERO,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    var transaction: TransactionEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    var user: UserEntity? = null,
)
