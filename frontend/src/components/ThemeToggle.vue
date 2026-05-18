<script setup lang="ts">
// кнопка переключения светлой / тёмной темы
import { useThemeStore } from '@/stores/themeStore'

const theme = useThemeStore()
</script>

<template>
  <button type="button" class="theme-btn" title="тема" @click="theme.pereklyuchit">
    {{ theme.tema === 'light' ? '🌙' : '☀' }}
  </button>
</template>
