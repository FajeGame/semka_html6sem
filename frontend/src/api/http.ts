// общий HTTP-клиент: baseURL, JWT в заголовке, редирект при 401
import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('jwt_token')
  if (token) {
    cfg.headers.Authorization = `Bearer ${token}`
  }
  return cfg
})

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

// true — данные из mockDb, false — запросы на Spring backend
export const useMock = import.meta.env.VITE_USE_MOCK === 'true'
