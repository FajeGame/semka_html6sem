<script setup lang="ts">
// регистрация: ник + пароль по правилам
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const auth = useAuthStore()

onMounted(() => {
  document.documentElement.setAttribute('data-theme', 'light')
})
onUnmounted(() => {
  const saved = localStorage.getItem('tema') || 'light'
  document.documentElement.setAttribute('data-theme', saved)
})

const email = ref('')
const parol = ref('')
const parol2 = ref('')
const nick = ref('')
const oshibka = ref('')

const pravila = computed(() => [
  { text: 'только латиница (a-z, A-Z)', ok: parol.value.length === 0 || /^[\x21-\x7E]+$/.test(parol.value) },
  { text: 'минимум 8 символов', ok: parol.value.length >= 8 },
  { text: 'заглавная A-Z', ok: /[A-Z]/.test(parol.value) },
  { text: 'строчная a-z', ok: /[a-z]/.test(parol.value) },
  { text: 'цифра', ok: /[0-9]/.test(parol.value) },
  { text: 'спецсимвол', ok: /[^A-Za-z0-9]/.test(parol.value) },
])

const vseOk = computed(() => pravila.value.every((p) => p.ok))

async function reg() {
  oshibka.value = ''
  if (!nick.value.trim()) {
    oshibka.value = 'придумайте ник'
    return
  }
  if (!vseOk.value) {
    oshibka.value = 'пароль не подходит по правилам'
    return
  }
  if (parol.value !== parol2.value) {
    oshibka.value = 'пароли не совпали'
    return
  }
  try {
    await auth.register(email.value, parol.value, nick.value.trim())
    router.push('/koshelki')
  } catch {
    oshibka.value = 'email или ник уже заняты'
  }
}
</script>

<template>
  <div class="page auth-page">
    <div class="card auth-card">
      <h1>Регистрация</h1>
      <p class="hint">аккаунт пустой — кошельки создаёте сами. Тест: papa / mama</p>

      <label>email</label>
      <input v-model="email" type="email" />

      <label>ник (для приглашений)</label>
      <input v-model="nick" maxlength="32" placeholder="masha_ivanova" />

      <label>пароль (латиница)</label>
      <input v-model="parol" type="password" autocomplete="new-password" />
      <ul class="rules">
        <li v-for="p in pravila" :key="p.text" :class="{ ok: p.ok }">{{ p.text }}</li>
      </ul>

      <label>повтор пароля</label>
      <input v-model="parol2" type="password" />

      <button type="button" class="btn-primary full" :disabled="!vseOk" @click="reg">
        создать аккаунт
      </button>
      <p v-if="oshibka" class="err">{{ oshibka }}</p>
      <router-link to="/login" class="link">уже есть аккаунт</router-link>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}
.auth-card {
  width: 100%;
  max-width: 420px;
}
.auth-card h1 {
  margin-top: 0;
  color: var(--color-base);
}
.hint {
  font-size: 0.85rem;
  color: var(--color-muted);
}
label {
  display: block;
  margin-top: 12px;
  font-size: 0.9rem;
  color: var(--color-muted);
}
.rules {
  font-size: 0.85rem;
  margin: 8px 0;
  padding-left: 20px;
  color: var(--color-danger);
}
.rules .ok {
  color: var(--color-income);
}
.full {
  width: 100%;
  margin-top: 16px;
  padding: 12px;
}
.err {
  color: var(--color-danger);
}
.link {
  display: block;
  margin-top: 16px;
  text-align: center;
}
</style>

