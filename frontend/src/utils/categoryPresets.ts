/**
 * шаблоны категорий и список ключей иконок для UI.
 * при создании кошелька (mock и backend CategoryTemplates) подставляются похожие наборы.
 */
import type { TipOper } from '@/types/models'

export interface ShablonKategorii {
  name: string
  tip: TipOper // INCOME или EXPENSE
  iconKey: string // ключ для CategorySvg.vue
  colorBg: string // hex фона кнопки категории
}

/** все доступные иконки в IconPicker */
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

/** категории расходов по умолчанию */
export const shablonyRashod: ShablonKategorii[] = [
  { name: 'Продукты', tip: 'EXPENSE', iconKey: 'cart', colorBg: '#d8f3e4' },
  { name: 'Транспорт', tip: 'EXPENSE', iconKey: 'car', colorBg: '#cce8f8' },
  { name: 'Дом', tip: 'EXPENSE', iconKey: 'home', colorBg: '#f5e6d3' },
  { name: 'Развлечения', tip: 'EXPENSE', iconKey: 'game', colorBg: '#f0d4e8' },
  { name: 'Здоровье', tip: 'EXPENSE', iconKey: 'health', colorBg: '#e8dff5' },
]

/** категории доходов по умолчанию */
export const shablonyDohod: ShablonKategorii[] = [
  { name: 'Зарплата', tip: 'INCOME', iconKey: 'wallet', colorBg: '#AAF0D1' },
  { name: 'Подарки', tip: 'INCOME', iconKey: 'gift', colorBg: '#d1c4e9' },
  { name: 'Прочее', tip: 'INCOME', iconKey: 'plus', colorBg: '#c8e6c9' },
]
