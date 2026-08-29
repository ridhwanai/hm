<template>
  <div class="h-full flex flex-col overflow-hidden">
    <!-- Header -->
    <div class="sticky top-0 z-10 bg-[#111318] px-4 py-3 border-b border-white/5 flex items-center justify-between">
      <div>
        <h1 class="text-base font-bold text-[#e2e2e9]">{{ t('games.title') }}</h1>
        <span class="text-[10px] text-[#c4c6d0] block">{{ gamesStore.games.length }} Game Terdaftar</span>
      </div>

      <button
        @click="openAddModal"
        class="px-3 py-1.5 rounded-full bg-[#a8c7fa] hover:bg-[#82b1ff] text-[#04305f] text-xs font-bold flex items-center gap-1 shadow-sm transition-all"
      >
        <Plus class="w-4 h-4" />
        <span>{{ t('games.addGame') }}</span>
      </button>
    </div>

    <!-- Search & Content -->
    <div class="scrollbar-hidden pb-safe-nav flex-1 overflow-y-auto px-4 py-3 space-y-3">
      <!-- Search Input -->
      <div class="relative">
        <Search class="w-4 h-4 text-[#c4c6d0] absolute left-3 top-1/2 -translate-y-1/2" />
        <input
          v-model="gamesStore.searchQuery"
          type="text"
          :placeholder="t('games.searchPlaceholder')"
          class="w-full bg-[#1e1f25] border border-white/5 rounded-xl py-2 pl-9 pr-3 text-xs text-[#e2e2e9] placeholder:text-[#8e9099] focus:outline-none focus:border-[#a8c7fa]"
        />
      </div>

      <!-- Games List -->
      <div v-if="filteredGames.length > 0" class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
        <div
          v-for="game in filteredGames"
          :key="game.package"
          class="md3-list-item px-4 py-3 flex items-center justify-between gap-3"
        >
          <!-- Left Icon & Info -->
          <div class="flex items-center gap-3 min-w-0 flex-1">
            <div class="w-10 h-10 rounded-xl bg-slate-800 flex items-center justify-center shrink-0 overflow-hidden">
              <img v-if="game.icon" :src="game.icon" :alt="game.name" class="w-full h-full object-cover" />
              <Gamepad2 v-else class="w-5 h-5 text-[#a8c7fa]" />
            </div>

            <div class="min-w-0 flex-1">
              <h3 class="text-xs font-bold text-[#e2e2e9] truncate">{{ game.name }}</h3>
              <p class="text-[10px] text-[#c4c6d0] font-mono truncate">{{ game.package }}</p>
              
              <div class="flex items-center gap-2 mt-1">
                <span class="text-[9px] px-1.5 py-0.2 rounded bg-rose-500/20 text-rose-300 font-semibold">
                  Performance
                </span>
                <span v-if="game.dnd" class="text-[9px] px-1.5 py-0.2 rounded bg-indigo-500/20 text-indigo-300 font-semibold">
                  DND ON
                </span>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-2 shrink-0">
            <button
              @click="toggleDND(game)"
              class="p-2 rounded-lg text-[#c4c6d0] hover:text-white"
              :title="game.dnd ? 'DND Aktif' : 'DND Nonaktif'"
            >
              <BellOff v-if="game.dnd" class="w-4 h-4 text-indigo-400" />
              <Bell v-else class="w-4 h-4 text-slate-500" />
            </button>

            <button
              @click="removeGame(game)"
              class="p-2 rounded-lg text-slate-400 hover:text-rose-400"
              title="Hapus Game"
            >
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-else class="bg-[#1e1f25] rounded-2xl p-6 text-center space-y-2 text-[#c4c6d0]">
        <Gamepad2 class="w-8 h-8 mx-auto text-slate-500" />
        <p class="text-xs">{{ t('games.noGames') }}</p>
        <button
          @click="openAddModal"
          class="px-3 py-1.5 rounded-full bg-[#a8c7fa] text-[#04305f] text-xs font-bold inline-flex items-center gap-1"
        >
          <Plus class="w-4 h-4" />
          <span>{{ t('games.addGame') }}</span>
        </button>
      </div>
    </div>

    <!-- Modal Add Game -->
    <Modal :isOpen="isAddModalOpen" @close="isAddModalOpen = false" :title="t('games.modal.selectApp')">
      <div class="space-y-3">
        <input
          v-model="installedAppSearch"
          type="text"
          placeholder="Cari aplikasi..."
          class="w-full bg-[#111318] border border-white/10 rounded-xl py-2 px-3 text-xs text-[#e2e2e9] placeholder:text-[#8e9099] focus:outline-none focus:border-[#a8c7fa]"
        />

        <div class="max-h-64 overflow-y-auto space-y-1 pr-1 scrollbar-hidden">
          <div
            v-for="app in filteredInstalledApps"
            :key="app.package"
            class="flex items-center justify-between p-2.5 rounded-xl bg-[#191b20] border border-white/5 hover:bg-[#23242a] transition-all"
          >
            <div class="flex items-center gap-2.5 min-w-0 flex-1">
              <div class="w-8 h-8 rounded-lg bg-slate-800 flex items-center justify-center shrink-0 overflow-hidden">
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
import { Plus, Search, Gamepad2, Trash2, Bell, BellOff, Package } from 'lucide-vue-next'
import { useZenithGamesStore } from '@/stores/ZenithGames'
import { useLocales } from '@/helpers/Locales'
import Modal from '@/components/Modal.vue'

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

async function toggleDND(game) {
  await gamesStore.updateGameConfig(game.package, { dnd: !game.dnd })
}

async function removeGame(game) {
  if (confirm(`Hapus ${game.name} dari daftar?`)) {
    await gamesStore.removeGame(game.package)
  }
}
</script>
