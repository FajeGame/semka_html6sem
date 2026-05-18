// Pinia: JWT-токен и данные текущего пользователя
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiLogin, apiMe, apiRegister } from '@/api/authApi'
import type { UserMe } from '@/types/models'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserMe | null>(null)
  const token = ref<string | null>(localStorage.getItem('jwt_token'))

  const isLogin = computed(() => !!token.value)

  async function login(email: string, password: string) {
    const otvet = await apiLogin(email, password)
    token.value = otvet.token
    user.value = otvet.user
    localStorage.setItem('jwt_token', otvet.token)
  }

  async function register(email: string, password: string, nick: string) {
    const otvet = await apiRegister({ email, password, nick })
    token.value = otvet.token
    user.value = otvet.user
    localStorage.setItem('jwt_token', otvet.token)
  }

  async function loadMe() {
    if (!token.value) return
    user.value = await apiMe()
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('jwt_token')
  }

  return { user, token, isLogin, login, register, loadMe, logout }
})
