import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as KernelSU from '@/helpers/KernelSU'

const GAMELIST_PATH = '/data/adb/.config/AZenith/gamelist/azenithApplist.json'

export const useZenithGamesStore = defineStore('zenithGames', () => {
  const games = ref([])
  const installedApps = ref([])
  const isLoading = ref(false)
  const searchQuery = ref('')

  async function loadGames() {
    isLoading.value = true
    try {
      let content = await KernelSU.readFile(GAMELIST_PATH)
      if (!content || !content.trim().startsWith('{')) {
        // Fallback default list
        content = '{}'
      }

      let parsed = {}
      try {
        parsed = JSON.parse(content)
      } catch {
        parsed = {}
      }

      const list = []
      for (const [pkg, conf] of Object.entries(parsed)) {
        const appName = await KernelSU.getAppLabel(pkg)
        const icon = await KernelSU.getAppIcon(pkg)
        list.push({
          package: pkg,
          name: appName || pkg,
          icon: icon || '',
          dnd: conf.dnd_on_gaming === 'true' || conf.dnd_on_gaming === true,
          renderer: conf.renderer || 'default',
          liteMode: conf.perf_lite_mode === 'true',
        })
      }

      games.value = list
    } catch (e) {
      console.error('Failed to load games:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function saveGames() {
    try {
      const obj = {}
      for (const g of games.value) {
        obj[g.package] = {
          perf_lite_mode: g.liteMode ? 'true' : 'default',
          dnd_on_gaming: g.dnd ? 'true' : 'default',
          app_priority: 'default',
          game_preload: 'default',
          refresh_rate: 'default',
          renderer: g.renderer || 'default',
        }
      }
      await KernelSU.writeFile(GAMELIST_PATH, JSON.stringify(obj, null, 2))
    } catch (e) {
      console.error('Failed to save games:', e)
      KernelSU.toast('Gagal menyimpan daftar game')
    }
  }

  async function addGame(pkg, appName) {
    if (games.value.some(g => g.package === pkg)) {
      KernelSU.toast('Game sudah ada di dalam daftar!')
      return
    }

    const icon = await KernelSU.getAppIcon(pkg)
    games.value.unshift({
      package: pkg,
      name: appName || pkg,
      icon: icon || '',
      dnd: true,
      renderer: 'default',
      liteMode: false,
    })

    await saveGames()
    KernelSU.toast(`${appName || pkg} berhasil ditambahkan!`)
  }

  async function removeGame(pkg) {
    games.value = games.value.filter(g => g.package !== pkg)
    await saveGames()
    KernelSU.toast('Game dihapus dari daftar optimasi')
  }

  async function updateGameConfig(pkg, config) {
    const idx = games.value.findIndex(g => g.package === pkg)
    if (idx !== -1) {
      games.value[idx] = { ...games.value[idx], ...config }
      await saveGames()
      KernelSU.toast('Pengaturan game diperbarui')
    }
  }

  async function loadInstalledApps() {
    try {
      const packages = await KernelSU.listApps()
      const apps = []
      for (const pkg of packages) {
        const name = await KernelSU.getAppLabel(pkg)
        const icon = await KernelSU.getAppIcon(pkg)
        apps.push({
          package: pkg,
          name: name || pkg,
          icon: icon || '',
        })
      }
      apps.sort((a, b) => a.name.localeCompare(b.name))
      installedApps.value = apps
    } catch (e) {
      console.error('Failed to list installed apps:', e)
    }
  }

  return {
    games,
    installedApps,
    isLoading,
    searchQuery,
    loadGames,
    saveGames,
    addGame,
    removeGame,
    updateGameConfig,
    loadInstalledApps,
  }
})
