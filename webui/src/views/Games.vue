<template>
  <div class="page games-page h-full flex flex-col overflow-hidden">
    <div class="max-w-3xl mx-auto h-full flex flex-col w-full">
      <!-- Header -->
      <div class="flex-none p-5 pb-0">
        <div class="flex justify-between items-center mb-6 text-on-surface">
          <h1 class="text-xl font-semibold">{{ t('games_page.title') }}</h1>
          
          <button
            @click="openAddModal"
            class="px-3 py-1.5 rounded-full bg-primary hover:bg-primary/80 text-on-primary text-xs font-medium flex items-center gap-1 transition-colors"
          >
            <span>+ {{ t('games_page.add_game') }}</span>
          </button>
        </div>

        <!-- Search -->
        <div class="bg-surface-container mb-4 p-3 rounded-full">
          <div class="flex items-center gap-3">
            <SearchIcon class="ml-2 text-on-surface-variant shrink-0" />
            <input
              v-model="gamesStore.searchQuery"
              type="text"
              :placeholder="t('games_page.search_placeholder')"
              class="bg-transparent border-none outline-none text-on-surface placeholder-on-surface-variant w-full text-sm"
            />
            <button
              v-if="gamesStore.searchQuery"
              @click="gamesStore.searchQuery = ''"
              class="text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer mr-3"
            >
              <CloseIcon class="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      <!-- List -->
      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5">
        <LoadingSpinner class="text-primary pt-8" v-if="gamesStore.isLoading" />

        <div v-else class="pb-4">
          <div
            v-for="app in filteredGames"
            :key="app.package"
            :class="['md3-list', { 'single-card-item': filteredGames.length === 1 }]"
          >
            <RippleComponent @click="onAppClick(app)" tabindex="0" class="md3-list-item">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <img
                    v-if="app.icon"
                    :src="app.icon"
                    loading="lazy"
                    class="w-12 h-12 rounded-full object-cover shrink-0"
                    :alt="app.name"
                  />
                  <div
                    v-else
                    class="w-12 h-12 rounded-full bg-primary-container flex items-center justify-center shrink-0"
                  >
                    <GamesIcon class="w-6 h-6 text-on-primary-container" />
                  </div>

                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-on-surface truncate">
                      {{ app.name || app.package }}
                    </h3>
                    <p class="text-xs text-on-surface-variant truncate mt-1">
                      {{ app.package }}
                    </p>
                    <div class="flex items-center gap-1 mt-1">
                      <span class="inline-flex items-center bg-primary rounded-sm px-1.5 py-0.5">
                        <span class="text-[10px] text-on-primary font-semibold uppercase">
                          {{ t('games_page.badges.tweak_enabled') }}
                        </span>
                      </span>
                      <span
                        v-if="app.liteMode"
                        class="inline-flex items-center bg-tertiary rounded-sm px-1.5 py-0.5"
                      >
                        <span class="text-[10px] text-on-tertiary font-semibold uppercase">
                          Lite
                        </span>
                      </span>
                    </div>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-surface-dim flex items-center justify-center shrink-0 ms-3">
                  <ChevronRightIcon class="text-on-surface-variant shrink-0 rtl:rotate-180" :size="22" />
                </div>
              </div>
            </RippleComponent>
          </div>

          <div
            v-if="filteredGames.length === 0 && !gamesStore.isLoading"
            class="text-center py-8 text-on-surface-variant"
          >
            <p>{{ t('games_page.no_apps_found') }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Add Game -->
    <Modal :show="isAddModalOpen" :title="t('games_page.select_app')" @close="isAddModalOpen = false">
      <div class="px-4 pb-2 space-y-3">
        <input
          v-model="installedAppSearch"
          type="text"
          :placeholder="t('games_page.search_placeholder')"
          class="w-full bg-surface-container border border-outline/20 rounded-xl py-2 px-3 text-xs text-on-surface placeholder:text-on-surface-variant focus:outline-none focus:border-primary"
        />

        <div class="max-h-64 overflow-y-auto space-y-1.5 pr-1 scrollbar-hidden">
          <div
            v-for="app in filteredInstalledApps"
            :key="app.package"
            class="flex items-center justify-between p-2.5 rounded-xl bg-surface-container hover:bg-surface-container-high transition-all"
          >
            <div class="flex items-center gap-2.5 min-w-0 flex-1">
              <img
                v-if="app.icon"
                :src="app.icon"
                :alt="app.name"
                class="w-8 h-8 rounded-full object-cover shrink-0"
              />
              <div
                v-else
                class="w-8 h-8 rounded-full bg-primary-container flex items-center justify-center shrink-0"
              >
                <GamesIcon class="w-4 h-4 text-on-primary-container" />
              </div>

              <div class="min-w-0 flex-1">
                <div class="text-xs font-medium text-on-surface truncate">{{ app.name }}</div>
                <div class="text-[10px] text-on-surface-variant font-mono truncate">{{ app.package }}</div>
              </div>
            </div>

            <button
              @click="addApp(app)"
              class="px-3 py-1 rounded-full text-xs font-medium shrink-0 transition-colors"
              :class="isAlreadyAdded(app.package)
                ? 'bg-surface-container-highest text-on-surface-variant cursor-not-allowed'
                : 'bg-primary text-on-primary hover:bg-primary/80'"
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
import { useZenithGamesStore } from '@/stores/ZenithGames'
import { useLocales } from '@/helpers/Locales'

import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import RippleComponent from '@/components/ui/Ripple.vue'
import SearchIcon from '@/components/icons/Search.vue'
import CloseIcon from '@/components/icons/Close.vue'
import ChevronRightIcon from '@/components/icons/ChevronRight.vue'
import GamesIcon from '@/components/icons/Games.vue'
import Modal from '@/components/ui/Modal.vue'

const router = useRouter()
const gamesStore = useZenithGamesStore()
const { t } = useLocales()

const isAddModalOpen = ref(false)
const installedAppSearch = ref('')

onMounted(async () => {
  if (gamesStore.games.length === 0) {
    await gamesStore.loadGames()
  }
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
