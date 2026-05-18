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

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApi(ex: ApiException): ResponseEntity<ErrorDto> =
        ResponseEntity.status(ex.status).body(ErrorDto(ex.code, ex.message, ex.details))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValid(ex: MethodArgumentNotValidException): ResponseEntity<ErrorDto> {
        val details = ex.bindingResult.allErrors.map {
            if (it is FieldError) "${it.field}: ${it.defaultMessage}" else it.defaultMessage ?: "ошибка"
        }
        return ResponseEntity.badRequest().body(ErrorDto("VALIDATION_ERROR", "неверные данные", details))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleAuth(): ResponseEntity<ErrorDto> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorDto("AUTH_ERROR", "неверный email или пароль"))

    @ExceptionHandler(AccessDeniedException::class)
    fun handleDenied(): ResponseEntity<ErrorDto> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorDto("FORBIDDEN", "нет доступа"))

    @ExceptionHandler(Exception::class)
    fun handleOther(ex: Exception): ResponseEntity<ErrorDto> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorDto("INTERNAL_ERROR", ex.message ?: "ошибка сервера"))
}
