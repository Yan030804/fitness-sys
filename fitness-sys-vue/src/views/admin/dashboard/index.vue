<template>
  <div v-loading="pageLoading" class="admin-dashboard" element-loading-text="加载运营数据…">
    <div v-if="loadError" class="load-error-banner">
      <el-alert :title="loadError" type="error" show-icon closable @close="loadError = ''" />
    </div>

    <div class="dash-header">
      <div class="dash-header-left">
        <h1 class="dash-title">智能健身推荐系统管理后台</h1>
        <div class="dash-meta">
          <el-tag effect="plain" type="info">
            <el-icon style="vertical-align: -2px; margin-right: 4px"><User /></el-icon>
            {{ adminName }}
          </el-tag>
          <span class="dash-date">
            <el-icon style="vertical-align: -2px; margin-right: 2px"><Calendar /></el-icon>
            {{ todayText }}
          </span>
        </div>
      </div>
      <el-button type="primary" :icon="Refresh" :loading="pageLoading" @click="loadAll">刷新数据</el-button>
    </div>

    <el-row :gutter="14" class="stat-grid">
      <el-col v-for="item in statCards" :key="item.key" :xs="12" :sm="8" :md="6" :lg="4">
        <div class="stat-card" :style="{ borderTopColor: item.color }">
          <div class="stat-card-icon" :style="{ background: item.bg }">
            <el-icon :size="20" :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-card-body">
            <span class="stat-card-label">{{ item.label }}</span>
            <span class="stat-card-value">{{ stats[item.key] ?? '—' }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="chart-row">
      <el-col :xs="24" :lg="8">
        <el-card class="dash-card" shadow="never">
          <template #header><span class="card-title">预约状态分布</span></template>
          <div ref="appointmentChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="dash-card" shadow="never">
          <template #header><span class="card-title">推荐类型分布</span></template>
          <div ref="recommendationChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="dash-card" shadow="never">
          <template #header><span class="card-title">计划类型分布</span></template>
          <div ref="planChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="table-row">
      <el-col :xs="24" :lg="14">
        <el-card class="dash-card" shadow="never">
          <template #header>
            <div class="card-head-flex">
              <span class="card-title">最新预约</span>
              <el-button type="primary" link @click="$router.push('/appointment')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="latestAppointments" border stripe size="small" empty-text="暂无数据">
            <el-table-column prop="name" label="预约人" min-width="80" show-overflow-tooltip />
            <el-table-column prop="appointmentType" label="类型" min-width="90" show-overflow-tooltip />
            <el-table-column label="预约日期" min-width="100">
              <template #default="{ row }">{{ row.reserveDate || '—' }}</template>
            </el-table-column>
            <el-table-column label="预约时间" min-width="80">
              <template #default="{ row }">{{ formatTime(row.reserveTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="appointmentTagType(row.status)" effect="light" size="small">
                  {{ appointmentStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="100" show-overflow-tooltip />
            <el-table-column label="操作" width="80" fixed="right" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" plain @click="openStatusDialog(row)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card class="dash-card" shadow="never">
          <template #header>
            <div class="card-head-flex">
              <span class="card-title">最新用户</span>
              <el-button type="primary" link @click="$router.push('/admin/users')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="latestUsers" border stripe size="small" empty-text="暂无数据">
            <el-table-column prop="username" label="用户名" min-width="90" show-overflow-tooltip />
            <el-table-column prop="realName" label="姓名" min-width="80" show-overflow-tooltip />
            <el-table-column prop="phone" label="手机号" min-width="110" show-overflow-tooltip />
            <el-table-column prop="fitnessGoal" label="健身目标" min-width="80" show-overflow-tooltip />
            <el-table-column label="状态" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light" size="small">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="注册时间" min-width="155">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="table-row">
      <el-col :span="24">
        <el-card class="dash-card" shadow="never">
          <template #header>
            <div class="card-head-flex">
              <span class="card-title">最新推荐记录</span>
              <el-button type="primary" link @click="$router.push('/recommendation')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="latestRecommendations" border stripe size="small" empty-text="暂无数据">
            <el-table-column prop="userId" label="用户ID" min-width="80" show-overflow-tooltip />
            <el-table-column label="推荐类型" min-width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="recTypeTag(row.recType)" effect="light" size="small">{{ row.recType || '—' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="targetName" label="推荐目标" min-width="140" show-overflow-tooltip />
            <el-table-column prop="algorithmType" label="算法" min-width="100" show-overflow-tooltip />
            <el-table-column label="评分" width="80" align="center">
              <template #default="{ row }">{{ row.score ?? '—' }}</template>
            </el-table-column>
            <el-table-column label="生成时间" min-width="155">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="statusDialogVisible" title="处理预约状态" width="420px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="appointmentTagType(currentAppointment?.status)" effect="light">
            {{ appointmentStatusLabel(currentAppointment?.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="statusForm.status" placeholder="选择状态" style="width: 100%">
            <el-option label="待处理 (PENDING)" value="PENDING" />
            <el-option label="已确认 (CONFIRMED)" value="CONFIRMED" />
            <el-option label="已取消 (CANCELLED)" value="CANCELLED" />
            <el-option label="已完成 (DONE)" value="DONE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="statusForm.remark" type="textarea" :rows="2" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusSubmitting" @click="submitStatus">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  Refresh,
  User,
  UserFilled,
  CircleCheck,
  CircleClose,
  Clock,
  Reading,
  Flag,
  Food,
  DataAnalysis,
  Document
} from '@element-plus/icons-vue'
import {
  fetchAdminMe,
  fetchAdminUsersPage,
  fetchAdminExercisesPage,
  fetchAdminFoodsPage,
  fetchAdminPlansPage,
  fetchAdminRecommendationsPage,
  fetchAdminAppointmentsPage,
  updateAppointmentStatus,
  normalizePagePayload
} from '../../../api/adminDashboard'

const pageLoading = ref(false)
const loadError = ref('')

const adminName = ref('')
const todayText = computed(() => {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
})

const stats = reactive({
  totalUsers: 0,
  normalUsers: 0,
  disabledUsers: 0,
  pendingAppointments: 0,
  totalExercises: 0,
  enabledExercises: 0,
  totalFoods: 0,
  enabledFoods: 0,
  todayAppointments: 0,
  confirmedAppointments: 0,
  totalPlans: 0,
  totalRecommendations: 0
})

const statCards = [
  { key: 'totalUsers', label: '用户总数', icon: UserFilled, color: '#2563eb', bg: '#eff6ff' },
  { key: 'normalUsers', label: '正常用户', icon: CircleCheck, color: '#16a34a', bg: '#f0fdf4' },
  { key: 'disabledUsers', label: '禁用用户', icon: CircleClose, color: '#dc2626', bg: '#fef2f2' },
  { key: 'pendingAppointments', label: '待处理预约', icon: Clock, color: '#ea580c', bg: '#fff7ed' },
  { key: 'totalExercises', label: '动作总数', icon: Reading, color: '#7c3aed', bg: '#f5f3ff' },
  { key: 'enabledExercises', label: '启用动作', icon: Flag, color: '#16a34a', bg: '#f0fdf4' },
  { key: 'totalFoods', label: '食材总数', icon: Food, color: '#ea580c', bg: '#fff7ed' },
  { key: 'enabledFoods', label: '启用食材', icon: CircleCheck, color: '#0891b2', bg: '#ecfeff' },
  { key: 'todayAppointments', label: '今日预约', icon: Calendar, color: '#2563eb', bg: '#eff6ff' },
  { key: 'confirmedAppointments', label: '已确认预约', icon: CircleCheck, color: '#16a34a', bg: '#f0fdf4' },
  { key: 'totalPlans', label: '计划总数', icon: Document, color: '#7c3aed', bg: '#f5f3ff' },
  { key: 'totalRecommendations', label: '推荐记录', icon: DataAnalysis, color: '#0891b2', bg: '#ecfeff' }
]

const appointmentDist = reactive({ PENDING: 0, CONFIRMED: 0, CANCELLED: 0, DONE: 0 })
const recDist = reactive({ EXERCISE: 0, FOOD: 0, PLAN: 0 })
const planDist = reactive({ TRAINING: 0, DIET: 0 })

const latestAppointments = ref([])
const latestUsers = ref([])
const latestRecommendations = ref([])

const statusDialogVisible = ref(false)
const statusSubmitting = ref(false)
const currentAppointment = ref(null)
const statusForm = reactive({ status: '', remark: '' })

const appointmentChartRef = ref(null)
const recommendationChartRef = ref(null)
const planChartRef = ref(null)
let appointmentChart = null
let recommendationChart = null
let planChart = null

async function safeTotal(fn) {
  try {
    const res = await fn()
    return normalizePagePayload(res).total
  } catch {
    return 0
  }
}

async function fetchAdminInfo() {
  try {
    const data = await fetchAdminMe()
    adminName.value = data?.username || data?.realName || data?.nickname || '管理员'
  } catch {
    adminName.value = '管理员'
  }
}

async function fetchStats() {
  const today = todayText.value
  const results = await Promise.all([
    safeTotal(() => fetchAdminUsersPage({ pageNum: 1, pageSize: 1 })),
    safeTotal(() => fetchAdminUsersPage({ pageNum: 1, pageSize: 1, status: 1 })),
    safeTotal(() => fetchAdminUsersPage({ pageNum: 1, pageSize: 1, status: 0 })),
    safeTotal(() => fetchAdminAppointmentsPage({ pageNum: 1, pageSize: 1, status: 'PENDING' })),
    safeTotal(() => fetchAdminExercisesPage({ pageNum: 1, pageSize: 1 })),
    safeTotal(() => fetchAdminExercisesPage({ pageNum: 1, pageSize: 1, status: 1 })),
    safeTotal(() => fetchAdminFoodsPage({ pageNum: 1, pageSize: 1 })),
    safeTotal(() => fetchAdminFoodsPage({ pageNum: 1, pageSize: 1, status: 1 })),
    safeTotal(() => fetchAdminAppointmentsPage({ pageNum: 1, pageSize: 1, reserveDate: today })),
    safeTotal(() => fetchAdminAppointmentsPage({ pageNum: 1, pageSize: 1, status: 'CONFIRMED' })),
    safeTotal(() => fetchAdminPlansPage({ pageNum: 1, pageSize: 1 })),
    safeTotal(() => fetchAdminRecommendationsPage({ pageNum: 1, pageSize: 1 }))
  ])

  stats.totalUsers = results[0]
  stats.normalUsers = results[1]
  stats.disabledUsers = results[2]
  stats.pendingAppointments = results[3]
  stats.totalExercises = results[4]
  stats.enabledExercises = results[5]
  stats.totalFoods = results[6]
  stats.enabledFoods = results[7]
  stats.todayAppointments = results[8]
  stats.confirmedAppointments = results[9]
  stats.totalPlans = results[10]
  stats.totalRecommendations = results[11]
}

async function fetchDistribution() {
  const [pending, confirmed, cancelled, done, exRec, foodRec, planRec, trainPlan, dietPlan] = await Promise.all([
    safeTotal(() => fetchAdminAppointmentsPage({ pageNum: 1, pageSize: 1, status: 'PENDING' })),
    safeTotal(() => fetchAdminAppointmentsPage({ pageNum: 1, pageSize: 1, status: 'CONFIRMED' })),
    safeTotal(() => fetchAdminAppointmentsPage({ pageNum: 1, pageSize: 1, status: 'CANCELLED' })),
    safeTotal(() => fetchAdminAppointmentsPage({ pageNum: 1, pageSize: 1, status: 'DONE' })),
    safeTotal(() => fetchAdminRecommendationsPage({ pageNum: 1, pageSize: 1, recType: 'EXERCISE' })),
    safeTotal(() => fetchAdminRecommendationsPage({ pageNum: 1, pageSize: 1, recType: 'FOOD' })),
    safeTotal(() => fetchAdminRecommendationsPage({ pageNum: 1, pageSize: 1, recType: 'PLAN' })),
    safeTotal(() => fetchAdminPlansPage({ pageNum: 1, pageSize: 1, planType: 'TRAINING' })),
    safeTotal(() => fetchAdminPlansPage({ pageNum: 1, pageSize: 1, planType: 'DIET' }))
  ])

  appointmentDist.PENDING = pending
  appointmentDist.CONFIRMED = confirmed
  appointmentDist.CANCELLED = cancelled
  appointmentDist.DONE = done
  recDist.EXERCISE = exRec
  recDist.FOOD = foodRec
  recDist.PLAN = planRec
  planDist.TRAINING = trainPlan
  planDist.DIET = dietPlan
}

async function fetchLists() {
  const [apptRes, userRes, recRes] = await Promise.allSettled([
    fetchAdminAppointmentsPage({ pageNum: 1, pageSize: 6 }),
    fetchAdminUsersPage({ pageNum: 1, pageSize: 6 }),
    fetchAdminRecommendationsPage({ pageNum: 1, pageSize: 6 })
  ])

  if (apptRes.status === 'fulfilled') {
    latestAppointments.value = normalizePagePayload(apptRes.value).list
  } else {
    latestAppointments.value = []
  }

  if (userRes.status === 'fulfilled') {
    latestUsers.value = normalizePagePayload(userRes.value).list
  } else {
    latestUsers.value = []
  }

  if (recRes.status === 'fulfilled') {
    latestRecommendations.value = normalizePagePayload(recRes.value).list
  } else {
    latestRecommendations.value = []
  }
}

function renderCharts() {
  if (appointmentChartRef.value) {
    if (!appointmentChart) appointmentChart = echarts.init(appointmentChartRef.value)
    appointmentChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, textStyle: { color: '#64748b', fontSize: 12 } },
      color: ['#f59e0b', '#3b82f6', '#94a3b8', '#22c55e'],
      series: [{
        type: 'pie',
        radius: ['40%', '68%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{c}', fontSize: 11 },
        data: [
          { name: '待处理', value: appointmentDist.PENDING },
          { name: '已确认', value: appointmentDist.CONFIRMED },
          { name: '已取消', value: appointmentDist.CANCELLED },
          { name: '已完成', value: appointmentDist.DONE }
        ]
      }]
    })
  }

  if (recommendationChartRef.value) {
    if (!recommendationChart) recommendationChart = echarts.init(recommendationChartRef.value)
    recommendationChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, textStyle: { color: '#64748b', fontSize: 12 } },
      color: ['#8b5cf6', '#f97316', '#06b6d4'],
      series: [{
        type: 'pie',
        radius: ['40%', '68%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{c}', fontSize: 11 },
        data: [
          { name: '运动推荐', value: recDist.EXERCISE },
          { name: '饮食推荐', value: recDist.FOOD },
          { name: '计划推荐', value: recDist.PLAN }
        ]
      }]
    })
  }

  if (planChartRef.value) {
    if (!planChart) planChart = echarts.init(planChartRef.value)
    planChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, textStyle: { color: '#64748b', fontSize: 12 } },
      color: ['#2563eb', '#16a34a'],
      series: [{
        type: 'pie',
        radius: ['40%', '68%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{c}', fontSize: 11 },
        data: [
          { name: '训练计划', value: planDist.TRAINING },
          { name: '饮食计划', value: planDist.DIET }
        ]
      }]
    })
  }
}

function formatTime(t) {
  if (!t) return '—'
  return String(t).slice(0, 5)
}

function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function appointmentStatusLabel(status) {
  const map = { PENDING: '待处理', CONFIRMED: '已确认', CANCELLED: '已取消', DONE: '已完成' }
  return map[status] || status || '—'
}

function appointmentTagType(status) {
  const map = { PENDING: 'warning', CONFIRMED: 'primary', CANCELLED: 'info', DONE: 'success' }
  return map[status] || 'info'
}

function recTypeTag(type) {
  const map = { EXERCISE: 'primary', FOOD: 'warning', PLAN: 'success' }
  return map[type] || 'info'
}

function openStatusDialog(row) {
  currentAppointment.value = row
  statusForm.status = row.status || 'PENDING'
  statusForm.remark = row.remark || ''
  statusDialogVisible.value = true
}

async function submitStatus() {
  if (!currentAppointment.value?.id) return
  statusSubmitting.value = true
  try {
    await updateAppointmentStatus(currentAppointment.value.id, {
      status: statusForm.status,
      remark: statusForm.remark
    })
    ElMessage.success('预约状态更新成功')
    statusDialogVisible.value = false
    await loadAll()
  } catch {
  } finally {
    statusSubmitting.value = false
  }
}

async function loadAll() {
  pageLoading.value = true
  loadError.value = ''
  try {
    await Promise.all([fetchAdminInfo(), fetchStats(), fetchDistribution(), fetchLists()])
    await nextTick()
    renderCharts()
  } catch (e) {
    loadError.value = e?.message || '数据加载失败，请稍后重试'
  } finally {
    pageLoading.value = false
  }
}

function onResize() {
  appointmentChart?.resize()
  recommendationChart?.resize()
  planChart?.resize()
}

onMounted(() => {
  loadAll()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  appointmentChart?.dispose()
  recommendationChart?.dispose()
  planChart?.dispose()
  appointmentChart = null
  recommendationChart = null
  planChart = null
})
</script>

<style scoped>
.admin-dashboard {
  min-height: calc(100vh - 100px);
  background: #f0f2f5;
  margin: -12px -12px 0;
  padding: 16px;
}

.load-error-banner {
  margin-bottom: 12px;
}

.dash-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding: 18px 22px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%);
  border-radius: 12px;
  margin-bottom: 16px;
  color: #fff;
}

.dash-header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dash-title {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 0.5px;
}

.dash-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.dash-meta .el-tag {
  --el-tag-bg-color: rgba(255,255,255,0.15);
  --el-tag-border-color: rgba(255,255,255,0.2);
  --el-tag-text-color: #fff;
  --el-tag-info-color: #fff;
}

.dash-date {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

.stat-grid {
  margin-bottom: 14px;
}

.stat-grid .el-col {
  margin-bottom: 10px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  border-top: 3px solid transparent;
  transition: box-shadow 0.2s, transform 0.2s;
  height: 100%;
}

.stat-card:hover {
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.stat-card-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-card-label {
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-card-value {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.3;
}

.chart-row {
  margin-bottom: 14px;
}

.table-row {
  margin-bottom: 14px;
}

.dash-card {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  height: 100%;
}

.card-title {
  font-weight: 700;
  font-size: 15px;
  color: #1e293b;
}

.card-head-flex {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.chart-box {
  width: 100%;
  height: 280px;
}
</style>
