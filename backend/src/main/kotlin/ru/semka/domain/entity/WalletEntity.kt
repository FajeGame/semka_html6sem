package ru.semka.domain.entity

import jakarta.persistence.*
import java.time.Instant

/**
 * JPA-сущность таблицы wallets — семейный (совместный) кошелёк.
 * owner_id — кто создал; остальные участники в wallet_members.
 * баланс не хранится здесь — считается по сумме transactions.
 */
// таблица кошельков
@Entity
@Table(name = "wallets")
class WalletEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null, // первичный ключ

    @Column(nullable = false)
    var name: String = "", // название («Семья», «Отпуск»)

    @Column(name = "owner_id", nullable = false)
    var ownerId: Long = 0, // user id создателя (дублируется в wallet_members как OWNER)

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(), // дата создания кошелька
)
