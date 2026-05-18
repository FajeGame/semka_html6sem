<script setup lang="ts">
// корневой компонент — только outlet для страниц
import { RouterView, useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <RouterView :key="route.fullPath" />
</template>
