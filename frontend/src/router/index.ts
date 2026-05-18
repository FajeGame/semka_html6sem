// маршруты и защита страниц (needsAuth)
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

// редирект на login, если нет токена
router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.meta.needsAuth && !auth.isLogin) {
    return '/login'
  }
  if ((to.path === '/login' || to.path === '/register') && auth.isLogin) {
    return '/koshelki'
  }
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
