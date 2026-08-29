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
          {{ t('settings_page.hibernate.title') }}
        </h1>

        <!-- Switch Group -->
        <div class="md3-list">
          <div class="md3-list-item px-5 py-4 flex items-center justify-between">
            <div class="pr-3 flex-1 min-w-0">
              <h3 class="text-sm font-medium text-on-surface">Aktifkan Screen-Off ECO</h3>
              <p class="text-xs text-on-surface-variant mt-0.5">Bekukan aplikasi latar belakang saat layar mati</p>
            </div>
            <ToggleSwitch :modelValue="tweaksStore.ecoEnabled" @update:modelValue="(val) => { tweaksStore.ecoEnabled = val; tweaksStore.saveEcoConfig() }" />
          </div>
        </div>

        <div v-if="tweaksStore.ecoEnabled" class="space-y-1.5">
          <!-- Mode Selector -->
          <div class="md3-list">
            <div class="md3-list-item px-5 py-4 flex items-center justify-between">
              <div>
                <h3 class="text-sm font-medium text-on-surface">{{ t('settings_page.hibernate.mode') }}</h3>
                <p class="text-xs text-on-surface-variant mt-0.5">
                  {{ tweaksStore.ecoModeDefault === 'full' ? t('settings_page.hibernate.mode_full') : t('settings_page.hibernate.mode_restrict') }}
                </p>
              </div>
              <div class="flex items-center gap-1 bg-surface-container-high p-1 rounded-xl">
                <button
                  @click="tweaksStore.ecoModeDefault = 'full'; tweaksStore.saveEcoConfig()"
                  class="px-3 py-1 rounded-lg text-xs font-semibold transition-all cursor-pointer"
                  :class="tweaksStore.ecoModeDefault === 'full' ? 'bg-primary-container text-on-primary-container' : 'text-on-surface-variant'"
                >
                  Full
                </button>
                <button
                  @click="tweaksStore.ecoModeDefault = 'restrict'; tweaksStore.saveEcoConfig()"
                  class="px-3 py-1 rounded-lg text-xs font-semibold transition-all cursor-pointer"
                  :class="tweaksStore.ecoModeDefault === 'restrict' ? 'bg-primary-container text-on-primary-container' : 'text-on-surface-variant'"
                >
                  Restrict
                </button>
              </div>
            </div>
          </div>

          <!-- Skip Charging -->
          <div class="md3-list">
            <div class="md3-list-item px-5 py-4 flex items-center justify-between">
              <div>
                <h3 class="text-sm font-medium text-on-surface">{{ t('settings_page.hibernate.skip_charging') }}</h3>
                <p class="text-xs text-on-surface-variant mt-0.5">Jangan bekukan saat HP sedang di-cas</p>
              </div>
              <ToggleSwitch :modelValue="tweaksStore.skipCharging" @update:modelValue="(val) => { tweaksStore.skipCharging = val; tweaksStore.saveEcoConfig() }" />
            </div>
          </div>

          <!-- Skip Audio -->
          <div class="md3-list">
            <div class="md3-list-item px-5 py-4 flex items-center justify-between">
              <div>
                <h3 class="text-sm font-medium text-on-surface">{{ t('settings_page.hibernate.skip_audio') }}</h3>
                <p class="text-xs text-on-surface-variant mt-0.5">Jangan bekukan jika Spotify / musik menyala</p>
              </div>
              <ToggleSwitch :modelValue="tweaksStore.skipAudio" @update:modelValue="(val) => { tweaksStore.skipAudio = val; tweaksStore.saveEcoConfig() }" />
            </div>
          </div>

          <!-- Delay -->
          <div class="md3-list">
            <div class="md3-list-item px-5 py-4 space-y-2">
              <div class="flex justify-between text-xs">
                <span class="font-medium text-on-surface">{{ t('settings_page.hibernate.delay') }}</span>
                <span class="font-bold font-mono text-primary">{{ tweaksStore.ecoDelay }} detik</span>
              </div>
              <input
                v-model.number="tweaksStore.ecoDelay"
                @change="tweaksStore.saveEcoConfig"
                type="range"
                min="30"
                max="600"
                step="30"
                class="w-full h-1.5 bg-surface-container-highest rounded appearance-none cursor-pointer accent-primary"
              />
            </div>
          </div>
        </div>

        <!-- Package list editor -->
        <div v-if="tweaksStore.ecoEnabled" class="bg-surface-container p-5 rounded-3xl space-y-2 text-on-surface">
          <h3 class="text-sm font-medium">{{ t('settings_page.hibernate.list') }}</h3>
          <p class="text-xs text-on-surface-variant">Satu package name per baris:</p>
          <textarea
            v-model="tweaksStore.rawHibernateList"
            rows="5"
            class="w-full bg-surface-container-high border border-outline/20 rounded-2xl p-3 text-xs font-mono text-on-surface focus:outline-none focus:border-primary"
          ></textarea>
          <RippleComponent
            @click="tweaksStore.saveEcoConfig"
            class="w-full py-3 rounded-2xl bg-primary-container text-on-primary-container text-xs font-bold text-center cursor-pointer transition-all"
          >
            {{ t('common.save') }}
          </RippleComponent>
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
