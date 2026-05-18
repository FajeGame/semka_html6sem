<script setup lang="ts">
// модальное окно добавления операции (запасной вариант, основная форма на KoshelekPage)
import { computed, ref, watch } from 'vue'
import type { Kategoriya, TipOper, Uchastnik } from '@/types/models'
import { apiCreateOperaciya, apiSplit } from '@/api/transactionApi'

const props = defineProps<{
  open: boolean
  walletId: number
  tip: TipOper
  kategorii: Kategoriya[]
  uchastniki: Uchastnik[]
}>()

const emit = defineEmits<{ close: []; saved: [] }>()

const summa = ref(0)
const katId = ref(0)
const dataOper = ref(new Date().toISOString().slice(0, 10))
const komment = ref('')

const nuzhenSplit = ref(false)
const splitPorovnu = ref(true)
const vybrUchast = ref<number[]>([])
const doliVruch = ref<Record<number, number>>({})

const filtrKat = computed(() => props.kategorii.filter((k) => k.tip === props.tip))

watch(
  () => props.open,
  (v) => {
    const pk = filtrKat.value[0]
    if (v && pk) katId.value = pk.id
    vybrUchast.value = props.uchastniki.map((u) => u.userId)
  },
)

function zakryt() {
  emit('close')
}

async function sohranit() {
  if (!summa.value || !katId.value) return

  if (props.tip === 'EXPENSE' && nuzhenSplit.value && vybrUchast.value.length > 0) {
    let shares: { userId: number; shareAmount: number }[] = []
    if (splitPorovnu.value) {
      const chast = Math.floor((summa.value / vybrUchast.value.length) * 100) / 100
      shares = vybrUchast.value.map((userId) => ({ userId, shareAmount: chast }))
    } else {
      shares = vybrUchast.value.map((userId) => ({
        userId,
        shareAmount: doliVruch.value[userId] || 0,
      }))
    }
    await apiSplit({
      walletId: props.walletId,
      categoryId: katId.value,
      totalAmount: summa.value,
      transactionDate: dataOper.value,
      comment: komment.value || 'split',
      shares,
    })
  } else {
    await apiCreateOperaciya({
      walletId: props.walletId,
      categoryId: katId.value,
      type: props.tip,
      amount: summa.value,
      transactionDate: dataOper.value,
      comment: komment.value,
    })
  }
  summa.value = 0
  komment.value = ''
  nuzhenSplit.value = false
  emit('saved')
  emit('close')
}
</script>

<template>
  <!-- оверлей модального окна -->
  <div v-if="open" class="modal-overlay" @click.self="zakryt">
    <div class="modal-box">
      <h2>{{ tip === 'INCOME' ? 'Добавить доход' : 'Добавить расход' }}</h2>

      <label>сумма</label>
      <input v-model.number="summa" type="number" min="0.01" max="99999999" step="0.01" />

      <label>категория</label>
      <select v-model.number="katId">
        <option v-for="k in filtrKat" :key="k.id" :value="k.id">{{ k.name }}</option>
      </select>

      <label>дата</label>
      <input v-model="dataOper" type="date" min="2020-01-01" />

      <label>комментарий</label>
      <input v-model="komment" />

      <template v-if="tip === 'EXPENSE'">
        <label class="chk">
          <input v-model="nuzhenSplit" type="checkbox" />
          разделить с участниками
        </label>

        <template v-if="nuzhenSplit">
          <div class="split-btns">
            <button
              type="button"
              :class="{ active: splitPorovnu }"
              @click="splitPorovnu = true"
            >
              поровну
            </button>
            <button
              type="button"
              :class="{ active: !splitPorovnu }"
              @click="splitPorovnu = false"
            >
              вручную
            </button>
          </div>

          <p class="podskazka">кого включить</p>
          <label v-for="u in uchastniki" :key="u.userId" class="chk">
            <input v-model="vybrUchast" type="checkbox" :value="u.userId" />
            @{{ u.nick }}
          </label>

          <template v-if="!splitPorovnu">
            <div v-for="uid in vybrUchast" :key="uid" class="dolya-row">
              <span>@{{ uchastniki.find((x) => x.userId === uid)?.nick }}</span>
              <input v-model.number="doliVruch[uid]" type="number" placeholder="сумма" />
            </div>
          </template>
        </template>
      </template>

      <div class="modal-actions">
        <button type="button" class="btn-ghost" @click="zakryt">отмена</button>
        <button type="button" class="btn-primary" @click="sohranit">сохранить</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chk {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
}
.split-btns {
  display: flex;
  gap: 8px;
  margin: 12px 0;
}
.split-btns button {
  flex: 1;
  padding: 10px;
  border-radius: 12px;
  border: 2px solid var(--color-base);
  background: #fff;
  cursor: pointer;
  font-family: inherit;
}
.split-btns button.active {
  background: var(--color-mint);
}
.podskazka {
  font-size: 0.85rem;
  color: var(--color-muted);
}
.dolya-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}
.dolya-row input {
  flex: 1;
}
.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  justify-content: flex-end;
}
label {
  display: block;
  margin-top: 12px;
  font-size: 0.9rem;
  color: var(--color-muted);
}
</style>

