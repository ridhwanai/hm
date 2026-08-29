<template>
  <div class="page h-full flex flex-col overflow-hidden bg-surface">
    <div class="max-w-3xl mx-auto h-full flex flex-col w-full">
      <div class="flex-none p-5 pb-3">
        <div class="flex items-center gap-4 mb-2">
          <button @click="router.back()" class="text-on-surface transition-colors cursor-pointer">
            <ArrowLeftIcon class="w-6 h-6 rtl:rotate-180" />
          </button>
        </div>
      </div>

      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5">
        <div class="space-y-6">
          <h1 class="text-4xl text-on-surface mt-10 mb-6 font-normal">
            Mode Lite
          </h1>

          <div class="bg-primary-container rounded-3xl p-5 -mx-1.5">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <h2 class="text-base font-medium text-on-primary-container">
                  Aktifkan Mode Lite Global
                </h2>
              </div>
              <ToggleSwitch :modelValue="isLiteModeGlobal" @update:modelValue="toggleGlobalLiteMode" />
            </div>
          </div>

          <div class="flex items-start gap-3 my-6">
            <InformationOutlineIcon class="text-on-surface-variant shrink-0 mt-0.5" :size="22" />
            <p class="text-sm text-on-surface-variant leading-relaxed">
              Mode Lite membatasi clockspeed frekuensi CPU/GPU ke batas aman yang efisien untuk mencegah suhu HP cepat panas saat bermain game atau multitasking berat, sekaligus memperpanjang daya tahan baterai secara signifikan.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as KernelSU from '@/helpers/KernelSU'

import ArrowLeftIcon from '@/components/icons/ArrowLeft.vue'
import ToggleSwitch from '@/components/ui/ToggleSwitch.vue'
import InformationOutlineIcon from '@/components/icons/InformationOutline.vue'

const router = useRouter()
const isLiteModeGlobal = ref(false)

onMounted(async () => {
  try {
    const val = await KernelSU.readFile('/data/adb/.config/wann/API/lite_mode')
    isLiteModeGlobal.value = val.trim() === '1'
  } catch {
    isLiteModeGlobal.value = false
  }
})

async function toggleGlobalLiteMode(enabled) {
  isLiteModeGlobal.value = enabled
  const modeVal = enabled ? '1' : '0'
  await KernelSU.writeFile('/data/adb/.config/wann/API/lite_mode', modeVal)
  await KernelSU.writeFile('/data/adb/.config/AZenith/API/lite_mode', modeVal)
  await KernelSU.exec(`setprop persist.sys.wannconf.litemode ${modeVal}`)
  KernelSU.toast(enabled ? 'Mode Lite Global diaktifkan' : 'Mode Lite dinonaktifkan')
}
</script>
