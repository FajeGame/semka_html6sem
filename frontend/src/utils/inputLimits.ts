// ограничения и проверки полей ввода (сумма, день, дата, лимит бюджета)

export const DEN_MIN = 1
export const DEN_MAX = 31

export const SUMMA_MIN = 0.01
export const SUMMA_MAX = 99_999_999

export const KOMMENT_MAX = 200
export const IMYA_KAT_MAX = 40
export const NICK_MAX = 32

/** дата операции: не раньше 2020, не позже конца след. года */
export function dataOperMin(): string {
  return '2020-01-01'
}

export function dataOperMax(): string {
  const d = new Date()
  d.setFullYear(d.getFullYear() + 1)
  d.setMonth(11, 31)
  return d.toISOString().slice(0, 10)
}

export function ogranicitDen(den: number): number {
  if (!Number.isFinite(den)) return DEN_MIN
  return Math.min(DEN_MAX, Math.max(DEN_MIN, Math.round(den)))
}

export function ogranicitSumma(summa: number): number {
  if (!Number.isFinite(summa)) return SUMMA_MIN
  return Math.min(SUMMA_MAX, Math.max(SUMMA_MIN, Math.round(summa * 100) / 100))
}

export function proveritDen(den: number): string | null {
  if (!Number.isInteger(den) || den < DEN_MIN || den > DEN_MAX) {
    return `день месяца: от ${DEN_MIN} до ${DEN_MAX}`
  }
  return null
}

export function proveritSumma(summa: number): string | null {
  if (!Number.isFinite(summa) || summa < SUMMA_MIN) return `сумма от ${SUMMA_MIN}`
  if (summa > SUMMA_MAX) return `сумма не больше ${SUMMA_MAX}`
  return null
}

/** лимит бюджета: 0 = без ограничения */
export function ogranicitLimitByudzhet(summa: number): number {
  if (!Number.isFinite(summa) || summa <= 0) return 0
  return ogranicitSumma(summa)
}

export function proveritLimitByudzhet(summa: number): string | null {
  if (!Number.isFinite(summa) || summa < 0) return 'лимит не может быть отрицательным'
  if (summa === 0) return null
  return proveritSumma(summa)
}

export function proveritData(data: string): string | null {
  if (!data) return 'укажите дату'
  if (data < dataOperMin() || data > dataOperMax()) {
    return `дата от ${dataOperMin()} до ${dataOperMax()}`
  }
  return null
}
