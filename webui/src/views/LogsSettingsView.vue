<template>
  <div class="space-y-4 pb-24 animate-fade-in">
    <!-- Header -->
    <div>
      <h1 class="text-xl font-extrabold text-white flex items-center gap-2">
        <TerminalSquare class="w-5 h-5 text-indigo-400" />
        {{ t('logs.title') }}
      </h1>
      <p class="text-xs text-slate-400 mt-0.5">{{ t('logs.subtitle') }}</p>
    </div>

    <!-- 1. Live Log Viewer -->
    <div class="glass-card rounded-2xl p-4 border border-white/10 space-y-3">
      <!-- Log Top Bar -->
      <div class="flex items-center justify-between">
        <!-- Log type switch -->
        <div class="flex items-center gap-1 bg-black/40 p-1 rounded-xl border border-white/5">
          <button
            @click="settingsStore.setLogType('azenith')"
            class="px-2.5 py-1 rounded-lg text-xs font-bold transition-all"
            :class="settingsStore.selectedLogType === 'azenith'
              ? 'bg-indigo-600 text-white shadow-sm'
              : 'text-slate-400 hover:text-white'"
          >
            AZenith.log
          </button>
          <button
            @click="settingsStore.setLogType('sysmon')"
            class="px-2.5 py-1 rounded-lg text-xs font-bold transition-all"
            :class="settingsStore.selectedLogType === 'sysmon'
              ? 'bg-indigo-600 text-white shadow-sm'
              : 'text-slate-400 hover:text-white'"
          >
            sysmon.log
          </button>
        </div>

        <!-- Auto Refresh Toggle -->
        <label class="flex items-center gap-1.5 cursor-pointer text-xs text-slate-300">
          <input
            type="checkbox"
            :checked="settingsStore.isAutoRefresh"
            @change="settingsStore.toggleAutoRefresh($event.target.checked)"
            class="w-3.5 h-3.5 rounded text-indigo-600 focus:ring-0 cursor-pointer"
          />
          <span class="text-[11px] font-medium">{{ t('logs.autoRefresh') }}</span>
        </label>
      </div>

      <!-- Terminal Output Box -->
      <div class="relative">
        <pre
          class="w-full h-56 bg-[#070a11] border border-white/10 rounded-xl p-3 text-[11px] font-mono text-emerald-400/90 overflow-y-auto whitespace-pre-wrap select-all leading-relaxed shadow-inner"
        >{{ settingsStore.logContent }}</pre>

        <!-- Floating action buttons -->
        <div class="absolute right-2 top-2 flex items-center gap-1">
          <button
            @click="settingsStore.fetchLogs"
            class="p-1.5 bg-slate-800/80 hover:bg-slate-700 text-slate-300 rounded-lg backdrop-blur-md transition-colors"
            :title="t('common.refresh')"
          >
            <RefreshCw class="w-3.5 h-3.5" :class="{ 'animate-spin': settingsStore.isFetchingLogs }" />
          </button>
          <button
            @click="settingsStore.copyLogs"
            class="p-1.5 bg-slate-800/80 hover:bg-slate-700 text-slate-300 rounded-lg backdrop-blur-md transition-colors"
            :title="t('logs.copyLogs')"
          >
            <Copy class="w-3.5 h-3.5" />
          </button>
          <button
            @click="settingsStore.clearLogs"
            class="p-1.5 bg-slate-800/80 hover:bg-rose-600/80 text-slate-300 hover:text-white rounded-lg backdrop-blur-md transition-colors"
            :title="t('logs.clearLogs')"
          >
            <Trash2 class="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>

    <!-- 2. Quick Preferences (Language & Shortcut) -->
    <div class="grid grid-cols-2 gap-3">
      <!-- Shortcut Button -->
      <button
        @click="settingsStore.createShortcut"
        class="glass-card glass-card-hover p-3.5 rounded-2xl border border-white/10 flex flex-col items-center justify-center text-center space-y-1.5 transition-all"
      >
        <div class="w-8 h-8 rounded-xl bg-indigo-500/20 text-indigo-400 flex items-center justify-center">
          <Smartphone class="w-4 h-4" />
        </div>
        <span class="text-xs font-bold text-slate-100">{{ t('logs.shortcutBtn') }}</span>
      </button>

      <!-- Language Selector -->
      <div class="glass-card p-3.5 rounded-2xl border border-white/10 flex flex-col items-center justify-center text-center space-y-1.5">
        <div class="w-8 h-8 rounded-xl bg-cyan-500/20 text-cyan-400 flex items-center justify-center">
          <Globe class="w-4 h-4" />
        </div>
        <div class="flex items-center gap-1 bg-black/40 p-0.5 rounded-lg border border-white/5">
          <button
            @click="setLanguage('id')"
            class="px-2 py-0.5 rounded text-[10px] font-bold transition-all"
            :class="currentLanguage === 'id' ? 'bg-cyan-600 text-white' : 'text-slate-400'"
          >
            ID 🇮🇩
          </button>
          <button
            @click="setLanguage('en')"
            class="px-2 py-0.5 rounded text-[10px] font-bold transition-all"
            :class="currentLanguage === 'en' ? 'bg-cyan-600 text-white' : 'text-slate-400'"
          >
            EN 🇬🇧
          </button>
        </div>
      </div>
    </div>

    <!-- 3. About & Credits Card -->
    <div class="glass-card rounded-2xl p-4 border border-white/10 space-y-3 text-xs">
      <div class="flex items-center justify-between pb-2 border-b border-white/5">
        <h3 class="font-bold text-slate-100 flex items-center gap-1.5">
          <Info class="w-4 h-4 text-indigo-400" />
          {{ t('logs.aboutTitle') }}
        </h3>
        <span class="text-indigo-400 font-mono font-bold">{{ homeStore.moduleVersion }}</span>
      </div>

      <div class="space-y-2 text-slate-300">
        <div>
          <div class="text-[10px] text-slate-400 uppercase font-medium">{{ t('logs.authors') }}</div>
          <p class="mt-0.5 font-medium leading-relaxed">
            • Remake & WebUI: <span class="text-indigo-300 font-bold">@wann</span><br>
            • Original Authors: <span class="text-slate-200">@Zexshia, @rianixia, @kanaochar</span><br>
            • References & Tweak Sources: <span class="text-slate-400">@Rem01Gaming (Encore), @MiAzami, @Feravolt</span>
          </p>
        </div>

        <div class="pt-1 border-t border-white/5">
          <div class="text-[10px] text-slate-400 uppercase font-medium">{{ t('logs.license') }}</div>
          <p class="text-[11px] text-slate-400 mt-0.5">{{ t('logs.licenseText') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { TerminalSquare, RefreshCw, Copy, Trash2, Smartphone, Globe, Info } from 'lucide-vue-next'
import { useZenithSettingsStore } from '@/stores/ZenithSettings'
import { useZenithHomeStore } from '@/stores/ZenithHome'
import { useLocales } from '@/helpers/Locales'

const settingsStore = useZenithSettingsStore()
const homeStore = useZenithHomeStore()
const { t, currentLanguage, setLanguage } = useLocales()

onMounted(() => {
  settingsStore.fetchLogs()
})
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in {
  animation: fadeIn 0.25s ease-out forwards;
}
</style>
