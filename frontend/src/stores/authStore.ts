/**
 * Pinia-store авторизации: токен и профиль текущего пользователя.
 * токен дублируется в localStorage (jwt_token), чтобы пережить F5.
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiLogin, apiMe, apiRegister } from '@/api/authApi'
import type { UserMe } from '@/types/models'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserMe | null>(null) // профиль после login или loadMe
  const token = ref<string | null>(localStorage.getItem('jwt_token')) // JWT или null

  const isLogin = computed(() => !!token.value) // есть ли токен

  // POST /auth/login → сохранить token и user
  async function login(email: string, password: string) {
    const otvet = await apiLogin(email, password)
    token.value = otvet.token
    user.value = otvet.user
    localStorage.setItem('jwt_token', otvet.token)
  }

  // POST /auth/register → сразу залогинен
  async function register(email: string, password: string, nick: string) {
    const otvet = await apiRegister({ email, password, nick })
    token.value = otvet.token
    user.value = otvet.user
    localStorage.setItem('jwt_token', otvet.token)
  }

  // GET /auth/me — подтянуть профиль, если есть token, но user пустой
  async function loadMe() {
    if (!token.value) return
    user.value = await apiMe()
  }

  // выход: очистить память и localStorage
  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('jwt_token')
  }

  return { user, token, isLogin, login, register, loadMe, logout }
})
