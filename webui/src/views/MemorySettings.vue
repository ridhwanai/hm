<template>
  <div class="page settings-detail-page h-full flex flex-col overflow-hidden">
    <div class="max-w-3xl mx-auto h-full flex flex-col w-full">
      <!-- Top Header -->
      <div class="flex-none p-5 pb-3">
        <div class="flex items-center gap-3 text-[#e2e2e9]">
          <button @click="router.back()" class="p-2 rounded-full hover:bg-white/10 transition-colors">
            <ArrowLeft class="w-5 h-5" />
          </button>
          <h1 class="text-xl font-semibold">{{ t('settings_page.memory.title') }}</h1>
        </div>
      </div>

      <!-- Content -->
      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5 space-y-4">
        <!-- Switch Group -->
        <div class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
          <div class="md3-list-item px-5 py-4 flex items-center justify-between">
            <div class="pr-3">
              <h3 class="text-sm font-medium text-[#e2e2e9]">Aktifkan Tuning Memori</h3>
              <p class="text-xs text-[#c4c6d0] mt-0.5">Optimalisasi alokasi ZRAM & Swappiness</p>
            </div>
            <button
              @click="tweaksStore.memEnabled = !tweaksStore.memEnabled"
              class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200"
              :class="tweaksStore.memEnabled ? 'bg-[#a8c7fa]' : 'bg-slate-700'"
            >
              <span
                class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-[#04305f] shadow-sm transition duration-200"
                :class="tweaksStore.memEnabled ? 'translate-x-5' : 'translate-x-0'"
              ></span>
            </button>
          </div>
        </div>

        <!-- Parameters if enabled -->
        <div v-if="tweaksStore.memEnabled" class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
          <!-- ZRAM Size -->
          <div class="md3-list-item px-5 py-4 space-y-2">
            <div class="flex justify-between text-xs">
              <span class="font-medium text-[#e2e2e9]">{{ t('settings_page.memory.size') }}</span>
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
            <div class="flex justify-between text-[10px] text-[#c4c6d0] font-mono">
              <span>512 MB</span>
              <span>2048 MB</span>
              <span>4096 MB</span>
            </div>
          </div>

          <!-- Swappiness -->
          <div class="md3-list-item px-5 py-4 space-y-2">
            <div class="flex justify-between text-xs">
              <span class="font-medium text-[#e2e2e9]">{{ t('settings_page.memory.swappiness') }}</span>
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
            <p class="text-[10px] text-[#c4c6d0]">Nilai 140 disarankan untuk Redmi Note 9 / Helio G85 RAM 4GB.</p>
          </div>

          <!-- Algorithm -->
          <div class="md3-list-item px-5 py-4 space-y-2">
            <label class="text-xs font-medium text-[#e2e2e9] block">{{ t('settings_page.memory.algo') }}</label>
            <div class="grid grid-cols-4 gap-1.5">
              <button
                v-for="algo in ['lz4', 'zstd', 'lzo', 'zram']"
                :key="algo"
                @click="tweaksStore.memAlgo = algo"
                class="py-1.5 rounded-lg text-xs font-bold uppercase font-mono border transition-all"
                :class="tweaksStore.memAlgo === algo
                  ? 'bg-[#234475] border-[#a8c7fa] text-[#d6e3ff]'
                  : 'bg-[#111318] border-transparent text-[#c4c6d0]'"
              >
                {{ algo }}
              </button>
            </div>
          </div>
        </div>

        <!-- Apply Button -->
        <button
          v-if="tweaksStore.memEnabled"
          @click="tweaksStore.applyMemoryTuning"
          :disabled="tweaksStore.isApplyingMem"
          class="w-full py-3.5 rounded-2xl bg-[#a8c7fa] hover:bg-[#82b1ff] text-[#04305f] text-xs font-bold transition-all shadow-sm flex items-center justify-center gap-2"
        >
          <span>{{ tweaksStore.isApplyingMem ? 'Menerapkan...' : t('settings_page.memory.apply_btn') }}</span>
        </button>

        <div v-if="tweaksStore.memStatusMsg" class="p-3 rounded-xl bg-[#191b20] text-xs font-mono text-[#a8c7fa] break-all">
          {{ tweaksStore.memStatusMsg }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowLeft } from 'lucide-vue-next'
import { useZenithTweaksStore } from '@/stores/ZenithTweaks'
import { useLocales } from '@/helpers/Locales'

const router = useRouter()
const tweaksStore = useZenithTweaksStore()
const { t } = useLocales()
</script>
