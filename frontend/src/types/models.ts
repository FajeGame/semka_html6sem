export type RolApp = 'USER' | 'ADMIN'
export type RolKoshelka = 'WALLET_OWNER' | 'WALLET_MEMBER'
// типы данных приложения (совпадают с будущим backend DTO)

export type TipOper = 'INCOME' | 'EXPENSE'

export interface UserMe {
  id: number
  email: string
  nick: string
  role: RolApp
}

export interface Koshelek {
  id: number
  name: string
  myRole: RolKoshelka
  canSeeBudget: boolean
  budgetRemaining?: number
  budgetLimit?: number
}

export interface Balans {
  income: number
  expense: number
  balance: number
}

export interface Uchastnik {
  id: number
  userId: number
  nick: string
  memberRole: RolKoshelka
  canSeeBudget: boolean
}

export interface Kategoriya {
  id: number
  walletId: number
  name: string
  tip: TipOper
  iconKey: string
  colorBg: string
}

export interface DolyaSplitInfo {
  nick: string
  shareAmount: number
}

export interface Operaciya {
  id: number
  walletId: number
  authorId: number
  authorNick: string
  categoryId: number
  categoryName: string
  type: TipOper
  amount: number
  transactionDate: string
  comment?: string
  splitDoli?: DolyaSplitInfo[]
}

export interface Byudzhet {
  id: number
  walletId: number
  categoryId: number | null
  categoryName?: string
  periodStart: string
  periodEnd: string
  limitAmount: number
  spent?: number
  remaining?: number
}

export interface PraviloMesyac {
  id: number
  walletId: number
  categoryId: number
  categoryName?: string
  amount: number
  dayOfMonth: number
  nextRunDate: string
  active: boolean
  comment?: string
}

export interface OtchetPeriod {
  walletId: number
  from: string
  to: string
  totalIncome: number
  totalExpense: number
  byCategory: { categoryId: number; categoryName: string; expense: number }[]
}

export interface RashodPoUchastniku {
  userId: number
  nick: string
  expense: number
}

export interface LoginOtvet {
  token: string
  user: UserMe
}

export interface NovayaKategoriya {
  walletId: number
  name: string
  tip: TipOper
  iconKey: string
  colorBg: string
}
