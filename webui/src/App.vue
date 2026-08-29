<template>
  <div id="app" class="min-h-screen flex flex-col bg-[#111318] text-[#e2e2e9] overflow-hidden">
    <main class="main-content flex-1 md:ml-20 overflow-hidden relative">
      <router-view v-slot="{ Component, route }">
        <transition :name="transitionName">
          <keep-alive>
            <component :is="Component" :key="route.path" />
          </keep-alive>
        </transition>
      </router-view>
    </main>

    <Navigation />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import Navigation from '@/components/ui/Navigation.vue'

const route = useRoute()
const transitionName = ref('')

const topLevelRoutes = ['/', '/games', '/settings']

watch(
  () => route.path,
  (to, from) => {
    if (topLevelRoutes.includes(to) && topLevelRoutes.includes(from)) {
      transitionName.value = ''
      return
    }

    const isOpeningChild = to.startsWith(from === '/' ? '' : from) && to.length > from.length
    const isClosingChild = from.startsWith(to === '/' ? '' : to) && from.length > to.length

    if (isOpeningChild) {
      transitionName.value = 'page-open'
    } else if (isClosingChild) {
      transitionName.value = 'page-close'
    } else {
      transitionName.value = ''
    }
  },
)
</script>
