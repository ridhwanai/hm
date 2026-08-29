<template>
  <nav
    class="fixed bottom-0 left-0 right-0 w-full bg-[#1e1f25] border-t border-white/5 z-50 flex items-center justify-around h-18 px-2"
    :style="{ paddingBottom: 'var(--window-inset-bottom, 0px)' }"
  >
    <button
      v-for="item in navItems"
      :key="item.id"
      @click="$emit('update:activeTab', item.id)"
      class="flex-1 flex flex-col items-center justify-center py-1 gap-1 focus:outline-none transition-all"
    >
      <!-- Pill indicator -->
      <div
        class="h-8 flex items-center justify-center rounded-full transition-all duration-200"
        :class="activeTab === item.id 
          ? 'bg-[#333a48] text-[#d8e2ff] px-5' 
          : 'text-[#c4c6d0] px-0'"
      >
        <component :is="item.icon" class="w-5 h-5" />
      </div>
      
      <span
        class="text-[11px] font-medium transition-colors"
        :class="activeTab === item.id ? 'text-[#e2e2e9] font-semibold' : 'text-[#c4c6d0]'"
      >
        {{ item.label }}
      </span>
    </button>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { Home, Gamepad2, Settings } from 'lucide-vue-next'
import { useLocales } from '@/helpers/Locales'

defineProps({
  activeTab: {
    type: String,
    required: true,
  },
})

defineEmits(['update:activeTab'])

const { t } = useLocales()

const navItems = computed(() => [
  { id: 'home', label: t('nav.dashboard'), icon: Home },
  { id: 'games', label: t('nav.games'), icon: Gamepad2 },
  { id: 'settings', label: t('nav.tweaks'), icon: Settings },
])
</script>
