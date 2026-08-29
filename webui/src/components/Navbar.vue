<template>
  <nav class="fixed bottom-0 left-0 right-0 z-40 bg-[#0c121e]/90 backdrop-blur-xl border-t border-white/10 px-3 py-2">
    <div class="max-w-md mx-auto flex items-center justify-around">
      <button
        v-for="item in navItems"
        :key="item.id"
        @click="$emit('update:activeTab', item.id)"
        class="relative flex flex-col items-center justify-center py-1 px-3 rounded-xl transition-all duration-200"
        :class="activeTab === item.id ? 'text-indigo-400 font-semibold' : 'text-slate-400 hover:text-slate-200'"
      >
        <!-- Active Background Pill -->
        <div
          v-if="activeTab === item.id"
          class="absolute inset-0 bg-indigo-500/15 rounded-xl border border-indigo-500/30 transition-all duration-300 -z-10"
        ></div>

        <!-- Icon -->
        <component :is="item.icon" class="w-5 h-5 mb-0.5 transition-transform duration-200" :class="{ 'scale-110': activeTab === item.id }" />
        
        <!-- Label -->
        <span class="text-[11px] tracking-tight">{{ item.label }}</span>
      </button>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { LayoutDashboard, Gamepad2, SlidersHorizontal, TerminalSquare } from 'lucide-vue-next'
import { useLocales } from '@/helpers/Locales'

const props = defineProps({
  activeTab: {
    type: String,
    required: true,
  },
})

defineEmits(['update:activeTab'])

const { t } = useLocales()

const navItems = computed(() => [
  { id: 'dashboard', label: t('nav.dashboard'), icon: LayoutDashboard },
  { id: 'games', label: t('nav.games'), icon: Gamepad2 },
  { id: 'tweaks', label: t('nav.tweaks'), icon: SlidersHorizontal },
  { id: 'logs', label: t('nav.logs'), icon: TerminalSquare },
])
</script>
