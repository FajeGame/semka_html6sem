export interface ProverkaParolya {
  ok: boolean
  oshibki: string[]
}

// пароль только латиницей (ASCII печатные символы)
const LATIN = /^[\x21-\x7E]+$/

export function proveritParol(parol: string): ProverkaParolya {
  const oshibki: string[] = []
  if (parol && !LATIN.test(parol)) oshibki.push('только латиница (буквы a-z, цифры, знаки)')
  if (parol.length < 8) oshibki.push('минимум 8 символов')
  if (!/[A-Z]/.test(parol)) oshibki.push('заглавная A-Z')
  if (!/[a-z]/.test(parol)) oshibki.push('строчная a-z')
  if (!/[0-9]/.test(parol)) oshibki.push('хотя бы одна цифра')
  if (!/[^A-Za-z0-9]/.test(parol)) oshibki.push('хотя бы один спецсимвол')
  return { ok: oshibki.length === 0, oshibki }
}
