// API авторизации: вход, регистрация, текущий пользователь
import http, { useMock } from './http'
import { mockDb } from './mockDb'
import type { LoginOtvet, UserMe } from '@/types/models'

export async function apiLogin(email: string, password: string): Promise<LoginOtvet> {
  if (useMock) return mockDb.login(email, password)
  const { data } = await http.post<LoginOtvet>('/auth/login', { email, password })
  return data
}

export async function apiRegister(body: {
  email: string
  password: string
  nick: string
}): Promise<LoginOtvet> {
  if (useMock) return mockDb.register(body)
  const { data } = await http.post<LoginOtvet>('/auth/register', body)
  return data
}

export async function apiMe(): Promise<UserMe> {
  if (useMock) return mockDb.me()
  const { data } = await http.get<UserMe>('/auth/me')
  return data
}

export async function apiDeleteAccount(password: string): Promise<void> {
  if (useMock) return mockDb.deleteAccount(password)
  await http.delete('/auth/me', { data: { password } })
}
