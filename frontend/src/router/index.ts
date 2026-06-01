/**
 * маршруты Vue Router.
 *
 * /koshelki — главная: список кошельков
 * /koshelek/:id — один кошелёк (операции, бюджет, отчёт)
 * /login, /register — без авторизации
 *
 * meta.needsAuth — guard: без jwt_token редирект на login
 * если уже залогинен и открыли login/register — редирект на /koshelki
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/koshelki' },
    { path: '/login', name: 'login', component: () => import('@/pages/LoginPage.vue') },
    { path: '/register', name: 'register', component: () => import('@/pages/RegisterPage.vue') },
    {
      path: '/koshelki',
      name: 'koshelki',
      component: () => import('@/pages/KoshelkiPage.vue'),
      meta: { needsAuth: true },
    },
    {
      path: '/koshelek/:id',
      name: 'koshelek',
      component: () => import('@/pages/KoshelekPage.vue'),
      meta: { needsAuth: true },
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.meta.needsAuth && !auth.isLogin) {
    return '/login'
  }
  if ((to.path === '/login' || to.path === '/register') && auth.isLogin) {
    return '/koshelki'
  }
  // токен есть, но user ещё не подгружен — запрос GET /auth/me
  if (auth.isLogin && !auth.user) {
    try {
      await auth.loadMe()
    } catch {
      auth.logout()
      return '/login'
    }
  }
})

export default router
