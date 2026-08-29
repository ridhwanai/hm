<template>
  <div class="page games-page h-full flex flex-col overflow-hidden">
    <div class="max-w-3xl mx-auto h-full flex flex-col w-full">
      <!-- Header -->
      <div class="flex-none p-5 pb-0">
        <div class="flex justify-between items-center mb-4 text-[#e2e2e9]">
          <h1 class="text-xl font-semibold">{{ t('games_page.title') }}</h1>
          
          <button
            @click="openAddModal"
            class="px-3 py-1.5 rounded-full bg-[#a8c7fa] hover:bg-[#82b1ff] text-[#04305f] text-xs font-bold flex items-center gap-1 shadow-sm transition-all"
          >
            <Plus class="w-4 h-4" />
            <span>{{ t('games_page.add_game') }}</span>
          </button>
        </div>

        <!-- Search Bar -->
        <div class="bg-[#1e1f25] mb-4 p-3 rounded-full flex items-center gap-3">
          <Search class="ml-2 text-[#c4c6d0] w-5 h-5 shrink-0" />
          <input
            v-model="gamesStore.searchQuery"
            type="text"
            :placeholder="t('games_page.search_placeholder')"
            class="bg-transparent border-none outline-none text-[#e2e2e9] placeholder-[#8e9099] w-full text-sm"
          />
          <button
            v-if="gamesStore.searchQuery"
            @click="gamesStore.searchQuery = ''"
            class="text-[#c4c6d0] hover:text-white mr-2"
          >
            <X class="w-4 h-4" />
          </button>
        </div>
      </div>

      <!-- Games List -->
      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5">
        <div v-if="filteredGames.length > 0" class="space-y-1 pb-4">
          <div
            v-for="app in filteredGames"
            :key="app.package"
            :class="['md3-list', { 'single-card-item': filteredGames.length === 1 }]"
          >
            <RippleComponent @click="onAppClick(app)" tabindex="0" class="md3-list-item">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-12 h-12 rounded-full bg-slate-800 flex items-center justify-center shrink-0 overflow-hidden">
                    <img v-if="app.icon" :src="app.icon" class="w-full h-full object-cover" :alt="app.name" />
                    <Gamepad2 v-else class="w-6 h-6 text-[#a8c7fa]" />
                  </div>

                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-[#e2e2e9] truncate">
                      {{ app.name || app.package }}
                    </h3>
                    <p class="text-xs text-[#c4c6d0] truncate mt-0.5">
                      {{ app.package }}
                    </p>
                    <div class="flex items-center gap-1.5 mt-1.5">
                      <span class="inline-flex items-center bg-[#234475] text-[#d6e3ff] rounded px-1.5 py-0.5 text-[10px] font-semibold uppercase">
                        {{ t('games_page.badges.tweak_enabled') }}
                      </span>
                      <span v-if="app.dnd" class="inline-flex items-center bg-indigo-500/20 text-indigo-300 rounded px-1.5 py-0.5 text-[10px] font-semibold">
                        DND
                      </span>
                    </div>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-[#191b20] flex items-center justify-center shrink-0 ms-3">
                  <ChevronRight class="text-[#c4c6d0] w-4 h-4 shrink-0" />
                </div>
              </div>
            </RippleComponent>
          </div>
        </div>

        <div v-else class="text-center py-10 text-[#c4c6d0] text-xs">
          <p>{{ t('games_page.no_apps_found') }}</p>
        </div>
      </div>
    </div>

    <!-- Modal Add Game -->
    <Modal :isOpen="isAddModalOpen" @close="isAddModalOpen = false" :title="t('games_page.select_app')">
      <div class="space-y-3">
        <input
          v-model="installedAppSearch"
          type="text"
          placeholder="Cari aplikasi..."
          class="w-full bg-[#111318] border border-white/10 rounded-xl py-2 px-3 text-xs text-[#e2e2e9] placeholder-[#8e9099] focus:outline-none focus:border-[#a8c7fa]"
        />

        <div class="max-h-64 overflow-y-auto space-y-1.5 pr-1 scrollbar-hidden">
          <div
            v-for="app in filteredInstalledApps"
            :key="app.package"
            class="flex items-center justify-between p-2.5 rounded-xl bg-[#191b20] border border-white/5 hover:bg-[#23242a] transition-all"
          >
            <div class="flex items-center gap-2.5 min-w-0 flex-1">
              <div class="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center shrink-0 overflow-hidden">
                <img v-if="app.icon" :src="app.icon" :alt="app.name" class="w-full h-full object-cover" />
                <Package v-else class="w-4 h-4 text-slate-400" />
              </div>
              <div class="min-w-0 flex-1">
                <div class="text-xs font-bold text-[#e2e2e9] truncate">{{ app.name }}</div>
                <div class="text-[10px] text-[#c4c6d0] font-mono truncate">{{ app.package }}</div>
              </div>
            </div>

            <button
              @click="addApp(app)"
              class="px-2.5 py-1 rounded-full text-[11px] font-bold shrink-0 transition-colors"
              :class="isAlreadyAdded(app.package)
                ? 'bg-slate-800 text-slate-500 cursor-not-allowed'
                : 'bg-[#a8c7fa] text-[#04305f] hover:bg-[#82b1ff]'"
              :disabled="isAlreadyAdded(app.package)"
            >
              {{ isAlreadyAdded(app.package) ? 'Ditambahkan' : 'Pilih' }}
            </button>
          </div>
        </div>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, X, Plus, ChevronRight, Gamepad2, Package } from 'lucide-vue-next'
import { useZenithGamesStore } from '@/stores/ZenithGames'
import { useLocales } from '@/helpers/Locales'
import RippleComponent from '@/components/ui/Ripple.vue'
import Modal from '@/components/Modal.vue'

const router = useRouter()
const gamesStore = useZenithGamesStore()
const { t } = useLocales()

const isAddModalOpen = ref(false)
const installedAppSearch = ref('')

onMounted(() => {
  gamesStore.loadGames()
})

const filteredGames = computed(() => {
  const q = gamesStore.searchQuery.toLowerCase().trim()
  if (!q) return gamesStore.games
  return gamesStore.games.filter(g =>
    g.name.toLowerCase().includes(q) || g.package.toLowerCase().includes(q)
  )
})

const filteredInstalledApps = computed(() => {
  const q = installedAppSearch.value.toLowerCase().trim()
  if (!q) return gamesStore.installedApps
  return gamesStore.installedApps.filter(a =>
    a.name.toLowerCase().includes(q) || a.package.toLowerCase().includes(q)
  )
})

function isAlreadyAdded(pkg) {
  return gamesStore.games.some(g => g.package === pkg)
}

async function openAddModal() {
  isAddModalOpen.value = true
  installedAppSearch.value = ''
  if (gamesStore.installedApps.length === 0) {
    await gamesStore.loadInstalledApps()
  }
}

async function addApp(app) {
  await gamesStore.addGame(app.package, app.name)
  isAddModalOpen.value = false
}

function onAppClick(app) {
  router.push(`/games/${app.package}`)
}
</script>
