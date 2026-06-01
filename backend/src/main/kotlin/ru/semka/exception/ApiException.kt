package ru.semka.exception

import org.springframework.http.HttpStatus

/**
 * осознанная бизнес-ошибка из service-слоя.
 * бросается вместо голого RuntimeException, чтобы GlobalExceptionHandler вернул нужный HTTP-код.
 */
class ApiException(
    val code: String, // код для фронта: NOT_FOUND, FORBIDDEN, CONFLICT, …
    override val message: String, // текст на русском для пользователя
    val status: HttpStatus = HttpStatus.BAD_REQUEST, // HTTP-статус ответа
    val details: List<String> = emptyList(), // доп. строки (редко)
) : RuntimeException(message)
