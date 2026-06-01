/**
 * обёртки над REST /auth — вход, регистрация, профиль, удаление аккаунта.
 */
import http, { useMock } from './http'
import { mockDb } from './mockDb'
import type { LoginOtvet, UserMe } from '@/types/models'

/** POST /auth/login — email + password → token и user */
export async function apiLogin(email: string, password: string): Promise<LoginOtvet> {
  if (useMock) return mockDb.login(email, password)
  const { data } = await http.post<LoginOtvet>('/auth/login', { email, password })
  return data
}

/** POST /auth/register — новый пользователь, сразу выдаётся token */
export async function apiRegister(body: {
  email: string
  password: string
  nick: string
}): Promise<LoginOtvet> {
  if (useMock) return mockDb.register(body)
  const { data } = await http.post<LoginOtvet>('/auth/register', body)
  return data
}

/** GET /auth/me — текущий пользователь по JWT */
export async function apiMe(): Promise<UserMe> {
  if (useMock) return mockDb.me()
  const { data } = await http.get<UserMe>('/auth/me')
  return data
}

/** DELETE /auth/me — удалить аккаунт (нужен password в теле) */
export async function apiDeleteAccount(password: string): Promise<void> {
  if (useMock) return mockDb.deleteAccount(password)
  await http.delete('/auth/me', { data: { password } })
}
