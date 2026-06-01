/**
 * общий HTTP-клиент axios для всего фронта.
 *
 * baseURL — из .env (VITE_API_URL), по умолчанию http://localhost:8080/api
 * на каждый запрос подставляется JWT из localStorage (ключ jwt_token)
 * при ответе 401 токен сбрасывается и браузер уходит на /login
 *
 * useMock — если VITE_USE_MOCK=true, api/* ходят в mockDb вместо backend
 */
import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
})

// перед отправкой: добавить Authorization: Bearer …
http.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('jwt_token')
  if (token) {
    cfg.headers.Authorization = `Bearer ${token}`
  }
  return cfg
})

// при 401 — разлогин и редирект (кроме страницы login)
http.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('jwt_token')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  },
)

export default http

export const useMock = import.meta.env.VITE_USE_MOCK === 'true'

if (useMock && import.meta.env.DEV) {
  console.warn(
    '[semka] VITE_USE_MOCK=true — кошельки только в памяти вкладки. Для двух браузеров и PostgreSQL: VITE_USE_MOCK=false и запущенный backend.',
  )
}
