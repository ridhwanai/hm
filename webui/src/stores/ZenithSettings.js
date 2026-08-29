import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as KernelSU from '@/helpers/KernelSU'

const LOG_FILE_PATH = '/data/adb/.config/wann/debug/wann.log'
const LEGACY_LOG_FILE_PATH = '/data/adb/.config/AZenith/debug/AZenith.log'
const SYSMON_LOG_PATH = '/data/adb/.config/wann/sysmon.log'
const LEGACY_SYSMON_LOG_PATH = '/data/adb/.config/AZenith/sysmon.log'

export const useZenithSettingsStore = defineStore('zenithSettings', () => {
  const logContent = ref('')
  const selectedLogType = ref('wann') // 'wann' | 'sysmon'
  const isAutoRefresh = ref(false)
  const isFetchingLogs = ref(false)

  let autoRefreshTimer = null

  async function fetchLogs() {
    isFetchingLogs.value = true
    try {
      let content = ''
      if (selectedLogType.value === 'wann') {
        content = await KernelSU.readFile(LOG_FILE_PATH)
        if (!content) content = await KernelSU.readFile(LEGACY_LOG_FILE_PATH)
      } else {
        content = await KernelSU.readFile(SYSMON_LOG_PATH)
        if (!content) content = await KernelSU.readFile(LEGACY_SYSMON_LOG_PATH)
      }
      logContent.value = content || 'Log masih kosong.'
    } catch {
      logContent.value = 'Gagal memuat file log.'
    } finally {
      isFetchingLogs.value = false
    }
  }

  async function clearLogs() {
    try {
      const targetPath = selectedLogType.value === 'wann' ? LOG_FILE_PATH : SYSMON_LOG_PATH
      const legacyPath = selectedLogType.value === 'wann' ? LEGACY_LOG_FILE_PATH : LEGACY_SYSMON_LOG_PATH
      await KernelSU.writeFile(targetPath, '')
      await KernelSU.writeFile(legacyPath, '')
      logContent.value = 'Log telah dibersihkan.'
      KernelSU.toast('Log berhasil dibersihkan!')
    } catch (e) {
      KernelSU.toast('Gagal membersihkan log')
    }
  }

  function setLogType(type) {
    selectedLogType.value = type
    fetchLogs()
  }

  function toggleAutoRefresh(val) {
    isAutoRefresh.value = val
    if (val) {
      autoRefreshTimer = setInterval(() => {
        fetchLogs()
      }, 3000)
    } else {
      if (autoRefreshTimer) {
        clearInterval(autoRefreshTimer)
        autoRefreshTimer = null
      }
    }
  }

  async function copyLogs() {
    try {
      await navigator.clipboard.writeText(logContent.value)
      KernelSU.toast('Log tersalin ke clipboard!')
    } catch {
      KernelSU.toast('Gagal menyalin log')
    }
  }

  function createShortcut() {
    KernelSU.createShortcut()
  }

  return {
    logContent,
    selectedLogType,
    isAutoRefresh,
    isFetchingLogs,
    fetchLogs,
    clearLogs,
    setLogType,
    toggleAutoRefresh,
    copyLogs,
    createShortcut,
  }
})
