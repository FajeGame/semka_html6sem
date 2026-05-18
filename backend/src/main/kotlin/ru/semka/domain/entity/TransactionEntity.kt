package ru.semka.domain.entity

import jakarta.persistence.*
import ru.semka.domain.enums.OperationType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "transactions")
class TransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "wallet_id", nullable = false)
    var walletId: Long = 0,

    @Column(name = "author_id", nullable = false)
    var authorId: Long = 0,

    @Column(name = "category_id", nullable = false)
    var categoryId: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: OperationType = OperationType.EXPENSE,

    @Column(nullable = false)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "transaction_date", nullable = false)
    var transactionDate: LocalDate = LocalDate.now(),

    var comment: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    var category: CategoryEntity? = null,

    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
    var splits: MutableList<SplitExpenseEntity> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    var author: UserEntity? = null,
)
