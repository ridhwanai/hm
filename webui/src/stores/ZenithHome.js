import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as KernelSU from '@/helpers/KernelSU'

const CONFIG_PATH = '/data/adb/.config/AZenith'
const MOD_PATH = '/data/adb/modules/AZenith'

export const useZenithHomeStore = defineStore('zenithHome', () => {
  const daemonStatus = ref('loading') // 'running' | 'stopped' | 'loading'
  const daemonPid = ref('')
  const currentProfile = ref('balanced') // 'performance' | 'balanced' | 'eco'
  const autoModeEnabled = ref(true)
  const moduleVersion = ref('v5.1')
  
  const deviceSpecs = ref({
    soc: 'Detecting...',
    kernel: '...',
    sdk: '...',
    battery: 85,
    temp: 34,
    isCharging: false,
  })

  let monitorTimer = null

  async function initialize() {
    await Promise.all([
      fetchDaemonStatus(),
      fetchProfile(),
      fetchAutoMode(),
      fetchSpecs(),
      fetchModuleVersion(),
    ])
    startMonitoring()
  }

  async function fetchDaemonStatus() {
    try {
      const { errno, stdout } = await KernelSU.exec('/system/bin/toybox pidof sys.azenith-service')
      const pid = stdout.trim()
      if (errno === 0 && pid) {
        daemonPid.value = pid
        daemonStatus.value = 'running'
      } else {
        daemonPid.value = ''
        daemonStatus.value = 'stopped'
      }
    } catch {
      daemonStatus.value = 'stopped'
    }
  }

  async function fetchProfile() {
    try {
      const modeStr = await KernelSU.readFile(`${CONFIG_PATH}/API/current_profile`)
      const trimmed = modeStr.trim()
      if (trimmed === '1' || trimmed.toLowerCase() === 'performance') {
        currentProfile.value = 'performance'
      } else if (trimmed === '3' || trimmed.toLowerCase() === 'eco' || trimmed.toLowerCase() === 'powersave') {
        currentProfile.value = 'eco'
      } else {
        currentProfile.value = 'balanced'
      }
    } catch {
      currentProfile.value = 'balanced'
    }
  }

  async function fetchAutoMode() {
    try {
      const modes = await KernelSU.readFile(`${CONFIG_PATH}/API/current_modes`)
      autoModeEnabled.value = modes.trim() !== '0'
    } catch {
      autoModeEnabled.value = true
    }
  }

  async function toggleAutoMode(val) {
    autoModeEnabled.value = val
    const modeVal = val ? '1' : '0'
    await KernelSU.writeFile(`${CONFIG_PATH}/API/current_modes`, modeVal)
    await KernelSU.exec(`setprop persist.sys.azenithconf.AIenabled ${modeVal}`)
    KernelSU.toast(val ? 'Mode Otomatis (AI) diaktifkan' : 'Mode Otomatis dinonaktifkan (Manual)')
  }

  async function setManualProfile(profile) {
    currentProfile.value = profile
    let code = '2'
    if (profile === 'performance') code = '1'
    if (profile === 'eco') code = '3'

    await KernelSU.writeFile(`${CONFIG_PATH}/API/current_profile`, code)
    
    // Call utility to apply profile immediately
    await KernelSU.exec(`[ -x ${MOD_PATH}/system/bin/sys.azenith-profilesettings ] && ${MOD_PATH}/system/bin/sys.azenith-profilesettings setprofile ${code}`)
    KernelSU.toast(`Profil beralih ke ${profile.toUpperCase()}`)
  }

  async function fetchSpecs() {
    try {
      const [socRes, hwRes, kernRes, sdkRes] = await Promise.all([
        KernelSU.exec('getprop ro.board.platform'),
        KernelSU.exec('getprop ro.hardware'),
        KernelSU.exec('uname -r -m'),
        KernelSU.exec('getprop ro.build.version.sdk'),
      ])

      const socRaw = socRes.stdout.trim() || hwRes.stdout.trim() || 'Generic ARM64'
      deviceSpecs.value.soc = socRaw.toUpperCase()
      deviceSpecs.value.kernel = kernRes.stdout.trim() || 'Linux Kernel'
      deviceSpecs.value.sdk = `Android SDK ${sdkRes.stdout.trim() || '30+'}`

      // Battery
      const { stdout: batOut } = await KernelSU.exec('cat /sys/class/power_supply/battery/capacity 2>/dev/null || dumpsys battery | grep level')
      const batVal = parseInt(batOut.replace(/[^0-9]/g, ''))
      if (!isNaN(batVal) && batVal > 0) {
        deviceSpecs.value.battery = batVal
      }

      // Temp
      const { stdout: tempOut } = await KernelSU.exec('cat /sys/class/power_supply/battery/temp 2>/dev/null')
      const tempVal = parseInt(tempOut.trim())
      if (!isNaN(tempVal)) {
        deviceSpecs.value.temp = tempVal > 100 ? (tempVal / 10).toFixed(0) : tempVal
      }

      // Charging status
      const { stdout: statusOut } = await KernelSU.exec('cat /sys/class/power_supply/battery/status 2>/dev/null')
      deviceSpecs.value.isCharging = statusOut.toLowerCase().includes('charging')
    } catch (e) {
      console.warn('Failed to fetch specs:', e)
    }
  }

  async function fetchModuleVersion() {
    try {
      const prop = await KernelSU.readFile(`${MOD_PATH}/module.prop`)
      const match = prop.match(/^version=(.*)$/m)
      if (match) {
        moduleVersion.value = match[1].trim()
      }
    } catch {
      moduleVersion.value = 'v5.1'
    }
  }

  function startMonitoring() {
    stopMonitoring()
    monitorTimer = setInterval(() => {
      fetchDaemonStatus()
      fetchProfile()
    }, 2000)
  }

  function stopMonitoring() {
    if (monitorTimer) {
      clearInterval(monitorTimer)
      monitorTimer = null
    }
  }

  return {
    daemonStatus,
    daemonPid,
    currentProfile,
    autoModeEnabled,
    moduleVersion,
    deviceSpecs,
    initialize,
    toggleAutoMode,
    setManualProfile,
    fetchDaemonStatus,
    fetchProfile,
    startMonitoring,
    stopMonitoring,
  }
})
