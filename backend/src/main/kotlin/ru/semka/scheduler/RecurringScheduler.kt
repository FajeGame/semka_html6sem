package ru.semka.scheduler

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.semka.service.RecurringService

@Component
class RecurringScheduler(
    private val recurringService: RecurringService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // каждый день в 03:00 — проверка автоплатежей
    @Scheduled(cron = "0 0 3 * * *")
    fun runRecurring() {
        log.info("запуск автоплатежей")
        recurringService.runDueRules()
    }
}
