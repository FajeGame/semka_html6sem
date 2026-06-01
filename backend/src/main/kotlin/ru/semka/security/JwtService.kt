package ru.semka.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

/**
 * создание и проверка JWT-токенов.
 * секрет и время жизни — в application.yml (semka.jwt.*).
 * в токене хранится userId; email и role — дополнительные claims (для отладки).
 */
@Service
class JwtService(
    @Value("\${semka.jwt.secret}") secret: String, // ключ подписи HMAC
    @Value("\${semka.jwt.expiration-ms}") private val expirationMs: Long, // срок жизни токена
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    // выдать токен после login/register
    fun generateToken(userId: Long, email: String, role: String): String {
        val now = Date()
        val exp = Date(now.time + expirationMs)
        return Jwts.builder()
            .subject(userId.toString()) // главное поле — id пользователя
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(exp)
            .signWith(key)
            .compact()
    }

    // достать userId из Bearer-токена; бросит исключение если подпись/срок неверны
    fun parseUserId(token: String): Long =
        Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload.subject.toLong()
}
