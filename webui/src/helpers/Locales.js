import { ref } from 'vue'

const currentLanguage = ref(localStorage.getItem('azenith_lang') || 'id')

export const translations = {
  id: {
    nav: {
      dashboard: 'Beranda',
      games: 'Game',
      tweaks: 'Optimasi',
      logs: 'Log & Info',
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
      copied: 'Tersalin ke papan klip!',
      clear: 'Bersihkan',
      confirm: 'Konfirmasi',
      edit: 'Ubah',
      enabled: 'Diaktifkan',
      disabled: 'Dinonaktifkan',
      default: 'Bawaan',
    },
    dashboard: {
      title: 'AZenith Optimizer',
      subtitle: 'Universal Dynamic Performance Engine',
      daemonStatus: 'Status Daemon',
      activeProfile: 'Profil Aktif',
      autoModeTitle: 'Mode Otomatis (AI Dynamic)',
      autoModeDesc: 'Deteksi game secara cerdas dan otomatis berpindah ke Profil Performa saat game dibuka.',
      manualSelector: 'Pilih Profil Manual',
      profiles: {
        performance: {
          name: 'Performa',
          badge: 'Gaming',
          desc: 'Performa CPU/GPU maksimal, prioritaskan game, responsivitas tinggi.',
        },
        balanced: {
          name: 'Seimbang',
          badge: 'Harian',
          desc: 'Keseimbangan optimal antara kestabilan sistem dan efisiensi daya harian.',
        },
        eco: {
          name: 'ECO / Hemat',
          badge: 'Hemat Baterai',
          desc: 'Konsumsi daya minimal, batasi frekuensi CPU untuk masa pakai baterai lama.',
        },
      },
      specs: {
        title: 'Info Perangkat & Sistem',
        soc: 'Chipset / SoC',
        kernel: 'Versi Kernel',
        android: 'Android SDK',
        battery: 'Baterai',
        temp: 'Suhu',
        charging: 'Mengisi Daya',
        discharging: 'Baterai Dipakai',
      },
    },
    games: {
      title: 'Daftar Game & Aplikasi',
      subtitle: 'Aplikasi yang memicu Profil Performa secara otomatis',
      addGame: 'Tambah Game',
      noGames: 'Belum ada game yang ditambahkan ke daftar.',
      searchPlaceholder: 'Cari nama atau paket game...',
      gameCount: '{count} Game terdaftar',
      modal: {
        selectApp: 'Pilih Aplikasi Terinstal',
        filterUser: 'Aplikasi Pengguna',
        filterAll: 'Semua Aplikasi',
        saveChanges: 'Simpan Pengaturan Game',
        settingsTitle: 'Pengaturan Game: {name}',
        dndTitle: 'Mode Jangan Ganggu (DND)',
        dndDesc: 'Senyapkan notifikasi otomatis saat game ini berjalan.',
        rendererTitle: 'Renderer Khusus Game',
        rendererDesc: 'Paksa renderer grafis khusus untuk game ini.',
        deletePrompt: 'Hapus {name} dari daftar game?',
      },
    },
    tweaks: {
      title: 'Pengaturan Lanjutan',
      subtitle: 'Tuning sistem mendalam untuk performa & efisiensi',
      memory: {
        title: 'Tuning Memori (ZRAM & Swap)',
        desc: 'Optimasi alokasi ZRAM dan agresivitas swappiness memori.',
        enableToggle: 'Aktifkan Tuning Memori',
        zramSize: 'Ukuran ZRAM (MB)',
        swappiness: 'Swappiness (0 - 200)',
        swappinessDesc: 'Semakin tinggi, sistem semakin aktif memindahkan RAM pasif ke ZRAM.',
        algo: 'Algoritma Kompresi ZRAM',
        applyBtn: 'Terapkan Tuning Memori',
        applying: 'Menerapkan...',
        successToast: 'Tuning memori berhasil diterapkan!',
      },
      hibernate: {
        title: 'Hibernasi Layar-Mati (Screen-Off ECO)',
        desc: 'Bekukan aplikasi latar belakang saat layar mati untuk menghemat baterai.',
        enableToggle: 'Aktifkan Hibernasi Layar Mati',
        modeTitle: 'Mode Hibernasi Default',
        modeFull: 'Full (Restricted + Force Stop - Sangat hemat)',
        modeRestrict: 'Restrict (Restricted saja - Notifikasi FCM tetap masuk)',
        skipCharging: 'Lewati saat mengisi daya',
        skipChargingDesc: 'Jangan hibernasikan aplikasi bila perangkat sedang di-charge.',
        skipAudio: 'Lewati saat audio/musik aktif',
        skipAudioDesc: 'Jangan hibernasikan jika sedang memutar musik layar mati.',
        delay: 'Jeda Hibernasi (Detik)',
        delayDesc: 'Waktu tunggu setelah layar mati sebelum aplikasi dibekukan.',
        manageList: 'Kelola Aplikasi Hibernasi',
      },
      fstrim: {
        title: 'Pemeliharaan Storage (FSTRIM)',
        desc: 'Optimasi partisi storage UFS/eMMC untuk menjaga performa I/O.',
        enableToggle: 'FSTRIM Otomatis Terjadwal',
        interval: 'Selang Waktu FSTRIM (Jam)',
        runNow: 'Jalankan FSTRIM Sekarang',
        running: 'Menjalankan FSTRIM...',
        result: 'Hasil Eksekusi FSTRIM',
      },
      renderer: {
        title: 'Renderer Grafis (HWUI)',
        desc: 'Pilih backend perenderan grafis UI sistem.',
        current: 'Renderer Aktif Saat Ini',
        options: {
          skiaglthreaded: 'SkiaGL Threaded (Direkomendasikan - Paling Lancar)',
          skiagl: 'SkiaGL (OpenGL ES Modern)',
          skiavkthreaded: 'SkiaVK Threaded (Vulkan Multi-Threaded)',
          skiavk: 'SkiaVK (Vulkan Backend)',
          opengl: 'OpenGL Tradisional',
          default: 'Bawaan Sistem ROM',
        },
      },
    },
    logs: {
      title: 'Log Sistem & Informasi',
      subtitle: 'Pantau aktivitas modul dan informasi versi',
      viewerTitle: 'Live Log AZenith',
      autoRefresh: 'Pembaruan Otomatis',
      clearLogs: 'Bersihkan Log',
      copyLogs: 'Salin Log',
      emptyLog: 'Belum ada catatan log.',
      shortcutBtn: 'Buat Shortcut di Layar Utama',
      aboutTitle: 'Tentang AZenith',
      version: 'Versi Modul',
      authors: 'Pengembang & Kontributor',
      license: 'Lisensi',
      licenseText: 'Apache License 2.0 - Dibuat untuk komunitas Android root.',
    },
  },
  en: {
    nav: {
      dashboard: 'Dashboard',
      games: 'Games',
      tweaks: 'Tweaks',
      logs: 'Logs & Info',
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
      default: 'Default',
    },
    dashboard: {
      title: 'AZenith Optimizer',
      subtitle: 'Universal Dynamic Performance Engine',
      daemonStatus: 'Daemon Status',
      activeProfile: 'Active Profile',
      autoModeTitle: 'Auto Profile (AI Dynamic)',
      autoModeDesc: 'Intelligently detects foreground games and automatically engages Performance Mode.',
      manualSelector: 'Manual Profile Selection',
      profiles: {
        performance: {
          name: 'Performance',
          badge: 'Gaming',
          desc: 'Maximum CPU/GPU output, foreground game priority, ultra-high responsiveness.',
        },
        balanced: {
          name: 'Balanced',
          badge: 'Daily',
          desc: 'Ideal balance between snappy daily performance and battery endurance.',
        },
        eco: {
          name: 'ECO / Battery',
          badge: 'Power Save',
          desc: 'Minimal battery drain, limits high frequencies for extended battery life.',
        },
      },
      specs: {
        title: 'Device & System Telemetry',
        soc: 'Chipset / SoC',
        kernel: 'Kernel Version',
        android: 'Android SDK',
        battery: 'Battery Level',
        temp: 'Temperature',
        charging: 'Charging',
        discharging: 'Discharging',
      },
    },
    games: {
      title: 'Games & App Registry',
      subtitle: 'Configured applications that trigger Performance Mode',
      addGame: 'Add Game',
      noGames: 'No games added to registry yet.',
      searchPlaceholder: 'Search game title or package...',
      gameCount: '{count} Games configured',
      modal: {
        selectApp: 'Select Installed App',
        filterUser: 'User Apps',
        filterAll: 'All Apps',
        saveChanges: 'Save Game Config',
        settingsTitle: 'Game Settings: {name}',
        dndTitle: 'Do Not Disturb (DND)',
        dndDesc: 'Automatically mute notifications while this game is active.',
        rendererTitle: 'Custom Game Renderer',
        rendererDesc: 'Override HWUI graphics renderer for this game.',
        deletePrompt: 'Remove {name} from game list?',
      },
    },
    tweaks: {
      title: 'Advanced Tweaks',
      subtitle: 'Deep kernel & memory tuning for maximum efficiency',
      memory: {
        title: 'Memory Tuning (ZRAM & Swap)',
        desc: 'Optimize ZRAM size, swappiness aggressiveness, and compression algorithm.',
        enableToggle: 'Enable Memory Tuning',
        zramSize: 'ZRAM Size (MB)',
        swappiness: 'Swappiness (0 - 200)',
        swappinessDesc: 'Higher values aggressively swap inactive anonymous pages into ZRAM.',
        algo: 'Compression Algorithm',
        applyBtn: 'Apply Memory Settings',
        applying: 'Applying...',
        successToast: 'Memory tuning applied successfully!',
      },
      hibernate: {
        title: 'Screen-Off ECO Hibernation',
        desc: 'Freeze idle background apps when screen is turned off to save battery.',
        enableToggle: 'Enable Screen-Off Hibernation',
        modeTitle: 'Default Hibernation Mode',
        modeFull: 'Full (Restricted + Force Stop - Maximum power save)',
        modeRestrict: 'Restrict (Restricted only - Keeps FCM push notifications)',
        skipCharging: 'Skip while charging',
        skipChargingDesc: 'Do not hibernate background apps if device is plugged in.',
        skipAudio: 'Skip while audio is active',
        skipAudioDesc: 'Prevent hibernation if music/podcast is playing with screen off.',
        delay: 'Hibernation Delay (Seconds)',
        delayDesc: 'Cooldown time after screen lock before hibernating apps.',
        manageList: 'Manage Hibernation App List',
      },
      fstrim: {
        title: 'Storage Maintenance (FSTRIM)',
        desc: 'Trim flash storage partitions to maintain fast write & read speeds.',
        enableToggle: 'Scheduled Auto FSTRIM',
        interval: 'FSTRIM Interval (Hours)',
        runNow: 'Run FSTRIM Now',
        running: 'Executing FSTRIM...',
        result: 'FSTRIM Result',
      },
      renderer: {
        title: 'HWUI Graphics Renderer',
        desc: 'Select the Android system UI 2D rendering pipeline.',
        current: 'Currently Active Renderer',
        options: {
          skiaglthreaded: 'SkiaGL Threaded (Recommended - Smooth & Fast)',
          skiagl: 'SkiaGL (Standard OpenGL ES)',
          skiavkthreaded: 'SkiaVK Threaded (Vulkan Multi-Threaded)',
          skiavk: 'SkiaVK (Vulkan Pipeline)',
          opengl: 'Legacy OpenGL',
          default: 'System ROM Default',
        },
      },
    },
    logs: {
      title: 'System Logs & About',
      subtitle: 'Inspect daemon activity and module details',
      viewerTitle: 'Live AZenith Logs',
      autoRefresh: 'Auto Refresh',
      clearLogs: 'Clear Logs',
      copyLogs: 'Copy Logs',
      emptyLog: 'No log output available yet.',
      shortcutBtn: 'Add Homescreen Shortcut',
      aboutTitle: 'About AZenith',
      version: 'Module Version',
      authors: 'Developers & Contributors',
      license: 'License',
      licenseText: 'Apache License 2.0 - Built for the root Android community.',
    },
  },
}

export function useLocales() {
  const setLanguage = (lang) => {
    currentLanguage.value = lang
    localStorage.setItem('azenith_lang', lang)
  }

  const t = (path, params = {}) => {
    const keys = path.split('.')
    let current = translations[currentLanguage.value] || translations['id']
    for (const key of keys) {
      if (!current || current[key] === undefined) {
        // Fallback to English
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
