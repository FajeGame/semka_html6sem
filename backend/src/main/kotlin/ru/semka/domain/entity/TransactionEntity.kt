package ru.semka.domain.entity

import jakarta.persistence.*
import ru.semka.domain.enums.OperationType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * JPA-сущность таблицы transactions — одна финансовая операция.
 * доход увеличивает баланс, расход уменьшает; split добавляет строки в split_expenses.
 * category и author — ленивые связи для удобной выдачи в DTO без лишних JOIN в коде.
 */
// таблица операций (доходы и расходы)
@Entity
@Table(name = "transactions")
class TransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null, // первичный ключ операции

    @Column(name = "wallet_id", nullable = false)
    var walletId: Long = 0, // кошелёк, к которому относится запись

    @Column(name = "author_id", nullable = false)
    var authorId: Long = 0, // кто внёс операцию (для отчёта «по участникам»)

    @Column(name = "category_id", nullable = false)
    var categoryId: Long = 0, // категория (продукты, зарплата, …)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: OperationType = OperationType.EXPENSE, // INCOME или EXPENSE

    @Column(nullable = false)
    var amount: BigDecimal = BigDecimal.ZERO, // сумма в рублях (всегда положительная)

    @Column(name = "transaction_date", nullable = false)
    var transactionDate: LocalDate = LocalDate.now(), // дата операции (не время создания)

    var comment: String? = null, // необязательный комментарий

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(), // когда запись попала в БД

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    var category: CategoryEntity? = null, // для DTO: имя категории без отдельного запроса

    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
    var splits: MutableList<SplitExpenseEntity> = mutableListOf(), // доли при split-чеке

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    var author: UserEntity? = null, // для DTO: nick автора
)
