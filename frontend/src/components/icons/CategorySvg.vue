<script setup lang="ts">
// SVG-иконки категорий по ключу (cart, car, home, …)
defineProps<{ iconKey: string }>()
</script>

<template>
  <svg v-if="iconKey === 'cart'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <circle cx="9" cy="20" r="1" /><circle cx="17" cy="20" r="1" />
    <path d="M1 1h4l2.6 13h9.8l2-8H6" />
  </svg>
  <svg v-else-if="iconKey === 'car'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <path d="M5 17h14v-5l-2-5H7l-2 5v5zM7 17a2 2 0 104 0M17 17a2 2 0 104 0" />
  </svg>
  <svg v-else-if="iconKey === 'home'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <path d="M3 11l9-8 9 8v10H3z" />
  </svg>
  <svg v-else-if="iconKey === 'game'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <rect x="2" y="6" width="20" height="12" rx="4" />
    <path d="M8 12h4M10 10v4M16 11h.01M18 13h.01" />
  </svg>
  <svg v-else-if="iconKey === 'health'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <path d="M12 21s-7-4.5-7-10a4 4 0 017-2 4 4 0 017 2c0 5.5-7 10-7 10z" />
  </svg>
  <svg v-else-if="iconKey === 'wallet'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <rect x="2" y="6" width="20" height="14" rx="2" /><path d="M16 12h2" />
  </svg>
  <svg v-else-if="iconKey === 'gift'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <rect x="3" y="8" width="18" height="12" /><path d="M12 8v12M3 12h18M12 8c-2-2-4-2-4 0s2 2 4 0 4-2 4 0-2-2-4 0" />
  </svg>
  <svg v-else-if="iconKey === 'food'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <path d="M18 8h1a4 4 0 010 8h-1M2 8h16v9a4 4 0 01-4 4H6a4 4 0 01-4-4V8z" />
    <path d="M6 1v3M10 1v3M14 1v3" />
  </svg>
  <svg v-else-if="iconKey === 'coffee'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <path d="M18 8h1a4 4 0 010 8h-1M2 8h16v4a4 4 0 01-4 4H6a4 4 0 01-4-4V8z" />
  </svg>
  <svg v-else-if="iconKey === 'bus'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <rect x="4" y="3" width="16" height="16" rx="2" /><path d="M4 11h16M8 15h.01M16 15h.01M6 19l-2 2M18 19l2 2" />
  </svg>
  <svg v-else-if="iconKey === 'plane'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <path d="M2 12h20M13 12l5-8-3 8 3 8-5-8H2" />
  </svg>
  <svg v-else-if="iconKey === 'edu'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <path d="M22 10L12 5 2 10l10 5 10-5zM6 12v5c0 2 4 3 6 3s6-1 6-3v-5" />
  </svg>
  <svg v-else-if="iconKey === 'pet'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <circle cx="11" cy="4" r="2" /><circle cx="18" cy="6" r="2" /><circle cx="4" cy="6" r="2" />
    <path d="M8 12c-2 2-3 5-3 8h14c0-3-1-6-3-8-2 2-6 2-8 0z" />
  </svg>
  <svg v-else-if="iconKey === 'clothes'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <path d="M16 3l4 4-4 2v12H8V9L4 7l4-4 4 3h4z" />
  </svg>
  <svg v-else-if="iconKey === 'phone'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <rect x="6" y="2" width="12" height="20" rx="2" /><path d="M12 18h.01" />
  </svg>
  <svg v-else-if="iconKey === 'book'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <path d="M4 19.5A2.5 2.5 0 016.5 17H20M4 4.5A2.5 2.5 0 016.5 7H20v13H6.5A2.5 2.5 0 004 17.5v-13z" />
  </svg>
  <svg v-else-if="iconKey === 'star'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <path d="M12 2l3 7h7l-5.5 4 2 7L12 17l-6.5 3 2-7L2 9h7l3-7z" />
  </svg>
  <svg v-else-if="iconKey === 'work'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <rect x="2" y="7" width="20" height="14" rx="2" /><path d="M16 7V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v2" />
  </svg>
  <svg v-else-if="iconKey === 'money'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <rect x="2" y="6" width="20" height="12" rx="2" /><circle cx="12" cy="12" r="2" />
  </svg>
  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <path d="M12 5v14M5 12h14" />
  </svg>
</template>

<style scoped>
svg {
  width: 28px;
  height: 28px;
}
</style>
