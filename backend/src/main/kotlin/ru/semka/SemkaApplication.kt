package ru.semka

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/** точка входа Spring Boot: REST API, Flyway, Redis-кэш, планировщик автоплатежей. */
@SpringBootApplication
@EnableScheduling
class SemkaApplication

fun main(args: Array<String>) {
    runApplication<SemkaApplication>(*args)
}
