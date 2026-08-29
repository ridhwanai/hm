<template>
  <div class="page settings-detail-page h-full flex flex-col overflow-hidden">
    <div class="max-w-3xl mx-auto h-full flex flex-col w-full">
      <!-- Top Header -->
      <div class="flex-none p-5 pb-3">
        <div class="flex items-center justify-between text-[#e2e2e9]">
          <div class="flex items-center gap-3">
            <button @click="router.back()" class="p-2 rounded-full hover:bg-white/10 transition-colors">
              <ArrowLeft class="w-5 h-5" />
            </button>
            <h1 class="text-xl font-semibold">{{ t('settings_page.logs.title') }}</h1>
          </div>

          <button @click="settingsStore.clearLogs" class="text-xs text-rose-400 font-semibold px-2 py-1">
            Bersihkan
          </button>
        </div>
      </div>

      <!-- Content -->
      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5 space-y-3 flex flex-col">
        <!-- Log Selector Tabs -->
        <div class="flex items-center gap-1 bg-[#1e1f25] p-1 rounded-xl border border-white/5">
          <button
            @click="settingsStore.setLogType('azenith')"
            class="flex-1 py-1.5 rounded-lg text-xs font-bold transition-all"
            :class="settingsStore.selectedLogType === 'azenith' ? 'bg-[#234475] text-[#d6e3ff]' : 'text-[#c4c6d0]'"
          >
            wann.log
          </button>
          <button
            @click="settingsStore.setLogType('sysmon')"
            class="flex-1 py-1.5 rounded-lg text-xs font-bold transition-all"
            :class="settingsStore.selectedLogType === 'sysmon' ? 'bg-[#234475] text-[#d6e3ff]' : 'text-[#c4c6d0]'"
          >
            sysmon.log
          </button>
        </div>

        <!-- Terminal Log Box -->
        <pre
          class="flex-1 min-h-[300px] bg-[#0c0e13] border border-white/10 rounded-2xl p-4 text-[11px] font-mono text-emerald-400 overflow-y-auto whitespace-pre-wrap select-all scrollbar-hidden"
        >{{ settingsStore.logContent || '// Belum ada log tercatat.' }}</pre>

        <!-- Copy button -->
        <button
          @click="settingsStore.copyLogs"
          class="w-full py-3 rounded-2xl bg-[#333a48] text-[#d8e2ff] text-xs font-bold transition-colors"
        >
          Salin Semua Log
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from 'lucide-vue-next'
import { useZenithSettingsStore } from '@/stores/ZenithSettings'
import { useLocales } from '@/helpers/Locales'

const router = useRouter()
const settingsStore = useZenithSettingsStore()
const { t } = useLocales()

onMounted(() => {
  settingsStore.fetchLogs()
})
</script>
