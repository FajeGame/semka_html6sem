<script setup lang="ts">
// страница входа
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const auth = useAuthStore()

// на входе всегда светлая тема, без переключателя
onMounted(() => {
  document.documentElement.setAttribute('data-theme', 'light')
})
onUnmounted(() => {
  const saved = localStorage.getItem('tema') || 'light'
  document.documentElement.setAttribute('data-theme', saved)
})

const email = ref('')
const parol = ref('')
const oshibka = ref('')

// отправка логина в authStore
async function voiti() {
  oshibka.value = ''
  try {
    await auth.login(email.value, parol.value)
    router.push('/koshelki')
  } catch {
    oshibka.value = 'не получилось войти'
  }
}
</script>

<template>
  <div class="page auth-page">
    <div class="card auth-card">
      <h1>Семейный кошелёк</h1>
      <p class="hint">тест: papa@test.ru / mama@test.ru — любой пароль</p>
      <label>email</label>
      <input v-model="email" type="email" autocomplete="email" />
      <label>пароль</label>
      <input v-model="parol" type="password" autocomplete="current-password" />
      <button type="button" class="btn-primary full" @click="voiti">войти</button>
      <p v-if="oshibka" class="err">{{ oshibka }}</p>
      <router-link to="/register" class="link">регистрация</router-link>
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
  max-width: 400px;
}
.auth-card h1 {
  margin-top: 0;
  color: var(--color-base);
}
.hint {
  font-size: 0.85rem;
  color: var(--color-muted);
  margin-bottom: 20px;
}
label {
  display: block;
  margin-top: 12px;
  font-size: 0.9rem;
  color: var(--color-muted);
}
.full {
  width: 100%;
  margin-top: 20px;
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


