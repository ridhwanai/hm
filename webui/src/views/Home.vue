<template>
  <div class="page home-page h-full flex flex-col">
    <!-- Header -->
    <div class="sticky top-0 z-10 bg-[#111318]">
      <div class="max-w-3xl mx-auto p-5 pb-3">
        <div class="flex justify-between items-center text-[#e2e2e9]">
          <h1 class="text-xl font-semibold">{{ t('home_page.title') }}</h1>
          <span
            class="text-[11px] px-2.5 py-1 rounded-full font-semibold border"
            :class="homeStore.autoModeEnabled 
              ? 'bg-indigo-500/15 text-indigo-300 border-indigo-500/30' 
              : 'bg-slate-800 text-slate-400 border-white/5'"
          >
            {{ homeStore.autoModeEnabled ? 'AI Dynamic: ON' : 'Manual' }}
          </span>
        </div>
      </div>
    </div>

    <!-- Scrollable Content -->
    <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll">
      <div class="max-w-3xl mx-auto p-5 py-1">
        <!-- Daemon Status -->
        <div class="bg-[#282a30] mb-4 p-4 rounded-2xl flex items-center justify-between text-[#e2e2e9] shadow-sm">
          <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-500 to-indigo-700 flex items-center justify-center text-white font-black text-2xl shadow-md shrink-0 mx-2">
            W
          </div>
          <div class="flex-1 flex flex-col px-3">
            <span class="text-base font-semibold">{{ daemonStatusText }}</span>
            <span class="text-xs text-[#c4c6d0] pt-1 block">{{ daemonPidText }}</span>
          </div>
        </div>

        <!-- Auto Mode Card -->
        <RippleComponent
          @click="homeStore.toggleAutoMode(!homeStore.autoModeEnabled)"
          class="cursor-pointer bg-[#1e1f25] mb-4 p-4 rounded-2xl w-full flex items-center justify-between text-[#e2e2e9]"
        >
          <div class="pr-3">
            <h2 class="text-sm font-medium">{{ t('home_page.auto_mode.title') }}</h2>
            <p class="text-xs text-[#c4c6d0] mt-0.5 leading-snug">{{ t('home_page.auto_mode.description') }}</p>
          </div>

          <button
            class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 pointer-events-none"
            :class="homeStore.autoModeEnabled ? 'bg-[#a8c7fa]' : 'bg-slate-700'"
          >
            <span
              class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-[#04305f] shadow-sm transition duration-200"
              :class="homeStore.autoModeEnabled ? 'translate-x-5' : 'translate-x-0'"
            ></span>
          </button>
        </RippleComponent>

        <!-- Device & Module Info Card -->
        <div class="bg-[#1e1f25] mb-4 p-4 rounded-2xl text-[#e2e2e9] space-y-3">
          <!-- Module -->
          <div class="py-1 px-2 flex items-start gap-4">
            <Sparkles class="w-5 h-5 text-[#a8c7fa] mt-1 shrink-0" />
            <div>
              <h3 class="text-sm font-medium text-[#e2e2e9]">{{ t('home_page.info_card.module') }}</h3>
              <span class="allow-copy text-xs text-[#c4c6d0] block mt-0.5">{{ homeStore.moduleVersion }}</span>
            </div>
          </div>

          <!-- Profile (Click to Switch) -->
          <div class="py-1 px-2 flex items-start gap-4">
            <Sliders class="w-5 h-5 text-[#a8c7fa] mt-1 shrink-0" />
            <div class="flex-1">
              <div class="flex items-center justify-between">
                <h3 class="text-sm font-medium text-[#e2e2e9]">{{ t('home_page.info_card.profile') }}</h3>
                <span class="text-[10px] px-2 py-0.5 rounded font-bold uppercase" :class="getProfileBadgeClass(homeStore.currentProfile)">
                  {{ homeStore.currentProfile }}
                </span>
              </div>
              <span class="allow-copy text-xs text-[#c4c6d0] block mt-0.5">{{ currentProfileText }}</span>

              <!-- Manual switch buttons -->
              <div class="grid grid-cols-3 gap-1.5 mt-2.5">
                <button
                  @click.stop="homeStore.setManualProfile('performance')"
                  class="py-1.5 rounded-lg text-xs font-semibold border transition-all"
                  :class="homeStore.currentProfile === 'performance' ? 'bg-rose-500/20 border-rose-500/50 text-rose-300' : 'bg-black/25 border-transparent text-[#c4c6d0]'"
                >
                  Performa
                </button>
                <button
                  @click.stop="homeStore.setManualProfile('balanced')"
                  class="py-1.5 rounded-lg text-xs font-semibold border transition-all"
                  :class="homeStore.currentProfile === 'balanced' ? 'bg-cyan-500/20 border-cyan-500/50 text-cyan-300' : 'bg-black/25 border-transparent text-[#c4c6d0]'"
                >
                  Seimbang
                </button>
                <button
                  @click.stop="homeStore.setManualProfile('eco')"
                  class="py-1.5 rounded-lg text-xs font-semibold border transition-all"
                  :class="homeStore.currentProfile === 'eco' ? 'bg-emerald-500/20 border-emerald-500/50 text-emerald-300' : 'bg-black/25 border-transparent text-[#c4c6d0]'"
                >
                  ECO
                </button>
              </div>
            </div>
          </div>

          <!-- Kernel -->
          <div class="py-1 px-2 flex items-start gap-4">
            <Terminal class="w-5 h-5 text-[#a8c7fa] mt-1 shrink-0" />
            <div>
              <h3 class="text-sm font-medium text-[#e2e2e9]">{{ t('home_page.info_card.kernel') }}</h3>
              <span class="allow-copy text-xs text-[#c4c6d0] block mt-0.5">{{ homeStore.deviceSpecs.kernel }}</span>
            </div>
          </div>

          <!-- Chipset -->
          <div class="py-1 px-2 flex items-start gap-4">
            <Cpu class="w-5 h-5 text-[#a8c7fa] mt-1 shrink-0" />
            <div>
              <h3 class="text-sm font-medium text-[#e2e2e9]">{{ t('home_page.info_card.chipset') }}</h3>
              <span class="allow-copy text-xs text-[#c4c6d0] block mt-0.5">{{ homeStore.deviceSpecs.soc }}</span>
            </div>
          </div>

          <!-- Android SDK -->
          <div class="py-1 px-2 flex items-start gap-4">
            <Smartphone class="w-5 h-5 text-[#a8c7fa] mt-1 shrink-0" />
            <div>
              <h3 class="text-sm font-medium text-[#e2e2e9]">{{ t('home_page.info_card.androidSDK') }}</h3>
              <span class="allow-copy text-xs text-[#c4c6d0] block mt-0.5">{{ homeStore.deviceSpecs.sdk }}</span>
            </div>
          </div>
        </div>

        <!-- Support Me Button -->
        <RippleComponent
          tabindex="0"
          class="cursor-pointer text-[#e2e2e9] bg-[#1e1f25] mb-4 p-4 py-5 rounded-2xl w-full"
        >
          <h2 class="text-sm font-medium px-2 mb-1">{{ t('home_page.support_button.title') }}</h2>
          <p class="text-xs text-[#c4c6d0] px-2 mb-1">{{ t('home_page.support_button.description') }}</p>
        </RippleComponent>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { Sparkles, Sliders, Terminal, Cpu, Smartphone } from 'lucide-vue-next'
import { useZenithHomeStore } from '@/stores/ZenithHome'
import { useLocales } from '@/helpers/Locales'
import RippleComponent from '@/components/ui/Ripple.vue'

const homeStore = useZenithHomeStore()
const { t } = useLocales()

onMounted(async () => {
  await homeStore.initialize()
})

const daemonStatusText = computed(() => {
  if (homeStore.daemonStatus === 'loading') return t('home_page.status_card.loading')
  if (homeStore.daemonStatus === 'running') return t('home_page.status_card.running')
  return t('home_page.status_card.stopped')
})

const daemonPidText = computed(() => {
  if (homeStore.daemonStatus === 'running' && homeStore.daemonPid) {
    return t('home_page.status_card.daemonPID', { pid: homeStore.daemonPid })
  }
  return t('home_page.status_card.daemon_inactive')
})

const currentProfileText = computed(() => {
  const p = homeStore.currentProfile
  return t(`home_page.profiles.${p}`) || p
})

function getProfileBadgeClass(profile) {
  if (profile === 'performance') return 'bg-rose-500/20 text-rose-300'
  if (profile === 'eco') return 'bg-emerald-500/20 text-emerald-300'
  return 'bg-cyan-500/20 text-cyan-300'
}
</script>
