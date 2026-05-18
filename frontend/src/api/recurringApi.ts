// API автоплатежей (RecurringRule)
import http, { useMock } from './http'
import { mockDb } from './mockDb'
import type { PraviloMesyac } from '@/types/models'

export async function apiListPravila(walletId: number): Promise<PraviloMesyac[]> {
  if (useMock) return mockDb.listPravila(walletId)
  const { data } = await http.get<PraviloMesyac[]>('/recurring-rules', { params: { walletId } })
  return data
}

export async function apiGetPravilo(id: number, walletId: number): Promise<PraviloMesyac> {
  if (useMock) {
    const p = mockDb.listPravila(walletId).find((x) => x.id === id)
    if (!p) throw new Error('правило не найдено')
    return p
  }
  const { data } = await http.get<PraviloMesyac>(`/recurring-rules/${id}`, { params: { walletId } })
  return data
}

export async function apiCreatePravilo(
  body: Omit<PraviloMesyac, 'id' | 'categoryName'>,
): Promise<PraviloMesyac> {
  if (useMock) return mockDb.addPravilo(body)
  const { data } = await http.post<PraviloMesyac>('/recurring-rules', body)
  return data
}

export async function apiUpdatePravilo(
  id: number,
  body: Omit<PraviloMesyac, 'id' | 'categoryName'>,
): Promise<PraviloMesyac> {
  if (useMock) return mockDb.updatePravilo(id, body)
  const { data } = await http.put<PraviloMesyac>(`/recurring-rules/${id}`, body)
  return data
}

export async function apiDeletePravilo(id: number, walletId: number): Promise<void> {
  if (useMock) return mockDb.deletePravilo(id)
  await http.delete(`/recurring-rules/${id}`, { params: { walletId } })
}
