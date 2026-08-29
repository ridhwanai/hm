<template>
  <div class="page game-settings-page h-full flex flex-col overflow-hidden bg-surface">
    <div class="max-w-3xl mx-auto h-full flex flex-col w-full">
      <!-- Top Header -->
      <div class="flex-none p-5 pb-3">
        <div class="flex items-center gap-4 mb-2">
          <button @click="router.back()" class="text-on-surface transition-colors cursor-pointer">
            <ArrowLeftIcon class="w-6 h-6 rtl:rotate-180" />
          </button>
        </div>
      </div>

      <!-- Settings Content -->
      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5 space-y-4">
        <!-- App Info Card -->
        <div class="bg-surface-container p-5 rounded-3xl flex items-center gap-4 text-on-surface">
          <img
            v-if="currentGame?.icon"
            :src="currentGame.icon"
            class="w-14 h-14 rounded-full object-cover shrink-0"
          />
          <div
            v-else
            class="w-14 h-14 rounded-full bg-primary-container flex items-center justify-center shrink-0"
          >
            <GamesIcon class="w-7 h-7 text-on-primary-container" />
          </div>

          <div class="min-w-0 flex-1">
            <h2 class="text-base font-semibold truncate">{{ currentGame?.name || packageName }}</h2>
            <p class="text-xs text-on-surface-variant font-mono truncate mt-0.5">{{ packageName }}</p>
          </div>
        </div>

        <!-- Group: Switches -->
        <div class="space-y-1.5">
          <!-- Lite Mode Toggle -->
          <div class="md3-list">
            <div class="md3-list-item px-5 py-4 flex items-center justify-between">
              <div class="pr-3 flex-1 min-w-0">
                <h3 class="text-sm font-medium text-on-surface">
                  Mode Lite (Hemat Daya Game)
                </h3>
                <p class="text-xs text-on-surface-variant mt-1">
                  Batasi clockspeed CPU/GPU agar HP tidak cepat panas saat memainkan game ini.
                </p>
              </div>
              <ToggleSwitch :modelValue="currentGame?.liteMode || false" @update:modelValue="toggleLiteMode" />
            </div>
          </div>

          <!-- DND Toggle -->
          <div class="md3-list">
            <div class="md3-list-item px-5 py-4 flex items-center justify-between">
              <div class="pr-3 flex-1 min-w-0">
                <h3 class="text-sm font-medium text-on-surface">
                  {{ t('games_page.settings.dnd') }}
                </h3>
                <p class="text-xs text-on-surface-variant mt-1">
                  {{ t('games_page.settings.dnd_desc') }}
                </p>
              </div>
              <ToggleSwitch :modelValue="currentGame?.dnd || false" @update:modelValue="toggleDnd" />
            </div>
          </div>

          <!-- Renderer Selector -->
          <div class="md3-list">
            <div class="md3-list-item px-5 py-4 space-y-2">
              <div>
                <h3 class="text-sm font-medium text-on-surface">
                  {{ t('games_page.settings.renderer') }}
                </h3>
                <p class="text-xs text-on-surface-variant mt-1">
                  {{ t('games_page.settings.renderer_desc') }}
                </p>
              </div>
              <select
                :value="currentGame?.renderer || 'default'"
                @change="updateRenderer($event.target.value)"
                class="w-full bg-surface-container-high border border-outline/20 rounded-xl px-3 py-2 text-xs text-on-surface focus:outline-none focus:border-primary"
              >
                <option value="default">Default Sistem ROM</option>
                <option value="skiaglthreaded">SkiaGL Threaded (Sangat Disarankan)</option>
                <option value="skiagl">SkiaGL (OpenGL ES)</option>
                <option value="skiavkthreaded">SkiaVK Threaded (Vulkan)</option>
                <option value="skiavk">SkiaVK (Vulkan)</option>
                <option value="opengl">OpenGL Tradisional</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Delete from list button -->
        <RippleComponent
          @click="handleDelete"
          class="w-full py-4 rounded-3xl bg-error-container text-on-error-container text-xs font-semibold text-center cursor-pointer transition-colors"
        >
          Hapus dari Daftar Permainan
        </RippleComponent>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useZenithGamesStore } from '@/stores/ZenithGames'
import { useLocales } from '@/helpers/Locales'

import ArrowLeftIcon from '@/components/icons/ArrowLeft.vue'
import GamesIcon from '@/components/icons/Games.vue'
import ToggleSwitch from '@/components/ui/ToggleSwitch.vue'
import RippleComponent from '@/components/ui/Ripple.vue'

const route = useRoute()
const router = useRouter()
const gamesStore = useZenithGamesStore()
const { t } = useLocales()

const packageName = computed(() => route.params.packageName)

const currentGame = computed(() => {
  return gamesStore.games.find(g => g.package === packageName.value)
})

async function toggleLiteMode(val) {
  if (!currentGame.value) return
  await gamesStore.updateGameConfig(packageName.value, { liteMode: val })
}

async function toggleDnd(val) {
  if (!currentGame.value) return
  await gamesStore.updateGameConfig(packageName.value, { dnd: val })
}

async function updateRenderer(val) {
  if (!currentGame.value) return
  await gamesStore.updateGameConfig(packageName.value, { renderer: val })
}

async function handleDelete() {
  if (confirm(`Hapus ${currentGame.value?.name || packageName.value} dari daftar?`)) {
    await gamesStore.removeGame(packageName.value)
    router.back()
  }
}
</script>
