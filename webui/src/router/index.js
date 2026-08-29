import { createRouter, createWebHashHistory } from 'vue-router'

// Views
import Home from '@/views/Home.vue'
import Games from '@/views/Games.vue'
import GameSettings from '@/views/GameSettings.vue'
import Settings from '@/views/Settings.vue'
import MemorySettings from '@/views/MemorySettings.vue'
import HibernateSettings from '@/views/HibernateSettings.vue'
import RendererSettings from '@/views/RendererSettings.vue'
import LogsView from '@/views/LogsView.vue'
import LanguageView from '@/views/LanguageView.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  },
  {
    path: '/games',
    name: 'Games',
    component: Games,
  },
  {
    path: '/games/:packageName',
    name: 'GameSettings',
    component: GameSettings,
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
  },
  {
    path: '/settings/memory',
    name: 'MemorySettings',
    component: MemorySettings,
  },
  {
    path: '/settings/hibernate',
    name: 'HibernateSettings',
    component: HibernateSettings,
  },
  {
    path: '/settings/renderer',
    name: 'RendererSettings',
    component: RendererSettings,
  },
  {
    path: '/settings/logs',
    name: 'LogsView',
    component: LogsView,
  },
  {
    path: '/settings/language',
    name: 'LanguageView',
    component: LanguageView,
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  // Use Hash history for reliable webroot loading across all root managers
  history: createWebHashHistory(),
  routes,
})

export default router
