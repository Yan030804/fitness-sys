<template>
  <div v-loading="pageLoading" class="user-dashboard" element-loading-text="加载中…">
    <div v-if="pageError" class="error-banner">
      <el-alert :title="pageError" type="error" show-icon closable @close="pageError = ''" />
    </div>

    <div class="hero-card">
      <div class="hero-bg-shape hero-bg-shape-1"></div>
      <div class="hero-bg-shape hero-bg-shape-2"></div>
      <div class="hero-bg-shape hero-bg-shape-3"></div>
      <div class="hero-content">
        <div class="hero-left">
          <h1 class="hero-title">你好，{{ user.nickname || user.realName || user.username || '用户' }} 👋</h1>
          <p class="hero-subtitle">今天也要坚持训练，系统已为你整理好今日数据。</p>
          <div class="hero-tags">
            <el-tag v-if="user.fitnessGoal" effect="dark" round class="hero-tag">
              <el-icon><Aim /></el-icon>
              <span>目标：{{ user.fitnessGoal }}</span>
            </el-tag>
            <el-tag v-if="user.activityLevel" effect="dark" round class="hero-tag hero-tag-orange">
              <el-icon><TrendCharts /></el-icon>
              <span>水平：{{ user.activityLevel }}</span>
            </el-tag>
            <el-tag v-if="user.dietPreference" effect="dark" round class="hero-tag hero-tag-green">
              <el-icon><Dish /></el-icon>
              <span>偏好：{{ user.dietPreference }}</span>
            </el-tag>
          </div>
        </div>
        <div class="hero-right">
          <div class="hero-metrics">
            <div class="hero-metric-item">
              <div class="metric-icon-wrap">
                <el-icon :size="20"><User /></el-icon>
              </div>
              <div class="metric-info">
                <span class="metric-label">身高</span>
                <span class="metric-value">{{ user.heightCm ?? '—' }}<small>cm</small></span>
              </div>
            </div>
            <div class="hero-metric-item">
              <div class="metric-icon-wrap metric-icon-weight">
                <el-icon :size="20"><ScaleToOriginal /></el-icon>
              </div>
              <div class="metric-info">
                <span class="metric-label">体重</span>
                <span class="metric-value">{{ user.weightKg ?? '—' }}<small>kg</small></span>
              </div>
            </div>
            <div class="hero-metric-item">
              <div class="metric-icon-wrap metric-icon-bmi">
                <el-icon :size="20"><DataLine /></el-icon>
              </div>
              <div class="metric-info">
                <span class="metric-label">BMI</span>
                <span class="metric-value">{{ user.bmi ?? '暂未完善' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-row :gutter="14" class="stat-grid">
      <el-col v-for="item in statCards" :key="item.key" :xs="12" :sm="8" :md="6" :lg="4">
        <div class="stat-card" :style="{ borderTopColor: item.color }">
          <div class="stat-card-icon" :style="{ background: item.bg }">
            <el-icon :size="20" :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-card-body">
            <span class="stat-card-label">{{ item.label }}</span>
            <span class="stat-card-value">{{ item.value }}<small>{{ item.unit }}</small></span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="module-row">
      <el-col :xs="24" :lg="12">
        <el-card class="module-card" shadow="never">
          <template #header>
            <div class="module-card-header">
              <span class="module-card-title">
                <el-icon class="title-icon title-icon-blue"><Bicycle /></el-icon>
                今日训练
              </span>
              <el-tag type="primary" size="small" effect="plain" round>{{ todayWorkouts.length }} 条</el-tag>
            </div>
          </template>
          <div v-if="todayWorkouts.length" class="record-list">
            <div v-for="item in todayWorkouts" :key="item.id" class="record-item">
              <div class="record-icon-wrap record-icon-workout">
                <el-icon><Bicycle /></el-icon>
              </div>
              <div class="record-body">
                <div class="record-name">{{ item.exerciseName }}</div>
                <div class="record-meta">
                  <span v-if="item.durationMin">{{ item.durationMin }}分钟</span>
                  <span v-if="item.setsCount">{{ item.setsCount }}组</span>
                  <span v-if="item.repsCount">{{ item.repsCount }}次</span>
                  <span v-if="item.caloriesBurned" class="record-cal">
                    <el-icon><Sunny /></el-icon>{{ item.caloriesBurned }} kcal
                  </span>
                </div>
              </div>
              <div class="record-right">
                <el-tag v-if="item.completionStatus === '已完成'" type="success" size="small" effect="plain" round>已完成</el-tag>
                <el-tag v-else type="warning" size="small" effect="plain" round>{{ item.completionStatus || '未完成' }}</el-tag>
                <span v-if="item.feedbackScore" class="feedback-score">{{ item.feedbackScore }}分</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-empty description="今天还没有训练记录" :image-size="80" />
            <el-button type="primary" round @click="router.push('/training')">
              <el-icon><Plus /></el-icon>添加训练记录
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="module-card" shadow="never">
          <template #header>
            <div class="module-card-header">
              <span class="module-card-title">
                <el-icon class="title-icon title-icon-orange"><Dish /></el-icon>
                今日饮食
              </span>
              <el-tag type="warning" size="small" effect="plain" round>{{ todayDiets.length }} 条</el-tag>
            </div>
          </template>
          <div v-if="todayDiets.length" class="record-list">
            <div v-for="item in todayDiets" :key="item.id" class="record-item">
              <div class="record-icon-wrap record-icon-diet">
                <el-icon><Food /></el-icon>
              </div>
              <div class="record-body">
                <div class="record-name">
                  <el-tag :type="mealTagType(item.mealType)" size="small" effect="plain" round class="meal-tag">{{ item.mealType }}</el-tag>
                  {{ item.foodName }}
                </div>
                <div class="record-meta">
                  <span v-if="item.intakeGrams">{{ item.intakeGrams }}g</span>
                  <span v-if="item.caloriesIntake" class="record-cal record-cal-orange">
                    <el-icon><Sunny /></el-icon>{{ item.caloriesIntake }} kcal
                  </span>
                  <el-tag v-if="item.isRecommended" type="success" size="mini" effect="plain" round class="rec-badge">推荐</el-tag>
                </div>
              </div>
              <div v-if="item.remark" class="record-right">
                <span class="record-remark">{{ item.remark }}</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-empty description="今天还没有饮食记录" :image-size="80" />
            <el-button type="warning" round @click="router.push('/diet')">
              <el-icon><Plus /></el-icon>添加饮食记录
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="module-row">
      <el-col :span="24">
        <el-card class="module-card" shadow="never">
          <template #header>
            <div class="module-card-header">
              <span class="module-card-title">
                <el-icon class="title-icon title-icon-purple"><MagicStick /></el-icon>
                最新推荐
                <el-tag v-if="recommendations.latestTime" type="info" size="small" effect="plain" round class="time-tag">
                  {{ formatDateTime(recommendations.latestTime) }}
                </el-tag>
              </span>
              <el-button type="primary" size="small" round :loading="recommendLoading" @click="handleGenerateRecommend">
                <el-icon><Refresh /></el-icon>
                重新生成推荐
              </el-button>
            </div>
          </template>
          <div v-if="recommendations.list.length" class="recommend-grid">
            <div v-for="(item, idx) in recommendations.list" :key="idx" class="recommend-card">
              <div class="recommend-type-badge" :class="'recommend-type-' + (item.recType || '').toLowerCase()">
                {{ recTypeLabel(item.recType) }}
              </div>
              <div class="recommend-card-title">{{ item.targetName }}</div>
              <div class="recommend-card-meta">
                <span v-if="item.algorithmType">算法：{{ item.algorithmType }}</span>
              </div>
              <div class="recommend-card-reason">{{ item.reason }}</div>
              <div class="recommend-card-footer">
                <span v-if="item.score != null" class="score-value">{{ item.score }}<small>分</small></span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无推荐，点击上方按钮生成" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="module-row">
      <el-col :xs="24" :lg="12">
        <el-card class="module-card" shadow="never">
          <template #header>
            <div class="module-card-header">
              <span class="module-card-title">
                <el-icon class="title-icon title-icon-green"><Document /></el-icon>
                我的计划
              </span>
              <el-button link type="primary" size="small" @click="router.push('/plan')">查看全部</el-button>
            </div>
          </template>
          <div v-if="plans.length" class="plan-list">
            <div v-for="item in plans" :key="item.id" class="plan-item" @click="openPlanDetail(item)">
              <div class="plan-item-left">
                <div class="plan-item-icon">
                  <el-icon><Document /></el-icon>
                </div>
              </div>
              <div class="plan-item-body">
                <div class="plan-item-name">{{ item.planName }}</div>
                <div class="plan-item-meta">
                  <el-tag :type="planTypeTag(item.planType)" size="small" effect="plain" round>{{ item.planType }}</el-tag>
                  <el-tag v-if="item.cycleType" type="info" size="small" effect="plain" round>{{ item.cycleType }}</el-tag>
                  <span class="plan-date">{{ item.startDate }} ~ {{ item.endDate }}</span>
                </div>
              </div>
              <div class="plan-item-right">
                <el-tag :type="planStatusTag(item.status)" size="small" effect="plain" round>{{ item.status }}</el-tag>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无计划" :image-size="80" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="module-card" shadow="never">
          <template #header>
            <div class="module-card-header">
              <span class="module-card-title">
                <el-icon class="title-icon title-icon-teal"><Calendar /></el-icon>
                我的预约
              </span>
              <el-button link type="primary" size="small" @click="router.push('/appointment')">查看全部</el-button>
            </div>
          </template>
          <div v-if="appointments.length" class="record-list">
            <div v-for="item in appointments" :key="item.id" class="record-item appointment-item">
              <div class="record-icon-wrap record-icon-appointment">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="record-body">
                <div class="record-name">{{ item.appointmentType || '健身预约' }}</div>
                <div class="record-meta">
                  <span><el-icon><Clock /></el-icon> {{ item.reserveDate }} {{ item.reserveTime }}</span>
                </div>
                <div v-if="item.remark" class="record-remark-text">{{ item.remark }}</div>
              </div>
              <div class="record-right">
                <el-tag :type="appointmentStatusTag(item.status)" size="small" effect="plain" round>
                  {{ appointmentStatusLabel(item.status) }}
                </el-tag>
                <el-button
                  v-if="canCancelAppointment(item)"
                  type="danger"
                  size="small"
                  round
                  plain
                  :loading="item._cancelling"
                  @click.stop="handleCancelAppointment(item)"
                >取消预约</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无预约记录" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="module-row">
      <el-col :span="24">
        <el-card class="module-card ai-card" shadow="never">
          <template #header>
            <div class="module-card-header">
              <span class="module-card-title">
                <el-icon class="title-icon title-icon-cyan"><ChatDotRound /></el-icon>
                AI 健身助手
              </span>
            </div>
          </template>
          <div class="ai-body">
            <div v-if="aiAnswer.answer" class="ai-answer-box">
              <div class="ai-answer-header">
                <el-icon><Monitor /></el-icon>
                <span>AI 回答</span>
                <el-tag v-if="aiAnswer.suggestionType" size="small" type="info" effect="plain" round>{{ aiAnswer.suggestionType }}</el-tag>
              </div>
              <div class="ai-answer-content">{{ aiAnswer.answer }}</div>
              <div v-if="aiAnswer.references && aiAnswer.references.length" class="ai-answer-refs">
                <span class="ref-label">参考：</span>
                <el-tag v-for="(ref, ridx) in aiAnswer.references" :key="ridx" size="small" effect="plain" round>{{ ref }}</el-tag>
              </div>
            </div>
            <div class="ai-input-area">
              <el-input
                v-model="aiInput"
                placeholder="输入你的健身问题，例如：减脂期间晚餐怎么吃？"
                class="ai-input"
                :disabled="aiLoading"
                @keyup.enter="handleSendAi"
              >
                <template #prefix>
                  <el-icon><ChatDotRound /></el-icon>
                </template>
                <template #append>
                  <el-button type="primary" :loading="aiLoading" @click="handleSendAi">
                    <el-icon v-if="!aiLoading"><Promotion /></el-icon>
                    发送
                  </el-button>
                </template>
              </el-input>
              <el-button type="primary" link class="ai-goto-btn" @click="router.push('/ai-chat')">
                进入完整问答页面 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="planDetailVisible" title="计划明细" width="680px" destroy-on-close>
      <div v-loading="planDetailLoading">
        <div v-if="planDetailItems.length" class="plan-detail-list">
          <div v-for="item in planDetailItems" :key="item.id || item.itemId" class="plan-detail-item">
            <div class="pd-left">
              <el-tag :type="item.itemType === 'TRAINING' ? 'primary' : 'warning'" size="small" effect="plain" round>
                {{ item.itemType === 'TRAINING' ? '训练' : '饮食' }}
              </el-tag>
              <span class="pd-name">{{ item.targetName }}</span>
              <span v-if="item.itemDate" class="pd-date">{{ item.itemDate }} {{ item.itemTime || '' }}</span>
            </div>
            <div class="pd-mid">
              <span v-if="item.setsCount">{{ item.setsCount }}组</span>
              <span v-if="item.repsCount">{{ item.repsCount }}次</span>
              <span v-if="item.durationMin">{{ item.durationMin }}分钟</span>
              <span v-if="item.intakeGrams">{{ item.intakeGrams }}g</span>
              <span v-if="item.calories" class="record-cal"><el-icon><Sunny /></el-icon>{{ item.calories }}kcal</span>
            </div>
            <div class="pd-right">
              <el-tag :type="item.completionStatus === '已完成' ? 'success' : 'warning'" size="small" effect="plain" round>
                {{ item.completionStatus || '未完成' }}
              </el-tag>
              <el-button
                v-if="item.completionStatus !== '已完成'"
                type="success"
                size="small"
                round
                :loading="item._completing"
                @click="handleCompletePlanItem(item)"
              >
                <el-icon><Check /></el-icon> 打卡完成
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无计划明细" :image-size="80" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Aim, TrendCharts, Dish, User, ScaleToOriginal, DataLine, Bicycle, Timer,
  Check, Food, Sunny, Refresh, Calendar, Clock, ChatDotRound, Promotion,
  ArrowRight, Document, Monitor, Plus, MagicStick
} from '@element-plus/icons-vue'
import {
  getCurrentUserDetail,
  getWorkoutRecordList,
  getDietRecordList,
  getPlanList,
  getPlanItems,
  completePlanItem,
  getMyAppointmentList,
  cancelAppointment,
  getLatestRecommendations,
  generateRecommendations,
  chatAi
} from '../api/userDashboard'

const router = useRouter()

const pageLoading = ref(false)
const pageError = ref('')

function getToday() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const today = getToday()

const user = reactive({
  nickname: '',
  realName: '',
  username: '',
  fitnessGoal: '',
  activityLevel: '',
  heightCm: null,
  weightKg: null,
  bmi: null,
  dietPreference: ''
})

const stats = reactive({
  todayWorkoutCount: 0,
  todayDuration: 0,
  todayCaloriesBurned: 0,
  todayCaloriesIntake: 0,
  planCount: 0,
  appointmentCount: 0,
  latestRecommendCount: 0
})

const statCards = computed(() => [
  { key: 'todayWorkoutCount', label: '今日训练次数', value: stats.todayWorkoutCount, unit: ' 次', icon: Bicycle, color: '#2563eb', bg: '#eff6ff' },
  { key: 'todayDuration', label: '今日运动时长', value: stats.todayDuration, unit: ' 分钟', icon: Timer, color: '#10b981', bg: '#f0fdf4' },
  { key: 'todayCaloriesBurned', label: '今日消耗热量', value: stats.todayCaloriesBurned, unit: ' kcal', icon: Sunny, color: '#f59e0b', bg: '#fff7ed' },
  { key: 'todayCaloriesIntake', label: '今日饮食摄入', value: stats.todayCaloriesIntake, unit: ' kcal', icon: Dish, color: '#6366f1', bg: '#f5f3ff' },
  { key: 'planCount', label: '当前计划数', value: stats.planCount, unit: '', icon: Document, color: '#0891b2', bg: '#ecfeff' },
  { key: 'appointmentCount', label: '我的预约数', value: stats.appointmentCount, unit: '', icon: Calendar, color: '#8b5cf6', bg: '#f5f3ff' },
  // { key: 'latestRecommendCount', label: '最新推荐数', value: stats.latestRecommendCount, unit: ' 条', icon: MagicStick, color: '#ec4899', bg: '#fdf2f8' }
])

const todayWorkouts = ref([])
const todayDiets = ref([])

const recommendations = reactive({
  latestTime: '',
  list: []
})
const recommendLoading = ref(false)

const plans = ref([])

const appointments = ref([])

const planDetailVisible = ref(false)
const planDetailLoading = ref(false)
const planDetailItems = ref([])

const aiInput = ref('')
const aiLoading = ref(false)
const aiAnswer = reactive({
  answer: '',
  suggestionType: '',
  references: []
})

function mealTagType(type) {
  const map = { '早餐': 'warning', '午餐': 'success', '晚餐': 'info', '加餐': '', 'BREAKFAST': 'warning', 'LUNCH': 'success', 'DINNER': 'info', 'SNACK': '' }
  return map[type] || ''
}

function recTypeLabel(type) {
  const map = { EXERCISE: '运动推荐', FOOD: '食物推荐', PLAN: '计划推荐', EXERCISE_RECOMMENDATION: '运动推荐', FOOD_RECOMMENDATION: '食物推荐', PLAN_RECOMMENDATION: '计划推荐' }
  return map[type] || type || '推荐'
}

function planTypeTag(type) {
  const map = { TRAINING: '', DIET: 'warning', COMPREHENSIVE: 'success' }
  return map[type] || 'info'
}

function planStatusTag(status) {
  const map = { ACTIVE: 'success', COMPLETED: 'info', PAUSED: 'warning', CANCELLED: 'danger' }
  return map[status] || 'info'
}

function appointmentStatusTag(status) {
  const map = { PENDING: 'warning', CONFIRMED: 'success', CANCELLED: 'info', DONE: '' }
  return map[status] || 'info'
}

function appointmentStatusLabel(status) {
  const map = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', DONE: '已完成' }
  return map[status] || status || '—'
}

function canCancelAppointment(item) {
  return item.status === 'PENDING' || item.status === 'CONFIRMED'
}

function formatDateTime(value) {
  if (!value) return ''
  const d = new Date(value)
  if (isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function loadUserProfile() {
  try {
    const res = await getCurrentUserDetail()
    if (res) {
      Object.assign(user, {
        nickname: res.nickname || '',
        realName: res.realName || res.real_name || '',
        username: res.username || '',
        fitnessGoal: res.fitnessGoal || res.fitness_goal || '',
        activityLevel: res.activityLevel || res.activity_level || '',
        heightCm: res.heightCm ?? res.height_cm ?? res.height ?? null,
        weightKg: res.weightKg ?? res.weight_kg ?? res.weight ?? null,
        bmi: res.bmi ?? null,
        dietPreference: res.dietPreference || res.diet_preference || ''
      })
    }
  } catch (e) {
    pageError.value = '加载用户信息失败'
  }
}

async function loadTodayWorkouts() {
  try {
    const res = await getWorkoutRecordList({
      workoutDateStart: today,
      workoutDateEnd: today,
      pageNum: 1,
      pageSize: 50
    })
    const list = res?.list || res?.records || res || []
    todayWorkouts.value = Array.isArray(list) ? list.slice(0, 10) : []
    stats.todayWorkoutCount = res?.total ?? list.length
    stats.todayDuration = todayWorkouts.value.reduce((s, r) => s + (r.durationMin || r.duration || r.durationMinutes || 0), 0)
    stats.todayCaloriesBurned = todayWorkouts.value.reduce((s, r) => s + (r.caloriesBurned || r.calories || 0), 0)
  } catch (e) {
    todayWorkouts.value = []
  }
}

async function loadTodayDiets() {
  try {
    const res = await getDietRecordList({
      dietDateStart: today,
      dietDateEnd: today,
      pageNum: 1,
      pageSize: 50
    })
    const list = res?.list || res?.records || res || []
    todayDiets.value = Array.isArray(list) ? list.slice(0, 10) : []
    stats.todayCaloriesIntake = todayDiets.value.reduce((s, r) => s + (r.caloriesIntake || r.calories || 0), 0)
  } catch (e) {
    todayDiets.value = []
  }
}

async function loadPlans() {
  try {
    const res = await getPlanList({ pageNum: 1, pageSize: 5 })
    const list = res?.list || res?.records || res || []
    plans.value = Array.isArray(list) ? list : []
    stats.planCount = res?.total ?? plans.value.length
  } catch (e) {
    plans.value = []
  }
}

async function loadAppointments() {
  try {
    const res = await getMyAppointmentList({ pageNum: 1, pageSize: 5 })
    const list = res?.list || res?.records || res || []
    appointments.value = Array.isArray(list) ? list.map(a => ({ ...a, _cancelling: false })) : []
    stats.appointmentCount = res?.total ?? appointments.value.length
  } catch (e) {
    appointments.value = []
  }
}

async function loadRecommendations() {
  try {
    const res = await getLatestRecommendations()
    const list = res?.list || res?.records || res || []
    recommendations.list = Array.isArray(list) ? list : []
    recommendations.latestTime = res?.latestTime || res?.latest_time || res?.createdAt || ''
    stats.latestRecommendCount = recommendations.list.length
  } catch (e) {
    recommendations.list = []
  }
}

async function handleGenerateRecommend() {
  recommendLoading.value = true
  try {
    await generateRecommendations({ recommendTypes: ['EXERCISE', 'FOOD', 'PLAN'] })
    ElMessage.success('推荐已重新生成')
    await loadRecommendations()
  } catch (e) {
  } finally {
    recommendLoading.value = false
  }
}

async function openPlanDetail(plan) {
  planDetailVisible.value = true
  planDetailLoading.value = true
  planDetailItems.value = []
  try {
    const res = await getPlanItems(plan.id)
    const list = res?.list || res?.records || res || []
    planDetailItems.value = Array.isArray(list) ? list.map(i => ({ ...i, _completing: false })) : []
  } catch (e) {
    planDetailItems.value = []
  } finally {
    planDetailLoading.value = false
  }
}

async function handleCompletePlanItem(item) {
  item._completing = true
  try {
    await completePlanItem(item.id || item.itemId, {
      completionStatus: '已完成',
      remark: '首页快捷打卡'
    })
    ElMessage.success('打卡成功')
    item.completionStatus = '已完成'
    await loadTodayWorkouts()
  } catch (e) {
  } finally {
    item._completing = false
  }
}

async function handleCancelAppointment(item) {
  try {
    await ElMessageBox.confirm('确定要取消该预约吗？', '取消预约', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      type: 'warning'
    })
  } catch {
    return
  }
  item._cancelling = true
  try {
    await cancelAppointment(item.id, { remark: '用户在首页取消预约' })
    ElMessage.success('预约已取消')
    await loadAppointments()
  } catch (e) {
  } finally {
    item._cancelling = false
  }
}

async function handleSendAi() {
  const msg = aiInput.value.trim()
  if (!msg) {
    ElMessage.warning('请输入你的问题')
    return
  }
  aiLoading.value = true
  aiAnswer.answer = ''
  aiAnswer.suggestionType = ''
  aiAnswer.references = []
  try {
    const res = await chatAi({ message: msg })
    aiAnswer.answer = res?.answer || res?.data?.answer || ''
    aiAnswer.suggestionType = res?.suggestionType || res?.suggestion_type || ''
    aiAnswer.references = res?.references || res?.data?.references || []
  } catch (e) {
    aiAnswer.answer = '抱歉，AI 服务暂时不可用，请稍后重试。'
  } finally {
    aiLoading.value = false
  }
}

onMounted(async () => {
  pageLoading.value = true
  pageError.value = ''
  try {
    await Promise.allSettled([
      loadUserProfile(),
      loadTodayWorkouts(),
      loadTodayDiets(),
      loadPlans(),
      loadAppointments(),
      loadRecommendations()
    ])
  } catch (e) {
    pageError.value = '部分数据加载失败'
  } finally {
    pageLoading.value = false
  }
})
</script>

<style scoped>
.user-dashboard {
  min-height: calc(100vh - 100px);
  margin: -12px -12px 0;
  padding: 16px;
}

.error-banner {
  margin-bottom: 12px;
}

.hero-card {
  position: relative;
  background: linear-gradient(135deg, #0d9488 0%, #2563eb 50%, #6366f1 100%);
  border-radius: 16px;
  padding: 36px 40px;
  color: #fff;
  overflow: hidden;
  min-height: 180px;
  margin-bottom: 14px;
}

.hero-bg-shape {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.hero-bg-shape-1 {
  width: 280px;
  height: 280px;
  top: -80px;
  right: -40px;
  background: radial-gradient(circle, rgba(255,255,255,0.12) 0%, transparent 70%);
}

.hero-bg-shape-2 {
  width: 180px;
  height: 180px;
  bottom: -60px;
  left: 30%;
  background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
}

.hero-bg-shape-3 {
  width: 110px;
  height: 110px;
  top: 20px;
  right: 35%;
  background: radial-gradient(circle, rgba(255,255,255,0.06) 0%, transparent 70%);
}

.hero-content {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 1;
}

.hero-title {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 10px;
  letter-spacing: 0.5px;
}

.hero-subtitle {
  font-size: 14px;
  opacity: 0.88;
  margin: 0 0 20px;
  max-width: 480px;
  line-height: 1.6;
}

.hero-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-tag {
  background: rgba(255,255,255,0.18) !important;
  border: 1px solid rgba(255,255,255,0.25) !important;
  color: #fff !important;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  font-size: 13px;
  backdrop-filter: blur(4px);
}

.hero-right {
  flex-shrink: 0;
}

.hero-metrics {
  display: flex;
  gap: 14px;
}

.hero-metric-item {
  background: rgba(255,255,255,0.12);
  backdrop-filter: blur(8px);
  border-radius: 14px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 120px;
  border: 1px solid rgba(255,255,255,0.15);
}

.metric-icon-wrap {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.metric-icon-weight {
  background: rgba(251, 191, 36, 0.25);
}

.metric-icon-bmi {
  background: rgba(52, 211, 153, 0.25);
}

.metric-info {
  display: flex;
  flex-direction: column;
}

.metric-label {
  font-size: 12px;
  opacity: 0.75;
  margin-bottom: 3px;
}

.metric-value {
  font-size: 20px;
  font-weight: 800;
}

.metric-value small {
  font-size: 12px;
  font-weight: 400;
  opacity: 0.75;
  margin-left: 2px;
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

.stat-card-value small {
  font-size: 12px;
  font-weight: 400;
  color: #94a3b8;
}

.module-row {
  margin-bottom: 14px;
}

.module-row .el-col {
  margin-bottom: 10px;
}

.module-card {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  height: 100%;
}

.module-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.module-card-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 20px;
}

.title-icon-blue { color: #2563eb; }
.title-icon-orange { color: #f59e0b; }
.title-icon-purple { color: #8b5cf6; }
.title-icon-teal { color: #14b8a6; }
.title-icon-green { color: #10b981; }
.title-icon-cyan { color: #06b6d4; }

.time-tag {
  margin-left: 8px;
  font-weight: 400;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.record-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.record-item:hover {
  background: #f1f5f9;
  border-color: #e2e8f0;
}

.record-icon-wrap {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
}

.record-icon-workout {
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
}

.record-icon-diet {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.record-icon-appointment {
  background: rgba(20, 184, 166, 0.1);
  color: #14b8a6;
}

.record-body {
  flex: 1;
  min-width: 0;
}

.record-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 3px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.meal-tag {
  flex-shrink: 0;
}

.record-meta {
  font-size: 12px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.record-meta .el-icon {
  margin-right: 2px;
}

.record-cal {
  color: #2563eb;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.record-cal-orange {
  color: #f59e0b;
}

.rec-badge {
  margin-left: 4px;
}

.record-right {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.feedback-score {
  font-size: 12px;
  color: #94a3b8;
}

.record-remark {
  font-size: 12px;
  color: #94a3b8;
}

.record-remark-text {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 3px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}

.recommend-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 18px;
  position: relative;
  border: 1px solid #f1f5f9;
  transition: all 0.2s;
}

.recommend-card:hover {
  border-color: #e2e8f0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}

.recommend-type-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 20px;
  margin-bottom: 10px;
}

.recommend-type-exercise,
.recommend-type-exercise_recommendation {
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
}

.recommend-type-food,
.recommend-type-food_recommendation {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.recommend-type-plan,
.recommend-type-plan_recommendation {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.recommend-card-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 6px;
}

.recommend-card-meta {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 6px;
}

.recommend-card-reason {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  margin-bottom: 12px;
  min-height: 36px;
}

.recommend-card-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.score-value {
  font-size: 22px;
  font-weight: 800;
  color: #2563eb;
}

.score-value small {
  font-size: 12px;
  font-weight: 400;
  color: #94a3b8;
}

.plan-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.plan-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid transparent;
  transition: all 0.2s;
  cursor: pointer;
}

.plan-item:hover {
  background: #f1f5f9;
  border-color: #e2e8f0;
}

.plan-item-left {
  flex-shrink: 0;
}

.plan-item-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
  display: flex;
  align-items: center;
  justify-content: center;
}

.plan-item-body {
  flex: 1;
  min-width: 0;
}

.plan-item-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.plan-item-meta {
  font-size: 12px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.plan-date {
  color: #94a3b8;
}

.plan-item-right {
  flex-shrink: 0;
}

.plan-detail-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 420px;
  overflow-y: auto;
}

.plan-detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
}

.pd-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.pd-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.pd-date {
  font-size: 12px;
  color: #94a3b8;
}

.pd-mid {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #64748b;
  flex-shrink: 0;
}

.pd-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.ai-card :deep(.el-card__body) {
  padding: 24px;
}

.ai-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ai-answer-box {
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 12px;
  padding: 16px 20px;
}

.ai-answer-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #0369a1;
  margin-bottom: 10px;
}

.ai-answer-content {
  font-size: 14px;
  color: #334155;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-answer-refs {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.ref-label {
  font-size: 12px;
  color: #94a3b8;
}

.ai-input-area {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

:deep(.ai-input .el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 8px;
}

:deep(.ai-input .el-input-group__append) {
  border-radius: 0 12px 12px 0;
  background: #2563eb;
}

:deep(.ai-input .el-input-group__append .el-button) {
  color: #fff;
}

.ai-goto-btn {
  align-self: flex-end;
  font-size: 13px;
}

@media (max-width: 1400px) {
  .hero-metrics {
    gap: 10px;
  }

  .hero-metric-item {
    min-width: 100px;
    padding: 14px 16px;
  }

  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1200px) {
  .hero-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-right {
    margin-top: 20px;
    width: 100%;
  }

  .hero-metrics {
    flex-direction: row;
    gap: 10px;
  }

  .hero-metric-item {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .hero-card {
    padding: 24px 20px;
  }

  .hero-title {
    font-size: 22px;
  }

  .hero-metrics {
    flex-direction: column;
  }
}
</style>
