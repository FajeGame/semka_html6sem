// API категорий доходов и расходов
import http, { useMock } from './http'
import { mockDb } from './mockDb'
import type { Kategoriya, NovayaKategoriya, TipOper } from '@/types/models'

export async function apiListKategorii(walletId: number, tip?: TipOper): Promise<Kategoriya[]> {
  if (useMock) return mockDb.listKategorii(walletId, tip)
  const { data } = await http.get<Kategoriya[]>('/categories', { params: { walletId, tip } })
  return data
}

export async function apiCreateKategoriya(body: NovayaKategoriya): Promise<Kategoriya> {
  if (useMock) return mockDb.addKategoriya(body)
  const { data } = await http.post<Kategoriya>('/categories', body)
  return data
}
