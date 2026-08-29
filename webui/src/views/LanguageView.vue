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
      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5 space-y-6">
        <h1 class="text-4xl text-on-surface mt-10 mb-6 font-normal">
          {{ t('settings_page.language.title') }}
        </h1>

        <div class="space-y-1.5">
          <!-- Indonesian -->
          <div class="md3-list">
            <div
              @click="setLanguage('id')"
              class="md3-list-item px-5 py-4 flex items-center justify-between cursor-pointer"
            >
              <div class="flex items-center gap-3">
                <span class="text-xl">🇮🇩</span>
                <div>
                  <h3 class="text-sm font-medium text-on-surface">Bahasa Indonesia</h3>
                  <p class="text-xs text-on-surface-variant">Indonesian</p>
                </div>
              </div>
              <div
                class="w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0"
                :class="currentLanguage === 'id' ? 'border-primary' : 'border-outline'"
              >
                <div v-if="currentLanguage === 'id'" class="w-2.5 h-2.5 rounded-full bg-primary"></div>
              </div>
            </div>
          </div>

          <!-- English -->
          <div class="md3-list">
            <div
              @click="setLanguage('en')"
              class="md3-list-item px-5 py-4 flex items-center justify-between cursor-pointer"
            >
              <div class="flex items-center gap-3">
                <span class="text-xl">🇬🇧</span>
                <div>
                  <h3 class="text-sm font-medium text-on-surface">English</h3>
                  <p class="text-xs text-on-surface-variant">English (US / UK)</p>
                </div>
              </div>
              <div
                class="w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0"
                :class="currentLanguage === 'en' ? 'border-primary' : 'border-outline'"
              >
                <div v-if="currentLanguage === 'en'" class="w-2.5 h-2.5 rounded-full bg-primary"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import ArrowLeftIcon from '@/components/icons/ArrowLeft.vue'
import { useLocales } from '@/helpers/Locales'

const router = useRouter()
const { t, currentLanguage, setLanguage } = useLocales()
</script>
