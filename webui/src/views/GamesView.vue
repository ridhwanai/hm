<template>
  <div class="space-y-4 pb-24 animate-fade-in">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-xl font-extrabold text-white flex items-center gap-2">
          <Gamepad2 class="w-5 h-5 text-indigo-400" />
          {{ t('games.title') }}
        </h1>
        <p class="text-xs text-slate-400 mt-0.5">{{ t('games.subtitle') }}</p>
      </div>

      <button
        @click="openAddGameModal"
        class="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold shadow-lg shadow-indigo-500/25 active:scale-95 transition-all"
      >
        <Plus class="w-4 h-4" />
        {{ t('games.addGame') }}
      </button>
    </div>

    <!-- Search Bar -->
    <div class="relative">
      <Search class="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
      <input
        v-model="gamesStore.searchQuery"
        type="text"
        :placeholder="t('games.searchPlaceholder')"
        class="w-full bg-[#121827] border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-indigo-500/60 focus:ring-1 focus:ring-indigo-500/60 transition-all"
      />
    </div>

    <!-- Stats Count -->
    <div class="flex items-center justify-between text-xs text-slate-400 px-1">
      <span>{{ filteredGames.length }} Game terdaftar</span>
      <button @click="gamesStore.loadGames" class="flex items-center gap-1 hover:text-slate-200 transition-colors">
        <RefreshCw class="w-3 h-3" :class="{ 'animate-spin': gamesStore.isLoading }" />
        <span>{{ t('common.refresh') }}</span>
      </button>
    </div>

    <!-- Games List -->
    <div v-if="filteredGames.length > 0" class="space-y-2.5">
      <div
        v-for="game in filteredGames"
        :key="game.package"
        class="glass-card rounded-2xl p-3.5 border border-white/10 flex items-center justify-between gap-3 hover:border-white/20 transition-all"
      >
        <!-- App Icon & Details -->
        <div class="flex items-center gap-3 min-w-0 flex-1">
          <div class="w-11 h-11 rounded-xl bg-slate-800/80 border border-white/10 flex items-center justify-center shrink-0 overflow-hidden shadow-md">
            <img v-if="game.icon" :src="game.icon" :alt="game.name" class="w-full h-full object-cover" />
            <Gamepad2 v-else class="w-6 h-6 text-indigo-400" />
          </div>

          <div class="min-w-0 flex-1">
            <h3 class="text-sm font-bold text-white truncate">{{ game.name }}</h3>
            <p class="text-[11px] text-slate-400 font-mono truncate">{{ game.package }}</p>

            <!-- Badges -->
            <div class="flex items-center gap-1.5 mt-1.5 flex-wrap">
              <span class="text-[9px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded bg-rose-500/20 text-rose-300 border border-rose-500/30">
                Performance
              </span>
              <span v-if="game.dnd" class="text-[9px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                DND Active
              </span>
              <span v-if="game.renderer && game.renderer !== 'default'" class="text-[9px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded bg-cyan-500/20 text-cyan-300 border border-cyan-500/30">
                {{ game.renderer }}
              </span>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex items-center gap-1 shrink-0">
          <button
            @click="openGameSettings(game)"
            class="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/10 transition-colors"
            title="Pengaturan Game"
          >
            <Settings2 class="w-4 h-4" />
          </button>

          <button
            @click="deleteGame(game)"
            class="p-2 rounded-xl text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
            title="Hapus Game"
          >
            <Trash2 class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="glass-card rounded-2xl p-8 text-center border border-white/10 space-y-3">
      <div class="w-12 h-12 rounded-2xl bg-indigo-500/15 text-indigo-400 flex items-center justify-center mx-auto">
        <Gamepad2 class="w-6 h-6" />
      </div>
      <div class="space-y-1">
        <h3 class="text-sm font-bold text-white">{{ t('games.noGames') }}</h3>
        <p class="text-xs text-slate-400 max-w-xs mx-auto">
          Tambahkan game favorit Anda agar AZenith otomatis mengoptimalkan performa saat game dibuka.
        </p>
      </div>
      <button
        @click="openAddGameModal"
        class="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-indigo-600 text-white text-xs font-bold shadow-lg shadow-indigo-500/25"
      >
        <Plus class="w-4 h-4" />
        {{ t('games.addGame') }}
      </button>
    </div>

    <!-- Modal: Add Game -->
    <Modal :isOpen="isAddModalOpen" @close="isAddModalOpen = false" :title="t('games.modal.selectApp')">
      <div class="space-y-3">
        <!-- Search inside modal -->
        <div class="relative">
          <Search class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            v-model="installedAppSearch"
            type="text"
            placeholder="Cari aplikasi terinstal..."
            class="w-full bg-[#0d131f] border border-white/10 rounded-xl py-2 pl-9 pr-3 text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-indigo-500"
          />
        </div>

        <div class="max-h-72 overflow-y-auto space-y-2 pr-1">
          <div
            v-for="app in filteredInstalledApps"
            :key="app.package"
            class="flex items-center justify-between p-2.5 rounded-xl bg-black/25 border border-white/5 hover:border-white/15 transition-all"
          >
            <div class="flex items-center gap-2.5 min-w-0 flex-1">
              <div class="w-8 h-8 rounded-lg bg-slate-800 flex items-center justify-center shrink-0 overflow-hidden">
                <img v-if="app.icon" :src="app.icon" :alt="app.name" class="w-full h-full object-cover" />
                <Package v-else class="w-4 h-4 text-slate-400" />
              </div>
              <div class="min-w-0 flex-1">
                <div class="text-xs font-bold text-slate-200 truncate">{{ app.name }}</div>
                <div class="text-[10px] text-slate-400 font-mono truncate">{{ app.package }}</div>
              </div>
            </div>

            <button
              @click="handleAddSelectedApp(app)"
              class="px-2.5 py-1 rounded-lg text-xs font-semibold shrink-0 transition-colors"
              :class="isAlreadyAdded(app.package)
                ? 'bg-slate-800 text-slate-500 cursor-not-allowed'
                : 'bg-indigo-600 hover:bg-indigo-500 text-white'"
              :disabled="isAlreadyAdded(app.package)"
            >
              {{ isAlreadyAdded(app.package) ? 'Ditambahkan' : 'Pilih' }}
            </button>
          </div>
        </div>
      </div>
    </Modal>

    <!-- Modal: Game Settings -->
    <Modal :isOpen="isSettingsModalOpen" @close="isSettingsModalOpen = false" :title="`Pengaturan: ${editingGame?.name || ''}`">
      <div v-if="editingGame" class="space-y-4 text-xs">
        <!-- DND Toggle -->
        <div class="flex items-center justify-between p-3 rounded-xl bg-black/25 border border-white/5">
          <div class="pr-2">
            <h4 class="font-bold text-slate-100">{{ t('games.modal.dndTitle') }}</h4>
            <p class="text-[11px] text-slate-400 mt-0.5 leading-tight">{{ t('games.modal.dndDesc') }}</p>
          </div>
          <button
            @click="editingGame.dnd = !editingGame.dnd"
            class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200"
            :class="editingGame.dnd ? 'bg-indigo-600' : 'bg-slate-700'"
          >
            <span
              class="pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white transition duration-200"
              :class="editingGame.dnd ? 'translate-x-4' : 'translate-x-0'"
            ></span>
          </button>
        </div>

        <!-- Custom Renderer Selector -->
        <div class="p-3 rounded-xl bg-black/25 border border-white/5 space-y-2">
          <div>
            <h4 class="font-bold text-slate-100">{{ t('games.modal.rendererTitle') }}</h4>
            <p class="text-[11px] text-slate-400 mt-0.5 leading-tight">{{ t('games.modal.rendererDesc') }}</p>
          </div>

          <select
            v-model="editingGame.renderer"
            class="w-full bg-[#121827] border border-white/10 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-indigo-500"
          >
            <option value="default">Default Sistem ROM</option>
            <option value="skiaglthreaded">SkiaGL Threaded (Sangat Direkomendasikan)</option>
            <option value="skiagl">SkiaGL (OpenGL ES)</option>
            <option value="skiavkthreaded">SkiaVK Threaded (Vulkan)</option>
            <option value="skiavk">SkiaVK (Vulkan Standard)</option>
            <option value="opengl">OpenGL Tradisional</option>
          </select>
        </div>
      </div>

      <template #footer>
        <button
          @click="isSettingsModalOpen = false"
          class="px-3 py-1.5 rounded-xl text-xs font-semibold text-slate-400 hover:text-white"
        >
          {{ t('common.cancel') }}
        </button>
        <button
          @click="saveCurrentGameSettings"
          class="px-4 py-1.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold shadow-lg shadow-indigo-500/25"
        >
          {{ t('common.save') }}
        </button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Gamepad2, Plus, Search, RefreshCw, Settings2, Trash2, Package } from 'lucide-vue-next'
import { useZenithGamesStore } from '@/stores/ZenithGames'
import { useLocales } from '@/helpers/Locales'
import Modal from '@/components/Modal.vue'

const gamesStore = useZenithGamesStore()
const { t } = useLocales()

const isAddModalOpen = ref(false)
const isSettingsModalOpen = ref(false)
const installedAppSearch = ref('')
const editingGame = ref(null)

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

async function openAddGameModal() {
  isAddModalOpen.value = true
  installedAppSearch.value = ''
  if (gamesStore.installedApps.length === 0) {
    await gamesStore.loadInstalledApps()
  }
}

async function handleAddSelectedApp(app) {
  await gamesStore.addGame(app.package, app.name)
  isAddModalOpen.value = false
}

function openGameSettings(game) {
  editingGame.value = { ...game }
  isSettingsModalOpen.value = true
}

async function saveCurrentGameSettings() {
  if (!editingGame.value) return
  await gamesStore.updateGameConfig(editingGame.value.package, {
    dnd: editingGame.value.dnd,
    renderer: editingGame.value.renderer,
  })
  isSettingsModalOpen.value = false
}

async function deleteGame(game) {
  if (confirm(`Hapus ${game.name} dari daftar game?`)) {
    await gamesStore.removeGame(game.package)
  }
}
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in {
  animation: fadeIn 0.25s ease-out forwards;
}
</style>
