<template>
  <div class="page game-settings-page h-full flex flex-col overflow-hidden">
    <div class="max-w-3xl mx-auto h-full flex flex-col w-full">
      <!-- Top Header with Back Button -->
      <div class="flex-none p-5 pb-3">
        <div class="flex items-center gap-3 text-[#e2e2e9]">
          <button
            @click="router.back()"
            class="p-2 rounded-full hover:bg-white/10 transition-colors"
          >
            <ArrowLeft class="w-5 h-5" />
          </button>
          <h1 class="text-xl font-semibold truncate">{{ currentGame?.name || packageName }}</h1>
        </div>
      </div>

      <!-- Settings Content -->
      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5 space-y-4">
        <!-- App Info Card -->
        <div class="bg-[#1e1f25] p-4 rounded-2xl flex items-center gap-4 text-[#e2e2e9]">
          <div class="w-14 h-14 rounded-2xl bg-slate-800 flex items-center justify-center shrink-0 overflow-hidden shadow-md">
            <img v-if="currentGame?.icon" :src="currentGame.icon" class="w-full h-full object-cover" />
            <Gamepad2 v-else class="w-7 h-7 text-[#a8c7fa]" />
          </div>
          <div class="min-w-0 flex-1">
            <h2 class="text-base font-bold truncate">{{ currentGame?.name || packageName }}</h2>
            <p class="text-xs text-[#c4c6d0] font-mono truncate mt-0.5">{{ packageName }}</p>
          </div>
        </div>

        <!-- Group: Settings -->
        <div class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
          <!-- DND Toggle -->
          <div class="md3-list-item px-5 py-4 flex items-center justify-between">
            <div class="pr-3">
              <h3 class="text-sm font-medium text-[#e2e2e9]">{{ t('games_page.settings.dnd') }}</h3>
              <p class="text-xs text-[#c4c6d0] mt-0.5">{{ t('games_page.settings.dnd_desc') }}</p>
            </div>
            <button
              @click="toggleDnd"
              class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200"
              :class="currentGame?.dnd ? 'bg-[#a8c7fa]' : 'bg-slate-700'"
            >
              <span
                class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-[#04305f] shadow-sm transition duration-200"
                :class="currentGame?.dnd ? 'translate-x-5' : 'translate-x-0'"
              ></span>
            </button>
          </div>

          <!-- Renderer Selector -->
          <div class="md3-list-item px-5 py-4 space-y-2">
            <div>
              <h3 class="text-sm font-medium text-[#e2e2e9]">{{ t('games_page.settings.renderer') }}</h3>
              <p class="text-xs text-[#c4c6d0] mt-0.5">{{ t('games_page.settings.renderer_desc') }}</p>
            </div>
            <select
              :value="currentGame?.renderer || 'default'"
              @change="updateRenderer($event.target.value)"
              class="w-full bg-[#111318] border border-white/10 rounded-xl px-3 py-2 text-xs text-[#e2e2e9] focus:outline-none focus:border-[#a8c7fa]"
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

        <!-- Delete from list button -->
        <button
          @click="handleDelete"
          class="w-full py-3.5 rounded-2xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 text-xs font-bold transition-colors flex items-center justify-center gap-2 border border-rose-500/20"
        >
          <Trash2 class="w-4 h-4" />
          <span>Hapus dari Daftar Permainan</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Gamepad2, Trash2 } from 'lucide-vue-next'
import { useZenithGamesStore } from '@/stores/ZenithGames'
import { useLocales } from '@/helpers/Locales'

const route = useRoute()
const router = useRouter()
const gamesStore = useZenithGamesStore()
const { t } = useLocales()

const packageName = computed(() => route.params.packageName)

const currentGame = computed(() => {
  return gamesStore.games.find(g => g.package === packageName.value)
})

async function toggleDnd() {
  if (!currentGame.value) return
  await gamesStore.updateGameConfig(packageName.value, { dnd: !currentGame.value.dnd })
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
