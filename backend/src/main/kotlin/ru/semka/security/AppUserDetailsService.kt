package ru.semka.security

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.semka.repository.UserRepository

@Service
class AppUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("нет пользователя") }
        return toDetails(user)
    }

    fun loadUserById(id: Long): AppUserDetails =
        userRepository.findById(id).map { toDetails(it) }
            .orElseThrow { UsernameNotFoundException("нет пользователя") }

    private fun toDetails(user: ru.semka.domain.entity.UserEntity) = AppUserDetails(
        id = user.id!!,
        email = user.email,
        nick = user.nick,
        role = user.role,
        passwordHash = user.passwordHash,
    )
}
