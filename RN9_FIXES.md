# AZenith 5.1 RN9 Edition — Catatan Perbaikan

Dokumen ini merangkum perbaikan build + penghapusan fitur untuk fork Redmi Note 9 (Helio G85).

## 1. Perbaikan error install (KernelSU Next)

**Gejala:** instalasi dibatalkan dengan pesan:
```
! azenith-drainmon.sh does not exists
! Installation aborted. The module may be corrupted.
```
**Akar masalah:** `mainfiles/customize.sh` mencoba meng-extract + cek integritas `azenith-drainmon.sh` dan `azenith-report`, padahal kedua file itu TIDAK pernah ikut dipaketkan ke ZIP (tidak ada di daftar `need_integrity` pada `.github/scripts/compile_zip.sh`).

**Perbaikan:** referensi ke `azenith-drainmon.sh` / `azenith-report` dihapus dari jalur instalasi. `customize.sh` sekarang hanya meng-extract helper yang benar-benar dipaketkan (`azenith-hibernate.sh`) beserta cek integritasnya. Tidak ada lagi file hilang saat install.

## 2. Perbaikan warning build (Rust)

Semua warning `unused` dari `cargo` dihapus dengan membuang dead-code:
- **binprofiles:** `which_maxfreq/minfreq/midfreq`, `devfreq_max/mid/min_perf`, `devfreq_unlock`, `clear_background_apps` (+ import `HashSet`), serta `init_maligpu_governor` / `sets_mali_gov`.
- **binutils:** import `std::thread` & `std::time::Duration`, fungsi `execute_command`, `get_debugmode`, `verbose`/`write_verbose`, serta helper Mali (`sets_mali_gov`, `check_mali_path`).

Tidak ada import/fungsi menggantung yang tersisa (sudah diverifikasi statis).

## 3. Fitur yang dihapus (tampilan + sistem)

| Fitur | Status |
|---|---|
| GPU Mali scheduling | Dihapus penuh (UI TweakScreen + state/fungsi ViewModel + backend Rust) |
| Laporan boros baterai (drain report) | Dihapus (monitor drain, CSV sample, extraction di customize.sh) |
| Checklist "Tampilkan aplikasi sistem" | Dihapus dari UI |
| Batasi frekuensi (cpulimit / lite mode) | Dihapus dari manager (state `liteState`, `updateLiteMode`, prop backup); limiter daemon sudah dikunci 100% |
| Refresh rate | Telemetry + key per-app dihapus dari manager |
| Preload permainan | Key per-app inert dihapus dari manager |
| Kontrol prioritas aplikasi | Key per-app inert dihapus dari manager |
| Frame aware, fpsgo, ged, pembersih memori (drop_caches), fstrim, thermalcore, skema layar, sched tuner, walt, latensi surfaceflinger, jit, nonaktif trace, nonaktif termal | Sudah tidak punya UI/backend aktif (dinetralkan di pekerjaan RN9 sebelumnya) — diverifikasi tidak ada sisa tampilan/efek |

Catatan: struktur GameConfig di C-daemon (`archdaemon`) dan beberapa key JSON per-app masih ada namun **inert** (nilai selalu `default` → `IS_DEFAULT` → no-op, tanpa UI). Ini sengaja dibiarkan agar `ndk-build` tetap aman. Bisa dieksisi lebih dalam sebagai lanjutan bila diminta.

## 4. Penyesuaian

- **Hibernasi:** tiap baris aplikasi sekarang memakai tombol/toggle aktif (`Switch`) seperti layar Performance, bukan checklist.
- **Tweak → Renderer:** tersedia pemilih render (SkiaGL, SkiaVK, SkiaGL Threaded, SkiaVK Threaded, OpenGL, Default) yang menulis properti render lalu me-restart service.

## 5. Verifikasi yang disarankan

```bash
# Sintaks skrip shell
bash -n mainfiles/customize.sh
bash -n mainfiles/azenith-hibernate.sh
bash -n mainfiles/service.sh
bash -n mainfiles/uninstall.sh

# Pastikan tidak ada sisa referensi file yang hilang
grep -RIn 'drainmon\|azenith-report' mainfiles .github || echo CLEAN

# Build Rust (harus tanpa warning)
cd binprofiles && cargo build --release && cd ..
cd binutils && cargo build --release && cd ..

# Build manager APK
cd manager && chmod +x ./gradlew && ./gradlew clean assembleRelease && cd ..
```

Atau cukup jalankan GitHub Actions workflow untuk membangun APK + ZIP flashable sekaligus.

> Catatan: perubahan diverifikasi secara statis (brace balance, tidak ada referensi menggantung, tidak ada import tak terpakai baru). Kompilasi penuh sebaiknya dikonfirmasi lewat workflow karena toolchain Android/Rust tidak dijalankan saat penyuntingan.

---

## Penambahan fitur performa & hibernasi (RN9)

### 1. Hibernasi per-app + kondisi (`mainfiles/azenith-hibernate.sh`)
- **Mode per-app**: `full` (Restricted + force-stop, hemat maksimal, notif mati) atau `restrict` (Restricted TANPA force-stop, app tetap hidup jadi notif FCM masih bisa masuk — cocok untuk chat).
  - Config: `eco/mode.default` (default `full`) dan `eco/mode.list` (baris `paket mode`, mis. `com.whatsapp restrict`).
- **Kondisi tunda**: `eco/skip_charging` (default 1 — tidak hibernasi saat ngecas) dan `eco/skip_audio` (default 1 — tidak hibernasi saat ada audio aktif, mis. musik layar-mati).
- `thaw` tetap mengembalikan app ke Baterai "Dioptimalkan".

### 2. Tuning memori ZRAM + swappiness (`mainfiles/azenith-memory.sh`, baru)
- Resize ZRAM (default 50% RAM, min 512, maks 3072 MB, algo `lz4`) + set `vm.swappiness` (default 140).
- Config: `mem/enabled`, `mem/zram_mb`, `mem/swappiness`, `mem/algo`. Dijalankan async oleh `service.sh` ~20 dtk setelah boot.

### 3. read_ahead_kb profil Performance (`binprofiles/src/profiles/mod.rs`)
- Profil Performance kini set `read_ahead_kb=128` (dulu 32) dan `nr_requests=64` — mempercepat baca eMMC. **Butuh build ulang Rust** agar aktif di flashable.

### 4. fstrim terjadwal async (`mainfiles/azenith-fstrim.sh`, baru)
- Trim berjalan ~5 menit setelah boot (async, tanpa ANR) lalu berkala tiap 24 jam.
- Config: `maint/fstrim_enabled`, `maint/fstrim_delay`, `maint/fstrim_interval`.

### 5. Renderer default SkiaGL Multi-threaded
- Default install kini `skiaglthreaded` (`customize.sh`). Karena `debug.hwui.*` reset tiap reboot, `service.sh` menerapkan ulang renderer tersimpan saat boot via `sys.azenith-utilityconf setrender`.

> Catatan menu (APK): logika di atas dikendalikan lewat file config sehingga langsung berfungsi setelah flash. Untuk toggle yang tampil di UI manager (Compose/Kotlin) perlu penambahan di `manager/` + build ulang APK; backend & default sudah disiapkan agar tinggal disambungkan.

---

## Patch RN9 v2 - Latensi profil + UI Pengaturan Lanjutan

### 1. Profil Performa lambat aktif saat aplikasi dibuka
Penyebab (archdaemon/jni/src/System/System.c):
- PID aplikasi hanya dicari lewat get_pids_of() yang membaca file background_apps.
  File itu diisi manager dari getRecentTasks(30), dan aplikasi yang BARU dibuka
  sering belum masuk daftar recent, jadi PID = 0.
- Saat PID = 0, daemon retry 5x tapi need_loop = true membuat poll_timeout = 0,
  sehingga 5 retry habis dalam hitungan mikrodetik. Aplikasi lalu di-drop dan
  baru dicoba lagi pada event inotify berikutnya (0.5-2 detik kemudian).

Perbaikan:
- Fast path: pakai current_system_cache.focused_pid dari app_status bila
  get_pids_of() kosong dan aplikasi fokus sama dengan aplikasi target.
- Retry kini punya jeda nyata usleep(120000), bukan terbakar instan.

### 2. Lambat kembali ke profil default setelah aplikasi ditutup
Penyebab: di blok need_profile_checkup, saat get_gamestart() mengembalikan NULL,
daemon hanya menyetel need_profile_checkup = false tanpa membersihkan gamestart.
Karena gamestart masih terisi, cabang di bawahnya terus masuk ke jalur
Performance. Profil baru turun setelah handle_background_apps_event() melihat PID
hilang, yaitu setelah Android benar-benar mematikan cached process.

Perbaikan: bila fokus sudah pindah ke aplikasi lain, gamestart, active_app_name,
game_pid_count, pid_retries, has_applied_renderer, dan grace_period_active
langsung direset sehingga Balanced diterapkan seketika.

### 3. Deteksi pergantian aplikasi (manager)
AppMonitor.kt:
- POLL_INTERVAL_MS 500 -> 150 ms.
- writeBackgroundApps() (mahal, getRecentTasks) dipisah ke cadence 750 ms atau
  saat status berubah, jadi poll cepat tidak menambah beban.
- Deteksi perubahan status kini mengabaikan baris battery_* yang selalu berubah;
  tulis instan hanya saat fokus/layar/saver/zen berubah, sisanya tetap di-throttle
  2 detik supaya daemon tidak kebanjiran event inotify.

### 4. Menu ZRAM / hibernasi lanjutan yang belum ada
Backend (azenith-memory.sh, azenith-hibernate.sh, azenith-fstrim.sh) sudah ada
sejak patch RN9 pertama tetapi murni config-file, tanpa UI. Sekarang:
- Screen baru manager/.../ui/subscreens/AdvancedTweakScreen.kt (route
  advanced_tweaks, terdaftar di MainActivity.kt).
- Entri "Pengaturan Lanjutan" ditaruh di dalam tab Tweak (TweakScreen.kt), di
  atas section Add-ons.
- Isi menu: aktif/nonaktif tuning memori, ukuran ZRAM (MB), swappiness,
  algoritma kompresi, tombol Terapkan sekarang + status; mode hibernasi default
  (full / restrict), skip saat charging, skip saat audio aktif, shortcut ke
  daftar aplikasi hibernasi; fstrim terjadwal + selang + jalankan sekarang.
- azenith-memory.sh menerima perintah baru apply-now (tanpa menunggu
  sys.boot_completed + sleep 20s) supaya tombol di UI langsung bekerja.
