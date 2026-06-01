<script setup lang="ts">
/**
 * корневой компонент приложения: только RouterView (страницы подключаются в router).
 * :key на fullPath — пересоздать страницу при смене кошелька в URL.
 */
import { RouterView, useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <RouterView :key="route.fullPath" />
</template>
