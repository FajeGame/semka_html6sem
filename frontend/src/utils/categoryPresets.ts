// шаблоны категорий и список иконок для нового кошелька
import type { TipOper } from '@/types/models'

export interface ShablonKategorii {
  name: string
  tip: TipOper
  iconKey: string
  colorBg: string
}

export const spisokIkonok = [
  'cart',
  'car',
  'home',
  'game',
  'health',
  'wallet',
  'gift',
  'plus',
  'food',
  'coffee',
  'bus',
  'plane',
  'edu',
  'pet',
  'clothes',
  'phone',
  'book',
  'star',
  'work',
  'money',
] as const

export type KlyuchIkony = (typeof spisokIkonok)[number]

export const shablonyRashod: ShablonKategorii[] = [
  { name: 'Продукты', tip: 'EXPENSE', iconKey: 'cart', colorBg: '#d8f3e4' },
  { name: 'Транспорт', tip: 'EXPENSE', iconKey: 'car', colorBg: '#cce8f8' },
  { name: 'Дом', tip: 'EXPENSE', iconKey: 'home', colorBg: '#f5e6d3' },
  { name: 'Развлечения', tip: 'EXPENSE', iconKey: 'game', colorBg: '#f0d4e8' },
  { name: 'Здоровье', tip: 'EXPENSE', iconKey: 'health', colorBg: '#e8dff5' },
]

export const shablonyDohod: ShablonKategorii[] = [
  { name: 'Зарплата', tip: 'INCOME', iconKey: 'wallet', colorBg: '#AAF0D1' },
  { name: 'Подарки', tip: 'INCOME', iconKey: 'gift', colorBg: '#d1c4e9' },
  { name: 'Прочее', tip: 'INCOME', iconKey: 'plus', colorBg: '#c8e6c9' },
]
