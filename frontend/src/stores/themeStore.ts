// Pinia: светлая / тёмная тема (localStorage)
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const tema = ref<'light' | 'dark'>(
    (localStorage.getItem('tema') as 'light' | 'dark') || 'light',
  )

  function primenit() {
    document.documentElement.setAttribute('data-theme', tema.value)
  }

  function pereklyuchit() {
    tema.value = tema.value === 'light' ? 'dark' : 'light'
  }

  watch(tema, (v) => {
    localStorage.setItem('tema', v)
    primenit()
  })

  primenit()

  return { tema, pereklyuchit, primenit }
})
