import { ref } from 'vue'

const currentLanguage = ref(localStorage.getItem('wann_lang') || 'id')

export const translations = {
  id: {
    nav: {
      dashboard: 'Beranda',
      games: 'Permainan',
      tweaks: 'Pengaturan',
    },
    common: {
      active: 'Aktif',
      inactive: 'Nonaktif',
      running: 'Berjalan',
      stopped: 'Berhenti',
      loading: 'Memuat...',
      save: 'Simpan',
      cancel: 'Batal',
      apply: 'Terapkan',
      applied: 'Diterapkan',
      delete: 'Hapus',
      search: 'Cari aplikasi...',
      refresh: 'Segarkan',
      copied: 'Tersalin ke clipboard!',
      clear: 'Bersihkan',
      confirm: 'Konfirmasi',
      edit: 'Ubah',
      enabled: 'Diaktifkan',
      disabled: 'Dinonaktifkan',
      default: 'Bawaan Sistem',
      unknown: 'Tidak diketahui',
      back: 'Kembali',
    },
    home_page: {
      title: 'Wann Tweaks',
      status_card: {
        running: 'Wann Daemon Aktif',
        stopped: 'Wann Daemon Nonaktif',
        loading: 'Memeriksa status daemon...',
        daemonPID: 'PID: {pid}',
        daemon_inactive: 'Daemon belum berjalan',
      },
      info_card: {
        module: 'Versi Modul',
        profile: 'Profil Aktif',
        kernel: 'Versi Kernel',
        chipset: 'Platform SoC',
        androidSDK: 'Android SDK',
      },
      profiles: {
        performance: 'Mode Performa (Gaming)',
        balanced: 'Mode Seimbang (Harian)',
        eco: 'Mode ECO (Hemat Baterai)',
        initializing: 'Menyiapkan...',
      },
      auto_mode: {
        title: 'Mode Otomatis (AI Dynamic)',
        description: 'Mendeteksi game secara otomatis dan mengaktifkan profil performa.',
      },
      support_button: {
        title: 'Dukung Pengembang',
        description: 'Dibuat dengan dedikasi oleh @wann untuk komunitas Android root.',
      },
    },
    games_page: {
      title: 'Daftar Permainan',
      search_placeholder: 'Cari aplikasi...',
      no_apps_found: 'Aplikasi tidak ditemukan.',
      add_game: 'Tambah Game',
      select_app: 'Pilih Aplikasi Terinstal',
      badges: {
        tweak_enabled: 'Optimal',
      },
      settings: {
        title: 'Pengaturan Game: {name}',
        enable_tweak: 'Optimasi Game Ini',
        enable_tweak_desc: 'Aktifkan profil performa khusus saat game ini dibuka.',
        dnd: 'Jangan Ganggu (DND)',
        dnd_desc: 'Senyapkan notifikasi otomatis selama bermain game.',
        renderer: 'Renderer Grafis',
        renderer_desc: 'Paksa renderer grafis khusus untuk aplikasi ini.',
      },
    },
    settings_page: {
      title: 'Pengaturan',
      section: {
        tweaks: 'Optimasi Performa',
        system: 'Sistem & Perangkat',
        others: 'Lainnya',
      },
      memory: {
        title: 'Tuning Memori (ZRAM & Swap)',
        description: 'Konfigurasi ukuran ZRAM, swappiness, dan algoritma kompresi.',
        size: 'Ukuran ZRAM',
        swappiness: 'Agresivitas Swap (Swappiness)',
        algo: 'Algoritma Kompresi',
        apply_btn: 'Terapkan Tuning Memori',
      },
      hibernate: {
        title: 'Hibernasi Layar-Mati (Screen-Off ECO)',
        description: 'Bekukan aplikasi latar belakang saat layar mati untuk hemat baterai.',
        mode: 'Mode Hibernasi',
        mode_full: 'Full (Restricted + Force Stop)',
        mode_restrict: 'Restrict (Notifikasi FCM tetap masuk)',
        skip_charging: 'Lewati saat mengisi daya',
        skip_audio: 'Lewati saat audio/musik aktif',
        delay: 'Jeda Waktu Hibernasi',
        list: 'Daftar Paket Hibernasi',
      },
      renderer: {
        title: 'Renderer Grafis (HWUI)',
        description: 'Ubah backend renderer 2D UI sistem (SkiaGL / Vulkan / OpenGL).',
      },
      fstrim: {
        title: 'Pemeliharaan Storage (FSTRIM)',
        description: 'Optimasi kecepatan baca/tulis partisi flash memory.',
        run_now: 'Jalankan FSTRIM Sekarang',
      },
      logs: {
        title: 'Lihat Log Modul',
        description: 'Pantau aktivitas real-time daemon dan sistem monitoring.',
      },
      shortcut: {
        title: 'Buat Shortcut di Layar Utama',
        description: 'Tambahkan ikon Wann WebUI ke homescreen perangkat.',
      },
      language: {
        title: 'Bahasa / Language',
      },
      about: {
        title: 'Tentang Wann',
        description: 'Wann Optimizer RN9 Edition - Dibuat oleh @wann.',
      },
    },
  },
  en: {
    nav: {
      dashboard: 'Home',
      games: 'Games',
      tweaks: 'Settings',
    },
    common: {
      active: 'Active',
      inactive: 'Inactive',
      running: 'Running',
      stopped: 'Stopped',
      loading: 'Loading...',
      save: 'Save',
      cancel: 'Cancel',
      apply: 'Apply',
      applied: 'Applied',
      delete: 'Delete',
      search: 'Search apps...',
      refresh: 'Refresh',
      copied: 'Copied to clipboard!',
      clear: 'Clear',
      confirm: 'Confirm',
      edit: 'Edit',
      enabled: 'Enabled',
      disabled: 'Disabled',
      default: 'System Default',
      unknown: 'Unknown',
      back: 'Back',
    },
    home_page: {
      title: 'Wann Tweaks',
      status_card: {
        running: 'Wann Daemon Active',
        stopped: 'Wann Daemon Inactive',
        loading: 'Checking daemon status...',
        daemonPID: 'PID: {pid}',
        daemon_inactive: 'Daemon is not running',
      },
      info_card: {
        module: 'Module Version',
        profile: 'Active Profile',
        kernel: 'Kernel Version',
        chipset: 'SoC Platform',
        androidSDK: 'Android SDK',
      },
      profiles: {
        performance: 'Performance Mode (Gaming)',
        balanced: 'Balanced Mode (Daily)',
        eco: 'ECO Mode (Power Save)',
        initializing: 'Initializing...',
      },
      auto_mode: {
        title: 'Auto Profile (AI Dynamic)',
        description: 'Automatically detects foreground games and applies performance profile.',
      },
      support_button: {
        title: 'Support Developer',
        description: 'Crafted with dedication by @wann for the root Android community.',
      },
    },
    games_page: {
      title: 'Games Registry',
      search_placeholder: 'Search apps...',
      no_apps_found: 'No apps found.',
      add_game: 'Add Game',
      select_app: 'Select Installed App',
      badges: {
        tweak_enabled: 'Optimized',
      },
      settings: {
        title: 'Game Settings: {name}',
        enable_tweak: 'Optimize This Game',
        enable_tweak_desc: 'Engage performance profile when this game is foreground.',
        dnd: 'Do Not Disturb (DND)',
        dnd_desc: 'Mute notifications automatically while playing this game.',
        renderer: 'Graphics Renderer',
        renderer_desc: 'Force custom 2D renderer for this application.',
      },
    },
    settings_page: {
      title: 'Settings',
      section: {
        tweaks: 'Performance Tweaks',
        system: 'System & Hardware',
        others: 'Others',
      },
      memory: {
        title: 'Memory Tuning (ZRAM & Swap)',
        description: 'Configure ZRAM capacity, swappiness aggressiveness, and compression.',
        size: 'ZRAM Size',
        swappiness: 'Swappiness',
        algo: 'Compression Algorithm',
        apply_btn: 'Apply Memory Settings',
      },
      hibernate: {
        title: 'Screen-Off ECO Hibernation',
        description: 'Freeze idle background apps on screen lock to conserve battery.',
        mode: 'Hibernation Mode',
        mode_full: 'Full (Restricted + Force Stop)',
        mode_restrict: 'Restrict (Retains FCM push notifications)',
        skip_charging: 'Skip while charging',
        skip_audio: 'Skip while audio is playing',
        delay: 'Hibernation Delay',
        list: 'Hibernation Package List',
      },
      renderer: {
        title: 'HWUI Graphics Renderer',
        description: 'Change system UI 2D rendering pipeline (SkiaGL / Vulkan / OpenGL).',
      },
      fstrim: {
        title: 'Storage Maintenance (FSTRIM)',
        description: 'Trim flash storage partitions to maintain fast I/O throughput.',
        run_now: 'Run FSTRIM Now',
      },
      logs: {
        title: 'Module Live Logs',
        description: 'Inspect real-time daemon logs and monitor telemetry.',
      },
      shortcut: {
        title: 'Add Homescreen Shortcut',
        description: 'Add Wann WebUI icon directly to your launcher.',
      },
      language: {
        title: 'Language',
      },
      about: {
        title: 'About Wann',
        description: 'Wann Optimizer RN9 Edition - Built by @wann.',
      },
    },
  },
}

export function useLocales() {
  const setLanguage = (lang) => {
    currentLanguage.value = lang
    localStorage.setItem('wann_lang', lang)
  }

  const t = (path, params = {}) => {
    const keys = path.split('.')
    let current = translations[currentLanguage.value] || translations['id']
    for (const key of keys) {
      if (!current || current[key] === undefined) {
        let fallback = translations['en']
        for (const fbKey of keys) {
          if (!fallback || fallback[fbKey] === undefined) return path
          fallback = fallback[fbKey]
        }
        return replaceParams(fallback, params)
      }
      current = current[key]
    }
    return replaceParams(current, params)
  }

  function replaceParams(str, params) {
    if (typeof str !== 'string') return str
    return str.replace(/{(\w+)}/g, (_, k) => params[k] !== undefined ? params[k] : `{${k}}`)
  }

  return {
    currentLanguage,
    setLanguage,
    t,
  }
}
