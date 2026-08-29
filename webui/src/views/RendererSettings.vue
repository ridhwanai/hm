<template>
  <div class="page settings-detail-page h-full flex flex-col overflow-hidden">
    <div class="max-w-3xl mx-auto h-full flex flex-col w-full">
      <!-- Top Header -->
      <div class="flex-none p-5 pb-3">
        <div class="flex items-center gap-3 text-[#e2e2e9]">
          <button @click="router.back()" class="p-2 rounded-full hover:bg-white/10 transition-colors">
            <ArrowLeft class="w-5 h-5" />
          </button>
          <h1 class="text-xl font-semibold">{{ t('settings_page.renderer.title') }}</h1>
        </div>
      </div>

      <!-- Content -->
      <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll px-5 space-y-4">
        <div class="md3-list-group overflow-hidden rounded-2xl space-y-0.5">
          <div
            v-for="opt in rendererOptions"
            :key="opt.id"
            @click="selectRenderer(opt.id)"
            class="md3-list-item px-5 py-4 flex items-center justify-between cursor-pointer"
          >
            <div class="pr-3">
              <h3 class="text-sm font-medium text-[#e2e2e9]">{{ opt.name }}</h3>
              <p class="text-xs text-[#c4c6d0] mt-0.5">{{ opt.desc }}</p>
            </div>
            <div
              class="w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0"
              :class="tweaksStore.currentRenderer === opt.id ? 'border-[#a8c7fa]' : 'border-slate-600'"
            >
              <div
                v-if="tweaksStore.currentRenderer === opt.id"
                class="w-2.5 h-2.5 rounded-full bg-[#a8c7fa]"
              ></div>
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
import { useZenithTweaksStore } from '@/stores/ZenithTweaks'
import { useLocales } from '@/helpers/Locales'

const router = useRouter()
const tweaksStore = useZenithTweaksStore()
const { t } = useLocales()

const rendererOptions = [
  { id: 'skiaglthreaded', name: 'SkiaGL Multi-Threaded', desc: 'Renderer 2D paling optimal untuk GPU Mali-G52 / Helio G85.' },
  { id: 'skiagl', name: 'SkiaGL (OpenGL ES)', desc: 'Skia engine standar berbasis OpenGL.' },
  { id: 'skiavkthreaded', name: 'SkiaVK Multi-Threaded (Vulkan)', desc: 'Renderer Skia modern berbasis Vulkan pipeline.' },
  { id: 'skiavk', name: 'SkiaVK (Vulkan)', desc: 'Vulkan single-threaded backend.' },
  { id: 'opengl', name: 'OpenGL Tradisional', desc: 'Fallback OpenGL renderer.' },
  { id: 'default', name: 'Default Sistem ROM', desc: 'Gunakan pengaturan bawaan dari Android OS.' },
]

function selectRenderer(id) {
  tweaksStore.setRenderer(id)
}
</script>
