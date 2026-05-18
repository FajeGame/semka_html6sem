import axios from 'axios'

/** Текст ошибки из axios (backend ErrorDto) или Error. */
export function soobshenieOshibki(e: unknown): string {
  if (axios.isAxiosError(e)) {
    const data = e.response?.data as { message?: string } | undefined
    if (data?.message) return data.message
    if (e.response?.status === 403) return 'нет доступа'
    if (e.response?.status === 404) return 'не найдено'
  }
  if (e instanceof Error) return e.message
  return 'ошибка'
}
