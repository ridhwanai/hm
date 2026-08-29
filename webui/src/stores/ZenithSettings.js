import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as KernelSU from '@/helpers/KernelSU'

const LOG_FILE_PATH = '/data/adb/.config/AZenith/debug/AZenith.log'
const SYSMON_LOG_PATH = '/data/adb/.config/AZenith/sysmon.log'

export const useZenithSettingsStore = defineStore('zenithSettings', () => {
  const logContent = ref('')
  const selectedLogType = ref('azenith') // 'azenith' | 'sysmon'
  const isAutoRefresh = ref(false)
  const isFetchingLogs = ref(false)

  let autoRefreshTimer = null

  async function fetchLogs() {
    isFetchingLogs.value = true
    try {
      const targetPath = selectedLogType.value === 'azenith' ? LOG_FILE_PATH : SYSMON_LOG_PATH
      const content = await KernelSU.readFile(targetPath)
      logContent.value = content || 'Log masih kosong.'
    } catch {
      logContent.value = 'Gagal memuat file log.'
    } finally {
      isFetchingLogs.value = false
    }
  }

  async function clearLogs() {
    try {
      const targetPath = selectedLogType.value === 'azenith' ? LOG_FILE_PATH : SYSMON_LOG_PATH
      await KernelSU.writeFile(targetPath, '')
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
