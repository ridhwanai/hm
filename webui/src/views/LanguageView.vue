<template>
  <div class="page settings-detail-page h-full flex flex-col overflow-hidden">
    <div class="max-w-3xl mx-auto h-full flex flex-col w-full">
      <!-- Top Header -->
      <div class="flex-none p-5 pb-3">
        <div class="flex items-center gap-3 text-[#e2e2e9]">
          <button @click="router.back()" class="p-2 rounded-full hover:bg-white/10 transition-colors">
            <ArrowLeft class="w-5 h-5" />
          </button>
          <h1 class="text-xl font-semibold">{{ t('settings_page.language.title') }}</h1>
        </div>
      </div>

      <!-- Content -->
      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5 space-y-4">
        <div class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
          <!-- Indonesian -->
          <div
            @click="setLanguage('id')"
            class="md3-list-item px-5 py-4 flex items-center justify-between cursor-pointer"
          >
            <div class="flex items-center gap-3">
              <span class="text-xl">🇮🇩</span>
              <div>
                <h3 class="text-sm font-medium text-[#e2e2e9]">Bahasa Indonesia</h3>
                <p class="text-xs text-[#c4c6d0]">Indonesian</p>
              </div>
            </div>
            <div
              class="w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0"
              :class="currentLanguage === 'id' ? 'border-[#a8c7fa]' : 'border-slate-600'"
            >
              <div v-if="currentLanguage === 'id'" class="w-2.5 h-2.5 rounded-full bg-[#a8c7fa]"></div>
            </div>
          </div>

          <!-- English -->
          <div
            @click="setLanguage('en')"
            class="md3-list-item px-5 py-4 flex items-center justify-between cursor-pointer"
          >
            <div class="flex items-center gap-3">
              <span class="text-xl">🇬🇧</span>
              <div>
                <h3 class="text-sm font-medium text-[#e2e2e9]">English</h3>
                <p class="text-xs text-[#c4c6d0]">English (United States / UK)</p>
              </div>
            </div>
            <div
              class="w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0"
              :class="currentLanguage === 'en' ? 'border-[#a8c7fa]' : 'border-slate-600'"
            >
              <div v-if="currentLanguage === 'en'" class="w-2.5 h-2.5 rounded-full bg-[#a8c7fa]"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowLeft } from 'lucide-vue-next'
import { useLocales } from '@/helpers/Locales'

const router = useRouter()
const { t, currentLanguage, setLanguage } = useLocales()
</script>
