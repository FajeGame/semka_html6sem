package ru.semka.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * servlet-фильтр: на каждый запрос читает заголовок Authorization: Bearer …
 * если токен валиден — в SecurityContext кладётся AppUserDetails для @AuthenticationPrincipal.
 */
@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val userDetailsService: AppUserDetailsService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader("Authorization")
        // есть Bearer-токен — попробовать авторизовать
        if (header != null && header.startsWith("Bearer ")) {
            try {
                val userId = jwtService.parseUserId(header.removePrefix("Bearer ").trim())
                val user = userDetailsService.loadUserById(userId)
                val auth = UsernamePasswordAuthenticationToken(user, null, user.authorities)
                SecurityContextHolder.getContext().authentication = auth
            } catch (_: Exception) {
                // битый токен — как гость; защищённые эндпоинты вернут 401
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }
}
