<template>
  <div class="food-detail-page" v-loading="loading">
    <el-page-header class="page-header" @back="goBack">
      <template #content>
        <div class="header-content">
          <span class="page-title">{{ food.foodName || '食材详情' }}</span>
          <el-tag v-if="food.category" type="success" effect="light">{{ food.category }}</el-tag>
        </div>
      </template>
    </el-page-header>

    <template v-if="food.id">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="16">
          <el-card class="detail-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>基础信息</span>
                <el-tag :type="food.status === 1 ? 'success' : 'danger'" effect="light">
                  {{ food.status === 1 ? '启用中' : '已停用' }}
                </el-tag>
              </div>
            </template>

            <el-descriptions :column="2" border class="detail-descriptions">
              <el-descriptions-item label="食材名称">{{ food.foodName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="食材分类">{{ food.category || '-' }}</el-descriptions-item>
              <el-descriptions-item label="适用目标">{{ food.suitableGoal || '-' }}</el-descriptions-item>
              <el-descriptions-item label="适用时间">{{ food.suitableTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDateTime(food.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ formatDateTime(food.updatedAt) }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card class="detail-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>食材说明</span>
              </div>
            </template>
            <div class="content-text">{{ food.description || '暂无食材说明' }}</div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="8">
          <el-card class="nutrition-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>营养成分（每100g）</span>
              </div>
            </template>

            <div class="nutrition-grid">
              <div class="nutrition-item energy">
                <div class="nutrition-label">热量</div>
                <div class="nutrition-value">{{ formatDecimal(food.caloriesPer100g, 'kcal') }}</div>
              </div>
              <div class="nutrition-item protein">
                <div class="nutrition-label">蛋白质</div>
                <div class="nutrition-value">{{ formatDecimal(food.proteinPer100g, 'g') }}</div>
              </div>
              <div class="nutrition-item fat">
                <div class="nutrition-label">脂肪</div>
                <div class="nutrition-value">{{ formatDecimal(food.fatPer100g, 'g') }}</div>
              </div>
              <div class="nutrition-item carb">
                <div class="nutrition-label">碳水</div>
                <div class="nutrition-value">{{ formatDecimal(food.carbPer100g, 'g') }}</div>
              </div>
            </div>

            <div class="nutrition-tip">
              数据为每 100g 参考值，实际摄入请结合食用重量灵活换算。
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <el-empty v-else-if="!loading" description="食材不存在或已被删除" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFoodDetail } from '../../api/food'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const food = ref({})

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const formatDecimal = (value, unit = '') => {
  if (value === null || value === undefined || value === '') return '-'
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) return `${value}${unit ? ` ${unit}` : ''}`.trim()
  const formatted = Number.isInteger(numberValue) ? String(numberValue) : numberValue.toFixed(2)
  return `${formatted}${unit ? ` ${unit}` : ''}`.trim()
}

const getDetail = async () => {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const res = await getFoodDetail(id)
    food.value = res || {}
  } catch (error) {
    console.error('获取食材详情失败:', error)
    ElMessage.error('获取食材详情失败')
    food.value = {}
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
.food-detail-page {
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
.nutrition-card {
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

.nutrition-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.nutrition-item {
  padding: 18px 16px;
  border-radius: 14px;
}

.energy {
  background: #fff7ed;
}

.protein {
  background: #eff6ff;
}

.fat {
  background: #fef2f2;
}

.carb {
  background: #f0fdf4;
}

.nutrition-label {
  margin-bottom: 8px;
  color: #909399;
  font-size: 13px;
}

.nutrition-value {
  color: #1f2937;
  font-size: 20px;
  font-weight: 700;
}

.nutrition-tip {
  margin-top: 18px;
  padding: 14px;
  border-radius: 12px;
  background: #f8fafc;
  color: #64748b;
  line-height: 1.7;
  font-size: 13px;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .nutrition-grid {
    grid-template-columns: 1fr;
  }
}
</style>
