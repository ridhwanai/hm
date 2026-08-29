<template>
  <div class="space-y-4 pb-24 animate-fade-in">
    <!-- Header -->
    <div>
      <h1 class="text-xl font-extrabold text-white flex items-center gap-2">
        <SlidersHorizontal class="w-5 h-5 text-indigo-400" />
        {{ t('tweaks.title') }}
      </h1>
      <p class="text-xs text-slate-400 mt-0.5">{{ t('tweaks.subtitle') }}</p>
    </div>

    <!-- 1. Memory & ZRAM Tuning Card -->
    <div class="glass-card rounded-2xl p-4 border border-white/10 space-y-4">
      <div class="flex items-start justify-between">
        <div class="flex items-center gap-2.5">
          <div class="w-9 h-9 rounded-xl bg-indigo-500/15 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
            <MemoryStick class="w-5 h-5" />
          </div>
          <div>
            <h3 class="text-sm font-bold text-white">{{ t('tweaks.memory.title') }}</h3>
            <p class="text-[11px] text-slate-400 leading-tight">{{ t('tweaks.memory.desc') }}</p>
          </div>
        </div>

        <button
          @click="tweaksStore.memEnabled = !tweaksStore.memEnabled"
          class="relative inline-flex h-5 w-10 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200"
          :class="tweaksStore.memEnabled ? 'bg-indigo-600' : 'bg-slate-700'"
        >
          <span
            class="pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white transition duration-200"
            :class="tweaksStore.memEnabled ? 'translate-x-5' : 'translate-x-0'"
          ></span>
        </button>
      </div>

      <div v-if="tweaksStore.memEnabled" class="space-y-3.5 pt-2 border-t border-white/5 text-xs">
        <!-- ZRAM Size -->
        <div class="space-y-1.5">
          <div class="flex justify-between font-medium">
            <span class="text-slate-300">{{ t('tweaks.memory.zramSize') }}</span>
            <span class="text-indigo-400 font-bold font-mono">{{ tweaksStore.zramSizeMB }} MB</span>
          </div>
          <input
            v-model.number="tweaksStore.zramSizeMB"
            type="range"
            min="512"
            max="4096"
            step="256"
            class="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer"
          />
          <div class="flex justify-between text-[10px] text-slate-500 font-mono">
            <span>512 MB</span>
            <span>2048 MB (Default)</span>
            <span>4096 MB</span>
          </div>
        </div>

        <!-- Swappiness -->
        <div class="space-y-1.5">
          <div class="flex justify-between font-medium">
            <span class="text-slate-300">{{ t('tweaks.memory.swappiness') }}</span>
            <span class="text-indigo-400 font-bold font-mono">{{ tweaksStore.swappiness }}</span>
          </div>
          <input
            v-model.number="tweaksStore.swappiness"
            type="range"
            min="0"
            max="200"
            step="10"
            class="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer"
          />
          <p class="text-[10px] text-slate-400 leading-tight">{{ t('tweaks.memory.swappinessDesc') }}</p>
        </div>

        <!-- Algo Selector -->
        <div class="space-y-1.5">
          <label class="text-slate-300 font-medium block">{{ t('tweaks.memory.algo') }}</label>
          <div class="grid grid-cols-4 gap-1.5">
            <button
              v-for="algo in ['lz4', 'zstd', 'lzo', 'zram']"
              :key="algo"
              @click="tweaksStore.memAlgo = algo"
              class="py-1.5 rounded-xl text-xs font-bold uppercase font-mono border transition-all"
              :class="tweaksStore.memAlgo === algo
                ? 'bg-indigo-600 border-indigo-400 text-white shadow-md'
                : 'bg-black/30 border-white/5 text-slate-400 hover:text-white'"
            >
              {{ algo }}
            </button>
          </div>
        </div>

        <!-- Apply Button -->
        <button
          @click="tweaksStore.applyMemoryTuning"
          :disabled="tweaksStore.isApplyingMem"
          class="w-full py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-700 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-lg shadow-indigo-500/25 active:scale-98 transition-all"
        >
          <Sparkles class="w-4 h-4" :class="{ 'animate-spin': tweaksStore.isApplyingMem }" />
          <span>{{ tweaksStore.isApplyingMem ? t('tweaks.memory.applying') : t('tweaks.memory.applyBtn') }}</span>
        </button>

        <!-- Status Result Feedback -->
        <div v-if="tweaksStore.memStatusMsg" class="p-2.5 rounded-xl bg-black/40 border border-indigo-500/20 text-[11px] font-mono text-indigo-300 break-words">
          {{ tweaksStore.memStatusMsg }}
        </div>
      </div>
    </div>

    <!-- 2. Screen-Off ECO Hibernation Card -->
    <div class="glass-card rounded-2xl p-4 border border-white/10 space-y-4">
      <div class="flex items-start justify-between">
        <div class="flex items-center gap-2.5">
          <div class="w-9 h-9 rounded-xl bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
            <Moon class="w-5 h-5" />
          </div>
          <div>
            <h3 class="text-sm font-bold text-white">{{ t('tweaks.hibernate.title') }}</h3>
            <p class="text-[11px] text-slate-400 leading-tight">{{ t('tweaks.hibernate.desc') }}</p>
          </div>
        </div>

        <button
          @click="tweaksStore.ecoEnabled = !tweaksStore.ecoEnabled; tweaksStore.saveEcoConfig()"
          class="relative inline-flex h-5 w-10 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200"
          :class="tweaksStore.ecoEnabled ? 'bg-emerald-600' : 'bg-slate-700'"
        >
          <span
            class="pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white transition duration-200"
            :class="tweaksStore.ecoEnabled ? 'translate-x-5' : 'translate-x-0'"
          ></span>
        </button>
      </div>

      <div v-if="tweaksStore.ecoEnabled" class="space-y-3 pt-2 border-t border-white/5 text-xs">
        <!-- Default Mode Selector (full vs restrict) -->
        <div class="space-y-1.5">
          <label class="text-slate-300 font-medium block">{{ t('tweaks.hibernate.modeTitle') }}</label>
          <div class="grid grid-cols-2 gap-2">
            <button
              @click="tweaksStore.ecoModeDefault = 'full'; tweaksStore.saveEcoConfig()"
              class="p-2.5 rounded-xl border text-left transition-all"
              :class="tweaksStore.ecoModeDefault === 'full'
                ? 'bg-emerald-500/20 border-emerald-500/50 text-white'
                : 'bg-black/25 border-white/5 text-slate-400'"
            >
              <div class="font-bold text-white flex items-center gap-1.5">
                <ShieldAlert class="w-3.5 h-3.5 text-emerald-400" />
                Full Eco
              </div>
              <p class="text-[10px] text-slate-400 mt-0.5">Restricted + Force Stop</p>
            </button>

            <button
              @click="tweaksStore.ecoModeDefault = 'restrict'; tweaksStore.saveEcoConfig()"
              class="p-2.5 rounded-xl border text-left transition-all"
              :class="tweaksStore.ecoModeDefault === 'restrict'
                ? 'bg-emerald-500/20 border-emerald-500/50 text-white'
                : 'bg-black/25 border-white/5 text-slate-400'"
            >
              <div class="font-bold text-white flex items-center gap-1.5">
                <Bell class="w-3.5 h-3.5 text-emerald-400" />
                Restrict Mode
              </div>
              <p class="text-[10px] text-slate-400 mt-0.5">Notif FCM tetap masuk</p>
            </button>
          </div>
        </div>

        <!-- Skip Conditions -->
        <div class="space-y-2 pt-1">
          <!-- Skip Charging -->
          <div class="flex items-center justify-between p-2.5 rounded-xl bg-black/25 border border-white/5">
            <div>
              <div class="font-bold text-slate-200">{{ t('tweaks.hibernate.skipCharging') }}</div>
              <div class="text-[10px] text-slate-400">{{ t('tweaks.hibernate.skipChargingDesc') }}</div>
            </div>
            <input
              v-model="tweaksStore.skipCharging"
              @change="tweaksStore.saveEcoConfig"
              type="checkbox"
              class="w-4 h-4 rounded text-emerald-600 focus:ring-0 cursor-pointer"
            />
          </div>

          <!-- Skip Audio -->
          <div class="flex items-center justify-between p-2.5 rounded-xl bg-black/25 border border-white/5">
            <div>
              <div class="font-bold text-slate-200">{{ t('tweaks.hibernate.skipAudio') }}</div>
              <div class="text-[10px] text-slate-400">{{ t('tweaks.hibernate.skipAudioDesc') }}</div>
            </div>
            <input
              v-model="tweaksStore.skipAudio"
              @change="tweaksStore.saveEcoConfig"
              type="checkbox"
              class="w-4 h-4 rounded text-emerald-600 focus:ring-0 cursor-pointer"
            />
          </div>
        </div>

        <!-- Delay -->
        <div class="space-y-1.5">
          <div class="flex justify-between font-medium">
            <span class="text-slate-300">{{ t('tweaks.hibernate.delay') }}</span>
            <span class="text-emerald-400 font-bold font-mono">{{ tweaksStore.ecoDelay }}s ({{ Math.round(tweaksStore.ecoDelay / 60) }} menit)</span>
          </div>
          <input
            v-model.number="tweaksStore.ecoDelay"
            @change="tweaksStore.saveEcoConfig"
            type="range"
            min="30"
            max="600"
            step="30"
            class="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer"
          />
        </div>

        <!-- Manage List Button -->
        <button
          @click="isEcoModalOpen = true"
          class="w-full py-2 rounded-xl bg-black/30 hover:bg-black/40 border border-white/10 text-slate-200 text-xs font-semibold flex items-center justify-center gap-2 transition-colors"
        >
          <ListFilter class="w-4 h-4 text-emerald-400" />
          <span>{{ t('tweaks.hibernate.manageList') }} ({{ tweaksStore.hibernateApps.length }} Paket)</span>
        </button>
      </div>
    </div>

    <!-- 3. HWUI Graphics Renderer Card -->
    <div class="glass-card rounded-2xl p-4 border border-white/10 space-y-3">
      <div class="flex items-center gap-2.5">
        <div class="w-9 h-9 rounded-xl bg-cyan-500/15 border border-cyan-500/30 flex items-center justify-center text-cyan-400">
          <Layers class="w-5 h-5" />
        </div>
        <div>
          <h3 class="text-sm font-bold text-white">{{ t('tweaks.renderer.title') }}</h3>
          <p class="text-[11px] text-slate-400 leading-tight">{{ t('tweaks.renderer.desc') }}</p>
        </div>
      </div>

      <div class="space-y-1.5 pt-1 text-xs">
        <div
          v-for="(label, key) in rendererOptions"
          :key="key"
          @click="tweaksStore.setRenderer(key)"
          class="p-2.5 rounded-xl border flex items-center justify-between cursor-pointer transition-all"
          :class="tweaksStore.currentRenderer === key
            ? 'bg-cyan-500/20 border-cyan-500/50 text-white font-bold'
            : 'bg-black/25 border-white/5 text-slate-300 hover:border-white/15'"
        >
          <span>{{ label }}</span>
          <Check v-if="tweaksStore.currentRenderer === key" class="w-4 h-4 text-cyan-400 shrink-0" />
        </div>
      </div>
    </div>

    <!-- 4. Storage Maintenance (FSTRIM) Card -->
    <div class="glass-card rounded-2xl p-4 border border-white/10 space-y-3">
      <div class="flex items-start justify-between">
        <div class="flex items-center gap-2.5">
          <div class="w-9 h-9 rounded-xl bg-amber-500/15 border border-amber-500/30 flex items-center justify-center text-amber-400">
            <HardDrive class="w-5 h-5" />
          </div>
          <div>
            <h3 class="text-sm font-bold text-white">{{ t('tweaks.fstrim.title') }}</h3>
            <p class="text-[11px] text-slate-400 leading-tight">{{ t('tweaks.fstrim.desc') }}</p>
          </div>
        </div>

        <button
          @click="tweaksStore.fstrimEnabled = !tweaksStore.fstrimEnabled; tweaksStore.saveFstrimConfig()"
          class="relative inline-flex h-5 w-10 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200"
          :class="tweaksStore.fstrimEnabled ? 'bg-amber-600' : 'bg-slate-700'"
        >
          <span
            class="pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white transition duration-200"
            :class="tweaksStore.fstrimEnabled ? 'translate-x-5' : 'translate-x-0'"
          ></span>
        </button>
      </div>

      <div class="space-y-2 pt-1 text-xs">
        <button
          @click="tweaksStore.runFstrimNow"
          :disabled="tweaksStore.isRunningFstrim"
          class="w-full py-2.5 rounded-xl bg-amber-600/80 hover:bg-amber-500 text-white font-bold flex items-center justify-center gap-2 shadow-md transition-all"
        >
          <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': tweaksStore.isRunningFstrim }" />
          <span>{{ tweaksStore.isRunningFstrim ? t('tweaks.fstrim.running') : t('tweaks.fstrim.runNow') }}</span>
        </button>

        <div v-if="tweaksStore.fstrimResult" class="p-2.5 rounded-xl bg-black/40 border border-white/10 text-[11px] font-mono text-amber-300 break-words whitespace-pre-line">
          {{ tweaksStore.fstrimResult }}
        </div>
      </div>
    </div>

    <!-- Modal: Manage Hibernate Apps -->
    <Modal :isOpen="isEcoModalOpen" @close="isEcoModalOpen = false" title="Daftar Paket Hibernasi">
      <div class="space-y-3 text-xs">
        <p class="text-slate-400 leading-relaxed">
          Tuliskan nama paket aplikasi (satu per baris) yang ingin dibekukan saat layar mati.
        </p>

        <textarea
          v-model="tweaksStore.rawHibernateList"
          rows="8"
          placeholder="com.facebook.katana&#10;com.instagram.android&#10;com.shopee.id"
          class="w-full bg-[#0d131f] border border-white/10 rounded-xl p-3 text-xs font-mono text-slate-100 placeholder:text-slate-600 focus:outline-none focus:border-emerald-500"
        ></textarea>
      </div>

      <template #footer>
        <button
          @click="isEcoModalOpen = false"
          class="px-3 py-1.5 rounded-xl text-xs font-semibold text-slate-400 hover:text-white"
        >
          {{ t('common.cancel') }}
        </button>
        <button
          @click="saveHibernateList"
          class="px-4 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold"
        >
          {{ t('common.save') }}
        </button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  SlidersHorizontal,
  MemoryStick,
  Sparkles,
  Moon,
  ShieldAlert,
  Bell,
  ListFilter,
  Layers,
  Check,
  HardDrive,
  RefreshCw,
} from 'lucide-vue-next'
import { useZenithTweaksStore } from '@/stores/ZenithTweaks'
import { useLocales } from '@/helpers/Locales'
import Modal from '@/components/Modal.vue'

const tweaksStore = useZenithTweaksStore()
const { t } = useLocales()

const isEcoModalOpen = ref(false)

onMounted(() => {
  tweaksStore.loadAllTweaks()
})

const rendererOptions = computed(() => ({
  skiaglthreaded: t('tweaks.renderer.options.skiaglthreaded'),
  skiagl: t('tweaks.renderer.options.skiagl'),
  skiavkthreaded: t('tweaks.renderer.options.skiavkthreaded'),
  skiavk: t('tweaks.renderer.options.skiavk'),
  opengl: t('tweaks.renderer.options.opengl'),
  default: t('tweaks.renderer.options.default'),
}))

async function saveHibernateList() {
  await tweaksStore.saveEcoConfig()
  isEcoModalOpen.value = false
}
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
