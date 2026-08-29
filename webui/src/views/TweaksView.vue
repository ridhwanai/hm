<template>
  <div class="h-full flex flex-col overflow-hidden">
    <!-- Header -->
    <div class="sticky top-0 z-10 bg-[#111318] px-4 py-3 border-b border-white/5 flex items-center justify-between">
      <h1 class="text-base font-bold text-[#e2e2e9]">{{ t('tweaks.title') }}</h1>
      <span class="text-[10px] text-[#c4c6d0]">Konfigurasi Sistem</span>
    </div>

    <!-- Scrollable Content -->
    <div class="scrollbar-hidden pb-safe-nav flex-1 overflow-y-auto px-4 py-3 space-y-4">
      <!-- Section 1: Memory Tuning -->
      <div>
        <h2 class="text-xs font-bold text-[#a8c7fa] uppercase tracking-wider px-1 mb-1.5">
          {{ t('tweaks.memory.title') }}
        </h2>

        <div class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
          <!-- Toggle ZRAM -->
          <div class="md3-list-item px-4 py-3 flex items-center justify-between">
            <div>
              <h3 class="text-xs font-bold text-[#e2e2e9]">{{ t('tweaks.memory.enableToggle') }}</h3>
              <p class="text-[10px] text-[#c4c6d0] mt-0.5">ZRAM Swap & Swappiness Optimizer</p>
            </div>
            <input
              v-model="tweaksStore.memEnabled"
              type="checkbox"
              class="w-4 h-4 rounded text-[#a8c7fa] cursor-pointer"
            />
          </div>

          <!-- Controls if enabled -->
          <div v-if="tweaksStore.memEnabled" class="md3-list-item px-4 py-3 space-y-2.5">
            <!-- ZRAM Size -->
            <div class="space-y-1">
              <div class="flex justify-between text-xs">
                <span class="text-[#c4c6d0]">{{ t('tweaks.memory.zramSize') }}</span>
                <span class="font-bold font-mono text-[#a8c7fa]">{{ tweaksStore.zramSizeMB }} MB</span>
              </div>
              <input
                v-model.number="tweaksStore.zramSizeMB"
                type="range"
                min="512"
                max="4096"
                step="256"
                class="w-full h-1 bg-slate-700 rounded appearance-none cursor-pointer"
              />
            </div>

            <!-- Swappiness -->
            <div class="space-y-1">
              <div class="flex justify-between text-xs">
                <span class="text-[#c4c6d0]">{{ t('tweaks.memory.swappiness') }}</span>
                <span class="font-bold font-mono text-[#a8c7fa]">{{ tweaksStore.swappiness }}</span>
              </div>
              <input
                v-model.number="tweaksStore.swappiness"
                type="range"
                min="0"
                max="200"
                step="10"
                class="w-full h-1 bg-slate-700 rounded appearance-none cursor-pointer"
              />
            </div>

            <!-- Apply Button -->
            <button
              @click="tweaksStore.applyMemoryTuning"
              :disabled="tweaksStore.isApplyingMem"
              class="w-full py-2 rounded-xl bg-[#234475] hover:bg-[#2b538e] text-[#d6e3ff] text-xs font-bold transition-colors flex items-center justify-center gap-1.5"
            >
              <span>{{ tweaksStore.isApplyingMem ? 'Menerapkan...' : 'Terapkan Tuning Memori' }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Section 2: Screen-Off ECO Hibernation -->
      <div>
        <h2 class="text-xs font-bold text-[#a8c7fa] uppercase tracking-wider px-1 mb-1.5">
          {{ t('tweaks.hibernate.title') }}
        </h2>

        <div class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
          <!-- Toggle -->
          <div class="md3-list-item px-4 py-3 flex items-center justify-between">
            <div>
              <h3 class="text-xs font-bold text-[#e2e2e9]">{{ t('tweaks.hibernate.enableToggle') }}</h3>
              <p class="text-[10px] text-[#c4c6d0] mt-0.5">Bekukan aplikasi saat layar mati</p>
            </div>
            <input
              v-model="tweaksStore.ecoEnabled"
              @change="tweaksStore.saveEcoConfig"
              type="checkbox"
              class="w-4 h-4 rounded text-emerald-500 cursor-pointer"
            />
          </div>

          <div v-if="tweaksStore.ecoEnabled" class="md3-list-item px-4 py-3 space-y-2">
            <!-- Mode Switcher -->
            <div class="flex items-center justify-between">
              <span class="text-xs text-[#c4c6d0]">Mode:</span>
              <div class="flex items-center gap-1 bg-[#111318] p-0.5 rounded-lg border border-white/5">
                <button
                  @click="tweaksStore.ecoModeDefault = 'full'; tweaksStore.saveEcoConfig()"
                  class="px-2.5 py-1 rounded-md text-[10px] font-bold transition-all"
                  :class="tweaksStore.ecoModeDefault === 'full' ? 'bg-emerald-600 text-white' : 'text-[#c4c6d0]'"
                >
                  Full Eco
                </button>
                <button
                  @click="tweaksStore.ecoModeDefault = 'restrict'; tweaksStore.saveEcoConfig()"
                  class="px-2.5 py-1 rounded-md text-[10px] font-bold transition-all"
                  :class="tweaksStore.ecoModeDefault === 'restrict' ? 'bg-emerald-600 text-white' : 'text-[#c4c6d0]'"
                >
                  Restrict (Notif ON)
                </button>
              </div>
            </div>

            <!-- Skip Charging -->
            <div class="flex items-center justify-between pt-1">
              <span class="text-xs text-[#c4c6d0]">{{ t('tweaks.hibernate.skipCharging') }}</span>
              <input
                v-model="tweaksStore.skipCharging"
                @change="tweaksStore.saveEcoConfig"
                type="checkbox"
                class="w-3.5 h-3.5 rounded text-emerald-500 cursor-pointer"
              />
            </div>

            <!-- Skip Audio -->
            <div class="flex items-center justify-between">
              <span class="text-xs text-[#c4c6d0]">{{ t('tweaks.hibernate.skipAudio') }}</span>
              <input
                v-model="tweaksStore.skipAudio"
                @change="tweaksStore.saveEcoConfig"
                type="checkbox"
                class="w-3.5 h-3.5 rounded text-emerald-500 cursor-pointer"
              />
            </div>

            <!-- Delay -->
            <div class="space-y-1 pt-1">
              <div class="flex justify-between text-xs">
                <span class="text-[#c4c6d0]">{{ t('tweaks.hibernate.delay') }}</span>
                <span class="font-bold font-mono text-emerald-400">{{ tweaksStore.ecoDelay }}s</span>
              </div>
              <input
                v-model.number="tweaksStore.ecoDelay"
                @change="tweaksStore.saveEcoConfig"
                type="range"
                min="30"
                max="600"
                step="30"
                class="w-full h-1 bg-slate-700 rounded appearance-none cursor-pointer"
              />
            </div>

            <!-- Manage List Button -->
            <button
              @click="isEcoModalOpen = true"
              class="w-full py-1.5 rounded-lg bg-[#111318] text-xs text-[#c4c6d0] hover:text-white font-medium border border-white/5"
            >
              {{ t('tweaks.hibernate.manageList') }} ({{ tweaksStore.hibernateApps.length }} Paket)
            </button>
          </div>
        </div>
      </div>

      <!-- Section 3: Renderer & Maintenance -->
      <div>
        <h2 class="text-xs font-bold text-[#a8c7fa] uppercase tracking-wider px-1 mb-1.5">
          Sistem & Grafis
        </h2>

        <div class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
          <!-- HWUI Renderer Selection -->
          <div class="md3-list-item px-4 py-3 space-y-1.5">
            <h3 class="text-xs font-bold text-[#e2e2e9]">{{ t('tweaks.renderer.title') }}</h3>
            <select
              v-model="tweaksStore.currentRenderer"
              @change="tweaksStore.setRenderer(tweaksStore.currentRenderer)"
              class="w-full bg-[#111318] border border-white/10 rounded-xl px-3 py-2 text-xs text-[#e2e2e9] focus:outline-none focus:border-[#a8c7fa]"
            >
              <option value="skiaglthreaded">SkiaGL Threaded (Direkomendasikan)</option>
              <option value="skiagl">SkiaGL (OpenGL ES)</option>
              <option value="skiavkthreaded">SkiaVK Threaded (Vulkan)</option>
              <option value="skiavk">SkiaVK (Vulkan)</option>
              <option value="opengl">OpenGL Tradisional</option>
              <option value="default">Default Sistem ROM</option>
            </select>
          </div>

          <!-- FSTRIM Button -->
          <div class="md3-list-item px-4 py-3 flex items-center justify-between">
            <div>
              <h3 class="text-xs font-bold text-[#e2e2e9]">{{ t('tweaks.fstrim.title') }}</h3>
              <p class="text-[10px] text-[#c4c6d0] mt-0.5">Bersihkan blok partisi storage UFS/eMMC</p>
            </div>
            <button
              @click="tweaksStore.runFstrimNow"
              :disabled="tweaksStore.isRunningFstrim"
              class="px-3 py-1.5 rounded-full bg-[#333a48] hover:bg-[#434b5c] text-[#d8e2ff] text-xs font-bold shrink-0"
            >
              {{ tweaksStore.isRunningFstrim ? 'Memproses...' : 'Trim Sekarang' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Section 4: Logs & Preferences -->
      <div>
        <h2 class="text-xs font-bold text-[#a8c7fa] uppercase tracking-wider px-1 mb-1.5">
          Lainnya & Utilitas
        </h2>

        <div class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
          <!-- Live Logs Button -->
          <div @click="isLogModalOpen = true; settingsStore.fetchLogs()" class="md3-list-item px-4 py-3 flex items-center justify-between cursor-pointer">
            <div class="flex items-center gap-3">
              <FileText class="w-4 h-4 text-[#a8c7fa]" />
              <span class="text-xs font-bold text-[#e2e2e9]">Lihat Log Modul (Live Viewer)</span>
            </div>
            <ChevronRight class="w-4 h-4 text-[#c4c6d0]" />
          </div>

          <!-- Homescreen Shortcut -->
          <div @click="settingsStore.createShortcut" class="md3-list-item px-4 py-3 flex items-center justify-between cursor-pointer">
            <div class="flex items-center gap-3">
              <Smartphone class="w-4 h-4 text-[#a8c7fa]" />
              <span class="text-xs font-bold text-[#e2e2e9]">{{ t('logs.shortcutBtn') }}</span>
            </div>
            <ChevronRight class="w-4 h-4 text-[#c4c6d0]" />
          </div>

          <!-- Language Selector -->
          <div class="md3-list-item px-4 py-3 flex items-center justify-between">
            <div class="flex items-center gap-3">
              <Globe class="w-4 h-4 text-[#a8c7fa]" />
              <span class="text-xs font-bold text-[#e2e2e9]">Bahasa / Language</span>
            </div>
            <div class="flex items-center gap-1 bg-[#111318] p-0.5 rounded-lg border border-white/5">
              <button
                @click="setLanguage('id')"
                class="px-2 py-0.5 rounded text-[10px] font-bold"
                :class="currentLanguage === 'id' ? 'bg-[#234475] text-[#d6e3ff]' : 'text-[#c4c6d0]'"
              >
                ID 🇮🇩
              </button>
              <button
                @click="setLanguage('en')"
                class="px-2 py-0.5 rounded text-[10px] font-bold"
                :class="currentLanguage === 'en' ? 'bg-[#234475] text-[#d6e3ff]' : 'text-[#c4c6d0]'"
              >
                EN 🇬🇧
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Eco Hibernate List -->
    <Modal :isOpen="isEcoModalOpen" @close="isEcoModalOpen = false" title="Daftar Paket Hibernasi">
      <div class="space-y-2">
        <textarea
          v-model="tweaksStore.rawHibernateList"
          rows="6"
          placeholder="com.facebook.katana&#10;com.instagram.android"
          class="w-full bg-[#111318] border border-white/10 rounded-xl p-2.5 text-xs font-mono text-[#e2e2e9] focus:outline-none focus:border-[#a8c7fa]"
        ></textarea>
        <button
          @click="tweaksStore.saveEcoConfig(); isEcoModalOpen = false"
          class="w-full py-2 rounded-xl bg-[#a8c7fa] text-[#04305f] text-xs font-bold"
        >
          {{ t('common.save') }}
        </button>
      </div>
    </Modal>

    <!-- Modal: Live Log Viewer -->
    <Modal :isOpen="isLogModalOpen" @close="isLogModalOpen = false" title="Live Log AZenith">
      <div class="space-y-2">
        <div class="flex items-center justify-between text-xs">
          <div class="flex items-center gap-1 bg-[#111318] p-0.5 rounded-lg border border-white/5">
            <button
              @click="settingsStore.setLogType('azenith')"
              class="px-2 py-1 rounded text-[10px] font-bold"
              :class="settingsStore.selectedLogType === 'azenith' ? 'bg-[#234475] text-[#d6e3ff]' : 'text-[#c4c6d0]'"
            >
              AZenith.log
            </button>
            <button
              @click="settingsStore.setLogType('sysmon')"
              class="px-2 py-1 rounded text-[10px] font-bold"
              :class="settingsStore.selectedLogType === 'sysmon' ? 'bg-[#234475] text-[#d6e3ff]' : 'text-[#c4c6d0]'"
            >
              sysmon.log
            </button>
          </div>

          <button @click="settingsStore.clearLogs" class="text-rose-400 font-semibold text-[11px]">
            Bersihkan
          </button>
        </div>

        <pre
          class="w-full h-52 bg-[#0c0e13] border border-white/10 rounded-xl p-2.5 text-[10px] font-mono text-emerald-400 overflow-y-auto whitespace-pre-wrap select-all scrollbar-hidden"
        >{{ settingsStore.logContent }}</pre>

        <button
          @click="settingsStore.copyLogs"
          class="w-full py-1.5 rounded-xl bg-[#333a48] text-[#d8e2ff] text-xs font-bold"
        >
          Salin ke Clipboard
        </button>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { FileText, Smartphone, Globe, ChevronRight } from 'lucide-vue-next'
import { useZenithTweaksStore } from '@/stores/ZenithTweaks'
import { useZenithSettingsStore } from '@/stores/ZenithSettings'
import { useLocales } from '@/helpers/Locales'
import Modal from '@/components/Modal.vue'

const tweaksStore = useZenithTweaksStore()
const settingsStore = useZenithSettingsStore()
const { t, currentLanguage, setLanguage } = useLocales()

const isEcoModalOpen = ref(false)
const isLogModalOpen = ref(false)

onMounted(() => {
  tweaksStore.loadAllTweaks()
})
</script>
