<template>
  <div class="h-screen flex flex-col justify-between bg-[#111318] text-[#e2e2e9] overflow-hidden">
    <!-- Main Content View -->
    <main class="flex-1 overflow-hidden relative">
      <DashboardView v-if="activeTab === 'home'" />
      <GamesView v-else-if="activeTab === 'games'" />
      <TweaksView v-else-if="activeTab === 'settings'" />
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

const activeTab = ref('home')
</script>
