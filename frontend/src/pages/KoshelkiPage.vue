<script setup lang="ts">
// список кошельков пользователя и создание нового
import { onMounted, ref } from 'vue'
import { apiCreateKoshelek, apiListKoshelki } from '@/api/walletApi'
import type { Koshelek } from '@/types/models'
import { useAuthStore } from '@/stores/authStore'
import ThemeToggle from '@/components/ThemeToggle.vue'

const auth = useAuthStore()
const spisok = ref<Koshelek[]>([])
const imyaNew = ref('')
const sozdaem = ref(false)

async function zagruzit() {
  spisok.value = await apiListKoshelki()
}

async function sozdat() {
  if (!imyaNew.value.trim()) return
  sozdaem.value = true
  try {
    await apiCreateKoshelek(imyaNew.value.trim())
    imyaNew.value = ''
    await zagruzit()
  } finally {
    sozdaem.value = false
  }
}

onMounted(zagruzit)
</script>

<template>
  <div class="page">
    <!-- шапка: ник, тема, выход -->
    <header class="head">
      <div>
        <h1>Кошельки</h1>
        <p class="sub">@{{ auth.user?.nick }}</p>
      </div>
      <ThemeToggle />
      <button type="button" class="btn-ghost" @click="auth.logout(); $router.push('/login')">
        выйти
      </button>
    </header>

    <p v-if="!spisok.length" class="empty card">
      пока нет кошельков — создайте первый ниже
    </p>

    <!-- карточки кошельков -->
    <router-link
      v-for="k in spisok"
      :key="k.id"
      :to="`/koshelek/${k.id}`"
      class="wallet-card card"
    >
      <div class="wc-top">
        <h2>{{ k.name }}</h2>
        <span class="badge">{{ k.myRole === 'WALLET_OWNER' ? 'владелец' : 'участник' }}</span>
      </div>
      <p v-if="k.budgetLimit != null" class="budget-line">
        бюджет месяца:
        <strong>{{ k.budgetRemaining ?? 0 }}</strong>
        / {{ k.budgetLimit }} ₽ осталось
      </p>
      <p v-else class="budget-line muted">бюджет не задан</p>
    </router-link>

    <!-- форма нового кошелька (только владелец при создании) -->
    <section class="card new-wallet">
      <h3>Новый кошелёк</h3>
      <input v-model="imyaNew" maxlength="50" placeholder="название, напр. Семья" />
      <button type="button" class="btn-primary" :disabled="sozdaem" @click="sozdat">
        создать
      </button>
    </section>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}
.head h1 {
  margin: 0;
  color: var(--color-base);
  font-size: clamp(1.5rem, 4vw, 2rem);
}
.sub {
  margin: 4px 0 0;
  color: var(--color-muted);
}
.wallet-card {
  display: block;
  margin-bottom: 14px;
  text-decoration: none;
  color: inherit;
  transition: transform 0.15s;
}
.wallet-card:hover {
  transform: translateY(-2px);
}
.wc-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.wc-top h2 {
  margin: 0;
  font-size: 1.2rem;
}
.badge {
  font-size: 0.75rem;
  background: var(--color-mint);
  color: var(--color-base);
  padding: 4px 10px;
  border-radius: 20px;
}
.budget-line {
  margin: 12px 0 0;
  font-size: 0.95rem;
}
.muted {
  color: var(--color-muted);
}
.new-wallet h3 {
  margin-top: 0;
}
.new-wallet input {
  margin-bottom: 10px;
}
.empty {
  text-align: center;
  color: var(--color-muted);
}
</style>
