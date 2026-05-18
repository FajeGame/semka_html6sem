package ru.semka.domain.entity

import jakarta.persistence.*
import ru.semka.domain.enums.MemberRole
import java.time.Instant

@Entity
@Table(name = "wallet_members")
class WalletMemberEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "wallet_id", nullable = false)
    var walletId: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false)
    var memberRole: MemberRole = MemberRole.WALLET_MEMBER,

    @Column(name = "can_see_budget", nullable = false)
    var canSeeBudget: Boolean = false,

    @Column(name = "joined_at", nullable = false)
    var joinedAt: Instant = Instant.now(),
)
