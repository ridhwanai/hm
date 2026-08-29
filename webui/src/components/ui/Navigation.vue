<template>
  <nav
    ref="navEl"
    class="fixed bottom-0 left-0 right-0 w-full flex items-end bg-[#1e1f25] shadow-lg z-50 md:left-0 md:top-0 md:bottom-0 md:w-20 md:h-full md:flex-col backdrop-blur-md border-t border-white/5"
    :style="{
      paddingBottom: 'var(--window-inset-bottom, 0px)',
      paddingRight: 'var(--window-inset-right, 0px)',
      paddingLeft: 'var(--window-inset-left, 0px)'
    }"
  >
    <div class="w-full h-18 flex items-center justify-around md:h-full md:flex-col md:justify-center">
      <router-link
        v-for="item in navItems"
        :key="item.name"
        :to="item.path"
        class="w-full flex justify-center items-center flex-col py-1 gap-1 text-xs no-underline transition-all duration-200"
        :class="{
          'text-[#e2e2e9]': isActive(item),
          'text-[#c4c6d0]': !isActive(item),
        }"
      >
        <div
          class="h-8 flex justify-center items-center rounded-full transition-all duration-200 ease-in-out"
          :class="{
            'bg-[#333a48] text-[#d8e2ff] px-5': isActive(item),
            'px-0 text-[#c4c6d0]': !isActive(item),
          }"
        >
          <component :is="item.icon" class="w-5 h-5" />
        </div>
        <span class="font-medium text-[11px]">{{ item.label }}</span>
      </router-link>
    </div>
  </nav>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { Home, Gamepad2, Settings } from 'lucide-vue-next'
import { useLocales } from '@/helpers/Locales'

const { t } = useLocales()
const route = useRoute()

const navItems = computed(() => [
  {
    name: 'Home',
    path: '/',
    label: t('nav.dashboard'),
    icon: Home,
  },
  {
    name: 'Games',
    path: '/games',
    label: t('nav.games'),
    icon: Gamepad2,
  },
  {
    name: 'Settings',
    path: '/settings',
    label: t('nav.tweaks'),
    icon: Settings,
  },
])

const isActive = (item) => {
  const currentPath = route.path
  if (item.path === '/') return currentPath === '/'
  return currentPath.startsWith(item.path)
}

const navEl = ref(null)
let ro = null

onMounted(() => {
  ro = new ResizeObserver(([entry]) => {
    const h = entry.borderBoxSize?.[0]?.blockSize ?? entry.target.offsetHeight
    document.documentElement.style.setProperty('--nav-height', `${h}px`)
  })
  if (navEl.value) ro.observe(navEl.value)
})

onBeforeUnmount(() => ro?.disconnect())
</script>
