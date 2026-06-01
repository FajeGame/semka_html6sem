package ru.semka.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import ru.semka.domain.enums.AppRole

/**
 * обёртка пользователя для Spring Security после входа или разбора JWT.
 * попадает в контроллеры как @AuthenticationPrincipal user: AppUserDetails.
 */
data class AppUserDetails(
    val id: Long, // user id из БД
    val email: String,
    val nick: String,
    val role: AppRole,
    private val passwordHash: String, // нужен Spring Security, в API не уходит
) : UserDetails {

    // роль для hasRole('ADMIN') и т.п.
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = passwordHash
    override fun getUsername(): String = email // в Security «username» = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
