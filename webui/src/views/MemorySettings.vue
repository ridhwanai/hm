<template>
  <div class="page settings-detail-page h-full flex flex-col overflow-hidden bg-surface">
    <div class="max-w-3xl mx-auto h-full flex flex-col w-full">
      <!-- Top Header -->
      <div class="flex-none p-5 pb-3">
        <div class="flex items-center gap-4 mb-2">
          <button @click="router.back()" class="text-on-surface transition-colors cursor-pointer">
            <ArrowLeftIcon class="w-6 h-6 rtl:rotate-180" />
          </button>
        </div>
      </div>

      <!-- Content -->
      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5 space-y-4">
        <h1 class="text-4xl text-on-surface mt-10 mb-6 font-normal">
          {{ t('settings_page.memory.title') }}
        </h1>

        <!-- Switch Group -->
        <div class="md3-list">
          <div class="md3-list-item px-5 py-4 flex items-center justify-between">
            <div class="pr-3 flex-1 min-w-0">
              <h3 class="text-sm font-medium text-on-surface">Aktifkan Tuning Memori</h3>
              <p class="text-xs text-on-surface-variant mt-0.5">Optimalisasi alokasi ZRAM & Swappiness</p>
            </div>
            <ToggleSwitch v-model="tweaksStore.memEnabled" />
          </div>
        </div>

        <!-- Parameters if enabled -->
        <div v-if="tweaksStore.memEnabled" class="space-y-1.5">
          <!-- ZRAM Size -->
          <div class="md3-list">
            <div class="md3-list-item px-5 py-4 space-y-2">
              <div class="flex justify-between text-xs">
                <span class="font-medium text-on-surface">{{ t('settings_page.memory.size') }}</span>
                <span class="font-bold font-mono text-primary">{{ tweaksStore.zramSizeMB }} MB</span>
              </div>
              <input
                v-model.number="tweaksStore.zramSizeMB"
                type="range"
                min="512"
                max="4096"
                step="256"
                class="w-full h-1.5 bg-surface-container-highest rounded appearance-none cursor-pointer accent-primary"
              />
              <div class="flex justify-between text-[10px] text-on-surface-variant font-mono">
                <span>512 MB</span>
                <span>2048 MB</span>
                <span>4096 MB</span>
              </div>
            </div>
          </div>

          <!-- Swappiness -->
          <div class="md3-list">
            <div class="md3-list-item px-5 py-4 space-y-2">
              <div class="flex justify-between text-xs">
                <span class="font-medium text-on-surface">{{ t('settings_page.memory.swappiness') }}</span>
                <span class="font-bold font-mono text-primary">{{ tweaksStore.swappiness }}</span>
              </div>
              <input
                v-model.number="tweaksStore.swappiness"
                type="range"
                min="0"
                max="200"
                step="10"
                class="w-full h-1.5 bg-surface-container-highest rounded appearance-none cursor-pointer accent-primary"
              />
              <p class="text-[10px] text-on-surface-variant">Nilai 140 disarankan untuk HP RAM 3GB/4GB/6GB.</p>
            </div>
          </div>

          <!-- Algorithm -->
          <div class="md3-list">
            <div class="md3-list-item px-5 py-4 space-y-2">
              <label class="text-xs font-medium text-on-surface block">{{ t('settings_page.memory.algo') }}</label>
              <div class="grid grid-cols-4 gap-1.5">
                <button
                  v-for="algo in ['lz4', 'zstd', 'lzo', 'zram']"
                  :key="algo"
                  @click="tweaksStore.memAlgo = algo"
                  class="py-1.5 rounded-lg text-xs font-bold uppercase font-mono border transition-all cursor-pointer"
                  :class="tweaksStore.memAlgo === algo
                    ? 'bg-primary-container border-primary text-on-primary-container'
                    : 'bg-surface-container-high border-transparent text-on-surface-variant'"
                >
                  {{ algo }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Apply Button -->
        <RippleComponent
          v-if="tweaksStore.memEnabled"
          @click="tweaksStore.applyMemoryTuning"
          class="w-full py-4 rounded-3xl bg-primary text-on-primary text-xs font-bold text-center cursor-pointer transition-colors shadow-sm"
        >
          <span>{{ tweaksStore.isApplyingMem ? 'Menerapkan...' : t('settings_page.memory.apply_btn') }}</span>
        </RippleComponent>

        <div v-if="tweaksStore.memStatusMsg" class="p-3 rounded-2xl bg-surface-container-high text-xs font-mono text-primary break-all">
          {{ tweaksStore.memStatusMsg }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import ArrowLeftIcon from '@/components/icons/ArrowLeft.vue'
import ToggleSwitch from '@/components/ui/ToggleSwitch.vue'
import RippleComponent from '@/components/ui/Ripple.vue'
import { useZenithTweaksStore } from '@/stores/ZenithTweaks'
import { useLocales } from '@/helpers/Locales'

const router = useRouter()
const tweaksStore = useZenithTweaksStore()
const { t } = useLocales()
</script>
