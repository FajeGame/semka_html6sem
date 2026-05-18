package ru.semka.service

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.semka.domain.entity.UserEntity
import ru.semka.domain.enums.AppRole
import ru.semka.dto.DeleteAccountRequest
import ru.semka.dto.LoginRequest
import ru.semka.dto.LoginResponse
import ru.semka.dto.RegisterRequest
import ru.semka.dto.UserDto
import ru.semka.exception.ApiException
import ru.semka.repository.UserRepository
import ru.semka.security.AppUserDetails
import ru.semka.security.JwtService

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    @Transactional
    fun register(req: RegisterRequest): LoginResponse {
        if (userRepository.existsByEmail(req.email)) {
            throw ApiException("CONFLICT", "email занят", HttpStatus.CONFLICT)
        }
        if (userRepository.existsByNick(req.nick)) {
            throw ApiException("CONFLICT", "ник занят", HttpStatus.CONFLICT)
        }
        if (req.nick in listOf("papa", "mama")) {
            throw ApiException("CONFLICT", "ник занят", HttpStatus.CONFLICT)
        }
        val user = userRepository.save(
            UserEntity(
                email = req.email.trim().lowercase(),
                nick = req.nick.trim(),
                passwordHash = passwordEncoder.encode(req.password),
                role = AppRole.USER,
            ),
        )
        return tokenFor(user)
    }

    fun login(req: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(req.email.trim().lowercase())
            .orElseThrow { ApiException("AUTH_ERROR", "неверный email или пароль", HttpStatus.UNAUTHORIZED) }
        if (!passwordEncoder.matches(req.password, user.passwordHash)) {
            throw ApiException("AUTH_ERROR", "неверный email или пароль", HttpStatus.UNAUTHORIZED)
        }
        return tokenFor(user)
    }

    fun me(user: AppUserDetails): UserDto = UserDto(user.id, user.email, user.nick, user.role)

    @Transactional
    fun deleteAccount(req: DeleteAccountRequest, user: AppUserDetails) {
        if (user.nick in listOf("papa", "mama")) {
            throw ApiException("VALIDATION_ERROR", "демо-аккаунт нельзя удалить")
        }
        val entity = userRepository.findById(user.id)
            .orElseThrow { ApiException("NOT_FOUND", "пользователь не найден") }
        if (!passwordEncoder.matches(req.password, entity.passwordHash)) {
            throw ApiException("AUTH_ERROR", "неверный пароль", HttpStatus.UNAUTHORIZED)
        }
        userRepository.delete(entity)
    }

    private fun tokenFor(user: UserEntity): LoginResponse {
        val token = jwtService.generateToken(user.id!!, user.email, user.role.name)
        return LoginResponse(token, user.toDto())
    }
}
