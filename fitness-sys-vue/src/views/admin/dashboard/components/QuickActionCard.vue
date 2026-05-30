<template>
  <div class="quick-action-card" role="button" tabindex="0" @click="go" @keydown.enter.prevent="go">
    <div class="qa-icon">
      <el-icon :size="22"><component :is="icon" /></el-icon>
    </div>
    <div class="qa-text">
      <div class="qa-title">{{ title }}</div>
      <div class="qa-desc">{{ description }}</div>
    </div>
    <el-icon class="qa-arrow"><ArrowRight /></el-icon>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'

const props = defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' },
  path: { type: String, required: true },
  icon: { type: [Object, Function], required: true }
})

const router = useRouter()

function go() {
  router.push(props.path)
}
</script>

<style scoped>
.quick-action-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  cursor: pointer;
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
}

.quick-action-card:hover {
  border-color: #c7d2fe;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.08);
}

.quick-action-card:focus-visible {
  outline: 2px solid #2563eb;
  outline-offset: 2px;
}

.qa-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: #eff6ff;
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.qa-text {
  flex: 1;
  min-width: 0;
}

.qa-title {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}

.qa-desc {
  margin-top: 2px;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}

.qa-arrow {
  color: #cbd5e1;
  flex-shrink: 0;
}
</style>
