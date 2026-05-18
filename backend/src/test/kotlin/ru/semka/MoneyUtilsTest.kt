package ru.semka

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.semka.service.money
import java.math.BigDecimal

/** Unit-тест округления сумм (бизнес-правило «копейки до 2 знаков»). */
class MoneyUtilsTest {
    @Test
    fun money_roundsToTwoDecimals() {
        assertEquals(BigDecimal("10.56"), BigDecimal("10.555").money())
        assertEquals(BigDecimal("10.55"), BigDecimal("10.554").money())
    }
}
