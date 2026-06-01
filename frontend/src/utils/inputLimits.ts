/**
 * ограничения и проверки полей ввода на фронте.
 * те же правила, что на backend (@DecimalMin, день месяца 1–31).
 * используется в формах KoshelekPage, OwnerPanel и в mockDb.
 */

// день месяца для автоплатежа
export const DEN_MIN = 1
export const DEN_MAX = 31

// сумма операции
export const SUMMA_MIN = 0.01
export const SUMMA_MAX = 99_999_999

export const KOMMENT_MAX = 200 // длина комментария к операции
export const IMYA_KAT_MAX = 40 // название категории
export const NICK_MAX = 32 // ник при регистрации

/** минимальная дата операции (не раньше 2020) */
export function dataOperMin(): string {
  return '2020-01-01'
}

/** максимальная дата операции (конец следующего года) */
export function dataOperMax(): string {
  const d = new Date()
  d.setFullYear(d.getFullYear() + 1)
  d.setMonth(11, 31)
  return d.toISOString().slice(0, 10)
}

/** обрезать день месяца в допустимый диапазон */
export function ogranicitDen(den: number): number {
  if (!Number.isFinite(den)) return DEN_MIN
  return Math.min(DEN_MAX, Math.max(DEN_MIN, Math.round(den)))
}

/** обрезать сумму до 2 знаков и лимитов MIN/MAX */
export function ogranicitSumma(summa: number): number {
  if (!Number.isFinite(summa)) return SUMMA_MIN
  return Math.min(SUMMA_MAX, Math.max(SUMMA_MIN, Math.round(summa * 100) / 100))
}

/** null = ок, иначе текст ошибки */
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

/** лимит бюджета: 0 означает «без лимита» */
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
