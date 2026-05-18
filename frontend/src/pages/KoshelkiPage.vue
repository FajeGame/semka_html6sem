<script setup lang="ts">
// список кошельков пользователя и создание нового
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiCreateKoshelek, apiListKoshelki } from '@/api/walletApi'
import { apiDeleteAccount } from '@/api/authApi'
import type { Koshelek } from '@/types/models'
import { useAuthStore } from '@/stores/authStore'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { soobshenieOshibki } from '@/utils/apiError'

const router = useRouter()
const auth = useAuthStore()
const spisok = ref<Koshelek[]>([])
const imyaNew = ref('')
const sozdaem = ref(false)
const parolUdalit = ref('')
const udalyaemAkkaunt = ref(false)

async function zagruzit() {
  spisok.value = await apiListKoshelki()
}

const oshibka = ref('')
const oshibkaAkkaunt = ref('')

async function sozdat() {
  const name = imyaNew.value.trim()
  if (!name) {
    oshibka.value = 'введите название'
    return
  }
  sozdaem.value = true
  oshibka.value = ''
  try {
    await apiCreateKoshelek(name)
    imyaNew.value = ''
    await zagruzit()
  } catch (e: unknown) {
    oshibka.value = soobshenieOshibki(e)
  } finally {
    sozdaem.value = false
  }
}

async function udalitAkkaunt() {
  oshibkaAkkaunt.value = ''
  if (!parolUdalit.value) {
    oshibkaAkkaunt.value = 'введите пароль для подтверждения'
    return
  }
  if (
    !confirm(
      'Удалить аккаунт безвозвратно? Ваши кошельки-владельца и операции будут удалены.',
    )
  ) {
    return
  }
  udalyaemAkkaunt.value = true
  try {
    await apiDeleteAccount(parolUdalit.value)
    auth.logout()
    await router.push('/login')
  } catch (e: unknown) {
    oshibkaAkkaunt.value = soobshenieOshibki(e)
  } finally {
    udalyaemAkkaunt.value = false
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
      <template v-if="k.canSeeBudget">
        <p v-if="k.budgetLimit != null" class="budget-line">
          бюджет месяца:
          <strong>{{ k.budgetRemaining ?? 0 }}</strong>
          / {{ k.budgetLimit }} ₽ осталось
        </p>
        <p v-else class="budget-line muted">бюджет не задан</p>
      </template>
    </router-link>

    <!-- форма нового кошелька (только владелец при создании) -->
    <section class="card new-wallet">
      <h3>Новый кошелёк</h3>
      <input v-model="imyaNew" maxlength="50" placeholder="название, напр. Семья" />
      <button type="button" class="btn-primary" :disabled="sozdaem" @click="sozdat">
        создать
      </button>
      <p v-if="oshibka" class="err">{{ oshibka }}</p>
    </section>

    <section class="card account-delete">
      <h3>Удалить аккаунт</h3>
      <p class="muted">
        Удалятся ваши кошельки-владельца и операции. Демо-аккаунты papa/mama удалить нельзя.
      </p>
      <input
        v-model="parolUdalit"
        type="password"
        placeholder="текущий пароль"
        autocomplete="current-password"
      />
      <button
        type="button"
        class="btn-danger-outline"
        :disabled="udalyaemAkkaunt"
        @click="udalitAkkaunt"
      >
        удалить аккаунт
      </button>
      <p v-if="oshibkaAkkaunt" class="err">{{ oshibkaAkkaunt }}</p>
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
.account-delete {
  margin-top: 20px;
  border-color: color-mix(in srgb, var(--color-danger) 35%, var(--color-border));
}
.account-delete h3 {
  margin: 0 0 8px;
  color: var(--color-danger);
  font-size: 1rem;
}
.account-delete input {
  margin-bottom: 10px;
}
.btn-danger-outline {
  border: 1px solid var(--color-danger);
  background: transparent;
  color: var(--color-danger);
  padding: 10px 16px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.95rem;
}
.btn-danger-outline:hover:not(:disabled) {
  background: color-mix(in srgb, var(--color-danger) 10%, transparent);
}
.btn-danger-outline:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.err {
  color: var(--color-danger);
  font-size: 0.85rem;
  margin-top: 8px;
}
</style>
