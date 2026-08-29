import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as KernelSU from '@/helpers/KernelSU'

const CONFIG_PATH = '/data/adb/.config/AZenith'
const MOD_PATH = '/data/adb/modules/AZenith'

export const useZenithTweaksStore = defineStore('zenithTweaks', () => {
  // Memory Tuning State
  const memEnabled = ref(true)
  const zramSizeMB = ref(2048)
  const swappiness = ref(140)
  const memAlgo = ref('lz4') // lz4, zstd, lzo, zram
  const isApplyingMem = ref(false)
  const memStatusMsg = ref('')

  // Screen-off ECO State
  const ecoEnabled = ref(true)
  const ecoDelay = ref(300)
  const ecoModeDefault = ref('full') // 'full' | 'restrict'
  const skipCharging = ref(true)
  const skipAudio = ref(true)
  const hibernateApps = ref([])
  const rawHibernateList = ref('')

  // FSTRIM State
  const fstrimEnabled = ref(true)
  const fstrimInterval = ref(86400) // seconds (24h)
  const isRunningFstrim = ref(false)
  const fstrimResult = ref('')

  // Renderer State
  const currentRenderer = ref('skiaglthreaded')

  async function loadAllTweaks() {
    await Promise.all([
      loadMemoryConfig(),
      loadEcoConfig(),
      loadFstrimConfig(),
      loadRendererConfig(),
    ])
  }

  // Memory
  async function loadMemoryConfig() {
    try {
      const [en, sz, sw, al] = await Promise.all([
        KernelSU.readFile(`${CONFIG_PATH}/mem/enabled`),
        KernelSU.readFile(`${CONFIG_PATH}/mem/zram_mb`),
        KernelSU.readFile(`${CONFIG_PATH}/mem/swappiness`),
        KernelSU.readFile(`${CONFIG_PATH}/mem/algo`),
      ])

      memEnabled.value = en.trim() !== '0'
      if (sz.trim()) zramSizeMB.value = parseInt(sz.trim()) || 2048
      if (sw.trim()) swappiness.value = parseInt(sw.trim()) || 140
      if (al.trim()) memAlgo.value = al.trim() || 'lz4'
    } catch (e) {
      console.warn('Failed to load mem config', e)
    }
  }

  async function applyMemoryTuning() {
    isApplyingMem.value = true
    memStatusMsg.value = ''
    try {
      await Promise.all([
        KernelSU.writeFile(`${CONFIG_PATH}/mem/enabled`, memEnabled.value ? '1' : '0'),
        KernelSU.writeFile(`${CONFIG_PATH}/mem/zram_mb`, zramSizeMB.value.toString()),
        KernelSU.writeFile(`${CONFIG_PATH}/mem/swappiness`, swappiness.value.toString()),
        KernelSU.writeFile(`${CONFIG_PATH}/mem/algo`, memAlgo.value),
      ])

      // Run apply-now
      const { stdout } = await KernelSU.exec(`sh ${MOD_PATH}/azenith-memory.sh apply-now`)
      memStatusMsg.value = stdout.trim() || 'Tuning ZRAM & Swappiness berhasil diterapkan!'
      KernelSU.toast('Tuning Memori Berhasil Diterapkan!')
    } catch (e) {
      memStatusMsg.value = `Gagal: ${e.message}`
      KernelSU.toast('Gagal menerapkan tuning memori')
    } finally {
      isApplyingMem.value = false
    }
  }

  // ECO / Hibernation
  async function loadEcoConfig() {
    try {
      const [en, dl, md, sc, sa, list] = await Promise.all([
        KernelSU.readFile(`${CONFIG_PATH}/eco/enabled`),
        KernelSU.readFile(`${CONFIG_PATH}/eco/delay`),
        KernelSU.readFile(`${CONFIG_PATH}/eco/mode.default`),
        KernelSU.readFile(`${CONFIG_PATH}/eco/skip_charging`),
        KernelSU.readFile(`${CONFIG_PATH}/eco/skip_audio`),
        KernelSU.readFile(`${CONFIG_PATH}/eco/hibernate.list`),
      ])

      ecoEnabled.value = en.trim() !== '0'
      if (dl.trim()) ecoDelay.value = parseInt(dl.trim()) || 300
      ecoModeDefault.value = md.trim() === 'restrict' ? 'restrict' : 'full'
      skipCharging.value = sc.trim() !== '0'
      skipAudio.value = sa.trim() !== '0'
      rawHibernateList.value = list.trim()

      const parsedList = list
        .split('\n')
        .map(l => l.trim())
        .filter(l => l && !l.startsWith('#'))
      hibernateApps.value = parsedList
    } catch (e) {
      console.warn('Failed to load ECO config', e)
    }
  }

  async function saveEcoConfig() {
    try {
      await Promise.all([
        KernelSU.writeFile(`${CONFIG_PATH}/eco/enabled`, ecoEnabled.value ? '1' : '0'),
        KernelSU.writeFile(`${CONFIG_PATH}/eco/delay`, ecoDelay.value.toString()),
        KernelSU.writeFile(`${CONFIG_PATH}/eco/mode.default`, ecoModeDefault.value),
        KernelSU.writeFile(`${CONFIG_PATH}/eco/skip_charging`, skipCharging.value ? '1' : '0'),
        KernelSU.writeFile(`${CONFIG_PATH}/eco/skip_audio`, skipAudio.value ? '1' : '0'),
        KernelSU.writeFile(`${CONFIG_PATH}/eco/hibernate.list`, rawHibernateList.value),
      ])
      KernelSU.toast('Pengaturan Hibernasi ECO tersimpan')
    } catch (e) {
      KernelSU.toast('Gagal menyimpan pengaturan ECO')
    }
  }

  // FSTRIM
  async function loadFstrimConfig() {
    try {
      const [en, iv] = await Promise.all([
        KernelSU.readFile(`${CONFIG_PATH}/maint/fstrim_enabled`),
        KernelSU.readFile(`${CONFIG_PATH}/maint/fstrim_interval`),
      ])

      fstrimEnabled.value = en.trim() !== '0'
      if (iv.trim()) fstrimInterval.value = parseInt(iv.trim()) || 86400
    } catch (e) {
      console.warn('Failed to load FSTRIM config', e)
    }
  }

  async function saveFstrimConfig() {
    try {
      await Promise.all([
        KernelSU.writeFile(`${CONFIG_PATH}/maint/fstrim_enabled`, fstrimEnabled.value ? '1' : '0'),
        KernelSU.writeFile(`${CONFIG_PATH}/maint/fstrim_interval`, fstrimInterval.value.toString()),
      ])
    } catch (e) {
      console.warn('Failed to save FSTRIM config', e)
    }
  }

  async function runFstrimNow() {
    isRunningFstrim.value = true
    fstrimResult.value = ''
    try {
      const { stdout } = await KernelSU.exec('fstrim -v /data /cache /system 2>&1 || fstrim -v /data 2>&1')
      fstrimResult.value = stdout.trim() || 'FSTRIM selesai tanpa error.'
      KernelSU.toast('FSTRIM berhasil dieksekusi!')
    } catch (e) {
      fstrimResult.value = `Error: ${e.message}`
      KernelSU.toast('Gagal menjalankan FSTRIM')
    } finally {
      isRunningFstrim.value = false
    }
  }

  // Renderer
  async function loadRendererConfig() {
    try {
      const { stdout } = await KernelSU.exec('getprop persist.sys.azenithconf.renderer')
      const r = stdout.trim()
      currentRenderer.value = r || 'skiaglthreaded'
    } catch {
      currentRenderer.value = 'skiaglthreaded'
    }
  }

  async function setRenderer(rendererName) {
    currentRenderer.value = rendererName
    try {
      await KernelSU.exec(`setprop persist.sys.azenithconf.renderer "${rendererName}"`)
      await KernelSU.exec(`[ -x ${MOD_PATH}/system/bin/sys.azenith-utilityconf ] && ${MOD_PATH}/system/bin/sys.azenith-utilityconf setrender "${rendererName}"`)
      KernelSU.toast(`Renderer disetel ke: ${rendererName}`)
    } catch (e) {
      KernelSU.toast('Gagal mengatur renderer')
    }
  }

  return {
    // Memory
    memEnabled,
    zramSizeMB,
    swappiness,
    memAlgo,
    isApplyingMem,
    memStatusMsg,
    applyMemoryTuning,

    // ECO
    ecoEnabled,
    ecoDelay,
    ecoModeDefault,
    skipCharging,
    skipAudio,
    hibernateApps,
    rawHibernateList,
    saveEcoConfig,

    // FSTRIM
    fstrimEnabled,
    fstrimInterval,
    isRunningFstrim,
    fstrimResult,
    saveFstrimConfig,
    runFstrimNow,

    // Renderer
    currentRenderer,
    setRenderer,

    loadAllTweaks,
  }
})
