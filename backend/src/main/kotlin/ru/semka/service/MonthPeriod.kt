package ru.semka.service

import java.time.LocalDate
import java.time.YearMonth

/**
 * вспомогательная функция: границы текущего календарного месяца.
 * используется в BudgetService и WalletService для лимитов и остатка бюджета.
 */
fun currentMonthPeriod(): Pair<LocalDate, LocalDate> {
    val ym = YearMonth.now() // текущий год-месяц
    return ym.atDay(1) to ym.atEndOfMonth() // с 1-го по последний день
}
