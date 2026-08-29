<template>
  <div class="space-y-4 pb-24 animate-fade-in">
    <!-- Top Hero Banner & Status -->
    <div class="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#151c2e] via-[#101626] to-[#0c101d] border border-white/10 p-5 shadow-2xl">
      <!-- Glow effect behind hero -->
      <div
        class="absolute -top-16 -right-16 w-44 h-44 rounded-full blur-3xl opacity-30 pointer-events-none"
        :class="{
          'bg-rose-500': homeStore.currentProfile === 'performance',
          'bg-cyan-500': homeStore.currentProfile === 'balanced',
          'bg-emerald-500': homeStore.currentProfile === 'eco'
        }"
      ></div>

      <div class="relative z-10 flex items-start justify-between">
        <div>
          <div class="flex items-center gap-2">
            <span class="px-2.5 py-0.5 rounded-full text-[10px] font-extrabold uppercase tracking-wider bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
              {{ homeStore.moduleVersion }}
            </span>
            <div
              class="flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-semibold border"
              :class="homeStore.daemonStatus === 'running' 
                ? 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30' 
                : 'bg-rose-500/10 text-rose-300 border-rose-500/30'"
            >
              <span class="w-1.5 h-1.5 rounded-full animate-pulse" :class="homeStore.daemonStatus === 'running' ? 'bg-emerald-400' : 'bg-rose-400'"></span>
              <span>{{ homeStore.daemonStatus === 'running' ? `Daemon Aktif (PID: ${homeStore.daemonPid || 'OK'})` : 'Daemon Berhenti' }}</span>
            </div>
          </div>

          <h1 class="text-2xl font-black tracking-tight text-white mt-2">
            AZenith <span class="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 via-cyan-300 to-teal-200">Optimizer</span>
          </h1>
          <p class="text-xs text-slate-400 mt-0.5">{{ t('dashboard.subtitle') }}</p>
        </div>

        <div class="w-10 h-10 rounded-2xl bg-indigo-600/20 border border-indigo-500/30 flex items-center justify-center text-indigo-400 shadow-inner">
          <Zap class="w-5 h-5 animate-pulse" />
        </div>
      </div>

      <!-- Current Profile Hero Card -->
      <div 
        class="mt-5 p-4 rounded-2xl border transition-all duration-300 relative overflow-hidden"
        :class="getProfileCardClasses(homeStore.currentProfile)"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl flex items-center justify-center shadow-lg" :class="getProfileIconBg(homeStore.currentProfile)">
              <Flame v-if="homeStore.currentProfile === 'performance'" class="w-6 h-6 text-white" />
              <Scale v-else-if="homeStore.currentProfile === 'balanced'" class="w-5 h-5 text-white" />
              <Leaf v-else class="w-5 h-5 text-white" />
            </div>
            <div>
              <div class="text-[10px] font-bold uppercase tracking-wider text-slate-300/80">{{ t('dashboard.activeProfile') }}</div>
              <div class="text-lg font-extrabold text-white flex items-center gap-2">
                {{ getProfileName(homeStore.currentProfile) }}
                <span class="text-[10px] px-2 py-0.5 rounded-md font-bold uppercase" :class="getProfileBadgeClass(homeStore.currentProfile)">
                  {{ getProfileBadge(homeStore.currentProfile) }}
                </span>
              </div>
            </div>
          </div>

          <div v-if="homeStore.autoModeEnabled" class="flex flex-col items-end">
            <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
              <Sparkles class="w-3 h-3 text-indigo-300 animate-spin-slow" />
              Auto AI
            </span>
          </div>
        </div>

        <p class="text-xs text-slate-300/90 mt-2.5 leading-relaxed">
          {{ getProfileDesc(homeStore.currentProfile) }}
        </p>
      </div>
    </div>

    <!-- Auto Mode (AI Dynamic) Toggle -->
    <div class="glass-card rounded-2xl p-4 flex items-center justify-between border border-white/10">
      <div class="flex items-center gap-3 pr-3">
        <div class="w-9 h-9 rounded-xl bg-indigo-500/15 border border-indigo-500/30 flex items-center justify-center text-indigo-400 shrink-0">
          <Sparkles class="w-4 h-4" />
        </div>
        <div>
          <h3 class="text-sm font-bold text-white">{{ t('dashboard.autoModeTitle') }}</h3>
          <p class="text-xs text-slate-400 mt-0.5 leading-tight">{{ t('dashboard.autoModeDesc') }}</p>
        </div>
      </div>

      <!-- Modern Switch -->
      <button
        @click="homeStore.toggleAutoMode(!homeStore.autoModeEnabled)"
        class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none"
        :class="homeStore.autoModeEnabled ? 'bg-indigo-600' : 'bg-slate-700'"
      >
        <span
          class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-lg ring-0 transition duration-200 ease-in-out"
          :class="homeStore.autoModeEnabled ? 'translate-x-5' : 'translate-x-0'"
        ></span>
      </button>
    </div>

    <!-- Manual Profile Switcher (Cards) -->
    <div class="space-y-2">
      <div class="flex items-center justify-between px-1">
        <h2 class="text-xs font-bold uppercase tracking-wider text-slate-400">
          {{ t('dashboard.manualSelector') }}
        </h2>
        <span v-if="homeStore.autoModeEnabled" class="text-[11px] text-amber-400/80 font-medium">
          (Auto AI aktif - profil beralih dinamis)
        </span>
      </div>

      <div class="grid grid-cols-3 gap-2.5">
        <!-- Performance -->
        <button
          @click="homeStore.setManualProfile('performance')"
          class="glass-card glass-card-hover p-3 rounded-2xl flex flex-col items-center text-center relative border transition-all duration-200"
          :class="homeStore.currentProfile === 'performance' 
            ? 'bg-rose-500/20 border-rose-500/50 glow-perf' 
            : 'hover:border-white/20 border-white/5'"
        >
          <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-rose-500 to-red-600 flex items-center justify-center text-white mb-2 shadow-md">
            <Flame class="w-4 h-4" />
          </div>
          <span class="text-xs font-bold text-white">{{ t('dashboard.profiles.performance.name') }}</span>
          <span class="text-[9px] text-rose-300 font-semibold mt-0.5">{{ t('dashboard.profiles.performance.badge') }}</span>
        </button>

        <!-- Balanced -->
        <button
          @click="homeStore.setManualProfile('balanced')"
          class="glass-card glass-card-hover p-3 rounded-2xl flex flex-col items-center text-center relative border transition-all duration-200"
          :class="homeStore.currentProfile === 'balanced' 
            ? 'bg-cyan-500/20 border-cyan-500/50 glow-balance' 
            : 'hover:border-white/20 border-white/5'"
        >
          <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-cyan-500 to-blue-600 flex items-center justify-center text-white mb-2 shadow-md">
            <Scale class="w-4 h-4" />
          </div>
          <span class="text-xs font-bold text-white">{{ t('dashboard.profiles.balanced.name') }}</span>
          <span class="text-[9px] text-cyan-300 font-semibold mt-0.5">{{ t('dashboard.profiles.balanced.badge') }}</span>
        </button>

        <!-- ECO -->
        <button
          @click="homeStore.setManualProfile('eco')"
          class="glass-card glass-card-hover p-3 rounded-2xl flex flex-col items-center text-center relative border transition-all duration-200"
          :class="homeStore.currentProfile === 'eco' 
            ? 'bg-emerald-500/20 border-emerald-500/50 glow-eco' 
            : 'hover:border-white/20 border-white/5'"
        >
          <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center text-white mb-2 shadow-md">
            <Leaf class="w-4 h-4" />
          </div>
          <span class="text-xs font-bold text-white">{{ t('dashboard.profiles.eco.name') }}</span>
          <span class="text-[9px] text-emerald-300 font-semibold mt-0.5">{{ t('dashboard.profiles.eco.badge') }}</span>
        </button>
      </div>
    </div>

    <!-- Device & System Specs -->
    <div class="glass-card rounded-2xl p-4 border border-white/10 space-y-3">
      <div class="flex items-center justify-between pb-2 border-b border-white/5">
        <h3 class="text-xs font-bold uppercase tracking-wider text-slate-300 flex items-center gap-2">
          <Cpu class="w-4 h-4 text-indigo-400" />
          {{ t('dashboard.specs.title') }}
        </h3>
        <span class="text-[11px] text-slate-400 font-mono">{{ homeStore.deviceSpecs.sdk }}</span>
      </div>

      <div class="grid grid-cols-2 gap-2 text-xs">
        <!-- SoC -->
        <div class="bg-black/25 p-2.5 rounded-xl border border-white/5">
          <div class="text-[10px] text-slate-400 uppercase font-medium">{{ t('dashboard.specs.soc') }}</div>
          <div class="font-bold text-slate-100 font-mono mt-0.5 truncate">{{ homeStore.deviceSpecs.soc }}</div>
        </div>

        <!-- Kernel -->
        <div class="bg-black/25 p-2.5 rounded-xl border border-white/5">
          <div class="text-[10px] text-slate-400 uppercase font-medium">{{ t('dashboard.specs.kernel') }}</div>
          <div class="font-bold text-slate-100 font-mono mt-0.5 truncate">{{ homeStore.deviceSpecs.kernel }}</div>
        </div>

        <!-- Battery -->
        <div class="bg-black/25 p-2.5 rounded-xl border border-white/5 flex items-center justify-between">
          <div>
            <div class="text-[10px] text-slate-400 uppercase font-medium">{{ t('dashboard.specs.battery') }}</div>
            <div class="font-bold text-slate-100 font-mono mt-0.5">{{ homeStore.deviceSpecs.battery }}%</div>
          </div>
          <BatteryCharging v-if="homeStore.deviceSpecs.isCharging" class="w-5 h-5 text-emerald-400" />
          <Battery v-else class="w-5 h-5 text-slate-400" />
        </div>

        <!-- Temperature -->
        <div class="bg-black/25 p-2.5 rounded-xl border border-white/5 flex items-center justify-between">
          <div>
            <div class="text-[10px] text-slate-400 uppercase font-medium">{{ t('dashboard.specs.temp') }}</div>
            <div class="font-bold text-slate-100 font-mono mt-0.5">{{ homeStore.deviceSpecs.temp }}°C</div>
          </div>
          <Thermometer class="w-5 h-5" :class="homeStore.deviceSpecs.temp > 42 ? 'text-rose-400' : 'text-cyan-400'" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { Zap, Flame, Scale, Leaf, Sparkles, Cpu, Battery, BatteryCharging, Thermometer } from 'lucide-vue-next'
import { useZenithHomeStore } from '@/stores/ZenithHome'
import { useLocales } from '@/helpers/Locales'

const homeStore = useZenithHomeStore()
const { t } = useLocales()

onMounted(() => {
  homeStore.initialize()
})

function getProfileCardClasses(profile) {
  if (profile === 'performance') {
    return 'bg-gradient-to-r from-rose-950/50 via-rose-900/30 to-red-950/40 border-rose-500/40 glow-perf'
  }
  if (profile === 'eco') {
    return 'bg-gradient-to-r from-emerald-950/50 via-emerald-900/30 to-teal-950/40 border-emerald-500/40 glow-eco'
  }
  return 'bg-gradient-to-r from-cyan-950/50 via-cyan-900/30 to-blue-950/40 border-cyan-500/40 glow-balance'
}

function getProfileIconBg(profile) {
  if (profile === 'performance') return 'bg-gradient-to-br from-rose-500 to-red-600'
  if (profile === 'eco') return 'bg-gradient-to-br from-emerald-500 to-teal-600'
  return 'bg-gradient-to-br from-cyan-500 to-blue-600'
}

function getProfileName(profile) {
  if (profile === 'performance') return t('dashboard.profiles.performance.name')
  if (profile === 'eco') return t('dashboard.profiles.eco.name')
  return t('dashboard.profiles.balanced.name')
}

function getProfileBadge(profile) {
  if (profile === 'performance') return t('dashboard.profiles.performance.badge')
  if (profile === 'eco') return t('dashboard.profiles.eco.badge')
  return t('dashboard.profiles.balanced.badge')
}

function getProfileBadgeClass(profile) {
  if (profile === 'performance') return 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
  if (profile === 'eco') return 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
  return 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/30'
}

function getProfileDesc(profile) {
  if (profile === 'performance') return t('dashboard.profiles.performance.desc')
  if (profile === 'eco') return t('dashboard.profiles.eco.desc')
  return t('dashboard.profiles.balanced.desc')
}
</script>

<style scoped>
@keyframes spinSlow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.animate-spin-slow {
  animation: spinSlow 8s linear infinite;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in {
  animation: fadeIn 0.25s ease-out forwards;
}
</style>
