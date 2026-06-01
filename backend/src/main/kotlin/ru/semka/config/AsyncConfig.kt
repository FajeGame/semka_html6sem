package ru.semka.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * пул потоков для @Async (AuditAsyncService и др.).
 * отдельные потоки, чтобы логирование не блокировало HTTP-ответ.
 */
@Configuration
@EnableAsync
class AsyncConfig {

    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): Executor {
        val ex = ThreadPoolTaskExecutor()
        ex.corePoolSize = 2 // минимум рабочих потоков
        ex.maxPoolSize = 4 // максимум при пике
        ex.setQueueCapacity(50) // очередь задач
        ex.setThreadNamePrefix("semka-async-") // префикс в логах
        ex.initialize()
        return ex
    }
}
