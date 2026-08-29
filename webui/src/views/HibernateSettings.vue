<template>
  <div class="page settings-detail-page h-full flex flex-col overflow-hidden">
    <div class="max-w-3xl mx-auto h-full flex flex-col w-full">
      <!-- Top Header -->
      <div class="flex-none p-5 pb-3">
        <div class="flex items-center gap-3 text-[#e2e2e9]">
          <button @click="router.back()" class="p-2 rounded-full hover:bg-white/10 transition-colors">
            <ArrowLeft class="w-5 h-5" />
          </button>
          <h1 class="text-xl font-semibold">{{ t('settings_page.hibernate.title') }}</h1>
        </div>
      </div>

      <!-- Content -->
      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5 space-y-4">
        <!-- Switch Group -->
        <div class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
          <div class="md3-list-item px-5 py-4 flex items-center justify-between">
            <div class="pr-3">
              <h3 class="text-sm font-medium text-[#e2e2e9]">Aktifkan Screen-Off ECO</h3>
              <p class="text-xs text-[#c4c6d0] mt-0.5">Bekukan aplikasi latar belakang saat layar mati</p>
            </div>
            <button
              @click="tweaksStore.ecoEnabled = !tweaksStore.ecoEnabled; tweaksStore.saveEcoConfig()"
              class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200"
              :class="tweaksStore.ecoEnabled ? 'bg-[#a8c7fa]' : 'bg-slate-700'"
            >
              <span
                class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-[#04305f] shadow-sm transition duration-200"
                :class="tweaksStore.ecoEnabled ? 'translate-x-5' : 'translate-x-0'"
              ></span>
            </button>
          </div>
        </div>

        <div v-if="tweaksStore.ecoEnabled" class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
          <!-- Mode Selector -->
          <div class="md3-list-item px-5 py-4 flex items-center justify-between">
            <div>
              <h3 class="text-sm font-medium text-[#e2e2e9]">{{ t('settings_page.hibernate.mode') }}</h3>
              <p class="text-xs text-[#c4c6d0] mt-0.5">
                {{ tweaksStore.ecoModeDefault === 'full' ? t('settings_page.hibernate.mode_full') : t('settings_page.hibernate.mode_restrict') }}
              </p>
            </div>
            <div class="flex items-center gap-1 bg-[#111318] p-0.5 rounded-lg border border-white/5">
              <button
                @click="tweaksStore.ecoModeDefault = 'full'; tweaksStore.saveEcoConfig()"
                class="px-2.5 py-1 rounded-md text-xs font-semibold transition-all"
                :class="tweaksStore.ecoModeDefault === 'full' ? 'bg-[#234475] text-[#d6e3ff]' : 'text-[#c4c6d0]'"
              >
                Full
              </button>
              <button
                @click="tweaksStore.ecoModeDefault = 'restrict'; tweaksStore.saveEcoConfig()"
                class="px-2.5 py-1 rounded-md text-xs font-semibold transition-all"
                :class="tweaksStore.ecoModeDefault === 'restrict' ? 'bg-[#234475] text-[#d6e3ff]' : 'text-[#c4c6d0]'"
              >
                Restrict
              </button>
            </div>
          </div>

          <!-- Skip Charging -->
          <div class="md3-list-item px-5 py-4 flex items-center justify-between">
            <div>
              <h3 class="text-sm font-medium text-[#e2e2e9]">{{ t('settings_page.hibernate.skip_charging') }}</h3>
              <p class="text-xs text-[#c4c6d0] mt-0.5">Jangan bekukan saat HP sedang di-cas</p>
            </div>
            <input
              v-model="tweaksStore.skipCharging"
              @change="tweaksStore.saveEcoConfig"
              type="checkbox"
              class="w-4 h-4 rounded text-[#a8c7fa] cursor-pointer"
            />
          </div>

          <!-- Skip Audio -->
          <div class="md3-list-item px-5 py-4 flex items-center justify-between">
            <div>
              <h3 class="text-sm font-medium text-[#e2e2e9]">{{ t('settings_page.hibernate.skip_audio') }}</h3>
              <p class="text-xs text-[#c4c6d0] mt-0.5">Jangan bekukan jika Spotify / musik menyala</p>
            </div>
            <input
              v-model="tweaksStore.skipAudio"
              @change="tweaksStore.saveEcoConfig"
              type="checkbox"
              class="w-4 h-4 rounded text-[#a8c7fa] cursor-pointer"
            />
          </div>

          <!-- Delay -->
          <div class="md3-list-item px-5 py-4 space-y-2">
            <div class="flex justify-between text-xs">
              <span class="font-medium text-[#e2e2e9]">{{ t('settings_page.hibernate.delay') }}</span>
              <span class="font-bold font-mono text-[#a8c7fa]">{{ tweaksStore.ecoDelay }} detik</span>
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
        </div>

        <!-- Package list editor -->
        <div v-if="tweaksStore.ecoEnabled" class="bg-[#1e1f25] p-5 rounded-2xl space-y-2 text-[#e2e2e9]">
          <h3 class="text-sm font-medium">{{ t('settings_page.hibernate.list') }}</h3>
          <p class="text-xs text-[#c4c6d0]">Satu package name per baris:</p>
          <textarea
            v-model="tweaksStore.rawHibernateList"
            rows="5"
            class="w-full bg-[#111318] border border-white/10 rounded-xl p-3 text-xs font-mono text-[#e2e2e9] focus:outline-none focus:border-[#a8c7fa]"
          ></textarea>
          <button
            @click="tweaksStore.saveEcoConfig"
            class="w-full py-2.5 rounded-xl bg-[#234475] text-[#d6e3ff] text-xs font-bold transition-all"
          >
            {{ t('common.save') }}
          </button>
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
