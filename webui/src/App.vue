<template>
  <div class="min-h-screen bg-[#090d16] text-slate-100 font-['Plus_Jakarta_Sans',sans-serif] flex flex-col justify-between">
    <!-- Main Content Container -->
    <main class="w-full max-w-md mx-auto px-4 pt-4 pb-20">
      <DashboardView v-if="activeTab === 'dashboard'" />
      <GamesView v-else-if="activeTab === 'games'" />
      <TweaksView v-else-if="activeTab === 'tweaks'" />
      <LogsSettingsView v-else-if="activeTab === 'logs'" />
    </main>

    <!-- Bottom Navigation Bar -->
    <Navbar v-model:activeTab="activeTab" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Navbar from '@/components/Navbar.vue'
import DashboardView from '@/views/DashboardView.vue'
import GamesView from '@/views/GamesView.vue'
import TweaksView from '@/views/TweaksView.vue'
import LogsSettingsView from '@/views/LogsSettingsView.vue'

const activeTab = ref('dashboard')
</script>
