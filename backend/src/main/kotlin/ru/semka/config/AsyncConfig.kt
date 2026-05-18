package ru.semka.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {

    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): Executor {
        val ex = ThreadPoolTaskExecutor()
        ex.corePoolSize = 2
        ex.maxPoolSize = 4
        ex.setQueueCapacity(50)
        ex.setThreadNamePrefix("semka-async-")
        ex.initialize()
        return ex
    }
}
