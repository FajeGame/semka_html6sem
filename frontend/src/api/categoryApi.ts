// API категорий доходов и расходов
import http, { useMock } from './http'
import { mockDb } from './mockDb'
import type { Kategoriya, NovayaKategoriya, TipOper } from '@/types/models'

export async function apiListKategorii(walletId: number, tip?: TipOper): Promise<Kategoriya[]> {
  if (useMock) return mockDb.listKategorii(walletId, tip)
  const { data } = await http.get<Kategoriya[]>('/categories', { params: { walletId, tip } })
  return data
}

export async function apiGetKategoriya(id: number): Promise<Kategoriya> {
  if (useMock) return mockDb.getKategoriya(id)
  const { data } = await http.get<Kategoriya>(`/categories/${id}`)
  return data
}

export async function apiCreateKategoriya(body: NovayaKategoriya): Promise<Kategoriya> {
  if (useMock) return mockDb.addKategoriya(body)
  const { data } = await http.post<Kategoriya>('/categories', body)
  return data
}

export async function apiUpdateKategoriya(
  id: number,
  body: Pick<Kategoriya, 'name' | 'iconKey' | 'colorBg'>,
): Promise<Kategoriya> {
  if (useMock) return mockDb.updateKategoriya(id, body)
  const { data } = await http.put<Kategoriya>(`/categories/${id}`, body)
  return data
}

export async function apiDeleteKategoriya(id: number): Promise<void> {
  if (useMock) return mockDb.deleteKategoriya(id)
  await http.delete(`/categories/${id}`)
}
