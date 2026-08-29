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
          {{ t('settings_page.renderer.title') }}
        </h1>

        <div class="space-y-1.5">
          <div
            v-for="opt in rendererOptions"
            :key="opt.id"
            class="md3-list"
          >
            <div
              @click="selectRenderer(opt.id)"
              class="md3-list-item px-5 py-4 flex items-center justify-between cursor-pointer"
            >
              <div class="pr-3 flex-1 min-w-0">
                <h3 class="text-sm font-medium text-on-surface">{{ opt.name }}</h3>
                <p class="text-xs text-on-surface-variant mt-0.5">{{ opt.desc }}</p>
              </div>
              <div
                class="w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0"
                :class="tweaksStore.currentRenderer === opt.id ? 'border-primary' : 'border-outline'"
              >
                <div
                  v-if="tweaksStore.currentRenderer === opt.id"
                  class="w-2.5 h-2.5 rounded-full bg-primary"
                ></div>
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
import { useZenithTweaksStore } from '@/stores/ZenithTweaks'
import { useLocales } from '@/helpers/Locales'

const router = useRouter()
const tweaksStore = useZenithTweaksStore()
const { t } = useLocales()

const rendererOptions = [
  { id: 'skiaglthreaded', name: 'SkiaGL Multi-Threaded', desc: 'Renderer 2D paling optimal untuk GPU Mali / Adreno.' },
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
