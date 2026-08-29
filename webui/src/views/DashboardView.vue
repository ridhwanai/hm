<template>
  <div class="h-full flex flex-col overflow-hidden">
    <!-- Header -->
    <div class="sticky top-0 z-10 bg-[#111318] px-4 py-3 border-b border-white/5 flex items-center justify-between">
      <div class="flex items-center gap-2">
        <div class="w-8 h-8 rounded-xl bg-indigo-500/20 text-indigo-400 flex items-center justify-center font-black text-sm border border-indigo-500/30">
          AZ
        </div>
        <div>
          <h1 class="text-base font-bold text-[#e2e2e9] leading-tight">AZenith</h1>
          <span class="text-[10px] text-[#c4c6d0] block">{{ homeStore.moduleVersion }}</span>
        </div>
      </div>
      
      <!-- Auto AI Status Badge -->
      <span
        class="text-[11px] px-2.5 py-1 rounded-full font-semibold border"
        :class="homeStore.autoModeEnabled 
          ? 'bg-indigo-500/15 text-indigo-300 border-indigo-500/30' 
          : 'bg-slate-800 text-slate-400 border-white/5'"
      >
        {{ homeStore.autoModeEnabled ? 'AI Dynamic: Aktif' : 'Manual Mode' }}
      </span>
    </div>

    <!-- Scrollable Content -->
    <div class="scrollbar-hidden pb-safe-nav flex-1 overflow-y-auto px-4 py-3 space-y-3">
      <!-- Status Card -->
      <div class="bg-[#282a30] p-4 rounded-2xl flex items-center justify-between text-[#e2e2e9]">
        <div class="flex items-center gap-3.5">
          <div
            class="w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 shadow-md"
            :class="getProfileIconBg(homeStore.currentProfile)"
          >
            <Flame v-if="homeStore.currentProfile === 'performance'" class="w-6 h-6 text-white" />
            <Scale v-else-if="homeStore.currentProfile === 'balanced'" class="w-6 h-6 text-white" />
            <Leaf v-else class="w-6 h-6 text-white" />
          </div>

          <div>
            <div class="flex items-center gap-2">
              <span class="text-sm font-bold text-white">
                {{ homeStore.daemonStatus === 'running' ? 'Daemon Aktif' : 'Daemon Nonaktif' }}
              </span>
              <span
                class="text-[10px] px-2 py-0.5 rounded-md font-bold uppercase"
                :class="getProfileBadgeClass(homeStore.currentProfile)"
              >
                {{ getProfileName(homeStore.currentProfile) }}
              </span>
            </div>
            <span class="text-xs text-[#c4c6d0] mt-0.5 block">
              {{ homeStore.daemonPid ? `PID: ${homeStore.daemonPid}` : 'Menunggu inisialisasi background service' }}
            </span>
          </div>
        </div>
      </div>

      <!-- Auto Mode (AI Dynamic) Toggle -->
      <div class="bg-[#1e1f25] p-4 rounded-2xl flex items-center justify-between">
        <div class="pr-3">
          <h3 class="text-sm font-bold text-[#e2e2e9]">{{ t('dashboard.autoModeTitle') }}</h3>
          <p class="text-xs text-[#c4c6d0] mt-0.5 leading-snug">{{ t('dashboard.autoModeDesc') }}</p>
        </div>

        <button
          @click="homeStore.toggleAutoMode(!homeStore.autoModeEnabled)"
          class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200"
          :class="homeStore.autoModeEnabled ? 'bg-indigo-600' : 'bg-slate-700'"
        >
          <span
            class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white transition duration-200"
            :class="homeStore.autoModeEnabled ? 'translate-x-5' : 'translate-x-0'"
          ></span>
        </button>
      </div>

      <!-- Manual Profile Selector Cards -->
      <div class="space-y-1.5">
        <div class="text-xs font-bold text-[#c4c6d0] px-1 uppercase tracking-wider">
          {{ t('dashboard.manualSelector') }}
        </div>

        <div class="grid grid-cols-3 gap-2">
          <!-- Performance -->
          <button
            @click="homeStore.setManualProfile('performance')"
            class="p-3 rounded-2xl flex flex-col items-center justify-center text-center transition-all border"
            :class="homeStore.currentProfile === 'performance'
              ? 'bg-rose-500/20 border-rose-500/50 text-white shadow-sm'
              : 'bg-[#1e1f25] border-transparent text-[#c4c6d0] hover:text-white'"
          >
            <Flame class="w-5 h-5 mb-1 text-rose-400" />
            <span class="text-xs font-bold">{{ t('dashboard.profiles.performance.name') }}</span>
          </button>

          <!-- Balanced -->
          <button
            @click="homeStore.setManualProfile('balanced')"
            class="p-3 rounded-2xl flex flex-col items-center justify-center text-center transition-all border"
            :class="homeStore.currentProfile === 'balanced'
              ? 'bg-cyan-500/20 border-cyan-500/50 text-white shadow-sm'
              : 'bg-[#1e1f25] border-transparent text-[#c4c6d0] hover:text-white'"
          >
            <Scale class="w-5 h-5 mb-1 text-cyan-400" />
            <span class="text-xs font-bold">{{ t('dashboard.profiles.balanced.name') }}</span>
          </button>

          <!-- ECO -->
          <button
            @click="homeStore.setManualProfile('eco')"
            class="p-3 rounded-2xl flex flex-col items-center justify-center text-center transition-all border"
            :class="homeStore.currentProfile === 'eco'
              ? 'bg-emerald-500/20 border-emerald-500/50 text-white shadow-sm'
              : 'bg-[#1e1f25] border-transparent text-[#c4c6d0] hover:text-white'"
          >
            <Leaf class="w-5 h-5 mb-1 text-emerald-400" />
            <span class="text-xs font-bold">{{ t('dashboard.profiles.eco.name') }}</span>
          </button>
        </div>
      </div>

      <!-- Device & System Info (MD3 Group) -->
      <div class="space-y-1.5">
        <div class="text-xs font-bold text-[#c4c6d0] px-1 uppercase tracking-wider">
          {{ t('dashboard.specs.title') }}
        </div>

        <div class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
          <!-- SoC -->
          <div class="md3-list-item px-4 py-3 flex items-center justify-between">
            <span class="text-xs text-[#c4c6d0]">{{ t('dashboard.specs.soc') }}</span>
            <span class="text-xs font-bold font-mono text-[#e2e2e9]">{{ homeStore.deviceSpecs.soc }}</span>
          </div>

          <!-- Kernel -->
          <div class="md3-list-item px-4 py-3 flex items-center justify-between">
            <span class="text-xs text-[#c4c6d0]">{{ t('dashboard.specs.kernel') }}</span>
            <span class="text-xs font-bold font-mono text-[#e2e2e9] truncate max-w-[200px]">{{ homeStore.deviceSpecs.kernel }}</span>
          </div>

          <!-- Android SDK -->
          <div class="md3-list-item px-4 py-3 flex items-center justify-between">
            <span class="text-xs text-[#c4c6d0]">{{ t('dashboard.specs.android') }}</span>
            <span class="text-xs font-bold font-mono text-[#e2e2e9]">{{ homeStore.deviceSpecs.sdk }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { Flame, Scale, Leaf } from 'lucide-vue-next'
import { useZenithHomeStore } from '@/stores/ZenithHome'
import { useLocales } from '@/helpers/Locales'

const homeStore = useZenithHomeStore()
const { t } = useLocales()

onMounted(() => {
  homeStore.initialize()
})

function getProfileIconBg(profile) {
  if (profile === 'performance') return 'bg-rose-600'
  if (profile === 'eco') return 'bg-emerald-600'
  return 'bg-cyan-600'
}

function getProfileName(profile) {
  if (profile === 'performance') return t('dashboard.profiles.performance.name')
  if (profile === 'eco') return t('dashboard.profiles.eco.name')
  return t('dashboard.profiles.balanced.name')
}

function getProfileBadgeClass(profile) {
  if (profile === 'performance') return 'bg-rose-500/20 text-rose-300'
  if (profile === 'eco') return 'bg-emerald-500/20 text-emerald-300'
  return 'bg-cyan-500/20 text-cyan-300'
}
</script>
