/**
 * границы текущего календарного месяца в формате YYYY-MM-DD.
 * совпадает с backend currentMonthPeriod() — для бюджета и отчётов.
 */

export function tekushiyPeriod(): { from: string; to: string } {
  const d = new Date()
  const y = d.getFullYear()
  const m = d.getMonth()
  const mm = String(m + 1).padStart(2, '0') // месяц 01–12
  const last = new Date(y, m + 1, 0).getDate() // последний день месяца
  return {
    from: `${y}-${mm}-01`,
    to: `${y}-${mm}-${String(last).padStart(2, '0')}`,
  }
}
