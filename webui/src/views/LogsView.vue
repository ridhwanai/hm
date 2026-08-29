<template>
  <div class="page settings-detail-page h-full flex flex-col overflow-hidden bg-surface">
    <div class="max-w-3xl mx-auto h-full flex flex-col w-full">
      <!-- Top Header -->
      <div class="flex-none p-5 pb-3">
        <div class="flex items-center justify-between text-on-surface">
          <div class="flex items-center gap-4">
            <button @click="router.back()" class="text-on-surface transition-colors cursor-pointer">
              <ArrowLeftIcon class="w-6 h-6 rtl:rotate-180" />
            </button>
            <h1 class="text-lg font-semibold">{{ t('settings_page.logs.title') }}</h1>
          </div>

          <button @click="settingsStore.clearLogs" class="text-xs text-error font-semibold px-2 py-1 cursor-pointer">
            Bersihkan
          </button>
        </div>
      </div>

      <!-- Content -->
      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5 space-y-3 flex flex-col">
        <!-- Log Selector Tabs -->
        <div class="flex items-center gap-1 bg-surface-container p-1 rounded-xl">
          <button
            @click="settingsStore.setLogType('wann')"
            class="flex-1 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer"
            :class="settingsStore.selectedLogType === 'wann' ? 'bg-primary-container text-on-primary-container' : 'text-on-surface-variant'"
          >
            wann.log
          </button>
          <button
            @click="settingsStore.setLogType('sysmon')"
            class="flex-1 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer"
            :class="settingsStore.selectedLogType === 'sysmon' ? 'bg-primary-container text-on-primary-container' : 'text-on-surface-variant'"
          >
            sysmon.log
          </button>
        </div>

        <!-- Terminal Log Box -->
        <pre
          class="flex-1 min-h-[320px] bg-surface-container-lowest border border-outline/20 rounded-3xl p-4 text-[11px] font-mono text-primary overflow-y-auto whitespace-pre-wrap select-all scrollbar-hidden"
        >{{ settingsStore.logContent || '// Belum ada log tercatat.' }}</pre>

        <!-- Copy button -->
        <RippleComponent
          @click="settingsStore.copyLogs"
          class="w-full py-4 rounded-3xl bg-secondary-container text-on-secondary-container text-xs font-bold text-center cursor-pointer transition-colors"
        >
          Salin Semua Log
        </RippleComponent>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ArrowLeftIcon from '@/components/icons/ArrowLeft.vue'
import RippleComponent from '@/components/ui/Ripple.vue'
import { useZenithSettingsStore } from '@/stores/ZenithSettings'
import { useLocales } from '@/helpers/Locales'

const router = useRouter()
const settingsStore = useZenithSettingsStore()
const { t } = useLocales()

onMounted(() => {
  settingsStore.fetchLogs()
})
</script>
