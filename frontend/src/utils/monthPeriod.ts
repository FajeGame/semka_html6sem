// границы текущего календарного месяца для бюджета и отчётов

/** границы текущего календарного месяца (YYYY-MM-DD) */
export function tekushiyPeriod(): { from: string; to: string } {
  const d = new Date()
  const y = d.getFullYear()
  const m = d.getMonth()
  const mm = String(m + 1).padStart(2, '0')
  const last = new Date(y, m + 1, 0).getDate()
  return {
    from: `${y}-${mm}-01`,
    to: `${y}-${mm}-${String(last).padStart(2, '0')}`,
  }
}
