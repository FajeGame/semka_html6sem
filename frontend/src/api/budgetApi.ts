// API бюджетов: список, создание/обновление лимита, удаление
import http, { useMock } from './http'
import { mockDb } from './mockDb'
import type { Byudzhet } from '@/types/models'

export async function apiListByudzhet(walletId: number, userId: number): Promise<Byudzhet[]> {
  if (useMock) return mockDb.listByudzhet(walletId, userId)
  const { data } = await http.get<Byudzhet[]>('/budgets', { params: { walletId } })
  return data
}

export async function apiUpsertByudzhet(
  walletId: number,
  categoryId: number | null,
  limitAmount: number,
): Promise<Byudzhet> {
  if (useMock) return mockDb.upsertByudzhet(walletId, categoryId, limitAmount)
  const { data } = await http.put<Byudzhet>('/budgets', {
    walletId,
    categoryId,
    limitAmount,
  })
  return data
}

export async function apiDeleteByudzhet(walletId: number, budgetId: number): Promise<void> {
  if (useMock) return mockDb.deleteByudzhet(walletId, budgetId)
  await http.delete(`/budgets/${budgetId}`, { params: { walletId } })
}
