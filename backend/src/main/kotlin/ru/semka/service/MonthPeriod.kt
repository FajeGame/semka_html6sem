package ru.semka.service

import java.time.LocalDate
import java.time.YearMonth

/** Границы текущего календарного месяца (для бюджета и карточки кошелька). */
fun currentMonthPeriod(): Pair<LocalDate, LocalDate> {
    val ym = YearMonth.now()
    return ym.atDay(1) to ym.atEndOfMonth()
}
