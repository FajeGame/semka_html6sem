package ru.semka.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

/** фоновое логирование важных событий (split), чтобы не тормозить HTTP-ответ. */
@Service
class AuditAsyncService {
    private val log = LoggerFactory.getLogger(javaClass)

    @Async("taskExecutor")
    fun logSplitCreated(transactionId: Long, authorNick: String) {
        log.info("split создан: tx={}, автор=@{}", transactionId, authorNick)
    }
}
