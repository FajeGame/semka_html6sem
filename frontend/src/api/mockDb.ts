// in-memory «база» для разработки без backend (VITE_USE_MOCK=true)

import type {
  Balans,
  Byudzhet,
  Kategoriya,
  Koshelek,
  LoginOtvet,
  NovayaKategoriya,
  Operaciya,
  OtchetPeriod,
  RashodPoUchastniku,
  PraviloMesyac,
  Uchastnik,
  UserMe,
} from '@/types/models'
import { shablonyDohod, shablonyRashod } from '@/utils/categoryPresets'
import {
  ogranicitDen,
  ogranicitLimitByudzhet,
  proveritDen,
  proveritLimitByudzhet,
  proveritSumma,
} from '@/utils/inputLimits'
import { tekushiyPeriod } from '@/utils/monthPeriod'

// отдельные счётчики — id кошелька не прыгает на 101
let seqWallet = 1
let seqUser = 2
let seqMember = 2
let seqCategory = 8
let seqOper = 2
let seqBudget = 2
let seqPravilo = 1
let seqOther = 1

function nidWallet() {
  return ++seqWallet
}
function nidUser() {
  return ++seqUser
}
function nidMember() {
  return ++seqMember
}
function nidCategory() {
  return ++seqCategory
}
function nidOper() {
  return ++seqOper
}
function nidBudget() {
  return ++seqBudget
}
function nidPravilo() {
  return ++seqPravilo
}
function nidOther() {
  return ++seqOther
}

// тестовые пользователи и демо-данные кошелька «Семья»
const userPapa: UserMe = { id: 1, email: 'papa@test.ru', nick: 'papa', role: 'USER' }
const userMama: UserMe = { id: 2, email: 'mama@test.ru', nick: 'mama', role: 'USER' }

/** Общий список регистраций для всех вкладок (mock без backend). */
const MOCK_USERS_KEY = 'semka_mock_users'

function loadRegistered(): UserMe[] {
  try {
    const raw = localStorage.getItem(MOCK_USERS_KEY)
    return raw ? (JSON.parse(raw) as UserMe[]) : []
  } catch {
    return []
  }
}

function saveRegistered(list: UserMe[]) {
  localStorage.setItem(MOCK_USERS_KEY, JSON.stringify(list))
}

let zaregistrirovannye: UserMe[] = loadRegistered()

function allKnownUsers(): UserMe[] {
  zaregistrirovannye = loadRegistered()
  return [...zaregistrirovannye, userPapa, userMama]
}

let curUser: UserMe | null = null

const koshelki: { id: number; name: string }[] = [{ id: 1, name: 'Семья' }]

type MemberRow = Uchastnik & { walletId: number }
const members: MemberRow[] = [
  { id: 1, walletId: 1, userId: 1, nick: 'papa', memberRole: 'WALLET_OWNER', canSeeBudget: true },
  { id: 2, walletId: 1, userId: 2, nick: 'mama', memberRole: 'WALLET_MEMBER', canSeeBudget: true },
]

const kategorii: Kategoriya[] = []
const operacii: Operaciya[] = []
const splitDoli: { id: number; transactionId: number; userId: number; nick: string; shareAmount: number }[] =
  []
const byudzhety: Byudzhet[] = []
const pravila: PraviloMesyac[] = []

function nickPoUserId(userId: number): string {
  if (userId === 1) return 'papa'
  if (userId === 2) return 'mama'
  const u = zaregistrirovannye.find((x) => x.id === userId)
  return u?.nick || '?'
}

// прикрепить доли split к операции для списка
function obogatitSplit(o: Operaciya): Operaciya {
  const doli = splitDoli.filter((s) => s.transactionId === o.id)
  if (!doli.length) return o
  return {
    ...o,
    splitDoli: doli.map((s) => ({ nick: s.nick, shareAmount: s.shareAmount })),
  }
}

function seedKategorii(walletId: number) {
  for (const s of [...shablonyRashod, ...shablonyDohod]) {
    kategorii.push({ id: nidCategory(), walletId, ...s })
  }
}

seedKategorii(1)

operacii.push(
  {
    id: 1,
    walletId: 1,
    authorId: 1,
    authorNick: 'papa',
    categoryId: 6,
    categoryName: 'Зарплата',
    type: 'INCOME',
    amount: 80000,
    transactionDate: '2026-05-01',
  },
  {
    id: 2,
    walletId: 1,
    authorId: 2,
    authorNick: 'mama',
    categoryId: 1,
    categoryName: 'Продукты',
    type: 'EXPENSE',
    amount: 3500,
    transactionDate: '2026-05-10',
  },
)

;(() => {
  const { from, to } = tekushiyPeriod()
  byudzhety.push(
    {
      id: 1,
      walletId: 1,
      categoryId: null,
      periodStart: from,
      periodEnd: to,
      limitAmount: 50000,
    },
    {
      id: 2,
      walletId: 1,
      categoryId: 1,
      categoryName: 'Продукты',
      periodStart: from,
      periodEnd: to,
      limitAmount: 15000,
    },
  )
})()

pravila.push({
  id: 1,
  walletId: 1,
  categoryId: 1,
  categoryName: 'Продукты',
  amount: 500,
  dayOfMonth: 5,
  nextRunDate: '2026-06-05',
  active: true,
  comment: 'интернет',
})

function findMember(walletId: number, userId: number): MemberRow | undefined {
  return members.find((m) => m.walletId === walletId && m.userId === userId)
}

function walletsForUser(userId: number): number[] {
  return [...new Set(members.filter((m) => m.userId === userId).map((m) => m.walletId))]
}

// баланс = сумма доходов − сумма расходов
function schetBalans(walletId: number): Balans {
  let income = 0
  let expense = 0
  for (const o of operacii) {
    if (o.walletId !== walletId) continue
    if (o.type === 'INCOME') income += o.amount
    else expense += o.amount
  }
  return { income, expense, balance: income - expense }
}

function schetSpent(walletId: number, catId: number | null, from: string, to: string): number {
  return operacii
    .filter(
      (o) =>
        o.walletId === walletId &&
        o.type === 'EXPENSE' &&
        o.transactionDate >= from &&
        o.transactionDate <= to &&
        (catId === null ? true : o.categoryId === catId),
    )
    .reduce((s, o) => s + o.amount, 0)
}

function obshiyByudzhet(walletId: number) {
  const { from, to } = tekushiyPeriod()
  const b = byudzhetZaPeriod(walletId, null, from)
  if (!b || b.limitAmount <= 0) return null
  const spent = schetSpent(walletId, null, from, to)
  return { limit: b.limitAmount, remaining: b.limitAmount - spent }
}

function byudzhetZaPeriod(walletId: number, categoryId: number | null, periodStart: string) {
  return byudzhety.find(
    (x) =>
      x.walletId === walletId &&
      x.categoryId === categoryId &&
      x.periodStart === periodStart,
  )
}

function obogatitByudzhet(b: Byudzhet, from: string, to: string): Byudzhet {
  const spent = schetSpent(b.walletId, b.categoryId, from, to)
  return { ...b, spent, remaining: b.limitAmount - spent }
}

function proveritVladelec(walletId: number) {
  if (!curUser) throw new Error('войдите')
  const mem = findMember(walletId, curUser.id)
  if (mem?.memberRole !== 'WALLET_OWNER') throw new Error('только владелец')
}

function toKoshelek(walletId: number, userId: number): Koshelek {
  const k = koshelki.find((x) => x.id === walletId)!
  const mem = findMember(walletId, userId)!
  const canSee = mem.memberRole === 'WALLET_OWNER' || mem.canSeeBudget
  const bud = canSee ? obshiyByudzhet(walletId) : null
  return {
    id: k.id,
    name: k.name,
    myRole: mem.memberRole,
    canSeeBudget: canSee,
    budgetLimit: bud?.limit,
    budgetRemaining: bud?.remaining,
  }
}

function seedWalletDefaults(walletId: number, owner: UserMe) {
  seedKategorii(walletId)
  const { from, to } = tekushiyPeriod()
  byudzhety.push({
    id: nidBudget(),
    walletId,
    categoryId: null,
    periodStart: from,
    periodEnd: to,
    limitAmount: 0,
  })
  members.push({
    id: nidMember(),
    walletId,
    userId: owner.id,
    nick: owner.nick,
    memberRole: 'WALLET_OWNER',
    canSeeBudget: true,
  })
}

// публичные методы — вызываются из *Api.ts при useMock
export const mockDb = {
  // --- авторизация ---
  login(email: string, _password: string): LoginOtvet {
    if (email.includes('mama')) curUser = { ...userMama }
    else if (email.includes('papa')) curUser = { ...userPapa }
    else {
      const u = loadRegistered().find((x) => x.email === email)
      if (!u) throw new Error('нет такого')
      curUser = u
    }
    return { token: 'mock-' + curUser.id, user: curUser }
  },

  register(data: { email: string; password: string; nick: string }): LoginOtvet {
    const existing = loadRegistered()
    if (existing.some((u) => u.email === data.email || u.nick === data.nick)) {
      throw new Error('занято')
    }
    if (['papa', 'mama'].includes(data.nick)) throw new Error('ник занят')
    const u: UserMe = { id: nidUser(), email: data.email, nick: data.nick, role: 'USER' }
    zaregistrirovannye = [...loadRegistered(), u]
    saveRegistered(zaregistrirovannye)
    curUser = u
    return { token: 'mock-' + u.id, user: u }
  },

  me(): UserMe {
    if (!curUser) throw new Error('нет входа')
    return curUser
  },

  // --- кошельки и участники ---
  listKoshelki(): Koshelek[] {
    if (!curUser) return []
    return walletsForUser(curUser.id).map((wid) => toKoshelek(wid, curUser!.id))
  },

  getKoshelek(walletId: number): Koshelek {
    if (!curUser) throw new Error('войдите')
    if (!findMember(walletId, curUser.id)) throw new Error('нет доступа')
    return toKoshelek(walletId, curUser.id)
  },

  getBalans(walletId: number): Balans {
    return schetBalans(walletId)
  },

  listUchastniki(walletId: number): Uchastnik[] {
    return members.filter((m) => m.walletId === walletId)
  },

  listKategorii(walletId: number, tip?: string): Kategoriya[] {
    return kategorii.filter((k) => k.walletId === walletId && (!tip || k.tip === tip))
  },

  getKategoriya(id: number): Kategoriya {
    const k = kategorii.find((c) => c.id === id)
    if (!k) throw new Error('категория не найдена')
    return k
  },

  listOperacii(walletId: number, tip?: string): Operaciya[] {
    return operacii
      .filter((o) => o.walletId === walletId && (!tip || o.type === tip))
      .sort((a, b) => b.transactionDate.localeCompare(a.transactionDate))
      .map(obogatitSplit)
  },

  getOperaciya(id: number): Operaciya {
    const o = operacii.find((x) => x.id === id)
    if (!o) throw new Error('операция не найдена')
    return obogatitSplit(o)
  },

  // --- бюджеты ---
  listByudzhet(walletId: number, userId: number): Byudzhet[] {
    const mem = findMember(walletId, userId)
    if (!mem) return []
    if (mem.memberRole !== 'WALLET_OWNER' && !mem.canSeeBudget) return []
    const { from, to } = tekushiyPeriod()
    return byudzhety
      .filter((b) => b.walletId === walletId && b.periodStart === from)
      .map((b) => obogatitByudzhet(b, from, to))
  },

  upsertByudzhet(walletId: number, categoryId: number | null, limitAmount: number): Byudzhet {
    proveritVladelec(walletId)
    const limit = ogranicitLimitByudzhet(limitAmount)
    const err = proveritLimitByudzhet(limitAmount)
    if (err) throw new Error(err)
    if (categoryId !== null) {
      const kat = kategorii.find((k) => k.id === categoryId && k.walletId === walletId)
      if (!kat || kat.tip !== 'EXPENSE') throw new Error('нужна категория расхода')
    }
    const { from, to } = tekushiyPeriod()
    let b = byudzhetZaPeriod(walletId, categoryId, from)
    const katName =
      categoryId !== null ? kategorii.find((k) => k.id === categoryId)?.name : undefined
    if (!b) {
      b = {
        id: nidBudget(),
        walletId,
        categoryId,
        categoryName: katName,
        periodStart: from,
        periodEnd: to,
        limitAmount: limit,
      }
      byudzhety.push(b)
    } else {
      b.limitAmount = limit
      if (katName) b.categoryName = katName
    }
    return obogatitByudzhet(b, from, to)
  },

  deleteByudzhet(walletId: number, budgetId: number): void {
    proveritVladelec(walletId)
    const idx = byudzhety.findIndex((b) => b.id === budgetId && b.walletId === walletId)
    if (idx < 0) throw new Error('бюджет не найден')
    const b = byudzhety[idx]!
    if (b.categoryId === null) throw new Error('общий бюджет нельзя удалить — задайте лимит 0')
    byudzhety.splice(idx, 1)
  },

  listPravila(walletId: number): PraviloMesyac[] {
    return pravila.filter((p) => p.walletId === walletId)
  },

  // --- отчёты ---
  rashodyPoLyudyam(walletId: number, from: string, to: string): RashodPoUchastniku[] {
    const map = new Map<number, number>()
    const list = operacii.filter(
      (o) =>
        o.walletId === walletId &&
        o.type === 'EXPENSE' &&
        o.transactionDate >= from &&
        o.transactionDate <= to,
    )
    for (const o of list) {
      const doli = splitDoli.filter((s) => s.transactionId === o.id)
      if (doli.length) {
        for (const d of doli) {
          map.set(d.userId, (map.get(d.userId) || 0) + d.shareAmount)
        }
      } else {
        map.set(o.authorId, (map.get(o.authorId) || 0) + o.amount)
      }
    }
    return members
      .filter((m) => m.walletId === walletId)
      .map((m) => ({
        userId: m.userId,
        nick: m.nick,
        expense: Math.round((map.get(m.userId) || 0) * 100) / 100,
      }))
      .filter((x) => x.expense > 0)
      .sort((a, b) => b.expense - a.expense)
  },

  getOtchet(walletId: number, from: string, to: string): OtchetPeriod {
    const list = operacii.filter(
      (o) => o.walletId === walletId && o.transactionDate >= from && o.transactionDate <= to,
    )
    let totalIncome = 0
    let totalExpense = 0
    const map = new Map<number, { categoryName: string; expense: number }>()
    for (const o of list) {
      if (o.type === 'INCOME') totalIncome += o.amount
      else {
        totalExpense += o.amount
        const cur = map.get(o.categoryId) || { categoryName: o.categoryName, expense: 0 }
        cur.expense += o.amount
        map.set(o.categoryId, cur)
      }
    }
    return {
      walletId,
      from,
      to,
      totalIncome,
      totalExpense,
      byCategory: [...map.entries()].map(([categoryId, v]) => ({
        categoryId,
        categoryName: v.categoryName,
        expense: v.expense,
      })),
    }
  },

  addKoshelek(name: string): Koshelek {
    if (!curUser) throw new Error('войдите')
    const id = nidWallet()
    koshelki.push({ id, name })
    seedWalletDefaults(id, curUser)
    return toKoshelek(id, curUser.id)
  },

  addUchastnik(walletId: number, nick: string): void {
    const u = allKnownUsers().find((x) => x.nick === nick)
    if (!u) throw new Error('ник не найден — человек должен зарегистрироваться в системе')
    if (findMember(walletId, u.id)) throw new Error('уже в кошельке')
    members.push({
      id: nidMember(),
      walletId,
      userId: u.id,
      nick: u.nick,
      memberRole: 'WALLET_MEMBER',
      canSeeBudget: false,
    })
  },

  setCanSeeBudget(uchastnikId: number, val: boolean): void {
    const u = members.find((x) => x.id === uchastnikId)
    if (u) u.canSeeBudget = val
  },

  removeUchastnik(walletId: number, uchastnikId: number): void {
    if (!curUser) throw new Error('войдите')
    proveritVladelec(walletId)
    const m = members.find((x) => x.id === uchastnikId && x.walletId === walletId)
    if (!m) throw new Error('участник не найден')
    if (m.memberRole === 'WALLET_OWNER') throw new Error('нельзя удалить владельца')
    members.splice(
      members.findIndex((x) => x.id === uchastnikId),
      1,
    )
  },

  leaveKoshelek(walletId: number): void {
    if (!curUser) throw new Error('войдите')
    const uid = curUser.id
    const m = findMember(walletId, uid)
    if (!m) throw new Error('нет доступа')
    if (m.memberRole === 'WALLET_OWNER') {
      throw new Error('владелец не может выйти — удалите кошелёк')
    }
    members.splice(
      members.findIndex((x) => x.walletId === walletId && x.userId === uid),
      1,
    )
  },

  deleteAccount(_password: string): void {
    if (!curUser) throw new Error('войдите')
    if (['papa', 'mama'].includes(curUser.nick)) throw new Error('демо-аккаунт нельзя удалить')
    const uid = curUser.id
    const owned = koshelki.filter((k) => members.some((m) => m.walletId === k.id && m.userId === uid && m.memberRole === 'WALLET_OWNER'))
    for (const w of owned) mockDb.deleteKoshelek(w.id)
    members.splice(0, members.length, ...members.filter((m) => m.userId !== uid))
    operacii.splice(0, operacii.length, ...operacii.filter((o) => o.authorId !== uid))
    zaregistrirovannye = loadRegistered().filter((u) => u.id !== uid)
    saveRegistered(zaregistrirovannye)
    curUser = null
  },

  addKategoriya(data: NovayaKategoriya): Kategoriya {
    const k: Kategoriya = { id: nidCategory(), ...data }
    kategorii.push(k)
    return k
  },

  addPravilo(data: Omit<PraviloMesyac, 'id' | 'categoryName'>): PraviloMesyac {
    const den = ogranicitDen(data.dayOfMonth)
    const errDen = proveritDen(den)
    const errSum = proveritSumma(data.amount)
    if (errDen) throw new Error(errDen)
    if (errSum) throw new Error(errSum)
    const kat = kategorii.find((k) => k.id === data.categoryId)
    const p: PraviloMesyac = { ...data, dayOfMonth: den, id: nidPravilo(), categoryName: kat?.name }
    pravila.push(p)
    return p
  },

  addOperaciya(data: {
    walletId: number
    categoryId: number
    type: Operaciya['type']
    amount: number
    transactionDate: string
    comment?: string
  }): Operaciya {
    const errSum = proveritSumma(data.amount)
    if (errSum) throw new Error(errSum)
    const kat = kategorii.find((k) => k.id === data.categoryId)
    const o: Operaciya = {
      id: nidOper(),
      walletId: data.walletId,
      authorId: curUser?.id || 0,
      authorNick: curUser?.nick || '?',
      categoryId: data.categoryId,
      categoryName: kat?.name || '?',
      type: data.type,
      amount: data.amount,
      transactionDate: data.transactionDate,
      comment: data.comment,
    }
    operacii.push(o)
    return o
  },

  // разделённый расход: одна операция + доли участников
  addSplit(data: {
    walletId: number
    categoryId: number
    totalAmount: number
    transactionDate: string
    comment?: string
    shares: { userId: number; shareAmount: number }[]
  }): Operaciya {
    if (!data.shares.length) throw new Error('выберите участников')
    const errTotal = proveritSumma(data.totalAmount)
    if (errTotal) throw new Error(errTotal)
    const summaDoley = data.shares.reduce((s, x) => s + x.shareAmount, 0)
    if (Math.abs(summaDoley - data.totalAmount) > 0.02) {
      throw new Error(`сумма долей ${summaDoley} ≠ ${data.totalAmount}`)
    }
    const o = this.addOperaciya({
      walletId: data.walletId,
      categoryId: data.categoryId,
      type: 'EXPENSE',
      amount: data.totalAmount,
      transactionDate: data.transactionDate,
      comment: data.comment || 'разделили чек',
    })
    for (const sh of data.shares) {
      splitDoli.push({
        id: nidOther(),
        transactionId: o.id,
        userId: sh.userId,
        nick: nickPoUserId(sh.userId),
        shareAmount: sh.shareAmount,
      })
    }
    return obogatitSplit(o)
  },

  delOperaciya(id: number): void {
    const i = operacii.findIndex((o) => o.id === id)
    if (i >= 0) operacii.splice(i, 1)
    for (let j = splitDoli.length - 1; j >= 0; j--) {
      const d = splitDoli[j]
      if (d && d.transactionId === id) splitDoli.splice(j, 1)
    }
  },

  updateKoshelek(id: number, name: string): Koshelek {
    const w = koshelki.find((k) => k.id === id)
    if (!w) throw new Error('кошелёк не найден')
    w.name = name
    return toKoshelek(id, curUser!.id)
  },

  deleteKoshelek(id: number): void {
    koshelki.splice(
      koshelki.findIndex((k) => k.id === id),
      1,
    )
    members.splice(
      0,
      members.length,
      ...members.filter((m) => m.walletId !== id),
    )
    kategorii.splice(
      0,
      kategorii.length,
      ...kategorii.filter((k) => k.walletId !== id),
    )
    operacii.splice(
      0,
      operacii.length,
      ...operacii.filter((o) => o.walletId !== id),
    )
    byudzhety.splice(
      0,
      byudzhety.length,
      ...byudzhety.filter((b) => b.walletId !== id),
    )
    pravila.splice(
      0,
      pravila.length,
      ...pravila.filter((p) => p.walletId !== id),
    )
  },

  updateKategoriya(
    id: number,
    data: { name: string; iconKey: string; colorBg: string },
  ): Kategoriya {
    const k = kategorii.find((c) => c.id === id)
    if (!k) throw new Error('категория не найдена')
    Object.assign(k, data)
    return k
  },

  deleteKategoriya(id: number): void {
    if (operacii.some((o) => o.categoryId === id)) throw new Error('есть операции с этой категорией')
    kategorii.splice(
      kategorii.findIndex((c) => c.id === id),
      1,
    )
  },

  updateOperaciya(
    id: number,
    data: {
      categoryId: number
      type: Operaciya['type']
      amount: number
      transactionDate: string
      comment?: string
    },
  ): Operaciya {
    if (splitDoli.some((d) => d.transactionId === id)) {
      throw new Error('операцию с разделением чека нельзя редактировать')
    }
    const o = operacii.find((x) => x.id === id)
    if (!o) throw new Error('операция не найдена')
    const kat = kategorii.find((k) => k.id === data.categoryId)
    Object.assign(o, data, { categoryName: kat?.name || '?' })
    return o
  },

  updatePravilo(id: number, data: Omit<PraviloMesyac, 'id' | 'categoryName'>): PraviloMesyac {
    const p = pravila.find((x) => x.id === id)
    if (!p) throw new Error('правило не найдено')
    Object.assign(p, data)
    p.categoryName = kategorii.find((k) => k.id === p.categoryId)?.name
    return p
  },

  deletePravilo(id: number): void {
    pravila.splice(
      pravila.findIndex((p) => p.id === id),
      1,
    )
  },
}
