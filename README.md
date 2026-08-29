# ⚡ Wann Optimizer (Material Design 3 WebUI Edition)

<p align="center">
  <img src="https://img.shields.io/badge/Author-wann-blue?style=for-the-badge" alt="Author">
  <img src="https://img.shields.io/badge/Status-Stable-green?style=for-the-badge" alt="Status">
  <img src="https://img.shields.io/badge/UI-MD3%20WebUI-purple?style=for-the-badge" alt="UI">
  <img src="https://img.shields.io/badge/License-Apache%202.0-orange?style=for-the-badge" alt="License">
</p>

---

## ⚡ Ringkasan / Overview
**Wann Optimizer** adalah modul optimasi sistem universal untuk Android (Root via KernelSU / APatch / Magisk) yang dirancang untuk meningkatkan performa gaming sekaligus menjaga efisiensi baterai dan kelancaran multitasking harian. 

Dilengkapi dengan antarmuka **Material Design 3 (MD3) WebUI** yang terintegrasi langsung di dalam manager tanpa memerlukan instalasi APK pihak ketiga.

---

## 🚀 Fitur Unggulan

### 🎮 1. Dynamic Game Optimization (Mode Otomatis AI)
* Mendeteksi otomatis saat game dibuka di latar depan (*foreground*) dan langsung beralih ke **Mode Performa (Gaming)**.
* Fitur **Do Not Disturb (DND)** otomatis saat bermain game.
* Pengaturan renderer grafis per-game (SkiaGL Threaded, Vulkan, OpenGL).

### 🧠 2. Tuning Memori (ZRAM & Swap Optimizer)
* Konfigurasi dinamis ukuran ZRAM (512MB – 4096MB) dengan kompresi hemat CPU (**LZ4 / ZSTD**).
* Optimalisasi `vm.swappiness` (disetel ke 140 untuk mendongkrak performa multitasking RAM 3GB/4GB/6GB).

### 🔋 3. Screen-Off ECO Hibernation
* Membekukan aplikasi latar belakang yang boros baterai saat layar HP dimatikan.
* Mode **Full Eco** atau **Restrict** (notifikasi FCM penting tetap masuk).
* Otomatis menunda hibernasi jika sedang mengisi daya (*charging*) atau memutar musik/audio.

### 💾 4. Pemeliharaan Storage (FSTRIM)
* Pembersihan blok memori flash eMMC/UFS terjadwal untuk menjaga kecepatan baca/tulis storage jangka panjang.

### 📱 5. Antarmuka WebUI Modern (Encore-Style MD3)
* Desain minimalis dan responsif berbasis **Vue 3, Pinia, TailwindCSS, dan Lucide Icons**.
* Bebas lag (0ms load time, no shell-flooding ANR).
* Mendukung mode dwibahasa: Bahasa Indonesia 🇮🇩 dan English 🇬🇧.

---

## 📦 Instalasi
1. Unduh file zip rilis **`Wann-WebUI-v5.1.zip`**.
2. Flash melalui **KernelSU / APatch / Magisk**.
3. Buka modul dari daftar modul di manager untuk mengakses WebUI dashboard.

---

## 👨‍💻 Developer & Credits
* **Author & Maintainer:** [@wann](https://github.com/ridhwanai)
* **Base Engine & Architecture:** Open source Android root community & contributors.

---

## ⚖️ License
Licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
