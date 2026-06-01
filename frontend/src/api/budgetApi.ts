/**
 * обёртки над REST /budgets — лимиты на текущий месяц.
 */
import http, { useMock } from './http'
import { mockDb } from './mockDb'
import type { Byudzhet } from '@/types/models'

/** GET /budgets?walletId */
export async function apiListByudzhet(walletId: number, userId: number): Promise<Byudzhet[]> {
  if (useMock) return mockDb.listByudzhet(walletId, userId)
  const { data } = await http.get<Byudzhet[]>('/budgets', { params: { walletId } })
  return data
}

/** POST /budgets — создать лимит */
export async function apiCreateByudzhet(
  walletId: number,
  categoryId: number | null,
  limitAmount: number,
): Promise<Byudzhet> {
  if (useMock) return mockDb.upsertByudzhet(walletId, categoryId, limitAmount)
  const { data } = await http.post<Byudzhet>('/budgets', { walletId, categoryId, limitAmount })
  return data
}

/** PUT /budgets — обновить лимит (upsert) */
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

/** DELETE /budgets/{id} */
export async function apiDeleteByudzhet(walletId: number, budgetId: number): Promise<void> {
  if (useMock) return mockDb.deleteByudzhet(walletId, budgetId)
  await http.delete(`/budgets/${budgetId}`, { params: { walletId } })
}
