import { wrapInputStream, Intent, WebUI } from 'webuix'
import { exec as ksuExec, toast as ksuToast } from 'kernelsu'
import { fileInterface, packageManagerInterface, moduleInterface } from './WXInterfaces'

// In-app mock state for browser dev testing
const mockFileSystem = {
  '/data/adb/modules/AZenith/module.prop': 'id=AZenith\nname=AZenith WebUI Edition\nversion=v5.1-WebUI (RN9)\nversionCode=511\nauthor=wann (fork ArchHaven)',
  '/data/adb/.config/AZenith/API/current_modes': '1',
  '/data/adb/.config/AZenith/API/current_profile': '1',
  '/data/adb/.config/AZenith/gamelist/azenithApplist.json': JSON.stringify({
    'com.mobile.legends': { 'dnd_on_gaming': 'true', 'renderer': 'skiaglthreaded' },
    'com.kurogame.wutheringwaves.global': { 'dnd_on_gaming': 'true', 'renderer': 'skiavkthreaded' },
    'com.HoYoverse.hkrpgoversea': { 'dnd_on_gaming': 'default', 'renderer': 'default' },
    'com.dts.freefireth': { 'dnd_on_gaming': 'false', 'renderer': 'default' }
  }, null, 2),
  '/data/adb/.config/AZenith/mem/enabled': '1',
  '/data/adb/.config/AZenith/mem/zram_mb': '2048',
  '/data/adb/.config/AZenith/mem/swappiness': '140',
  '/data/adb/.config/AZenith/mem/algo': 'lz4',
  '/data/adb/.config/AZenith/eco/enabled': '1',
  '/data/adb/.config/AZenith/eco/delay': '300',
  '/data/adb/.config/AZenith/eco/mode.default': 'full',
  '/data/adb/.config/AZenith/eco/skip_charging': '1',
  '/data/adb/.config/AZenith/eco/skip_audio': '1',
  '/data/adb/.config/AZenith/eco/hibernate.list': 'com.facebook.katana\ncom.instagram.android\ncom.shopee.id\ncom.zhiliaoapp.musically',
  '/data/adb/.config/AZenith/maint/fstrim_enabled': '1',
  '/data/adb/.config/AZenith/maint/fstrim_interval': '86400',
  '/data/adb/.config/AZenith/debug/AZenith.log': '[2026-08-29 15:30:00] [INFO] AZenith Service Initialized\n[2026-08-29 15:30:01] [INFO] Screen-off ECO watcher active (delay: 300s)\n[2026-08-29 15:30:02] [INFO] ZRAM tuned to 2048 MB (lz4, swappiness 140)\n[2026-08-29 15:30:05] [INFO] Focused app changed: com.mobile.legends (PID: 14820)\n[2026-08-29 15:30:05] [INFO] Engaged Performance Profile for com.mobile.legends\n[2026-08-29 15:35:10] [INFO] Focus returned to launcher, switched to Balanced Profile\n',
  '/data/adb/.config/AZenith/sysmon.log': '[SysMon] AppMonitor active on PID 2381\n[SysMon] Monitoring ActivityTaskManager & PowerState\n',
}

const mockInstalledApps = [
  { packageName: 'com.mobile.legends', appName: 'Mobile Legends: Bang Bang' },
  { packageName: 'com.kurogame.wutheringwaves.global', appName: 'Wuthering Waves' },
  { packageName: 'com.HoYoverse.hkrpgoversea', appName: 'Honkai: Star Rail' },
  { packageName: 'com.dts.freefireth', appName: 'Free Fire' },
  { packageName: 'com.pubg.imobile', appName: 'PUBG Mobile' },
  { packageName: 'com.tencent.ig', appName: 'PUBG Mobile Global' },
  { packageName: 'com.miHoYo.GenshinImpact', appName: 'Genshin Impact' },
  { packageName: 'com.whatsapp', appName: 'WhatsApp' },
  { packageName: 'org.telegram.messenger', appName: 'Telegram' },
  { packageName: 'com.instagram.android', appName: 'Instagram' },
  { packageName: 'com.facebook.katana', appName: 'Facebook' },
  { packageName: 'com.shopee.id', appName: 'Shopee' },
  { packageName: 'com.zhiliaoapp.musically', appName: 'TikTok' },
  { packageName: 'com.spotify.music', appName: 'Spotify' },
  { packageName: 'com.google.android.youtube', appName: 'YouTube' }
]

export function isKSUWebUI() {
  return typeof ksu !== 'undefined' || typeof $azenith !== 'undefined' || typeof $encore !== 'undefined'
}

export function isRunningOnWebUIX() {
  return typeof $azenith !== 'undefined' || (typeof $encore !== 'undefined' && Object.keys($encore).length > 0)
}

export async function exec(command) {
  if (!isKSUWebUI()) {
    // Mock simulation for browser preview
    console.log(`[Mock exec] ${command}`)
    
    if (command.includes('pidof sys.azenith-service')) {
      return { errno: 0, stdout: '4192\n', stderr: '' }
    }
    if (command.includes('getprop ro.board.platform')) {
      return { errno: 0, stdout: 'mt6769z (Helio G85)\n', stderr: '' }
    }
    if (command.includes('getprop ro.hardware')) {
      return { errno: 0, stdout: 'mt6768\n', stderr: '' }
    }
    if (command.includes('uname -r -m')) {
      return { errno: 0, stdout: '4.14.336-zenith-perf aarch64\n', stderr: '' }
    }
    if (command.includes('getprop ro.build.version.sdk')) {
      return { errno: 0, stdout: '33 (Android 13)\n', stderr: '' }
    }
    if (command.includes('dumpsys battery') || command.includes('cat /sys/class/power_supply/battery/capacity')) {
      return { errno: 0, stdout: '84', stderr: '' }
    }
    if (command.includes('fstrim')) {
      return { errno: 0, stdout: '/data: 1.2 GiB trimmed\n/cache: 120 MiB trimmed\n/system: 0 B trimmed\n', stderr: '' }
    }
    if (command.includes('azenith-memory.sh apply-now')) {
      return { errno: 0, stdout: '[AZenith Mem] ZRAM reallocated: 2048 MB, vm.swappiness=140, algo=lz4 [OK]\n', stderr: '' }
    }
    if (command.includes('pm list packages -3')) {
      const pkgs = mockInstalledApps.map(a => `package:${a.packageName}`).join('\n')
      return { errno: 0, stdout: pkgs, stderr: '' }
    }
    if (command.startsWith('echo ') && command.includes(' > ')) {
      const match = command.match(/^echo\s+['"]?(.*?)['"]?\s+>\s+["']?(.*?)["']?$/)
      if (match) {
        mockFileSystem[match[2].trim()] = match[1]
      }
      return { errno: 0, stdout: '', stderr: '' }
    }
    return { errno: 0, stdout: 'OK\n', stderr: '' }
  }

  return await ksuExec(command)
}

export async function readFile(filePath) {
  if (!isKSUWebUI()) {
    if (mockFileSystem[filePath] !== undefined) {
      return mockFileSystem[filePath].trim()
    }
    return ''
  }

  if (fileInterface && typeof fileInterface.exists === 'function' && fileInterface.exists(filePath)) {
    return fileInterface.read(filePath).trim()
  }

  const { errno, stdout } = await ksuExec(`[ -f "${filePath}" ] && cat "${filePath}"`)
  if (errno !== 0) return ''
  return stdout.trim()
}

export async function writeFile(filePath, content) {
  if (!isKSUWebUI()) {
    mockFileSystem[filePath] = content
    console.log(`[Mock writeFile] ${filePath} =>`, content)
    return
  }

  if (fileInterface && typeof fileInterface.write === 'function') {
    fileInterface.write(filePath, content)
    return
  }

  const escapedContent = content.replace(/'/g, "'\\''")
  await ksuExec(`mkdir -p "$(dirname "${filePath}")" && echo '${escapedContent}' > "${filePath}"`)
}

export async function fileExists(filePath) {
  if (!isKSUWebUI()) {
    return mockFileSystem[filePath] !== undefined
  }

  if (fileInterface && typeof fileInterface.exists === 'function') {
    return fileInterface.exists(filePath)
  }

  const { errno } = await ksuExec(`[ -f "${filePath}" ]`)
  return errno === 0
}

export function toast(message) {
  if (typeof ksuToast === 'function') {
    ksuToast(message)
    return
  }
  
  if (typeof ksu !== 'undefined' && typeof ksu.toast === 'function') {
    ksu.toast(message)
    return
  }

  // Fallback in-browser toast
  showBrowserToast(message)
}

let toastTimeout = null
function showBrowserToast(msg) {
  let toastEl = document.getElementById('azenith-browser-toast')
  if (!toastEl) {
    toastEl = document.createElement('div')
    toastEl.id = 'azenith-browser-toast'
    toastEl.style.cssText = `
      position: fixed;
      bottom: 84px;
      left: 50%;
      transform: translateX(-50%);
      background: rgba(15, 23, 42, 0.95);
      color: #f8fafc;
      padding: 10px 20px;
      border-radius: 9999px;
      font-size: 13px;
      font-weight: 600;
      border: 1px solid rgba(99, 102, 241, 0.4);
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5), 0 0 15px rgba(99, 102, 241, 0.3);
      z-index: 99999;
      pointer-events: none;
      transition: all 0.25s ease-out;
      opacity: 0;
    `
    document.body.appendChild(toastEl)
  }

  toastEl.innerText = msg
  toastEl.style.opacity = '1'
  toastEl.style.transform = 'translateX(-50%) translateY(0)'

  if (toastTimeout) clearTimeout(toastTimeout)
  toastTimeout = setTimeout(() => {
    toastEl.style.opacity = '0'
    toastEl.style.transform = 'translateX(-50%) translateY(10px)'
  }, 2500)
}

export function createShortcut() {
  if (isRunningOnWebUIX() && moduleInterface?.createShortcut) {
    moduleInterface.createShortcut()
    toast('Shortcut berhasil dibuat!')
    return
  }

  if (typeof ksu !== 'undefined' && typeof ksu.createShortcut === 'function') {
    ksu.createShortcut()
    toast('Shortcut berhasil dibuat!')
    return
  }

  toast('Shortcut tidak didukung di environment ini.')
}

export async function listApps() {
  if (!isKSUWebUI()) {
    return mockInstalledApps.map(a => a.packageName)
  }

  if (typeof ksu !== 'undefined' && typeof ksu.listUserPackages === 'function') {
    return JSON.parse(ksu.listUserPackages())
  }

  const { errno, stdout } = await ksuExec('pm list packages -3')
  if (errno !== 0) return []

  return stdout
    .split('\n')
    .filter(line => line.startsWith('package:'))
    .map(line => line.replace('package:', '').trim())
}

export async function getAppLabel(packageName) {
  if (!isKSUWebUI()) {
    const found = mockInstalledApps.find(a => a.packageName === packageName)
    return found ? found.appName : packageName
  }

  try {
    if (typeof ksu !== 'undefined' && typeof ksu.getPackagesInfo === 'function') {
      const res = JSON.parse(ksu.getPackagesInfo(JSON.stringify([packageName])))
      if (res && res[0] && res[0].appLabel) return res[0].appLabel
    }

    if (packageManagerInterface?.getApplicationInfo) {
      const info = packageManagerInterface.getApplicationInfo(packageName, 0, 0)
      if (info?.getLabel()) return info.getLabel()
    }
  } catch (e) {
    console.warn(`[getAppLabel error] ${packageName}`, e)
  }

  return packageName
}

export async function getAppIcon(packageName) {
  if (!isKSUWebUI()) {
    return ''
  }

  try {
    if (typeof ksu !== 'undefined' && typeof ksu.listPackages !== 'undefined') {
      return `ksu://icon/${packageName}`
    }

    if (packageManagerInterface?.getApplicationIcon) {
      const stream = packageManagerInterface.getApplicationIcon(packageName, 0, 0)
      if (stream) {
        const wrapped = await wrapInputStream(stream)
        const buf = await wrapped.arrayBuffer()
        const b64 = arrayBufferToBase64(buf)
        return `data:image/png;base64,${b64}`
      }
    }
  } catch (e) {
    console.warn(`[getAppIcon error] ${packageName}`, e)
  }

  return ''
}

function arrayBufferToBase64(buffer) {
  let binary = ''
  const bytes = new Uint8Array(buffer)
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i])
  }
  return btoa(binary)
}
