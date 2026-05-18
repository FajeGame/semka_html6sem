<script setup lang="ts">
// страница одного кошелька: баланс, операции, бюджет, отчёт
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { apiGetBalans, apiGetKoshelek, apiListUchastniki } from '@/api/walletApi'
import { apiListKategorii } from '@/api/categoryApi'
import { apiListOperacii, apiDeleteOperaciya, apiCreateOperaciya, apiSplit } from '@/api/transactionApi'
import { apiListByudzhet } from '@/api/budgetApi'
import { apiListPravila } from '@/api/recurringApi'
import { apiOtchetPeriod, apiRashodyPoLyudyam } from '@/api/reportApi'
import type {
  Balans,
  Byudzhet,
  Kategoriya,
  Koshelek,
  Operaciya,
  OtchetPeriod,
  PraviloMesyac,
  RashodPoUchastniku,
  TipOper,
  Uchastnik,
} from '@/types/models'
import { tekushiyPeriod } from '@/utils/monthPeriod'
import { useAuthStore } from '@/stores/authStore'
import OwnerPanel from '@/components/wallet/OwnerPanel.vue'
import CategorySvg from '@/components/icons/CategorySvg.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'
import {
  dataOperMax,
  dataOperMin,
  KOMMENT_MAX,
  ogranicitSumma,
  proveritData,
  proveritSumma,
  SUMMA_MAX,
  SUMMA_MIN,
} from '@/utils/inputLimits'

const route = useRoute()
const auth = useAuthStore()
const walletId = computed(() => Number(route.params.id))

// данные кошелька с сервера / мока
const koshelek = ref<Koshelek | null>(null)
const balans = ref<Balans | null>(null)
const kategorii = ref<Kategoriya[]>([])
const operacii = ref<Operaciya[]>([])
const uchastniki = ref<Uchastnik[]>([])
const byudzhety = ref<Byudzhet[]>([])
const pravila = ref<PraviloMesyac[]>([])

// UI-состояние страницы
const aktivTip = ref<TipOper>('EXPENSE')
const panelOpen = ref(false) // боковое меню настроек владельца
const lockMsg = ref('')
const formaOtkryta = ref(true)

// форма новой операции
const vybrKatId = ref(0)
const summa = ref<number | null>(null)
const dataOper = ref(new Date().toISOString().slice(0, 10))
const komment = ref('')
const nuzhenSplit = ref(false)
const splitPorovnu = ref(true)
const vybrUchast = ref<number[]>([])
const doliVruch = ref<Record<number, number>>({})
const oshibkaFormy = ref('')
const udalitZhdy = ref<number | null>(null)

// отчёты и расходы по участникам
const rashodyPoLyudyam = ref<RashodPoUchastniku[]>([])
const otchetOtkryt = ref(false)
const otchetFrom = ref(tekushiyPeriod().from)
const otchetTo = ref(tekushiyPeriod().to)
const otchet = ref<OtchetPeriod | null>(null)

const isOwner = computed(() => koshelek.value?.myRole === 'WALLET_OWNER')

// производные значения для шаблона
const raznica = computed(() => {
  if (!balans.value) return 0
  return balans.value.income - balans.value.expense
})

const canSeeBudget = computed(() => {
  if (isOwner.value) return true
  const me = uchastniki.value.find((u) => u.nick === auth.user?.nick)
  return me?.canSeeBudget === true
})

const byudzhetyPokaz = computed(() => byudzhety.value.filter((b) => b.limitAmount > 0))

const periodMesyac = computed(() => tekushiyPeriod())

const katFiltered = computed(() => kategorii.value.filter((k) => k.tip === aktivTip.value))

const previewSplit = computed(() => {
  if (!nuzhenSplit.value || !summa.value || vybrUchast.value.length === 0) return []
  return raschetDoley()
    .map((d) => ({
      nick: uchastniki.value.find((u) => u.userId === d.userId)?.nick || '?',
      shareAmount: d.shareAmount,
    }))
    .filter((d) => d.shareAmount > 0)
})

const summaDoleyPreview = computed(() =>
  previewSplit.value.reduce((s, d) => s + d.shareAmount, 0),
)

// расчёт долей при split (поровну или вручную)
function raschetDoley(): { userId: number; shareAmount: number }[] {
  const ids = vybrUchast.value
  if (!summa.value || !ids.length) return []
  if (splitPorovnu.value) {
    const n = ids.length
    const baza = Math.floor((summa.value / n) * 100) / 100
    let ostatok = Math.round((summa.value - baza * n) * 100) / 100
    return ids.map((userId, i) => ({
      userId,
      shareAmount: i === 0 ? Math.round((baza + ostatok) * 100) / 100 : baza,
    }))
  }
  return ids.map((userId) => ({
    userId,
    shareAmount: doliVruch.value[userId] || 0,
  }))
}

function toggleUchastnik(userId: number) {
  if (vybrUchast.value.includes(userId)) {
    vybrUchast.value = vybrUchast.value.filter((x) => x !== userId)
  } else {
    vybrUchast.value = [...vybrUchast.value, userId]
  }
}

watch(aktivTip, () => {
  const perv = katFiltered.value[0]
  vybrKatId.value = perv?.id ?? 0
})

// загрузка всех данных кошелька
async function zagruzit() {
  if (!auth.user) return
  const wid = walletId.value
  koshelek.value = await apiGetKoshelek(wid)
  balans.value = await apiGetBalans(wid)
  kategorii.value = await apiListKategorii(wid)
  operacii.value = await apiListOperacii(wid, aktivTip.value)
  uchastniki.value = await apiListUchastniki(wid)
  byudzhety.value = await apiListByudzhet(wid, auth.user.id)
  const { from, to } = periodMesyac.value
  rashodyPoLyudyam.value = await apiRashodyPoLyudyam(wid, from, to)
  if (isOwner.value) pravila.value = await apiListPravila(wid)
  vybrUchast.value = uchastniki.value.map((u) => u.userId)
  const pk = katFiltered.value[0]
  if (pk && !vybrKatId.value) vybrKatId.value = pk.id
}

async function smenitTip(t: TipOper) {
  aktivTip.value = t
  operacii.value = await apiListOperacii(walletId.value, t)
}

function vybratKat(id: number) {
  vybrKatId.value = id
}

// открытие бокового меню (только владелец)
function otkritNastroiki() {
  if (isOwner.value) panelOpen.value = true
  else lockMsg.value = 'Настройки только у владельца. Свяжитесь с ним.'
}

async function postroitOtchet() {
  otchet.value = await apiOtchetPeriod(walletId.value, otchetFrom.value, otchetTo.value)
  rashodyPoLyudyam.value = await apiRashodyPoLyudyam(
    walletId.value,
    otchetFrom.value,
    otchetTo.value,
  )
}

// сохранение дохода/расхода или split
async function sohranitOper() {
  oshibkaFormy.value = ''
  if (!vybrKatId.value) {
    oshibkaFormy.value = 'выберите категорию'
    return
  }
  if (summa.value != null) summa.value = ogranicitSumma(summa.value)
  const errS = summa.value != null ? proveritSumma(summa.value) : 'укажите сумму'
  const errD = proveritData(dataOper.value)
  if (errS || errD) {
    oshibkaFormy.value = errS || errD || ''
    return
  }
  const summaItog = summa.value as number
  const wid = walletId.value

  try {
    if (aktivTip.value === 'EXPENSE' && nuzhenSplit.value) {
      if (!vybrUchast.value.length) {
        oshibkaFormy.value = 'выберите хотя бы одного участника'
        return
      }
      const shares = raschetDoley()
      const sumD = shares.reduce((s, x) => s + x.shareAmount, 0)
      if (Math.abs(sumD - summaItog) > 0.02) {
        oshibkaFormy.value = `сумма долей (${sumD}) должна быть ${summaItog}`
        return
      }
      await apiSplit({
        walletId: wid,
        categoryId: vybrKatId.value,
        totalAmount: summaItog,
        transactionDate: dataOper.value,
        comment: komment.value || 'разделили чек',
        shares,
      })
    } else {
      await apiCreateOperaciya({
        walletId: wid,
        categoryId: vybrKatId.value,
        type: aktivTip.value,
        amount: summaItog,
        transactionDate: dataOper.value,
        comment: komment.value,
      })
    }
    summa.value = null
    komment.value = ''
    nuzhenSplit.value = false
    await zagruzit()
  } catch (e: unknown) {
    oshibkaFormy.value = e instanceof Error ? e.message : 'ошибка сохранения'
  }
}

// подтверждение удаления операции
function nazhatUdalit(id: number, e: Event) {
  e.stopPropagation()
  udalitZhdy.value = udalitZhdy.value === id ? null : id
}

function otmenitUdalit() {
  udalitZhdy.value = null
}

async function podtverditUdalit(id: number, authorNick: string, e: Event) {
  e.stopPropagation()
  if (!isOwner.value && authorNick !== auth.user?.nick) return
  await apiDeleteOperaciya(id)
  udalitZhdy.value = null
  await zagruzit()
}

onMounted(async () => {
  await auth.loadMe()
  await zagruzit()
  window.addEventListener('click', otmenitUdalit)
})

onUnmounted(() => {
  window.removeEventListener('click', otmenitUdalit)
})
</script>

<template>
  <div class="page koshelek">
    <!-- шапка: назад, название, тема, настройки -->
    <header class="top">
      <router-link to="/koshelki" class="back">←</router-link>
      <h1>{{ koshelek?.name || '...' }}</h1>
      <ThemeToggle />
      <button
        type="button"
        class="gear"
        :class="{ owner: isOwner, locked: !isOwner }"
        title="настройки"
        @click="otkritNastroiki"
      >
        ⚙
      </button>
    </header>
    <p v-if="lockMsg" class="lock-msg" @click="lockMsg = ''">{{ lockMsg }}</p>

    <!-- сводка: доход / расход / разница -->
    <section class="card summary">
      <div class="toggle-row">
        <button type="button" :class="{ on: aktivTip === 'INCOME' }" @click="smenitTip('INCOME')">
          <span class="lbl">доход</span>
          <strong>{{ balans?.income ?? 0 }} ₽</strong>
        </button>
        <button type="button" :class="{ on: aktivTip === 'EXPENSE' }" @click="smenitTip('EXPENSE')">
          <span class="lbl">расход</span>
          <strong>{{ balans?.expense ?? 0 }} ₽</strong>
        </button>
      </div>
      <p class="raznica">
        разница: <strong :class="raznica >= 0 ? 'plus' : 'minus'">{{ raznica }} ₽</strong>
      </p>
    </section>

    <!-- форма добавления операции -->
    <section class="card forma-blok">
      <button type="button" class="btn-big" @click="formaOtkryta = !formaOtkryta">
        {{ aktivTip === 'EXPENSE' ? 'добавить расход' : 'добавить доход' }}
      </button>

      <div v-show="formaOtkryta" class="forma-inner">
        <h2 class="form-title">выберите категорию</h2>
        <div class="kat-grid">
          <button
            v-for="k in katFiltered"
            :key="k.id"
            type="button"
            class="kat-btn"
            :class="{ active: vybrKatId === k.id }"
            :style="{ '--kat-bg': k.colorBg }"
            @click="vybratKat(k.id)"
          >
            <CategorySvg :icon-key="k.iconKey" />
            <span>{{ k.name }}</span>
          </button>
        </div>

        <div class="fields">
          <label>сумма</label>
          <input
            v-model.number="summa"
            type="number"
            :min="SUMMA_MIN"
            :max="SUMMA_MAX"
            step="0.01"
            placeholder="0"
          />

          <label>дата</label>
          <input v-model="dataOper" type="date" :min="dataOperMin()" :max="dataOperMax()" />

          <label>комментарий</label>
          <input v-model="komment" :maxlength="KOMMENT_MAX" placeholder="необязательно" />

          <!-- split: разделить чек между участниками -->
          <template v-if="aktivTip === 'EXPENSE'">
            <div class="chk-wrap">
              <label class="chk-row">
                <input v-model="nuzhenSplit" type="checkbox" class="chk-inp" />
                <span class="chk-txt">разделить с участниками</span>
              </label>
            </div>
            <template v-if="nuzhenSplit">
              <div class="split-btns">
                <button type="button" :class="{ on: splitPorovnu }" @click="splitPorovnu = true">
                  поровну
                </button>
                <button type="button" :class="{ on: !splitPorovnu }" @click="splitPorovnu = false">
                  вручную
                </button>
              </div>
              <p class="split-lbl">кто участвует</p>
              <div class="uchast-grid">
                <button
                  v-for="u in uchastniki"
                  :key="u.userId"
                  type="button"
                  class="uchast-chip"
                  :class="{ on: vybrUchast.includes(u.userId) }"
                  @click="toggleUchastnik(u.userId)"
                >
                  @{{ u.nick }}
                </button>
              </div>
              <template v-if="!splitPorovnu">
                <div v-for="uid in vybrUchast" :key="uid" class="dolya-row">
                  <span>@{{ uchastniki.find((x) => x.userId === uid)?.nick }}</span>
                  <input
                    v-model.number="doliVruch[uid]"
                    type="number"
                    :min="SUMMA_MIN"
                    :max="SUMMA_MAX"
                    step="0.01"
                    placeholder="сумма"
                  />
                </div>
              </template>
              <div v-if="previewSplit.length" class="split-preview">
                <p class="split-lbl">распределение:</p>
                <div v-for="d in previewSplit" :key="d.nick" class="preview-row">
                  <span>@{{ d.nick }}</span>
                  <strong>{{ d.shareAmount }} ₽</strong>
                </div>
                <p
                  class="preview-itog"
                  :class="{ warn: summa && Math.abs(summaDoleyPreview - summa) > 0.02 }"
                >
                  итого долей: {{ summaDoleyPreview }} ₽
                </p>
              </div>
            </template>
          </template>

          <p v-if="oshibkaFormy" class="err-form">{{ oshibkaFormy }}</p>

          <button
            type="button"
            class="btn-primary save-btn"
            :disabled="!vybrKatId || !summa"
            @click="sohranitOper"
          >
            сохранить
          </button>
        </div>
      </div>
    </section>

    <!-- блок бюджета (если есть лимиты и доступ) -->
    <section v-if="canSeeBudget && byudzhetyPokaz.length" class="card budget">
      <div class="budget-head">
        <h2>Бюджет</h2>
        <button v-if="isOwner" type="button" class="btn-link" @click="panelOpen = true">
          настроить
        </button>
      </div>
      <div
        v-for="b in byudzhetyPokaz"
        :key="b.id"
        class="budget-row"
        :class="{ overrun: (b.remaining ?? 0) < 0 }"
      >
        <span>{{ b.categoryName || 'весь кошелёк' }}</span>
        <span>
          <span v-if="(b.remaining ?? 0) < 0" class="bud-warn">перерасход!</span>
          осталось <strong>{{ b.remaining }}</strong> / {{ b.limitAmount }} ₽
          <span class="spent-hint">(потрачено {{ b.spent ?? 0 }})</span>
        </span>
      </div>
    </section>

    <!-- расходы по каждому участнику за месяц -->
    <section v-if="rashodyPoLyudyam.length" class="card people-exp">
      <h2>Расходы по людям</h2>
      <p class="muted period-hint">за {{ periodMesyac.from }} — {{ periodMesyac.to }}</p>
      <ul class="people-list">
        <li v-for="p in rashodyPoLyudyam" :key="p.userId" class="people-row">
          <span>@{{ p.nick }}</span>
          <strong>{{ p.expense }} ₽</strong>
        </li>
      </ul>
    </section>

    <!-- отчёт за выбранный период -->
    <section class="card otchet-blok">
      <button type="button" class="otchet-toggle" @click="otchetOtkryt = !otchetOtkryt">
        {{ otchetOtkryt ? '▼' : '▶' }} отчёт за период
      </button>
      <div v-show="otchetOtkryt" class="otchet-inner">
        <div class="otchet-dates">
          <label>с</label>
          <input v-model="otchetFrom" type="date" :min="dataOperMin()" :max="dataOperMax()" />
          <label>по</label>
          <input v-model="otchetTo" type="date" :min="dataOperMin()" :max="dataOperMax()" />
        </div>
        <button type="button" class="btn-primary" @click="postroitOtchet">построить</button>
        <template v-if="otchet">
          <p class="otchet-itog">
            доходы: <strong>{{ otchet.totalIncome }}</strong> ₽ · расходы:
            <strong>{{ otchet.totalExpense }}</strong> ₽
          </p>
          <ul v-if="otchet.byCategory.length" class="otchet-kat">
            <li v-for="c in otchet.byCategory" :key="c.categoryId">
              {{ c.categoryName }}: {{ c.expense }} ₽
            </li>
          </ul>
        </template>
      </div>
    </section>

    <!-- список операций -->
    <section class="card">
      <h2>Операции</h2>
      <ul class="oper-list">
        <li v-for="o in operacii" :key="o.id" class="oper-item">
          <div class="oper-main">
            <span class="sum" :class="o.type">{{ o.type === 'INCOME' ? '+' : '−' }}{{ o.amount }}</span>
            <div class="oper-info">
              <span>{{ o.categoryName }} · {{ o.authorNick }}</span>
              <div v-if="o.splitDoli?.length" class="split-tag">
                разделено:
                <span v-for="d in o.splitDoli" :key="d.nick" class="split-chip">
                  @{{ d.nick }} {{ d.shareAmount }}₽
                </span>
              </div>
            </div>
          </div>
          <div
            v-if="isOwner || o.authorNick === auth.user?.nick"
            class="del-wrap"
            @click.stop
          >
            <button type="button" class="del" @click="nazhatUdalit(o.id, $event)">×</button>
            <div v-if="udalitZhdy === o.id" class="confirm-del">
              <span>Вы точно хотите удалить?</span>
              <button type="button" class="btn-da" @click="podtverditUdalit(o.id, o.authorNick, $event)">
                да
              </button>
            </div>
          </div>
        </li>
      </ul>
      <p v-if="!operacii.length" class="empty">пока пусто</p>
    </section>

    <!-- боковое меню настроек владельца -->
    <OwnerPanel
      v-if="isOwner"
      :open="panelOpen"
      :wallet-id="walletId"
      :uchastniki="uchastniki"
      :kategorii="kategorii"
      :pravila="pravila"
      :byudzhety="byudzhety"
      @close="panelOpen = false"
      @refresh="zagruzit"
    />
  </div>
</template>

<style scoped>
.koshelek .top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.koshelek h1 {
  flex: 1;
  margin: 0;
  font-size: clamp(1.25rem, 4vw, 1.75rem);
  color: var(--color-base);
}
.back {
  font-size: 1.5rem;
  text-decoration: none;
  color: var(--color-base);
}
.gear {
  border: none;
  background: none;
  font-size: 1.65rem;
  line-height: 1;
  cursor: pointer;
  padding: 4px;
  color: var(--color-gear-locked);
}
.gear.owner {
  color: var(--color-gear);
}
.lock-msg {
  background: var(--color-mint);
  color: var(--color-base);
  padding: 10px 14px;
  border-radius: 10px;
  margin-bottom: 12px;
  cursor: pointer;
  font-size: 0.9rem;
}
.summary {
  text-align: center;
}
.toggle-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.toggle-row button {
  padding: 16px;
  border-radius: var(--radius-md);
  border: 2px solid var(--color-border);
  background: var(--color-bg);
  cursor: pointer;
  font-family: inherit;
  color: var(--color-text);
}
.toggle-row button.on {
  border-color: var(--color-base);
  background: var(--color-mint);
  color: var(--color-base);
}
.toggle-row .lbl {
  display: block;
  font-size: 0.85rem;
  color: var(--color-muted);
}
.raznica {
  margin-top: 16px;
}
.plus {
  color: var(--color-income);
}
.minus {
  color: var(--color-danger);
}
.forma-blok .btn-big {
  margin-top: 0;
}
.forma-inner {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}
.form-title {
  margin: 0 0 12px;
  font-size: 1rem;
  color: var(--color-muted);
}
.kat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(92px, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}
.kat-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 6px;
  border: 2px solid transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-family: inherit;
  font-size: 0.8rem;
  background: var(--kat-bg, var(--color-bg));
  color: var(--color-kat-icon);
  transition: filter 0.2s, border-color 0.2s, transform 0.15s;
}
.kat-btn {
  background: color-mix(in srgb, var(--kat-bg) 70%, var(--color-card));
}
.kat-btn.active {
  border-color: var(--color-kat-active);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-mint) 55%, transparent);
  transform: scale(1.04);
}
[data-theme='light'] .kat-btn.active {
  filter: none;
  color: var(--color-kat-icon);
}
[data-theme='dark'] .kat-btn.active {
  background: var(--color-mint);
  color: var(--color-base);
  filter: none;
}
.fields label:not(.chk-row) {
  display: block;
  margin-top: 10px;
  font-size: 0.85rem;
  color: var(--color-muted);
}
.chk-wrap {
  margin: 14px 0 8px;
}
.chk-row {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  cursor: pointer;
  user-select: none;
}
.chk-inp {
  width: 1.125rem;
  height: 1.125rem;
  min-width: 1.125rem;
  margin: 0;
  padding: 0;
  flex-shrink: 0;
  accent-color: var(--color-base);
  vertical-align: middle;
}
.chk-txt {
  line-height: 1.125rem;
  font-size: 0.95rem;
  color: var(--color-text);
}
.split-lbl {
  font-size: 0.85rem;
  color: var(--color-muted);
  margin: 10px 0 6px;
}
.uchast-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.uchast-chip {
  padding: 8px 14px;
  border-radius: 20px;
  border: 2px solid var(--color-border);
  background: var(--color-bg);
  color: var(--color-text);
  font-family: inherit;
  font-size: 0.9rem;
  cursor: pointer;
}
.uchast-chip.on {
  border-color: var(--color-base);
  background: var(--color-mint);
  color: var(--color-base);
  font-weight: 600;
}
.split-preview {
  background: var(--color-bg);
  border-radius: 10px;
  padding: 12px;
  margin-top: 10px;
  border: 1px solid var(--color-border);
}
.preview-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 0.9rem;
}
.preview-itog {
  margin: 8px 0 0;
  font-size: 0.85rem;
  color: var(--color-income);
}
.preview-itog.warn {
  color: var(--color-danger);
}
.err-form {
  color: var(--color-danger);
  font-size: 0.9rem;
  margin-top: 8px;
}
.split-btns {
  display: flex;
  gap: 8px;
  margin: 8px 0;
}
.split-btns button {
  flex: 1;
  padding: 8px;
  border-radius: 10px;
  border: 2px solid var(--color-border);
  background: var(--color-bg);
  color: var(--color-text);
  cursor: pointer;
  font-family: inherit;
}
.split-btns button.on {
  background: var(--color-mint);
  border-color: var(--color-base);
  color: var(--color-base);
}
.dolya-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}
.dolya-row input {
  flex: 1;
}
.save-btn {
  width: 100%;
  margin-top: 16px;
  padding: 14px;
}
.budget-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.budget-head h2 {
  margin: 0;
}
.btn-link {
  border: none;
  background: none;
  color: var(--color-base);
  font-size: 0.9rem;
  cursor: pointer;
  text-decoration: underline;
}
.budget-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border);
  gap: 12px;
  flex-wrap: wrap;
}
.spent-hint {
  display: block;
  font-size: 0.8rem;
  color: var(--color-muted);
  font-weight: normal;
}
.budget-row.overrun {
  color: var(--color-danger);
}
.bud-warn {
  display: block;
  font-weight: 700;
  font-size: 0.85rem;
}
.people-exp h2 {
  margin: 0 0 4px;
}
.period-hint {
  font-size: 0.85rem;
  margin: 0 0 12px;
}
.people-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.people-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
}
.otchet-toggle {
  width: 100%;
  text-align: left;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--color-base);
  cursor: pointer;
  padding: 0;
}
.otchet-inner {
  margin-top: 14px;
}
.otchet-dates {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 8px 10px;
  align-items: center;
  margin-bottom: 10px;
}
.otchet-dates label {
  font-size: 0.85rem;
  color: var(--color-muted);
}
.otchet-itog {
  margin: 12px 0 8px;
}
.otchet-kat {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 0.9rem;
}
.otchet-kat li {
  padding: 6px 0;
  border-bottom: 1px solid var(--color-border);
}
.oper-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.oper-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border);
}
.oper-main {
  display: flex;
  gap: 10px;
  flex: 1;
  min-width: 0;
}
.oper-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.split-tag {
  font-size: 0.78rem;
  color: var(--color-muted);
}
.split-chip {
  display: inline-block;
  margin-right: 6px;
  padding: 2px 6px;
  background: var(--color-mint);
  color: var(--color-base);
  border-radius: 6px;
  font-size: 0.75rem;
}
.sum {
  font-weight: 700;
  min-width: 72px;
  flex-shrink: 0;
}
.sum.INCOME {
  color: var(--color-income);
}
.sum.EXPENSE {
  color: var(--color-danger);
}
.del-wrap {
  position: relative;
  flex-shrink: 0;
}
.del {
  border: none;
  background: none;
  font-size: 1.35rem;
  line-height: 1;
  cursor: pointer;
  color: var(--color-muted);
  padding: 4px 8px;
}
.confirm-del {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 4px;
  z-index: 5;
  background: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 10px 12px;
  box-shadow: var(--shadow);
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.85rem;
}
.btn-da {
  border: none;
  background: var(--color-danger);
  color: #fff;
  padding: 4px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-family: inherit;
}
.empty {
  text-align: center;
  color: var(--color-muted);
}
</style>


