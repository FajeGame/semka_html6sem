package ru.semka.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.semka.dto.ErrorDto

/**
 * глобальный перехватчик исключений для всех @RestController.
 * любая ошибка превращается в JSON ErrorDto с подходящим HTTP-кодом.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    // наши ApiException из сервисов
    @ExceptionHandler(ApiException::class)
    fun handleApi(ex: ApiException): ResponseEntity<ErrorDto> =
        ResponseEntity.status(ex.status).body(ErrorDto(ex.code, ex.message, ex.details))

    // @Valid на DTO не прошёл (пустой email, сумма < 0.01, …)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValid(ex: MethodArgumentNotValidException): ResponseEntity<ErrorDto> {
        val details = ex.bindingResult.allErrors.map {
            if (it is FieldError) "${it.field}: ${it.defaultMessage}" else it.defaultMessage ?: "ошибка"
        }
        return ResponseEntity.badRequest().body(ErrorDto("VALIDATION_ERROR", "неверные данные", details))
    }

    // стандартная ошибка Spring Security при неверном пароле
    @ExceptionHandler(BadCredentialsException::class)
    fun handleAuth(): ResponseEntity<ErrorDto> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorDto("AUTH_ERROR", "неверный email или пароль"))

    // нет прав на действие
    @ExceptionHandler(AccessDeniedException::class)
    fun handleDenied(): ResponseEntity<ErrorDto> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorDto("FORBIDDEN", "нет доступа"))

    // всё остальное — 500 (в проде лучше логировать stack trace)
    @ExceptionHandler(Exception::class)
    fun handleOther(ex: Exception): ResponseEntity<ErrorDto> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorDto("INTERNAL_ERROR", ex.message ?: "ошибка сервера"))
}
