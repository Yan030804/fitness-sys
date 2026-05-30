<template>
  <div class="recommendation-page">
    <el-card class="filter-card" shadow="never">
      <div class="page-header">
        <div>
          <div class="page-title">{{ isAdmin ? '推荐记录总览' : '个性化推荐中心' }}</div>
          <div class="page-subtitle">
            {{ isAdmin ? '查看全部用户的推荐记录与推荐对象信息' : '根据你的训练、饮食和目标生成个性化动作、食材与计划推荐' }}
          </div>
        </div>
      </div>

      <el-form :inline="true" :model="queryParams" class="filter-form">
        <!-- <el-form-item v-if="isAdmin" label="用户ID">
          <el-input
            v-model="queryParams.userId"
            placeholder="请输入用户ID"
            clearable
            style="width: 160px"
            @keyup.enter="handleQuery"
          />
        </el-form-item> -->

        <el-form-item v-if="isAdmin">
          <template #label>
            <span class="label-with-tip">
              手机号
              <el-tooltip content="填写手机号时优先按手机号查询" placement="top">
                <el-icon class="label-tip-icon"><InfoFilled /></el-icon>
              </el-tooltip>
            </span>
          </template>
          <el-input
            v-model="queryParams.phone"
            placeholder="请输入手机号"
            clearable
            maxlength="20"
            style="width: 160px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="推荐类型">
          <el-select v-model="queryParams.recType" placeholder="全部类型" clearable style="width: 180px">
            <el-option label="动作推荐" value="EXERCISE" />
            <el-option label="食材推荐" value="FOOD" />
            <el-option label="计划推荐" value="PLAN" />
          </el-select>
        </el-form-item>

        <el-form-item class="action-buttons">
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="!isAdmin" class="generator-card" shadow="never">
      <template #header>
        <div class="section-header">
          <span>生成推荐</span>
          <el-tag type="success" effect="plain">智能个性化</el-tag>
        </div>
      </template>

      <el-form :model="generateForm" label-width="100px" class="generate-form">
        <el-form-item label="推荐类型">
          <el-checkbox-group v-model="generateForm.recommendTypes">
            <el-checkbox label="EXERCISE">动作推荐</el-checkbox>
            <el-checkbox label="FOOD">食材推荐</el-checkbox>
            <el-checkbox label="PLAN">计划推荐</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="generateLoading" @click="handleGenerate">
            立即生成推荐
          </el-button>
          <span class="generate-tip">推荐生成会综合用户资料、训练记录、饮食记录等信息</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="!isAdmin" class="latest-card" shadow="never">
      <template #header>
        <div class="section-header">
          <span>最新推荐结果</span>
          <el-tag type="info" effect="plain">
            {{ latestTime ? `推荐时间：${formatDateTime(latestTime)}` : '暂无最新推荐' }}
          </el-tag>
        </div>
      </template>

      <div v-loading="latestLoading" class="latest-content">
        <template v-if="latestList.length > 0">
          <div class="latest-summary-grid">
            <div
              v-for="item in latestSummaryCards"
              :key="item.key"
              class="summary-card"
            >
              <div class="summary-label">{{ item.label }}</div>
              <div class="summary-value">{{ item.value }}</div>
            </div>
          </div>

          <div class="latest-list">
            <div
              v-for="item in latestList"
              :key="item.id"
              class="recommend-row"
            >
              <div class="recommend-main">
                <div class="recommend-head">
                  <div class="recommend-title-group">
                    <el-tag :type="getRecTypeTagType(item.recType)" effect="light">
                      {{ formatRecType(item.recType) }}
                    </el-tag>
                    <span class="recommend-name">{{ item.targetName || '-' }}</span>
                  </div>
                  <div class="recommend-score-wrap">
                    <span class="score-label">推荐得分</span>
                    <span class="recommend-score">{{ formatScore(item.score) }}</span>
                  </div>
                </div>

                <div class="recommend-meta-row">
                  <span class="meta-chip">算法：{{ item.algorithmType || '-' }}</span>
                  <span class="meta-chip">对象ID：{{ item.targetId || '-' }}</span>
                  <span class="meta-chip">时间：{{ formatDateTime(item.createdAt) }}</span>
                </div>

                <div class="recommend-reason-block">
                  <span class="reason-label">推荐理由</span>
                  <p class="recommend-reason">{{ item.reason || '暂无推荐理由' }}</p>
                </div>
              </div>

              <div class="recommend-actions">
                <el-button type="primary" plain @click="handleViewDetail(item)">查看详情</el-button>
              </div>
            </div>
          </div>
        </template>

        <el-empty v-else description="暂无最新推荐结果，点击上方按钮生成推荐" />
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="section-header">
          <span>{{ isAdmin ? '全部推荐记录' : '推荐历史' }}</span>
          <el-tag type="info" effect="plain">共 {{ total }} 条</el-tag>
        </div>
      </template>

      <el-table v-loading="loading" :data="historyList" border stripe class="history-table">
        <el-table-column label="编号" width="80" align="center">
          <template #default="{ $index }">
            {{ getRowNumber($index) }}
          </template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="用户姓名" width="120" align="center">
          <template #default="{ row }">
            {{ userNameMap[row.userId] || row.userId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="推荐类型" min-width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getRecTypeTagType(row.recType)" effect="light">
              {{ formatRecType(row.recType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetName" label="推荐对象" min-width="160" show-overflow-tooltip />
        <el-table-column prop="algorithmType" label="算法类型" min-width="120" show-overflow-tooltip />
        <el-table-column label="推荐得分" min-width="100" align="center">
          <template #default="{ row }">
            {{ formatScore(row.score) }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="推荐原因" min-width="240" show-overflow-tooltip />
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="120" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="handleViewDetail(row)">查看详情</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && historyList.length === 0" description="暂无推荐记录" />
    </el-card>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-drawer
      v-model="detailVisible"
      title="推荐详情"
      size="520px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-panel">
        <template v-if="detailData">
          <div class="detail-section">
            <div class="section-title">基础信息</div>
            <div class="detail-grid">
              <div class="detail-item"><span class="label">推荐ID</span><span class="value">{{ detailData.id || '-' }}</span></div>
              <div class="detail-item"><span class="label">用户ID</span><span class="value">{{ detailData.userId || '-' }}</span></div>
              <div class="detail-item"><span class="label">推荐类型</span><span class="value">{{ formatRecType(detailData.recType) }}</span></div>
              <div class="detail-item"><span class="label">目标对象ID</span><span class="value">{{ detailData.targetId || '-' }}</span></div>
              <div class="detail-item"><span class="label">推荐对象</span><span class="value">{{ detailData.targetName || '-' }}</span></div>
              <div class="detail-item"><span class="label">算法类型</span><span class="value">{{ detailData.algorithmType || '-' }}</span></div>
              <div class="detail-item"><span class="label">推荐得分</span><span class="value">{{ formatScore(detailData.score) }}</span></div>
              <div class="detail-item"><span class="label">创建时间</span><span class="value">{{ formatDateTime(detailData.createdAt) }}</span></div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">推荐理由</div>
            <div class="detail-grid single-column">
              <div class="detail-item">
                <span class="label">原因说明</span>
                <span class="value">{{ detailData.reason || '暂无推荐理由' }}</span>
              </div>
            </div>
          </div>
        </template>

        <el-empty v-else-if="!detailLoading" description="暂无详情数据" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '../../store/auth'
import { isAdminByRole } from '../../utils/auth'
import { getAdminUserDetail } from '../../api/user'
import {
  generateRecommendations,
  getAdminRecommendationList,
  getLatestRecommendations,
  getRecommendationDetail,
  getRecommendationHistory
} from '../../api/recommendation'

const authStore = useAuthStore()
const isAdmin = computed(() => isAdminByRole(authStore.roleCode))

const createDefaultQueryParams = () => ({
  pageNum: 1,
  pageSize: 10,
  userId: '',
  phone: '',
  recType: ''
})

const loading = ref(false)
const latestLoading = ref(false)
const generateLoading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const latestTime = ref('')
const latestList = ref([])
const historyList = ref([])
const userNameMap = ref({})
const total = ref(0)
const detailData = ref(null)

const queryParams = reactive(createDefaultQueryParams())
const generateForm = reactive({
  recommendTypes: ['EXERCISE', 'FOOD', 'PLAN']
})

const normalizePageData = (payload) => {
  return {
    list: payload?.list || payload?.records || payload?.rows || [],
    total: Number(payload?.total || 0)
  }
}

const normalizeListData = (payload) => {
  return payload?.list || payload?.records || payload?.rows || []
}

const formatUserDisplayName = (user, userId) => {
  return user?.realName || user?.nickname || user?.username || String(userId)
}

const loadUserNameMap = async (list) => {
  if (!isAdmin.value) return

  const userIds = [...new Set(list.map((item) => item.userId).filter(Boolean))]
  const needLoadIds = userIds.filter((id) => !userNameMap.value[id])

  await Promise.all(
    needLoadIds.map(async (id) => {
      try {
        const user = await getAdminUserDetail(id)
        userNameMap.value[id] = formatUserDisplayName(user, id)
      } catch (error) {
        userNameMap.value[id] = String(id)
      }
    })
  )
}

const latestSummaryCards = computed(() => {
  const counters = {
    total: latestList.value.length,
    EXERCISE: 0,
    FOOD: 0,
    PLAN: 0
  }

  latestList.value.forEach((item) => {
    if (counters[item.recType] !== undefined) {
      counters[item.recType] += 1
    }
  })

  return [
    { key: 'total', label: '本次推荐总数', value: counters.total },
    { key: 'exercise', label: '动作推荐', value: counters.EXERCISE },
    { key: 'food', label: '食材推荐', value: counters.FOOD },
    { key: 'plan', label: '计划推荐', value: counters.PLAN }
  ]
})

const buildQueryParams = () => {
  const params = {
    pageNum: queryParams.pageNum,
    pageSize: queryParams.pageSize
  }

  if (queryParams.recType) params.recType = queryParams.recType
  if (isAdmin.value) {
    const phone = queryParams.phone.trim()
    const userId = queryParams.userId.trim()
    if (phone) params.phone = phone
    if (userId) params.userId = userId
  }

  return params
}

const getHistoryList = async () => {
  loading.value = true
  try {
    const requestApi = isAdmin.value ? getAdminRecommendationList : getRecommendationHistory
    const res = await requestApi(buildQueryParams())
    const { list, total: totalCount } = normalizePageData(res)
    historyList.value = list
    total.value = totalCount
    loadUserNameMap(list)
  } catch (error) {
    console.error('获取推荐记录失败:', error)
  } finally {
    loading.value = false
  }
}

const getLatestList = async () => {
  if (isAdmin.value) return

  latestLoading.value = true
  try {
    const params = {}
    if (queryParams.recType) params.recType = queryParams.recType
    const res = await getLatestRecommendations(params)
    latestTime.value = res?.latestTime || ''
    latestList.value = normalizeListData(res)
  } catch (error) {
    console.error('获取最新推荐失败:', error)
  } finally {
    latestLoading.value = false
  }
}

const handleGenerate = async () => {
  if (!generateForm.recommendTypes.length) {
    ElMessage.warning('请至少选择一种推荐类型')
    return
  }

  generateLoading.value = true
  try {
    const res = await generateRecommendations({
      recommendTypes: generateForm.recommendTypes
    })
    const generatedCount = Number(res?.generatedCount || 0)
    ElMessage.success(`推荐生成成功，本次生成 ${generatedCount} 条`)
    latestTime.value = res?.latestTime || latestTime.value
    latestList.value = normalizeListData(res)
    queryParams.pageNum = 1
    await getLatestList()
    await getHistoryList()
  } catch (error) {
    console.error('生成推荐失败:', error)
  } finally {
    generateLoading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getHistoryList()
  if (!isAdmin.value) {
    getLatestList()
  }
}

const resetQuery = () => {
  Object.assign(queryParams, createDefaultQueryParams())
  getHistoryList()
  if (!isAdmin.value) {
    getLatestList()
  }
}

const handleSizeChange = (value) => {
  queryParams.pageSize = value
  queryParams.pageNum = 1
  getHistoryList()
}

const handleCurrentChange = (value) => {
  queryParams.pageNum = value
  getHistoryList()
}

const handleViewDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null

  try {
    if (isAdmin.value) {
      detailData.value = row
    } else {
      const res = await getRecommendationDetail(row.id)
      detailData.value = res || row || null
    }
  } catch (error) {
    console.error('获取推荐详情失败:', error)
  } finally {
    detailLoading.value = false
  }
}

const getRowNumber = (index) => {
  return (queryParams.pageNum - 1) * queryParams.pageSize + index + 1
}

const formatRecType = (value) => {
  if (value === 'EXERCISE') return '动作推荐'
  if (value === 'FOOD') return '食材推荐'
  if (value === 'PLAN') return '计划推荐'
  return value || '-'
}

const getRecTypeTagType = (value) => {
  if (value === 'EXERCISE') return 'primary'
  if (value === 'FOOD') return 'success'
  if (value === 'PLAN') return 'warning'
  return 'info'
}

const formatScore = (value) => {
  if (value === null || value === undefined || value === '') return '-'
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) return value
  return numberValue.toFixed(2)
}

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

onMounted(() => {
  getHistoryList()
  if (!isAdmin.value) {
    getLatestList()
  }
})
</script>

<style scoped>
.recommendation-page {
  min-height: calc(100vh - 120px);
}

.filter-card,
.generator-card,
.latest-card,
.table-card {
  border-radius: 14px;
}

.filter-card,
.generator-card,
.latest-card {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.page-subtitle {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
}

.label-with-tip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.label-tip-icon {
  cursor: help;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.action-buttons {
  margin-left: auto;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.generate-form {
  padding-right: 12px;
}

.generate-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}

.latest-content {
  min-height: 180px;
}

.latest-summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.summary-card {
  padding: 18px 20px;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.summary-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}

.summary-value {
  color: #1f2937;
  font-size: 28px;
  font-weight: 700;
}

.latest-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.recommend-row {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 20px;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  background: #fff;
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
}

.recommend-row:hover {
  border-color: #dbeafe;
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.06);
}

.recommend-main {
  flex: 1;
  min-width: 0;
}

.recommend-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.recommend-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.recommend-score-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}

.score-label {
  color: #909399;
  font-size: 12px;
}

.recommend-score {
  color: #f59e0b;
  font-size: 22px;
  font-weight: 700;
}

.recommend-name {
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  background: #f8fafc;
  border-radius: 999px;
  color: #64748b;
  font-size: 12px;
}

.recommend-reason-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reason-label {
  color: #909399;
  font-size: 12px;
}

.recommend-reason {
  margin: 0;
  line-height: 1.8;
  color: #606266;
  font-size: 14px;
}

.recommend-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.history-table {
  width: 100%;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.detail-panel {
  min-height: 240px;
  padding-right: 8px;
}

.detail-section {
  margin-bottom: 20px;
  padding: 18px 18px 6px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 12px;
}

.section-title {
  margin-bottom: 14px;
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 16px;
}

.single-column {
  grid-template-columns: 1fr;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label {
  font-size: 12px;
  color: #909399;
}

.value {
  color: #303133;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

@media (max-width: 992px) {
  .action-buttons {
    margin-left: 0;
  }

  .latest-summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .recommend-row {
    flex-direction: column;
  }

  .recommend-actions {
    justify-content: flex-end;
  }
}

@media (max-width: 768px) {
  .page-header,
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .generate-tip {
    display: block;
    margin-left: 0;
    margin-top: 8px;
  }

  .latest-summary-grid {
    grid-template-columns: 1fr;
  }

  .recommend-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .recommend-score-wrap {
    align-items: flex-start;
  }
}
</style>
