<template>
  <div class="page home-page h-full flex flex-col">
    <!-- Header -->
    <div class="sticky top-0 z-10 bg-background">
      <div class="max-w-3xl mx-auto p-5 pb-3">
        <div class="flex justify-between items-center text-on-surface">
          <h1 class="text-xl font-semibold">{{ t('home_page.title') }}</h1>
        </div>
      </div>
    </div>

    <!-- Scrollable Content -->
    <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll">
      <div class="max-w-3xl mx-auto p-5 py-1">
        <!-- Daemon Status Card -->
        <div
          class="bg-secondary-container mb-4 p-4 rounded-xl flex items-center justify-between text-on-secondary-container"
        >
          <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-500 to-indigo-700 flex items-center justify-center text-white font-black text-2xl shadow-md shrink-0 mx-2">
            W
          </div>
          <div class="flex-1 flex flex-col px-3">
            <span class="text-lg font-semibold">{{ daemonStatusText }}</span>
            <span class="text-xs pt-1 block">{{ daemonPidText }}</span>
          </div>
        </div>

        <!-- Device & Module Info -->
        <div class="bg-surface-container mb-4 p-4 rounded-xl text-on-surface">
          <!-- Module -->
          <div class="py-2 px-2 flex items-start gap-4">
            <StarIcon class="text-primary mt-2 shrink-0" />
            <div>
              <h3 class="text-sm font-medium text-on-surface">
                {{ t('home_page.info_card.module') }}
              </h3>
              <span class="allow-copy text-xs text-on-surface-variant block mt-1">
                {{ homeStore.moduleVersion }}
              </span>
            </div>
          </div>

          <!-- Profile -->
          <div class="py-2 px-2 flex items-start gap-4">
            <StarlyGear class="text-primary mt-2 shrink-0" />
            <div class="flex-1">
              <div class="flex items-center justify-between">
                <h3 class="text-sm font-medium text-on-surface">
                  {{ t('home_page.info_card.profile') }}
                </h3>
                <span
                  class="text-[10px] px-2 py-0.5 rounded font-bold uppercase"
                  :class="getProfileBadgeClass(homeStore.currentProfile)"
                >
                  {{ homeStore.currentProfile }}
                </span>
              </div>
              <span class="allow-copy text-xs text-on-surface-variant block mt-1">
                {{ currentProfileText }}
              </span>

              <!-- Manual Profile Switcher -->
              <div class="grid grid-cols-3 gap-1.5 mt-2.5">
                <button
                  @click="homeStore.setManualProfile('performance')"
                  class="py-1.5 rounded-lg text-xs font-semibold border transition-all"
                  :class="homeStore.currentProfile === 'performance' ? 'bg-rose-500/20 border-rose-500/50 text-rose-300' : 'bg-surface border-transparent text-on-surface-variant'"
                >
                  Performa
                </button>
                <button
                  @click="homeStore.setManualProfile('balanced')"
                  class="py-1.5 rounded-lg text-xs font-semibold border transition-all"
                  :class="homeStore.currentProfile === 'balanced' ? 'bg-cyan-500/20 border-cyan-500/50 text-cyan-300' : 'bg-surface border-transparent text-on-surface-variant'"
                >
                  Seimbang
                </button>
                <button
                  @click="homeStore.setManualProfile('eco')"
                  class="py-1.5 rounded-lg text-xs font-semibold border transition-all"
                  :class="homeStore.currentProfile === 'eco' ? 'bg-emerald-500/20 border-emerald-500/50 text-emerald-300' : 'bg-surface border-transparent text-on-surface-variant'"
                >
                  ECO
                </button>
              </div>
            </div>
          </div>

          <!-- Kernel -->
          <div class="py-2 px-2 flex items-start gap-4">
            <ConsoleIcon class="text-primary mt-2 shrink-0" />
            <div>
              <h3 class="text-sm font-medium text-on-surface">
                {{ t('home_page.info_card.kernel') }}
              </h3>
              <span class="allow-copy text-xs text-on-surface-variant block mt-1">
                {{ homeStore.deviceSpecs.kernel }}
              </span>
            </div>
          </div>

          <!-- Chipset -->
          <div class="py-2 px-2 flex items-start gap-4">
            <ChipsetIcon class="text-primary mt-2 shrink-0" />
            <div>
              <h3 class="text-sm font-medium text-on-surface">
                {{ t('home_page.info_card.chipset') }}
              </h3>
              <span class="allow-copy text-xs text-on-surface-variant block mt-1">
                {{ homeStore.deviceSpecs.soc }}
              </span>
            </div>
          </div>

          <!-- Android SDK -->
          <div class="py-2 px-2 flex items-start gap-4">
            <AndroidIcon class="text-primary mt-2 shrink-0" />
            <div>
              <h3 class="text-sm font-medium text-on-surface">
                {{ t('home_page.info_card.androidSDK') }}
              </h3>
              <span class="allow-copy text-xs text-on-surface-variant block mt-1">
                {{ homeStore.deviceSpecs.sdk }}
              </span>
            </div>
          </div>
        </div>

        <!-- Support Me Button -->
        <RippleComponent
          tabindex="0"
          class="cursor-pointer text-on-surface bg-surface-container mb-4 p-4 py-5 rounded-xl w-full"
        >
          <h2 class="text-sm font-medium px-2 mb-1 relative z-10">
            {{ t('home_page.support_button.title') }}
          </h2>
          <p class="text-sm text-on-surface-variant px-2 mb-1 relative z-10">
            {{ t('home_page.support_button.description') }}
          </p>
        </RippleComponent>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { useZenithHomeStore } from '@/stores/ZenithHome'
import { useLocales } from '@/helpers/Locales'

import RippleComponent from '@/components/ui/Ripple.vue'
import StarIcon from '@/components/icons/Star.vue'
import StarlyGear from '@/components/icons/StarlyGear.vue'
import ConsoleIcon from '@/components/icons/Console.vue'
import ChipsetIcon from '@/components/icons/Chipset.vue'
import AndroidIcon from '@/components/icons/Android.vue'

const { t } = useLocales()
const homeStore = useZenithHomeStore()

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
