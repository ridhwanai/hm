<template>
  <div class="page settings-page h-full flex flex-col overflow-hidden bg-surface">
    <div class="sticky top-0 z-10 bg-background">
      <div class="max-w-3xl mx-auto p-5 pb-3">
        <div class="flex justify-between items-center text-on-surface">
          <h1 class="text-xl font-semibold">{{ t('settings_page.title') }}</h1>
        </div>
      </div>
    </div>

    <div class="scrollbar-hidden pb-safe-nav flex-1 min-h-0 overflow-y-scroll">
      <div class="max-w-3xl mx-auto p-5 py-1">
        <!-- Section: Preferences -->
        <div class="px-4 py-2 mb-1">
          <h2 class="text-sm font-medium text-on-surface-variant">
            {{ t('settings_page.section.tweaks') }}
          </h2>
        </div>

        <div class="space-y-1.5 mb-4">
          <!-- Lite Mode -->
          <div class="md3-list">
            <RippleComponent @click="router.push('/settings/lite_mode')" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-primary-container flex items-center justify-center shrink-0">
                    <FeatherIcon class="w-5 h-5 text-on-primary-container" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-on-surface">
                      Mode Lite
                    </h3>
                    <p class="text-xs text-on-surface-variant mt-1 line-clamp-2">
                      Optimasi clockspeed hemat daya & suhu adem saat bermain game
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-surface-dim flex items-center justify-center shrink-0 ms-3">
                  <ChevronRightIcon class="text-on-surface-variant shrink-0 rtl:rotate-180" :size="22" />
                </div>
              </div>
            </RippleComponent>
          </div>

          <!-- Memory Tuning -->
          <div class="md3-list">
            <RippleComponent @click="router.push('/settings/memory')" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-primary-container flex items-center justify-center shrink-0">
                    <TuneIcon class="w-5 h-5 text-on-primary-container" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-on-surface">
                      {{ t('settings_page.memory.title') }}
                    </h3>
                    <p class="text-xs text-on-surface-variant mt-1 line-clamp-2">
                      {{ t('settings_page.memory.description') }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-surface-dim flex items-center justify-center shrink-0 ms-3">
                  <ChevronRightIcon class="text-on-surface-variant shrink-0 rtl:rotate-180" :size="22" />
                </div>
              </div>
            </RippleComponent>
          </div>

          <!-- Language -->
          <div class="md3-list">
            <RippleComponent @click="router.push('/settings/language')" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-primary-container flex items-center justify-center shrink-0">
                    <LanguageIcon class="w-5 h-5 text-on-primary-container" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-on-surface">
                      {{ t('settings_page.language.title') }}
                    </h3>
                    <p class="text-xs text-on-surface-variant mt-1">
                      {{ currentLanguage === 'id' ? 'Bahasa Indonesia (ID)' : 'English (EN)' }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-surface-dim flex items-center justify-center shrink-0 ms-3">
                  <ChevronRightIcon class="text-on-surface-variant shrink-0 rtl:rotate-180" :size="22" />
                </div>
              </div>
            </RippleComponent>
          </div>
        </div>

        <!-- Section: System -->
        <div class="px-4 py-2 mb-1">
          <h2 class="text-sm font-medium text-on-surface-variant">
            {{ t('settings_page.section.system') }}
          </h2>
        </div>

        <div class="space-y-1.5 mb-4">
          <!-- Screen-Off ECO -->
          <div class="md3-list">
            <RippleComponent @click="router.push('/settings/hibernate')" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-primary-container flex items-center justify-center shrink-0">
                    <BugIcon class="w-5 h-5 text-on-primary-container" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-on-surface">
                      {{ t('settings_page.hibernate.title') }}
                    </h3>
                    <p class="text-xs text-on-surface-variant mt-1 line-clamp-2">
                      {{ t('settings_page.hibernate.description') }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-surface-dim flex items-center justify-center shrink-0 ms-3">
                  <ChevronRightIcon class="text-on-surface-variant shrink-0 rtl:rotate-180" :size="22" />
                </div>
              </div>
            </RippleComponent>
          </div>

          <!-- HWUI Renderer -->
          <div class="md3-list">
            <RippleComponent @click="router.push('/settings/renderer')" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-primary-container flex items-center justify-center shrink-0">
                    <ChipsetIcon class="w-5 h-5 text-on-primary-container" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-on-surface">
                      {{ t('settings_page.renderer.title') }}
                    </h3>
                    <p class="text-xs text-on-surface-variant mt-1 line-clamp-2">
                      {{ t('settings_page.renderer.description') }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-surface-dim flex items-center justify-center shrink-0 ms-3">
                  <ChevronRightIcon class="text-on-surface-variant shrink-0 rtl:rotate-180" :size="22" />
                </div>
              </div>
            </RippleComponent>
          </div>

          <!-- FSTRIM Maintenance -->
          <div class="md3-list">
            <RippleComponent @click="handleRunFstrim" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-primary-container flex items-center justify-center shrink-0">
                    <TextIcon class="w-5 h-5 text-on-primary-container" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-on-surface">
                      {{ t('settings_page.fstrim.title') }}
                    </h3>
                    <p class="text-xs text-on-surface-variant mt-1 line-clamp-2">
                      {{ isRunningFstrim ? 'Menjalankan FSTRIM...' : t('settings_page.fstrim.description') }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-surface-dim flex items-center justify-center shrink-0 ms-3">
                  <ChevronRightIcon class="text-on-surface-variant shrink-0 rtl:rotate-180" :size="22" />
                </div>
              </div>
            </RippleComponent>
          </div>
        </div>

        <!-- Section: Others -->
        <div class="px-4 py-2 mb-1">
          <h2 class="text-sm font-medium text-on-surface-variant">
            {{ t('settings_page.section.others') }}
          </h2>
        </div>

        <div class="space-y-1.5 mb-4">
          <!-- Live Logs -->
          <div class="md3-list">
            <RippleComponent @click="router.push('/settings/logs')" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-primary-container flex items-center justify-center shrink-0">
                    <ContentSaveIcon class="w-5 h-5 text-on-primary-container" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-on-surface">
                      {{ t('settings_page.logs.title') }}
                    </h3>
                    <p class="text-xs text-on-surface-variant mt-1 line-clamp-2">
                      {{ t('settings_page.logs.description') }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-surface-dim flex items-center justify-center shrink-0 ms-3">
                  <ChevronRightIcon class="text-on-surface-variant shrink-0 rtl:rotate-180" :size="22" />
                </div>
              </div>
            </RippleComponent>
          </div>

          <!-- Create Shortcut -->
          <div class="md3-list">
            <RippleComponent @click="createShortcut" class="md3-list-item" tabindex="0">
              <div class="flex items-center justify-between px-5 py-4">
                <div class="flex items-center gap-4 min-w-0 flex-1">
                  <div class="w-10 h-10 rounded-full bg-primary-container flex items-center justify-center shrink-0">
                    <HomePlusIcon class="w-5 h-5 text-on-primary-container" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-medium text-on-surface">
                      {{ t('settings_page.shortcut.title') }}
                    </h3>
                    <p class="text-xs text-on-surface-variant mt-1 line-clamp-2">
                      {{ t('settings_page.shortcut.description') }}
                    </p>
                  </div>
                </div>

                <div class="w-7 h-7 rounded-full bg-surface-dim flex items-center justify-center shrink-0 ms-3">
                  <ChevronRightIcon class="text-on-surface-variant shrink-0 rtl:rotate-180" :size="22" />
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
import { useLocales } from '@/helpers/Locales'
import * as KernelSU from '@/helpers/KernelSU'

import RippleComponent from '@/components/ui/Ripple.vue'
import ChevronRightIcon from '@/components/icons/ChevronRight.vue'
import FeatherIcon from '@/components/icons/Feather.vue'
import TuneIcon from '@/components/icons/Tune.vue'
import LanguageIcon from '@/components/icons/Language.vue'
import BugIcon from '@/components/icons/Bug.vue'
import ChipsetIcon from '@/components/icons/Chipset.vue'
import TextIcon from '@/components/icons/Text.vue'
import ContentSaveIcon from '@/components/icons/ContentSave.vue'
import HomePlusIcon from '@/components/icons/HomePlus.vue'

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
