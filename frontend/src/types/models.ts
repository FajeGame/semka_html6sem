/**
 * типы данных Vue-приложения — зеркало JSON backend (dto/Dtos.kt).
 * при изменении API на Kotlin обновлять поля здесь же.
 */

// роль в системе (users.role)
export type RolApp = 'USER' | 'ADMIN'

// роль в конкретном кошельке (wallet_members.member_role)
export type RolKoshelka = 'WALLET_OWNER' | 'WALLET_MEMBER'

// доход или расход
export type TipOper = 'INCOME' | 'EXPENSE'

/** GET /auth/me, user в LoginOtvet */
export interface UserMe {
  id: number // id пользователя
  email: string
  nick: string
  role: RolApp
}

/** кошелёк в списке и на карточке */
export interface Koshelek {
  id: number
  name: string // название
  myRole: RolKoshelka // моя роль
  canSeeBudget: boolean // показывать ли блок бюджета
  budgetRemaining?: number // остаток общего лимита месяца
  budgetLimit?: number // общий лимит
}

/** GET /wallets/{id}/balance */
export interface Balans {
  income: number // сумма доходов
  expense: number // сумма расходов
  balance: number // income - expense
}

/** участник кошелька */
export interface Uchastnik {
  id: number // id wallet_members
  userId: number
  nick: string
  memberRole: RolKoshelka
  canSeeBudget: boolean
}

/** категория дохода/расхода */
export interface Kategoriya {
  id: number
  walletId: number
  name: string
  tip: TipOper
  iconKey: string // ключ иконки на фронте
  colorBg: string // цвет фона
}

/** доля в split для списка операций */
export interface DolyaSplitInfo {
  nick: string
  shareAmount: number
}

/** одна операция */
export interface Operaciya {
  id: number
  walletId: number
  authorId: number
  authorNick: string // кто внёс
  categoryId: number
  categoryName: string
  type: TipOper
  amount: number
  transactionDate: string // ISO дата YYYY-MM-DD
  comment?: string
  splitDoli?: DolyaSplitInfo[] // только у split
}

/** лимит бюджета на период */
export interface Byudzhet {
  id: number
  walletId: number
  categoryId: number | null // null = весь кошелёк
  categoryName?: string
  periodStart: string
  periodEnd: string
  limitAmount: number
  spent?: number // уже потрачено
  remaining?: number // остаток
}

/** правило автоплатежа */
export interface PraviloMesyac {
  id: number
  walletId: number
  categoryId: number
  categoryName?: string
  amount: number
  dayOfMonth: number // 1–31
  nextRunDate: string
  active: boolean
  comment?: string
}

/** GET /reports/period */
export interface OtchetPeriod {
  walletId: number
  from: string
  to: string
  totalIncome: number
  totalExpense: number
  byCategory: { categoryId: number; categoryName: string; expense: number }[]
}

/** GET /reports/by-member */
export interface RashodPoUchastniku {
  userId: number
  nick: string
  expense: number
}

/** ответ login / register */
export interface LoginOtvet {
  token: string // JWT
  user: UserMe
}

/** тело POST /categories */
export interface NovayaKategoriya {
  walletId: number
  name: string
  tip: TipOper
  iconKey: string
  colorBg: string
}
