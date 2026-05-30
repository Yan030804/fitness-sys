<template>
  <div class="exercise-detail-page" v-loading="loading">
    <el-page-header class="page-header" @back="goBack">
      <template #content>
        <div class="header-content">
          <span class="page-title">{{ exercise.exerciseName || '动作详情' }}</span>
          <el-tag v-if="exercise.id" :type="getDifficultyType(exercise.difficulty)" effect="light">
            {{ formatDifficulty(exercise.difficulty) }}
          </el-tag>
        </div>
      </template>
    </el-page-header>

    <template v-if="exercise.id">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="16">
          <el-card class="detail-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>基础信息</span>
                <el-tag :type="exercise.status === 1 ? 'success' : 'danger'" effect="light">
                  {{ exercise.status === 1 ? '启用中' : '已停用' }}
                </el-tag>
              </div>
            </template>

            <el-descriptions :column="2" border class="detail-descriptions">
              <el-descriptions-item label="动作名称">{{ exercise.exerciseName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="动作分类">{{ exercise.category || '-' }}</el-descriptions-item>
              <el-descriptions-item label="训练部位">{{ exercise.bodyPart || '-' }}</el-descriptions-item>
              <el-descriptions-item label="难度等级">{{ formatDifficulty(exercise.difficulty) }}</el-descriptions-item>
              <el-descriptions-item label="器械要求">{{ exercise.equipment || '无需器械' }}</el-descriptions-item>
              <el-descriptions-item label="每小时热量">{{ formatCalories(exercise.caloriesPerHour) }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDateTime(exercise.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ formatDateTime(exercise.updatedAt) }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card class="detail-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>动作说明</span>
              </div>
            </template>
            <div class="content-text">{{ exercise.description || '暂无动作说明' }}</div>
          </el-card>

          <el-card class="detail-card warning-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>注意事项</span>
              </div>
            </template>
            <div class="content-text warning-text">{{ exercise.caution || '暂无注意事项' }}</div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="8">
          <el-card class="suggest-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>训练建议</span>
              </div>
            </template>

            <div class="suggest-grid">
              <div class="suggest-item">
                <div class="suggest-label">默认组数</div>
                <div class="suggest-value">{{ exercise.defaultSets ?? 0 }} 组</div>
              </div>
              <div class="suggest-item">
                <div class="suggest-label">默认次数</div>
                <div class="suggest-value">{{ formatReps(exercise.defaultReps) }}</div>
              </div>
              <div class="suggest-item">
                <div class="suggest-label">建议时长</div>
                <div class="suggest-value">{{ formatDuration(exercise.durationMin) }}</div>
              </div>
            </div>

            <div class="suggest-tip">
              请结合自身身体状态循序渐进训练，动作过程中注意呼吸节奏与关节稳定。
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <el-empty v-else-if="!loading" description="动作不存在或已被删除" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getExerciseDetail } from '../../api/exercise'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const exercise = ref({})

const formatDifficulty = (difficulty) => {
  if (difficulty === null || difficulty === undefined || difficulty === '') return '-'
  const map = {
    1: '初级',
    2: '中级',
    3: '高级',
    '1': '初级',
    '2': '中级',
    '3': '高级'
  }
  return map[difficulty] || String(difficulty)
}

const formatReps = (reps) => {
  if (reps === null || reps === undefined || reps === '') return '-'
  return `${reps} 次`
}

const formatDuration = (minutes) => {
  if (minutes === null || minutes === undefined || minutes === '') return '-'
  return `${minutes} 分钟`
}

const formatCalories = (value) => {
  if (value === null || value === undefined || value === '') return '-'
  return `${value} kcal/h`
}

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const getDifficultyType = (difficulty) => {
  const label = formatDifficulty(difficulty)
  switch (label) {
    case '初级':
      return 'success'
    case '中级':
      return 'warning'
    case '高级':
      return 'danger'
    default:
      return 'info'
  }
}

const getDetail = async () => {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const res = await getExerciseDetail(id)
    exercise.value = res || {}
  } catch (error) {
    console.error('获取动作详情失败:', error)
    ElMessage.error('获取动作详情失败')
    exercise.value = {}
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  getDetail()
})
</script>

<style scoped>
.exercise-detail-page {
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.detail-card,
.suggest-card {
  margin-bottom: 20px;
  border-radius: 14px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 700;
  color: #1f2937;
}

.detail-descriptions :deep(.el-descriptions__label) {
  width: 110px;
}

.content-text {
  line-height: 1.9;
  color: #606266;
  white-space: pre-wrap;
  font-size: 14px;
}

.warning-card {
  border-left: 4px solid #e6a23c;
}

.warning-text {
  color: #b88230;
}

.suggest-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

.suggest-item {
  padding: 16px;
  border-radius: 12px;
  background: #f8fafc;
}

.suggest-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 6px;
}

.suggest-value {
  color: #2563eb;
  font-size: 18px;
  font-weight: 700;
}

.suggest-tip {
  margin-top: 18px;
  padding: 14px;
  border-radius: 12px;
  background: #f0f9eb;
  color: #4d7c0f;
  line-height: 1.7;
  font-size: 13px;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
