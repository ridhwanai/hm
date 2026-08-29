<template>
  <div class="page settings-page h-full flex flex-col overflow-hidden">
    <!-- Header -->
    <div class="sticky top-0 z-10 bg-[#111318]">
      <div class="max-w-3xl mx-auto p-5 pb-3">
        <div class="flex justify-between items-center text-[#e2e2e9]">
          <h1 class="text-xl font-semibold">{{ t('settings_page.title') }}</h1>
        </div>
      </div>
    </div>

    <!-- Scrollable Content -->
    <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll">
      <div class="max-w-3xl mx-auto p-5 py-1">
        <!-- Section: Tweaks -->
        <div class="px-4 py-2 mb-1">
          <h2 class="text-sm font-medium text-[#c4c6d0]">
            {{ t('settings_page.section.tweaks') }}
          </h2>
        </div>

        <div class="space-y-1.5 mb-4">
          <!-- Memory Tuning -->
          <div class="md3-list">
            <RippleComponent @click="router.push('/settings/memory')" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-[#234475] flex items-center justify-center shrink-0">
                    <MemoryStick class="w-5 h-5 text-[#d6e3ff]" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-[#e2e2e9]">
                      {{ t('settings_page.memory.title') }}
                    </h3>
                    <p class="text-xs text-[#c4c6d0] mt-0.5 truncate">
                      {{ t('settings_page.memory.description') }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-[#191b20] flex items-center justify-center shrink-0 ms-3">
                  <ChevronRight class="text-[#c4c6d0] w-4 h-4" />
                </div>
              </div>
            </RippleComponent>
          </div>

          <!-- Screen-Off ECO -->
          <div class="md3-list">
            <RippleComponent @click="router.push('/settings/hibernate')" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-[#234475] flex items-center justify-center shrink-0">
                    <Moon class="w-5 h-5 text-[#d6e3ff]" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-[#e2e2e9]">
                      {{ t('settings_page.hibernate.title') }}
                    </h3>
                    <p class="text-xs text-[#c4c6d0] mt-0.5 truncate">
                      {{ t('settings_page.hibernate.description') }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-[#191b20] flex items-center justify-center shrink-0 ms-3">
                  <ChevronRight class="text-[#c4c6d0] w-4 h-4" />
                </div>
              </div>
            </RippleComponent>
          </div>

          <!-- HWUI Renderer -->
          <div class="md3-list">
            <RippleComponent @click="router.push('/settings/renderer')" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-[#234475] flex items-center justify-center shrink-0">
                    <Layers class="w-5 h-5 text-[#d6e3ff]" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-[#e2e2e9]">
                      {{ t('settings_page.renderer.title') }}
                    </h3>
                    <p class="text-xs text-[#c4c6d0] mt-0.5 truncate">
                      {{ t('settings_page.renderer.description') }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-[#191b20] flex items-center justify-center shrink-0 ms-3">
                  <ChevronRight class="text-[#c4c6d0] w-4 h-4" />
                </div>
              </div>
            </RippleComponent>
          </div>
        </div>

        <!-- Section: System -->
        <div class="px-4 py-2 mb-1">
          <h2 class="text-sm font-medium text-[#c4c6d0]">
            {{ t('settings_page.section.system') }}
          </h2>
        </div>

        <div class="space-y-1.5 mb-4">
          <!-- FSTRIM Maintenance -->
          <div class="md3-list">
            <RippleComponent @click="handleRunFstrim" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-[#234475] flex items-center justify-center shrink-0">
                    <HardDrive class="w-5 h-5 text-[#d6e3ff]" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-[#e2e2e9]">
                      {{ t('settings_page.fstrim.title') }}
                    </h3>
                    <p class="text-xs text-[#c4c6d0] mt-0.5 truncate">
                      {{ isRunningFstrim ? 'Menjalankan FSTRIM...' : t('settings_page.fstrim.description') }}
                    </p>
                  </div>
                </div>

                <div class="px-3 py-1 rounded-full bg-[#333a48] text-[11px] font-bold text-[#d8e2ff] shrink-0 ms-3">
                  Trim
                </div>
              </div>
            </RippleComponent>
          </div>

          <!-- Live Logs -->
          <div class="md3-list">
            <RippleComponent @click="router.push('/settings/logs')" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-[#234475] flex items-center justify-center shrink-0">
                    <FileText class="w-5 h-5 text-[#d6e3ff]" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-[#e2e2e9]">
                      {{ t('settings_page.logs.title') }}
                    </h3>
                    <p class="text-xs text-[#c4c6d0] mt-0.5 truncate">
                      {{ t('settings_page.logs.description') }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-[#191b20] flex items-center justify-center shrink-0 ms-3">
                  <ChevronRight class="text-[#c4c6d0] w-4 h-4" />
                </div>
              </div>
            </RippleComponent>
          </div>
        </div>

        <!-- Section: Others -->
        <div class="px-4 py-2 mb-1">
          <h2 class="text-sm font-medium text-[#c4c6d0]">
            {{ t('settings_page.section.others') }}
          </h2>
        </div>

        <div class="space-y-1.5 mb-4">
          <!-- Homescreen Shortcut -->
          <div class="md3-list">
            <RippleComponent @click="createShortcut" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-[#234475] flex items-center justify-center shrink-0">
                    <Home class="w-5 h-5 text-[#d6e3ff]" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-[#e2e2e9]">
                      {{ t('settings_page.shortcut.title') }}
                    </h3>
                    <p class="text-xs text-[#c4c6d0] mt-0.5 truncate">
                      {{ t('settings_page.shortcut.description') }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-[#191b20] flex items-center justify-center shrink-0 ms-3">
                  <ChevronRight class="text-[#c4c6d0] w-4 h-4" />
                </div>
              </div>
            </RippleComponent>
          </div>

          <!-- Language -->
          <div class="md3-list">
            <RippleComponent @click="router.push('/settings/language')" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-[#234475] flex items-center justify-center shrink-0">
                    <Globe class="w-5 h-5 text-[#d6e3ff]" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-[#e2e2e9]">
                      {{ t('settings_page.language.title') }}
                    </h3>
                    <p class="text-xs text-[#c4c6d0] mt-0.5">
                      {{ currentLanguage === 'id' ? 'Bahasa Indonesia (ID)' : 'English (EN)' }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-[#191b20] flex items-center justify-center shrink-0 ms-3">
                  <ChevronRight class="text-[#c4c6d0] w-4 h-4" />
                </div>
              </div>
            </RippleComponent>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ChevronRight,
  MemoryStick,
  Moon,
  Layers,
  HardDrive,
  FileText,
  Home,
  Globe,
} from 'lucide-vue-next'
import { useLocales } from '@/helpers/Locales'
import * as KernelSU from '@/helpers/KernelSU'
import RippleComponent from '@/components/ui/Ripple.vue'

const router = useRouter()
const { t, currentLanguage } = useLocales()

const isRunningFstrim = ref(false)

async function handleRunFstrim() {
  if (isRunningFstrim.value) return
  isRunningFstrim.value = true
  KernelSU.toast('Menjalankan FSTRIM...')
  try {
    const { stdout } = await KernelSU.exec('fstrim -v /data /cache /system 2>&1 || fstrim -v /data 2>&1')
    KernelSU.toast(stdout.trim() ? `FSTRIM Selesai: ${stdout.trim()}` : 'FSTRIM Berhasil!')
  } catch {
    KernelSU.toast('Gagal menjalankan FSTRIM')
  } finally {
    isRunningFstrim.value = false
  }
}

function createShortcut() {
  KernelSU.createShortcut()
}
</script>
