// API автоплатежей (RecurringRule)
import http, { useMock } from './http'
import { mockDb } from './mockDb'
import type { PraviloMesyac } from '@/types/models'

export async function apiListPravila(walletId: number): Promise<PraviloMesyac[]> {
  if (useMock) return mockDb.listPravila(walletId)
  const { data } = await http.get<PraviloMesyac[]>('/recurring-rules', { params: { walletId } })
  return data
}

export async function apiCreatePravilo(
  body: Omit<PraviloMesyac, 'id' | 'categoryName'>,
): Promise<PraviloMesyac> {
  if (useMock) return mockDb.addPravilo(body)
  const { data } = await http.post<PraviloMesyac>('/recurring-rules', body)
  return data
}
