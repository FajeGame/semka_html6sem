package ru.semka.domain.entity

import jakarta.persistence.*
import ru.semka.domain.enums.AppRole
import java.time.Instant

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false, unique = true)
    var nick: String = "",

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: AppRole = AppRole.USER,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
)
