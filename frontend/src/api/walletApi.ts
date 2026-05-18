// API кошельков: список, баланс, участники, приглашения
import http, { useMock } from './http'
import { mockDb } from './mockDb'
import type { Balans, Koshelek, Uchastnik } from '@/types/models'

export async function apiListKoshelki(): Promise<Koshelek[]> {
  if (useMock) return mockDb.listKoshelki()
  const { data } = await http.get<Koshelek[]>('/wallets')
  return data
}

export async function apiGetKoshelek(walletId: number): Promise<Koshelek> {
  if (useMock) return mockDb.getKoshelek(walletId)
  const { data } = await http.get<Koshelek>(`/wallets/${walletId}`)
  return data
}

export async function apiCreateKoshelek(name: string): Promise<Koshelek> {
  if (useMock) return mockDb.addKoshelek(name)
  const { data } = await http.post<Koshelek>('/wallets', { name })
  return data
}

export async function apiUpdateKoshelek(walletId: number, name: string): Promise<Koshelek> {
  if (useMock) return mockDb.updateKoshelek(walletId, name)
  const { data } = await http.put<Koshelek>(`/wallets/${walletId}`, { name })
  return data
}

export async function apiDeleteKoshelek(walletId: number): Promise<void> {
  if (useMock) return mockDb.deleteKoshelek(walletId)
  await http.delete(`/wallets/${walletId}`)
}

export async function apiGetBalans(walletId: number): Promise<Balans> {
  if (useMock) return mockDb.getBalans(walletId)
  const { data } = await http.get<Balans>(`/wallets/${walletId}/balance`)
  return data
}

export async function apiListUchastniki(walletId: number): Promise<Uchastnik[]> {
  if (useMock) return mockDb.listUchastniki(walletId)
  const { data } = await http.get<Uchastnik[]>(`/wallets/${walletId}/members`)
  return data
}

export async function apiAddUchastnik(walletId: number, nick: string): Promise<void> {
  if (useMock) return mockDb.addUchastnik(walletId, nick)
  await http.post(`/wallets/${walletId}/members`, { nick })
}

export async function apiSetCanSeeBudget(
  walletId: number,
  memberId: number,
  canSeeBudget: boolean,
): Promise<void> {
  if (useMock) return mockDb.setCanSeeBudget(memberId, canSeeBudget)
  await http.patch(`/wallets/${walletId}/members/${memberId}`, { canSeeBudget })
}

export async function apiRemoveUchastnik(walletId: number, memberId: number): Promise<void> {
  if (useMock) return mockDb.removeUchastnik(walletId, memberId)
  await http.delete(`/wallets/${walletId}/members/${memberId}`)
}

export async function apiLeaveKoshelek(walletId: number): Promise<void> {
  if (useMock) return mockDb.leaveKoshelek(walletId)
  await http.post(`/wallets/${walletId}/leave`)
}
