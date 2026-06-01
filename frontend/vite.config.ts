/**
 * конфигурация Vite: Vue 3, алиас @ → src/.
 * dev-server по умолчанию :5173, прокси на backend не настроен — API через VITE_API_URL.
 */
import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
})
