<script setup lang="ts">
// выбор иконки для новой категории
import { spisokIkonok } from '@/utils/categoryPresets'
import CategorySvg from '@/components/icons/CategorySvg.vue'

const model = defineModel<string>({ required: true })
</script>

<template>
  <div class="icon-picker">
    <button
      v-for="key in spisokIkonok"
      :key="key"
      type="button"
      class="icon-opt"
      :class="{ on: model === key }"
      @click="model = key"
    >
      <CategorySvg :icon-key="key" />
    </button>
  </div>
</template>

<style scoped>
.icon-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 8px 0 12px;
}
.icon-opt {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  border: 2px solid var(--color-border);
  background: var(--color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--color-kat-icon);
}
.icon-opt.on {
  border-color: var(--color-base);
  background: var(--color-mint);
}
</style>
