package ru.semka.domain.entity

import jakarta.persistence.*
import ru.semka.domain.enums.AppRole
import java.time.Instant

/**
 * JPA-сущность таблицы users — аккаунт для входа в приложение.
 * пароль в БД только в виде bcrypt-хэша; JWT не хранит пароль, только userId.
 */
// таблица пользователей
@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null, // первичный ключ

    @Column(nullable = false, unique = true)
    var email: String = "", // логин (уникальный), при регистрации приводится к lower case

    @Column(nullable = false, unique = true)
    var nick: String = "", // короткое имя для приглашений в кошелёк

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = "", // bcrypt, никогда не отдаётся в API

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: AppRole = AppRole.USER, // USER или ADMIN приложения

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(), // когда зарегистрировался
)
