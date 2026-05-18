<script setup lang="ts">
// боковое меню настроек владельца кошелька
import { computed, ref, watch } from 'vue'
import type { Byudzhet, Kategoriya, PraviloMesyac, TipOper, Uchastnik } from '@/types/models'
import { apiDeleteByudzhet, apiUpsertByudzhet } from '@/api/budgetApi'
import { apiCreateKategoriya } from '@/api/categoryApi'
import { apiCreatePravilo } from '@/api/recurringApi'
import { apiAddUchastnik, apiSetCanSeeBudget } from '@/api/walletApi'
import IconPicker from '@/components/wallet/IconPicker.vue'
import {
  DEN_MAX,
  DEN_MIN,
  ogranicitDen,
  ogranicitLimitByudzhet,
  ogranicitSumma,
  proveritDen,
  proveritLimitByudzhet,
  proveritSumma,
  SUMMA_MAX,
  SUMMA_MIN,
} from '@/utils/inputLimits'

const props = defineProps<{
  open: boolean
  walletId: number
  uchastniki: Uchastnik[]
  kategorii: Kategoriya[]
  pravila: PraviloMesyac[]
  byudzhety: Byudzhet[]
}>()

const emit = defineEmits<{ close: []; refresh: [] }>()

// приглашение участника
const nickNew = ref('')

// своя категория
const imyaKat = ref('')
const tipKat = ref<TipOper>('EXPENSE')
const iconKat = ref('cart')
const colorKat = ref('#AAF0D1')

// автоплатёж (recurring)
const pravAmount = ref(0)
const pravDay = ref(5)
const pravKat = ref(0)
const oshibkaPrav = ref('')

// управление бюджетом
const limitObshiy = ref<number | null>(null)
const budKatId = ref(0)
const limitKat = ref<number | null>(null)
const oshibkaBud = ref('')

const rashodKat = computed(() => props.kategorii.filter((k) => k.tip === 'EXPENSE'))

const obshiyBud = computed(() => props.byudzhety.find((b) => b.categoryId === null))
const katByudzhety = computed(() =>
  props.byudzhety.filter((b) => b.categoryId !== null && b.limitAmount > 0),
)

watch(
  () => props.open,
  (v) => {
    if (!v) return
    limitObshiy.value = obshiyBud.value?.limitAmount ?? null
    budKatId.value = rashodKat.value[0]?.id ?? 0
    limitKat.value = null
    oshibkaBud.value = ''
  },
)

watch(obshiyBud, (b) => {
  if (props.open && b) limitObshiy.value = b.limitAmount
})

async function priglasit() {
  await apiAddUchastnik(props.walletId, nickNew.value.trim())
  nickNew.value = ''
  emit('refresh')
}

async function toggleBudget(u: Uchastnik) {
  await apiSetCanSeeBudget(props.walletId, u.id, !u.canSeeBudget)
  emit('refresh')
}

async function sohranitObshiy() {
  oshibkaBud.value = ''
  if (limitObshiy.value == null) {
    oshibkaBud.value = 'укажите лимит (0 — убрать)'
    return
  }
  const limit = ogranicitLimitByudzhet(limitObshiy.value)
  const err = proveritLimitByudzhet(limitObshiy.value)
  if (err) {
    oshibkaBud.value = err
    return
  }
  try {
    await apiUpsertByudzhet(props.walletId, null, limit)
    emit('refresh')
  } catch (e: unknown) {
    oshibkaBud.value = e instanceof Error ? e.message : 'ошибка'
  }
}

async function sohranitKatByudzhet() {
  oshibkaBud.value = ''
  if (!budKatId.value) {
    oshibkaBud.value = 'выберите категорию'
    return
  }
  if (limitKat.value == null) {
    oshibkaBud.value = 'укажите лимит (0 — убрать)'
    return
  }
  const limit = ogranicitLimitByudzhet(limitKat.value)
  const err = proveritLimitByudzhet(limitKat.value)
  if (err) {
    oshibkaBud.value = err
    return
  }
  try {
    await apiUpsertByudzhet(props.walletId, budKatId.value, limit)
    limitKat.value = null
    emit('refresh')
  } catch (e: unknown) {
    oshibkaBud.value = e instanceof Error ? e.message : 'ошибка'
  }
}

async function udalitKatByudzhet(id: number) {
  oshibkaBud.value = ''
  try {
    await apiDeleteByudzhet(props.walletId, id)
    emit('refresh')
  } catch (e: unknown) {
    oshibkaBud.value = e instanceof Error ? e.message : 'ошибка'
  }
}

async function addKat() {
  await apiCreateKategoriya({
    walletId: props.walletId,
    name: imyaKat.value.trim(),
    tip: tipKat.value,
    iconKey: iconKat.value,
    colorBg: colorKat.value,
  })
  imyaKat.value = ''
  emit('refresh')
}

function naDenBlur() {
  pravDay.value = ogranicitDen(pravDay.value)
}

async function addPravilo() {
  oshibkaPrav.value = ''
  if (!pravKat.value) {
    oshibkaPrav.value = 'выберите категорию'
    return
  }
  pravDay.value = ogranicitDen(pravDay.value)
  pravAmount.value = ogranicitSumma(pravAmount.value)
  const errDen = proveritDen(pravDay.value)
  const errSum = proveritSumma(pravAmount.value)
  if (errDen || errSum) {
    oshibkaPrav.value = errDen || errSum || ''
    return
  }
  const d = new Date()
  const denStr = String(pravDay.value).padStart(2, '0')
  const next = `${d.getFullYear()}-${String(d.getMonth() + 2).padStart(2, '0')}-${denStr}`
  await apiCreatePravilo({
    walletId: props.walletId,
    categoryId: pravKat.value,
    amount: pravAmount.value,
    dayOfMonth: pravDay.value,
    nextRunDate: next,
    active: true,
    comment: 'автоплатёж',
  })
  pravAmount.value = 0
  pravDay.value = 5
  emit('refresh')
}
</script>

<template>
  <!-- затемнение и выезд панели справа -->
  <div v-if="open" class="drawer-overlay" @click.self="emit('close')">
    <aside class="drawer">
      <header>
        <h2>Настройки</h2>
        <button type="button" class="x" @click="emit('close')">×</button>
      </header>

      <!-- приглашение участника -->
      <section>
        <h3>Пригласить по нику</h3>
        <input v-model="nickNew" placeholder="ник пользователя" />
        <button type="button" class="btn-primary" @click="priglasit">пригласить</button>
      </section>

      <!-- лимиты бюджета -->
      <section>
        <h3>Бюджет месяца</h3>
        <p class="muted">лимиты на текущий месяц · 0 — убрать ограничение</p>

        <label class="field-lbl">весь кошелёк</label>
        <input
          v-model.number="limitObshiy"
          type="number"
          min="0"
          :max="SUMMA_MAX"
          step="0.01"
          placeholder="лимит, ₽ (0 — без лимита)"
        />
        <button type="button" class="btn-primary" @click="sohranitObshiy">сохранить общий</button>
        <p v-if="obshiyBud && obshiyBud.limitAmount > 0" class="bud-hint">
          потрачено {{ obshiyBud.spent ?? 0 }} · осталось {{ obshiyBud.remaining ?? 0 }} ₽
        </p>

        <label class="field-lbl">лимит по категории</label>
        <select v-model.number="budKatId">
          <option :value="0">категория расхода</option>
          <option v-for="k in rashodKat" :key="k.id" :value="k.id">{{ k.name }}</option>
        </select>
        <input
          v-model.number="limitKat"
          type="number"
          min="0"
          :max="SUMMA_MAX"
          step="0.01"
          placeholder="лимит, ₽ (0 — убрать)"
        />
        <button type="button" class="btn-primary" @click="sohranitKatByudzhet">
          сохранить по категории
        </button>

        <ul v-if="katByudzhety.length" class="bud-list">
          <li v-for="b in katByudzhety" :key="b.id" class="bud-row">
            <div>
              <strong>{{ b.categoryName }}</strong>
              <span class="muted">
                {{ b.spent ?? 0 }} / {{ b.limitAmount }} ₽ · осталось {{ b.remaining ?? 0 }}
              </span>
            </div>
            <button type="button" class="btn-ghost-sm" @click="udalitKatByudzhet(b.id)">
              убрать
            </button>
          </li>
        </ul>

        <p v-if="oshibkaBud" class="err">{{ oshibkaBud }}</p>
      </section>

      <!-- видимость бюджета для участников -->
      <section>
        <h3>Кто видит бюджет</h3>
        <ul class="member-list">
          <li v-for="u in uchastniki" :key="u.id" class="member-row">
            <span class="member-nick">@{{ u.nick }}</span>
            <span v-if="u.memberRole === 'WALLET_OWNER'" class="muted">владелец</span>
            <label v-else class="budget-toggle">
              <input type="checkbox" :checked="u.canSeeBudget" @change="toggleBudget(u)" />
              <span>показывать бюджет</span>
            </label>
          </li>
        </ul>
      </section>

      <!-- добавление своей категории -->
      <section>
        <h3>Своя категория</h3>
        <select v-model="tipKat">
          <option value="EXPENSE">расход</option>
          <option value="INCOME">доход</option>
        </select>
        <input v-model="imyaKat" maxlength="40" placeholder="название" />
        <p class="lbl">иконка</p>
        <IconPicker v-model="iconKat" />
        <label class="color-lbl">цвет фона</label>
        <input v-model="colorKat" type="color" class="color-inp" />
        <button type="button" class="btn-primary" @click="addKat">добавить</button>
      </section>

      <!-- регулярные платежи раз в месяц -->
      <section>
        <h3>Автоплатёж</h3>
        <p class="muted">раз в месяц в выбранный день</p>
        <select v-model.number="pravKat">
          <option :value="0">категория</option>
          <option v-for="k in kategorii.filter((c) => c.tip === 'EXPENSE')" :key="k.id" :value="k.id">
            {{ k.name }}
          </option>
        </select>
        <input
          v-model.number="pravAmount"
          type="number"
          :min="0.01"
          :max="99999999"
          step="0.01"
          placeholder="сумма"
        />
        <input
          v-model.number="pravDay"
          type="number"
          :min="DEN_MIN"
          :max="DEN_MAX"
          placeholder="день месяца (1–31)"
          @blur="naDenBlur"
        />
        <p v-if="oshibkaPrav" class="err">{{ oshibkaPrav }}</p>
        <button type="button" class="btn-primary" @click="addPravilo">добавить</button>
        <ul>
          <li v-for="p in pravila" :key="p.id">{{ p.dayOfMonth }}-е — {{ p.amount }} ₽</li>
        </ul>
      </section>
    </aside>
  </div>
</template>

<style scoped>
.drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1100;
}
.drawer {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: min(400px, 100%);
  background: var(--color-card);
  color: var(--color-text);
  padding: 20px;
  overflow-y: auto;
  box-shadow: var(--shadow);
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.x {
  border: none;
  background: none;
  font-size: 28px;
  cursor: pointer;
  color: var(--color-base);
}
section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}
section h3 {
  margin: 0 0 12px;
  color: var(--color-base);
}
.muted {
  font-size: 0.85rem;
  color: var(--color-muted);
}
.lbl {
  font-size: 0.85rem;
  color: var(--color-muted);
  margin: 8px 0 0;
}
.member-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.member-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
  flex-wrap: wrap;
}
.member-nick {
  font-weight: 600;
  min-width: 80px;
}
.budget-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 0.9rem;
  cursor: pointer;
  white-space: nowrap;
}
.budget-toggle input {
  width: auto;
  margin: 0;
}
.color-inp {
  height: 44px;
  padding: 4px;
}
input,
select {
  margin-bottom: 8px;
}
.err {
  color: var(--color-danger);
  font-size: 0.85rem;
  margin: 4px 0;
}
.field-lbl {
  display: block;
  font-size: 0.85rem;
  color: var(--color-muted);
  margin: 12px 0 4px;
}
.bud-hint {
  font-size: 0.85rem;
  color: var(--color-muted);
  margin: 4px 0 0;
}
.bud-list {
  list-style: none;
  padding: 0;
  margin: 12px 0 0;
}
.bud-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
}
.bud-row strong {
  display: block;
}
.bud-row .muted {
  display: block;
  font-size: 0.8rem;
  margin-top: 2px;
}
.btn-ghost-sm {
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text);
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 0.8rem;
  cursor: pointer;
  flex-shrink: 0;
}
</style>
