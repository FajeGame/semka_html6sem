// API отчётов: за период и расходы по участникам
import http, { useMock } from './http'
import { mockDb } from './mockDb'
import type { OtchetPeriod, RashodPoUchastniku } from '@/types/models'

export async function apiOtchetPeriod(
  walletId: number,
  from: string,
  to: string,
): Promise<OtchetPeriod> {
  if (useMock) return mockDb.getOtchet(walletId, from, to)
  const { data } = await http.get<OtchetPeriod>('/reports/period', {
    params: { walletId, from, to },
  })
  return data
}

export async function apiRashodyPoLyudyam(
  walletId: number,
  from: string,
  to: string,
): Promise<RashodPoUchastniku[]> {
  if (useMock) return mockDb.rashodyPoLyudyam(walletId, from, to)
  const { data } = await http.get<RashodPoUchastniku[]>('/reports/by-member', {
    params: { walletId, from, to },
  })
  return data
}
