<template>
  <div
    class="stat-metric-card"
    :class="[`theme-${theme}`, { clickable }]"
    role="button"
    :tabindex="clickable ? 0 : -1"
    @click="emitClick"
    @keydown.enter.prevent="emitClick"
  >
    <div class="stat-icon-wrap">
      <el-icon :size="22"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-body">
      <div class="stat-title">{{ title }}</div>
      <div class="stat-value">{{ displayValue }}</div>
      <div class="stat-desc">{{ description }}</div>
      <div v-if="trendText" class="stat-trend">{{ trendText }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  value: { type: [String, Number], default: 0 },
  description: { type: String, default: '' },
  trendText: { type: String, default: '' },
  icon: { type: [Object, Function], required: true },
  theme: {
    type: String,
    default: 'blue',
    validator: (v) => ['blue', 'green', 'orange', 'purple', 'red', 'cyan'].includes(v)
  },
  clickable: { type: Boolean, default: false }
})

const emit = defineEmits(['click'])

const displayValue = computed(() =>
  props.value === null || props.value === undefined || props.value === '' ? '—' : props.value
)

function emitClick() {
  if (props.clickable) emit('click')
}
</script>

<style scoped>
.stat-metric-card {
  display: flex;
  gap: 14px;
  padding: 18px 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
  height: 100%;
  transition:
    box-shadow 0.2s,
    transform 0.2s;
}

.stat-metric-card.clickable {
  cursor: pointer;
}

.stat-metric-card.clickable:hover {
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.1);
  transform: translateY(-2px);
}

.stat-icon-wrap {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.theme-blue .stat-icon-wrap {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}
.theme-green .stat-icon-wrap {
  background: linear-gradient(135deg, #22c55e, #16a34a);
}
.theme-orange .stat-icon-wrap {
  background: linear-gradient(135deg, #fb923c, #ea580c);
}
.theme-purple .stat-icon-wrap {
  background: linear-gradient(135deg, #a855f7, #7c3aed);
}
.theme-red .stat-icon-wrap {
  background: linear-gradient(135deg, #f87171, #dc2626);
}
.theme-cyan .stat-icon-wrap {
  background: linear-gradient(135deg, #22d3ee, #0891b2);
}

.stat-body {
  min-width: 0;
  flex: 1;
}

.stat-title {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.stat-value {
  margin-top: 6px;
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.stat-desc {
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
}

.stat-trend {
  margin-top: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #16a34a;
}

.theme-red .stat-trend {
  color: #dc2626;
}
</style>
