package ru.semka.domain.entity

import jakarta.persistence.*
import ru.semka.domain.enums.MemberRole
import java.time.Instant

/**
 * JPA-сущность таблицы wallet_members — связь «пользователь ↔ кошелёк».
 * одна строка = один человек в одном кошельке с ролью и правом видеть бюджет.
 */
// таблица участников кошелька
@Entity
@Table(name = "wallet_members")
class WalletMemberEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null, // id записи участника (нужен для PATCH/DELETE members)

    @Column(name = "wallet_id", nullable = false)
    var walletId: Long = 0, // в каком кошельке

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0, // какой пользователь

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false)
    var memberRole: MemberRole = MemberRole.WALLET_MEMBER, // владелец или обычный участник

    @Column(name = "can_see_budget", nullable = false)
    var canSeeBudget: Boolean = false, // может ли участник видеть лимиты и остаток бюджета

    @Column(name = "joined_at", nullable = false)
    var joinedAt: Instant = Instant.now(), // когда присоединился (приглашение или создание)
)
