package ru.semka

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SemkaApplication

fun main(args: Array<String>) {
    runApplication<SemkaApplication>(*args)
}
