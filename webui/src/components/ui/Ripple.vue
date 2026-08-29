<template>
  <div class="relative overflow-hidden" @pointerdown="createRipple">
    <slot></slot>
    <span
      v-for="r in ripples"
      :key="r.id"
      class="pointer-events-none absolute rounded-full bg-white/15 animate-ripple"
      :style="{
        top: `${r.y}px`,
        left: `${r.x}px`,
        width: `${r.size}px`,
        height: `${r.size}px`,
      }"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'

const ripples = ref([])
let count = 0

function createRipple(e) {
  const rect = e.currentTarget.getBoundingClientRect()
  const size = Math.max(rect.width, rect.height) * 2
  const x = e.clientX - rect.left - size / 2
  const y = e.clientY - rect.top - size / 2

  const id = ++count
  ripples.value.push({ id, x, y, size })

  setTimeout(() => {
    ripples.value = ripples.value.filter(r => r.id !== id)
  }, 400)
}
</script>

<style scoped>
@keyframes ripple {
  from {
    transform: scale(0);
    opacity: 1;
  }
  to {
    transform: scale(1);
    opacity: 0;
  }
}
.animate-ripple {
  animation: ripple 0.4s ease-out forwards;
}
</style>
