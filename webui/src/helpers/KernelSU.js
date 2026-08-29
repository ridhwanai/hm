import { wrapInputStream, Intent, WebUI } from 'webuix'
import { exec as ksuExec, toast as ksuToast } from 'kernelsu'
import { fileInterface, packageManagerInterface, moduleInterface } from './WXInterfaces'

// In-app mock state for browser dev testing
const mockFileSystem = {
  '/data/adb/modules/wann/module.prop': 'id=wann\nname=Wann Optimizer\nversion=v5.1-WebUI (RN9)\nversionCode=511\nauthor=wann',
  '/data/adb/.config/wann/API/current_modes': '1',
  '/data/adb/.config/wann/API/current_profile': '1',
  '/data/adb/.config/wann/gamelist/wannApplist.json': JSON.stringify({
    'com.mobile.legends': { 'dnd_on_gaming': 'true', 'renderer': 'skiaglthreaded' },
    'com.kurogame.wutheringwaves.global': { 'dnd_on_gaming': 'true', 'renderer': 'skiavkthreaded' },
    'com.HoYoverse.hkrpgoversea': { 'dnd_on_gaming': 'default', 'renderer': 'default' },
  }, null, 2),
  '/data/adb/.config/wann/mem/enabled': '1',
  '/data/adb/.config/wann/mem/zram_mb': '2048',
  '/data/adb/.config/wann/mem/swappiness': '140',
  '/data/adb/.config/wann/mem/algo': 'lz4',
  '/data/adb/.config/wann/eco/enabled': '1',
  '/data/adb/.config/wann/eco/delay': '300',
  '/data/adb/.config/wann/eco/mode.default': 'full',
  '/data/adb/.config/wann/eco/skip_charging': '1',
  '/data/adb/.config/wann/eco/skip_audio': '1',
  '/data/adb/.config/wann/eco/hibernate.list': 'com.facebook.katana\ncom.instagram.android\ncom.shopee.id\ncom.zhiliaoapp.musically',
  '/data/adb/.config/wann/maint/fstrim_enabled': '1',
  '/data/adb/.config/wann/maint/fstrim_interval': '86400',
  '/data/adb/.config/wann/debug/wann.log': '[INFO] Wann Service active\n[INFO] Screen-off ECO watcher active (delay: 300s)\n[INFO] ZRAM tuned to 2048 MB (lz4, swappiness 140)\n[INFO] Focused app: com.mobile.legends (Performance profile applied)\n',
  '/data/adb/.config/wann/sysmon.log': '[SysMon] AppMonitor active on PID 2381\n',
}

const mockInstalledApps = [
  { packageName: 'com.mobile.legends', appName: 'Mobile Legends: Bang Bang' },
  { packageName: 'com.kurogame.wutheringwaves.global', appName: 'Wuthering Waves' },
  { packageName: 'com.HoYoverse.hkrpgoversea', appName: 'Honkai: Star Rail' },
  { packageName: 'com.dts.freefireth', appName: 'Free Fire' },
  { packageName: 'com.pubg.imobile', appName: 'PUBG Mobile' },
  { packageName: 'com.whatsapp', appName: 'WhatsApp' },
  { packageName: 'org.telegram.messenger', appName: 'Telegram' },
  { packageName: 'com.instagram.android', appName: 'Instagram' },
  { packageName: 'com.facebook.katana', appName: 'Facebook' },
]

export function isKSUWebUI() {
  return typeof ksu !== 'undefined' || typeof $wann !== 'undefined' || typeof $azenith !== 'undefined' || typeof $encore !== 'undefined'
}

export function isRunningOnWebUIX() {
  return typeof $wann !== 'undefined' || typeof $azenith !== 'undefined' || (typeof $encore !== 'undefined' && Object.keys($encore).length > 0)
}

export async function exec(command) {
  if (!isKSUWebUI()) {
    console.log(`[Mock exec] ${command}`)
    if (command.includes('pidof sys.azenith-service') || command.includes('pidof sys.wann-service')) return { errno: 0, stdout: '4192\n', stderr: '' }
    if (command.includes('getprop ro.board.platform')) return { errno: 0, stdout: 'mt6769z (Helio G85)\n', stderr: '' }
    if (command.includes('uname -r -m')) return { errno: 0, stdout: '4.14.336-zenith aarch64\n', stderr: '' }
    if (command.includes('getprop ro.build.version.sdk')) return { errno: 0, stdout: '33\n', stderr: '' }
    if (command.includes('fstrim')) return { errno: 0, stdout: '/data: 1.2 GiB trimmed\n/cache: 120 MiB trimmed\n', stderr: '' }
    if (command.includes('wann-memory.sh apply-now') || command.includes('azenith-memory.sh apply-now')) return { errno: 0, stdout: 'ZRAM: 2048 MB, swappiness: 140 [OK]\n', stderr: '' }
    if (command.includes('pm list packages -3')) {
      return { errno: 0, stdout: mockInstalledApps.map(a => `package:${a.packageName}`).join('\n'), stderr: '' }
    }
    return { errno: 0, stdout: 'OK\n', stderr: '' }
  }

  return await ksuExec(command)
}

export async function readFile(filePath) {
  if (!isKSUWebUI()) {
    return (mockFileSystem[filePath] || '').trim()
  }

  if (fileInterface && typeof fileInterface.exists === 'function' && fileInterface.exists(filePath)) {
    return (fileInterface.read(filePath) || '').trim()
  }

  try {
    const { errno, stdout } = await ksuExec(`[ -f "${filePath}" ] && cat "${filePath}"`)
    if (errno !== 0) return ''
    return (stdout || '').trim()
  } catch {
    return ''
  }
}

export async function writeFile(filePath, content) {
  if (!isKSUWebUI()) {
    mockFileSystem[filePath] = content
    return
  }

  if (fileInterface && typeof fileInterface.write === 'function') {
    fileInterface.write(filePath, content)
    return
  }

  const escapedContent = content.replace(/'/g, "'\\''")
  await ksuExec(`mkdir -p "$(dirname "${filePath}")" && echo '${escapedContent}' > "${filePath}"`)
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
  showBrowserToast(message)
}

let toastTimeout = null
function showBrowserToast(msg) {
  let toastEl = document.getElementById('wann-toast')
  if (!toastEl) {
    toastEl = document.createElement('div')
    toastEl.id = 'wann-toast'
    toastEl.style.cssText = `
      position: fixed;
      bottom: 84px;
      left: 50%;
      transform: translateX(-50%);
      background: #1e1f25;
      color: #e2e2e9;
      padding: 8px 16px;
      border-radius: 9999px;
      font-size: 12px;
      font-weight: 500;
      border: 1px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
      z-index: 99999;
      pointer-events: none;
      transition: all 0.2s ease-out;
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
    toastEl.style.transform = 'translateX(-50%) translateY(8px)'
  }, 2000)
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
  toast('Fitur shortcut tidak didukung di environment ini.')
}

export function openWebsite(link) {
  if (isRunningOnWebUIX()) {
    try {
      const webui = new WebUI()
      const intent = new Intent(Intent.ACTION_VIEW)
      intent.setData(link)
      webui.startActivity(intent)
      return
    } catch {}
  }
  ksuExec(`am start -a android.intent.action.VIEW -d "${link}"`)
}

export async function listApps() {
  if (!isKSUWebUI()) return mockInstalledApps.map(a => a.packageName)

  if (typeof ksu !== 'undefined' && typeof ksu.listUserPackages === 'function') {
    try {
      return JSON.parse(ksu.listUserPackages())
    } catch {}
  }

  try {
    const { errno, stdout } = await ksuExec('pm list packages -3')
    if (errno !== 0) return []
    return stdout.split('\n')
      .filter(l => l.startsWith('package:'))
      .map(l => l.replace('package:', '').trim())
  } catch {
    return []
  }
}

export async function getAppLabel(packageName) {
  if (!isKSUWebUI()) {
    const found = mockInstalledApps.find(a => a.packageName === packageName)
    return found ? found.appName : packageName
  }

  try {
    if (typeof ksu !== 'undefined' && typeof ksu.getPackagesInfo === 'function') {
      const res = JSON.parse(ksu.getPackagesInfo(JSON.stringify([packageName])))
      if (res?.[0]?.appLabel) return res[0].appLabel
    }
    if (packageManagerInterface?.getApplicationInfo) {
      const info = packageManagerInterface.getApplicationInfo(packageName, 0, 0)
      if (info?.getLabel()) return info.getLabel()
    }
  } catch {}

  return packageName
}

export async function getAppIcon(packageName) {
  if (!isKSUWebUI()) return ''
  try {
    if (typeof ksu !== 'undefined' && typeof ksu.listPackages !== 'undefined') {
      return `ksu://icon/${packageName}`
    }
    if (packageManagerInterface?.getApplicationIcon) {
      const stream = packageManagerInterface.getApplicationIcon(packageName, 0, 0)
      if (stream) {
        const wrapped = await wrapInputStream(stream)
        const buf = await wrapped.arrayBuffer()
        return `data:image/png;base64,${arrayBufferToBase64(buf)}`
      }
    }
  } catch {}
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
