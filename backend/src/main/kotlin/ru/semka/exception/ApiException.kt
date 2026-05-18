package ru.semka.exception

import org.springframework.http.HttpStatus

class ApiException(
    val code: String,
    override val message: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST,
    val details: List<String> = emptyList(),
) : RuntimeException(message)
