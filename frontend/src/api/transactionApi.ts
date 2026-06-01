/**
 * обёртки над REST /transactions — доходы, расходы, split.
 */
import http, { useMock } from './http'
import { mockDb } from './mockDb'
import type { Operaciya, TipOper } from '@/types/models'

/** GET /transactions?walletId&tip */
export async function apiListOperacii(walletId: number, tip?: TipOper): Promise<Operaciya[]> {
  if (useMock) return mockDb.listOperacii(walletId, tip)
  const { data } = await http.get<Operaciya[]>('/transactions', { params: { walletId, tip } })
  return data
}

/** GET /transactions/{id} */
export async function apiGetOperaciya(id: number): Promise<Operaciya> {
  if (useMock) return mockDb.getOperaciya(id)
  const { data } = await http.get<Operaciya>(`/transactions/${id}`)
  return data
}

/** POST /transactions */
export async function apiCreateOperaciya(body: {
  walletId: number
  categoryId: number
  type: TipOper
  amount: number
  transactionDate: string
  comment?: string
}): Promise<Operaciya> {
  if (useMock) return mockDb.addOperaciya(body)
  const { data } = await http.post<Operaciya>('/transactions', body)
  return data
}

/** PUT /transactions/{id} */
export async function apiUpdateOperaciya(
  id: number,
  body: {
    categoryId: number
    type: TipOper
    amount: number
    transactionDate: string
    comment?: string
  },
): Promise<Operaciya> {
  if (useMock) return mockDb.updateOperaciya(id, body)
  const { data } = await http.put<Operaciya>(`/transactions/${id}`, body)
  return data
}

/** DELETE /transactions/{id} */
export async function apiDeleteOperaciya(id: number): Promise<void> {
  if (useMock) return mockDb.delOperaciya(id)
  await http.delete(`/transactions/${id}`)
}

/** POST /transactions/split — разделить чек */
export async function apiSplit(body: {
  walletId: number
  categoryId: number
  totalAmount: number
  transactionDate: string
  comment?: string
  shares: { userId: number; shareAmount: number }[]
}): Promise<void> {
  if (useMock) {
    mockDb.addSplit(body)
    return
  }
  await http.post('/transactions/split', body)
}
