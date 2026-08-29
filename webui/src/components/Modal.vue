<template>
  <Teleport to="body">
    <div
      v-if="isOpen"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md transition-opacity animate-fadeIn"
      @click.self="$emit('close')"
    >
      <div
        class="bg-[#121827] border border-white/10 rounded-2xl w-full max-w-md max-h-[85vh] flex flex-col shadow-2xl overflow-hidden animate-scaleUp"
      >
        <!-- Header -->
        <div class="px-5 py-4 border-b border-white/10 flex items-center justify-between">
          <h3 class="font-bold text-base text-slate-100 flex items-center gap-2">
            <slot name="title">{{ title }}</slot>
          </h3>
          <button
            @click="$emit('close')"
            class="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-white/10 transition-colors"
          >
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- Body -->
        <div class="p-5 overflow-y-auto space-y-4 flex-1">
          <slot></slot>
        </div>

        <!-- Footer -->
        <div v-if="$slots.footer" class="px-5 py-3.5 bg-black/20 border-t border-white/10 flex items-center justify-end gap-2">
          <slot name="footer"></slot>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({
  isOpen: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: '',
  },
})

defineEmits(['close'])
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleUp {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.animate-fadeIn {
  animation: fadeIn 0.2s ease-out forwards;
}

.animate-scaleUp {
  animation: scaleUp 0.2s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
</style>
