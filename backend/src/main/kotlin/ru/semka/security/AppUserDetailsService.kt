package ru.semka.security

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.semka.repository.UserRepository

/**
 * загрузка пользователя из БД для Spring Security.
 * loadUserByUsername — по email (стандартный механизм, у нас почти не вызывается).
 * loadUserById — из JWT в JwtAuthFilter.
 */
@Service
class AppUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    // поиск по email (username в Security = email)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("нет пользователя") }
        return toDetails(user)
    }

    // поиск по id из JWT subject
    fun loadUserById(id: Long): AppUserDetails =
        userRepository.findById(id).map { toDetails(it) }
            .orElseThrow { UsernameNotFoundException("нет пользователя") }

    // маппинг UserEntity → AppUserDetails
    private fun toDetails(user: ru.semka.domain.entity.UserEntity) = AppUserDetails(
        id = user.id!!,
        email = user.email,
        nick = user.nick,
        role = user.role,
        passwordHash = user.passwordHash,
    )
}
