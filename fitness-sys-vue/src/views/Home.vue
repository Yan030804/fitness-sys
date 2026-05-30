<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="hero-card">
          <div class="hero-bg-shape hero-bg-shape-1"></div>
          <div class="hero-bg-shape hero-bg-shape-2"></div>
          <div class="hero-bg-shape hero-bg-shape-3"></div>
          <div class="hero-content">
            <div class="hero-left">
              <h1 class="hero-title">你好，{{ userProfile.nickname || authStore.username || '用户' }} 👋</h1>
              <p class="hero-subtitle">今天也要坚持训练，系统已为你整理好今日计划。</p>
              <div class="hero-tags">
                <el-tag effect="dark" round class="hero-tag">
                  <el-icon><Aim /></el-icon>
                  <span>目标：{{ userProfile.fitnessGoal }}</span>
                </el-tag>
                <el-tag effect="dark" round class="hero-tag hero-tag-orange">
                  <el-icon><TrendCharts /></el-icon>
                  <span>水平：{{ userProfile.activityLevel }}</span>
                </el-tag>
                <el-tag effect="dark" round class="hero-tag hero-tag-green">
                  <el-icon><Dish /></el-icon>
                  <span>偏好：{{ userProfile.dietPreference }}</span>
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
                    <span class="metric-value">{{ userProfile.height }}<small>cm</small></span>
                  </div>
                </div>
                <div class="hero-metric-item">
                  <div class="metric-icon-wrap metric-icon-weight">
                    <el-icon :size="20"><ScaleToOriginal /></el-icon>
                  </div>
                  <div class="metric-info">
                    <span class="metric-label">体重</span>
                    <span class="metric-value">{{ userProfile.weight }}<small>kg</small></span>
                  </div>
                </div>
                <div class="hero-metric-item">
                  <div class="metric-icon-wrap metric-icon-bmi">
                    <el-icon :size="20"><DataLine /></el-icon>
                  </div>
                  <div class="metric-info">
                    <span class="metric-label">BMI</span>
                    <span class="metric-value">{{ userProfile.bmi }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="(stat, index) in summaryStats" :key="index">
        <div class="stat-card">
          <div class="stat-card-icon" :style="{ background: stat.bgColor }">
            <el-icon :style="{ color: stat.color }" :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-card-body">
            <span class="stat-card-label">{{ stat.label }}</span>
            <span class="stat-card-value">{{ stat.value }}<small>{{ stat.unit }}</small></span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :span="24">
        <div class="completion-card">
          <div class="completion-header">
            <span class="completion-title">
              <el-icon><Finished /></el-icon>
              今日完成进度
            </span>
            <span class="completion-rate">{{ todayPlans.completionRate }}%</span>
          </div>
          <el-progress
            :percentage="todayPlans.completionRate"
            :stroke-width="14"
            :color="progressColors"
            class="completion-progress"
          />
          <div class="completion-detail">
            <span>训练 {{ completedTrainingCount }}/{{ todayPlans.trainingItems.length }} 项</span>
            <span>饮食 {{ completedDietCount }}/{{ todayPlans.dietItems.length }} 项</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :span="12">
        <el-card class="module-card" shadow="hover">
          <template #header>
            <div class="module-card-header">
              <span class="module-card-title">
                <el-icon class="title-icon title-icon-blue"><Bicycle /></el-icon>
                今日训练计划
              </span>
              <el-tag type="primary" size="small" effect="plain" round>{{ todayPlans.trainingItems.length }} 项</el-tag>
            </div>
          </template>
          <div v-if="todayPlans.trainingItems.length" class="plan-list">
            <div
              v-for="item in todayPlans.trainingItems"
              :key="item.id"
              class="plan-item"
              :class="{ 'plan-item-done': item.completed }"
            >
              <div class="plan-item-left">
                <div class="plan-item-icon" :class="{ 'plan-item-icon-done': item.completed }">
                  <el-icon v-if="item.completed"><Select /></el-icon>
                  <el-icon v-else><Timer /></el-icon>
                </div>
              </div>
              <div class="plan-item-body">
                <div class="plan-item-name">{{ item.exerciseName }}</div>
                <div class="plan-item-meta">
                  <span v-if="item.sets && item.reps">{{ item.sets }}组 × {{ item.reps }}次</span>
                  <span v-else-if="item.durationMinutes">{{ item.durationMinutes }}分钟</span>
                  <el-divider direction="vertical" />
                  <span class="plan-item-cal">
                    <el-icon><Sunny /></el-icon>
                    {{ item.estimatedCalories }} kcal
                  </span>
                </div>
              </div>
              <div class="plan-item-right">
                <el-tag v-if="item.completed" type="success" size="small" effect="plain" round>已完成</el-tag>
                <el-button
                  v-else
                  type="success"
                  size="small"
                  round
                  :loading="item._completing"
                  @click="handleCompleteTraining(item)"
                >
                  <el-icon><Check /></el-icon>
                  打卡完成
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无训练计划" :image-size="80" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="module-card" shadow="hover">
          <template #header>
            <div class="module-card-header">
              <span class="module-card-title">
                <el-icon class="title-icon title-icon-orange"><Dish /></el-icon>
                今日饮食计划
              </span>
              <el-tag type="warning" size="small" effect="plain" round>{{ todayPlans.dietItems.length }} 项</el-tag>
            </div>
          </template>
          <div v-if="todayPlans.dietItems.length" class="plan-list">
            <div
              v-for="item in todayPlans.dietItems"
              :key="item.id"
              class="plan-item"
              :class="{ 'plan-item-done': item.completed }"
            >
              <div class="plan-item-left">
                <div class="plan-item-icon diet-icon" :class="{ 'plan-item-icon-done': item.completed }">
                  <el-icon v-if="item.completed"><Select /></el-icon>
                  <el-icon v-else><Food /></el-icon>
                </div>
              </div>
              <div class="plan-item-body">
                <div class="plan-item-name">
                  <el-tag :type="mealTagType(item.mealType)" size="small" effect="plain" round class="meal-tag">{{ item.mealType }}</el-tag>
                  {{ item.foodName }}
                </div>
                <div class="plan-item-meta">
                  <span>{{ item.amount }}</span>
                  <el-divider direction="vertical" />
                  <span class="plan-item-cal plan-item-cal-orange">
                    <el-icon><Sunny /></el-icon>
                    {{ item.estimatedCalories }} kcal
                  </span>
                </div>
              </div>
              <div class="plan-item-right">
                <el-tag v-if="item.completed" type="success" size="small" effect="plain" round>已完成</el-tag>
                <el-button
                  v-else
                  type="warning"
                  size="small"
                  round
                  :loading="item._completing"
                  @click="handleCompleteDiet(item)"
                >
                  <el-icon><Check /></el-icon>
                  打卡完成
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无饮食计划" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :span="24">
        <el-card class="module-card" shadow="hover">
          <template #header>
            <div class="module-card-header">
              <span class="module-card-title">
                <el-icon class="title-icon title-icon-purple"><DataAnalysis /></el-icon>
                智能推荐
              </span>
              <el-button
                type="primary"
                size="small"
                round
                :loading="recommendLoading"
                @click="handleGenerateRecommendations"
              >
                <el-icon><Refresh /></el-icon>
                重新生成推荐
              </el-button>
            </div>
          </template>
          <div v-if="displayedRecommendations.length" class="recommend-grid">
            <div v-for="item in displayedRecommendations" :key="item.id" class="recommend-card">
              <div class="recommend-type-badge" :class="'recommend-type-' + item.type">
                {{ recommendTypeLabel(item.type) }}
              </div>
              <div class="recommend-card-title">{{ item.title }}</div>
              <div class="recommend-card-reason">{{ item.reason }}</div>
              <div class="recommend-card-footer">
                <div class="recommend-score">
                  <span class="score-label">推荐指数</span>
                  <span class="score-value">{{ item.score }}</span>
                </div>
                <div class="recommend-actions">
                  <el-button size="small" round @click="handleViewRecommend(item)">查看详情</el-button>
                  <el-button type="primary" size="small" round @click="handleAddToPlan(item)">加入计划</el-button>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无推荐，点击上方按钮生成" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :span="isAdmin ? 24 : 12">
        <el-card class="module-card" shadow="hover">
          <template #header>
            <div class="module-card-header">
              <span class="module-card-title">
                <el-icon class="title-icon title-icon-teal"><Calendar /></el-icon>
                最近预约提醒
              </span>
              <el-button link type="primary" @click="router.push('/appointment')">查看全部</el-button>
            </div>
          </template>
          <div v-if="appointments.length" class="appointment-list">
            <div v-for="item in appointments" :key="item.id" class="appointment-item">
              <div class="appointment-icon-box">
                <el-icon :size="24"><Calendar /></el-icon>
              </div>
              <div class="appointment-info">
                <div class="appointment-title">健身指导预约</div>
                <div class="appointment-meta">
                  <span><el-icon><User /></el-icon> 教练：{{ item.coachName }}</span>
                  <span><el-icon><Clock /></el-icon> {{ item.appointmentTime }}</span>
                </div>
              </div>
              <div class="appointment-status">
                <el-tag
                  :type="appointmentStatusType(item.status)"
                  size="small"
                  effect="plain"
                  round
                >{{ item.status }}</el-tag>
                <el-button link type="primary" size="small" @click="router.push('/appointment')">查看预约</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无预约记录" :image-size="80" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card v-if="!isAdmin" class="module-card ai-card" shadow="hover">
          <template #header>
            <div class="module-card-header">
              <span class="module-card-title">
                <el-icon class="title-icon title-icon-blue"><ChatDotRound /></el-icon>
                AI 健身助手
              </span>
            </div>
          </template>
          <div class="ai-assistant-body">
            <div class="ai-greeting">
              <div class="ai-avatar">
                <el-icon :size="28"><Monitor /></el-icon>
              </div>
              <div class="ai-message">
                <p>你好！我是你的 AI 健身助手 💪</p>
                <p class="ai-hint">你可以问我：</p>
                <div class="ai-suggestions">
                  <el-tag
                    v-for="(q, idx) in aiSuggestions"
                    :key="idx"
                    class="ai-suggestion-tag"
                    effect="plain"
                    round
                    @click="handleAiSuggestion(q)"
                  >{{ q }}</el-tag>
                </div>
              </div>
            </div>
            <div class="ai-input-area">
              <el-input
                v-model="aiInput"
                placeholder="输入你的健身问题..."
                class="ai-input"
                @keyup.enter="handleSendAi"
              >
                <template #prefix>
                  <el-icon><ChatDotRound /></el-icon>
                </template>
                <template #append>
                  <el-button type="primary" @click="handleSendAi" :loading="aiLoading">
                    <el-icon><Promotion /></el-icon>
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

    <el-row :gutter="20" class="section-row">
      <el-col :span="24">
        <el-card class="module-card" shadow="hover">
          <template #header>
            <div class="module-card-header">
              <span class="module-card-title">
                <el-icon class="title-icon title-icon-green"><Grid /></el-icon>
                快捷功能入口
              </span>
            </div>
          </template>
          <div class="quick-entry-grid">
            <div
              v-for="entry in quickEntries"
              :key="entry.label"
              class="quick-entry-item"
              @click="router.push(entry.path)"
            >
              <div class="quick-entry-icon" :style="{ background: entry.bgColor }">
                <el-icon :style="{ color: entry.color }" :size="24"><component :is="entry.icon" /></el-icon>
              </div>
              <div class="quick-entry-label">{{ entry.label }}</div>
              <div class="quick-entry-desc">{{ entry.desc }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { ElMessage } from 'element-plus'
import {
  Aim, TrendCharts, Dish, User, ScaleToOriginal, DataLine, Bicycle, Timer,
  Select, Check, Food, Sunny, Refresh, DataAnalysis, Calendar, Clock,
  ChatDotRound, Promotion, ArrowRight, Grid, Finished, Document,
  ForkSpoon, Memo, VideoPlay, Monitor
} from '@element-plus/icons-vue'

import { getCurrentUserDetail } from '../api/user'
import { getPlanList, getPlanItems, completePlanItem } from '../api/plan'
import { getWorkoutRecordList } from '../api/workoutRecord'
import { getDietRecordList } from '../api/dietRecord'
import { getLatestRecommendations, generateRecommendations } from '../api/recommendation'
import { getMyAppointmentList } from '../api/appointment'
import { chatAi } from '../api/ai'
import { isAdminByRole } from '../utils/auth'

const router = useRouter()
const authStore = useAuthStore()
const isAdmin = computed(() => isAdminByRole(authStore.roleCode))

const userProfile = reactive({
  nickname: '',
  fitnessGoal: '减脂',
  activityLevel: '初级',
  height: 175,
  weight: 70,
  bmi: 22.86,
  dietPreference: '高蛋白'
})

const todayPlans = reactive({
  trainingItems: [],
  dietItems: [],
  completionRate: 0
})

const workoutSummary = reactive({
  todayDuration: 0,
  todayCaloriesBurned: 0,
  weeklyWorkoutCount: 0
})

const dietSummary = reactive({
  todayCaloriesIntake: 0
})

const recommendations = ref([])
const recommendLoading = ref(false)

const displayedRecommendations = computed(() => recommendations.value.slice(0, 6))

const appointments = ref([])

const aiInput = ref('')
const aiLoading = ref(false)
const aiSuggestions = [
  '今天适合练什么？',
  '减脂期间晚餐怎么吃？',
  '帮我安排一个训练计划'
]

const progressColors = [
  { color: '#f59e0b', percentage: 30 },
  { color: '#10b981', percentage: 70 },
  { color: '#2563eb', percentage: 100 }
]

const completedTrainingCount = computed(() =>
  todayPlans.trainingItems.filter(i => i.completed).length
)

const completedDietCount = computed(() =>
  todayPlans.dietItems.filter(i => i.completed).length
)

const summaryStats = computed(() => [
  {
    label: '今日训练时长',
    value: workoutSummary.todayDuration,
    unit: ' 分钟',
    icon: Timer,
    color: '#2563eb',
    bgColor: 'rgba(37, 99, 235, 0.1)'
  },
  {
    label: '今日消耗热量',
    value: workoutSummary.todayCaloriesBurned,
    unit: ' kcal',
    icon: Sunny,
    color: '#f59e0b',
    bgColor: 'rgba(245, 158, 11, 0.1)'
  },
  {
    label: '本周训练次数',
    value: workoutSummary.weeklyWorkoutCount,
    unit: ' 次',
    icon: Bicycle,
    color: '#10b981',
    bgColor: 'rgba(16, 185, 129, 0.1)'
  },
  {
    label: '今日摄入热量',
    value: dietSummary.todayCaloriesIntake,
    unit: ' kcal',
    icon: Dish,
    color: '#6366f1',
    bgColor: 'rgba(99, 102, 241, 0.1)'
  }
])

const quickEntries = [
  { label: '生成健身计划', desc: 'AI 智能生成训练方案', icon: Document, path: '/plan', color: '#2563eb', bgColor: 'rgba(37, 99, 235, 0.1)' },
  { label: '记录训练', desc: '记录今日训练内容', icon: Bicycle, path: '/training', color: '#10b981', bgColor: 'rgba(16, 185, 129, 0.1)' },
  { label: '记录饮食', desc: '记录今日饮食摄入', icon: Memo, path: '/diet', color: '#f59e0b', bgColor: 'rgba(245, 158, 11, 0.1)' },
  { label: '查看动作库', desc: '浏览训练动作详情', icon: VideoPlay, path: '/exercise/list', color: '#6366f1', bgColor: 'rgba(99, 102, 241, 0.1)' },
  { label: '查看食材库', desc: '浏览营养食材信息', icon: ForkSpoon, path: '/food/list', color: '#ec4899', bgColor: 'rgba(236, 72, 153, 0.1)' },
  { label: '预约健身指导', desc: '预约专业教练指导', icon: Calendar, path: '/appointment', color: '#06b6d4', bgColor: 'rgba(6, 182, 212, 0.1)' },
  { label: '查看智能推荐', desc: '获取个性化推荐', icon: DataAnalysis, path: '/recommendation', color: '#8b5cf6', bgColor: 'rgba(139, 92, 246, 0.1)' },
  { label: 'AI 问答', desc: '与 AI 健身助手对话', icon: ChatDotRound, path: '/ai-chat', color: '#14b8a6', bgColor: 'rgba(20, 184, 166, 0.1)' }
]

function mealTagType(mealType) {
  const map = { '早餐': 'warning', '午餐': 'success', '晚餐': 'info', '加餐': '' }
  return map[mealType] || ''
}

function recommendTypeLabel(type) {
  const map = { training: '训练推荐', food: '食物推荐', plan: '计划推荐' }
  return map[type] || type
}

function appointmentStatusType(status) {
  const map = { '待确认': 'warning', '已确认': 'success', '已取消': 'info', '已完成': '' }
  return map[status] || 'info'
}

function calcCompletionRate() {
  const allItems = [...todayPlans.trainingItems, ...todayPlans.dietItems]
  if (!allItems.length) {
    todayPlans.completionRate = 0
    return
  }
  const done = allItems.filter(i => i.completed).length
  todayPlans.completionRate = Math.round((done / allItems.length) * 100)
}

async function handleCompleteTraining(item) {
  item._completing = true
  try {
    await completePlanItem(item.id)
    item.completed = true
    calcCompletionRate()
    ElMessage.success(`"${item.exerciseName}" 已打卡完成`)
  } catch (e) {
    ElMessage.error('打卡失败，请重试')
  } finally {
    item._completing = false
  }
}

async function handleCompleteDiet(item) {
  item._completing = true
  try {
    await completePlanItem(item.id)
    item.completed = true
    calcCompletionRate()
    ElMessage.success(`"${item.foodName}" 已打卡完成`)
  } catch (e) {
    ElMessage.error('打卡失败，请重试')
  } finally {
    item._completing = false
  }
}

async function handleGenerateRecommendations() {
  recommendLoading.value = true
  try {
    const res = await generateRecommendations()
    recommendations.value = res || []
    ElMessage.success('推荐已重新生成')
  } catch (e) {
    ElMessage.error('生成推荐失败，请重试')
  } finally {
    recommendLoading.value = false
  }
}

function handleViewRecommend(item) {
  router.push('/recommendation')
}

function handleAddToPlan(item) {
  router.push({ path: '/plan', query: { recommendId: item.id } })
}

function handleSendAi() {
  if (!aiInput.value.trim()) {
    ElMessage.warning('请输入你的问题')
    return
  }
  router.push({ path: '/ai-chat', query: { question: aiInput.value } })
}

function handleAiSuggestion(q) {
  router.push({ path: '/ai-chat', query: { question: q } })
}

async function loadUserProfile() {
  try {
    const res = await getCurrentUserDetail()
    if (res) {
      Object.assign(userProfile, {
        nickname: res.nickname || res.username || '',
        fitnessGoal: res.fitnessGoal || res.fitness_goal || '减脂',
        activityLevel: res.activityLevel || res.activity_level || '初级',
        height: res.height || 175,
        weight: res.weight || 70,
        bmi: res.bmi || 22.86,
        dietPreference: res.dietPreference || res.diet_preference || '高蛋白'
      })
    }
  } catch (e) {
    useMockUserProfile()
  }
}

async function loadTodayPlans() {
  try {
    const planRes = await getPlanList({ status: 'ACTIVE', page: 1, pageSize: 1 })
    const plans = planRes?.records || planRes?.list || planRes || []
    if (plans.length) {
      const planId = plans[0].id
      const itemsRes = await getPlanItems(planId)
      const items = itemsRes || []
      todayPlans.trainingItems = items
        .filter(i => i.type === 'TRAINING' || i.type === 'training')
        .map(i => ({ ...i, _completing: false }))
      todayPlans.dietItems = items
        .filter(i => i.type === 'DIET' || i.type === 'diet')
        .map(i => ({ ...i, _completing: false }))
      calcCompletionRate()
    } else {
      useMockTodayPlans()
    }
  } catch (e) {
    useMockTodayPlans()
  }
}

async function loadWorkoutSummary() {
  try {
    const res = await getWorkoutRecordList({ date: new Date().toISOString().slice(0, 10) })
    const records = res?.records || res?.list || res || []
    workoutSummary.todayDuration = records.reduce((s, r) => s + (r.durationMinutes || r.duration || 0), 0)
    workoutSummary.todayCaloriesBurned = records.reduce((s, r) => s + (r.caloriesBurned || r.calories || 0), 0)
  } catch (e) {
    workoutSummary.todayDuration = 45
    workoutSummary.todayCaloriesBurned = 360
  }
}

async function loadDietSummary() {
  try {
    const res = await getDietRecordList({ date: new Date().toISOString().slice(0, 10) })
    const records = res?.records || res?.list || res || []
    dietSummary.todayCaloriesIntake = records.reduce((s, r) => s + (r.calories || r.caloriesIntake || 0), 0)
  } catch (e) {
    dietSummary.todayCaloriesIntake = 1680
  }
}

async function loadWeeklyWorkoutCount() {
  try {
    const res = await getWorkoutRecordList({ period: 'week' })
    const records = res?.records || res?.list || res || []
    workoutSummary.weeklyWorkoutCount = records.length
  } catch (e) {
    workoutSummary.weeklyWorkoutCount = 4
  }
}

async function loadRecommendations() {
  try {
    const res = await getLatestRecommendations()
    recommendations.value = res?.records || res?.list || res || []
  } catch (e) {
    useMockRecommendations()
  }
}

async function loadAppointments() {
  try {
    const res = await getMyAppointmentList({ status: 'PENDING', page: 1, pageSize: 3 })
    appointments.value = res?.records || res?.list || res || []
  } catch (e) {
    useMockAppointments()
  }
}

function useMockUserProfile() {
  Object.assign(userProfile, {
    nickname: '张三',
    fitnessGoal: '减脂',
    activityLevel: '初级',
    height: 175,
    weight: 70,
    bmi: 22.86,
    dietPreference: '高蛋白'
  })
}

function useMockTodayPlans() {
  todayPlans.trainingItems = [
    { id: 1, exerciseName: '深蹲', sets: 4, reps: 12, estimatedCalories: 180, completed: false, _completing: false },
    { id: 2, exerciseName: '跑步', durationMinutes: 30, estimatedCalories: 300, completed: true, _completing: false },
    { id: 3, exerciseName: '俯卧撑', sets: 3, reps: 15, estimatedCalories: 120, completed: false, _completing: false },
    { id: 4, exerciseName: '平板支撑', durationMinutes: 10, estimatedCalories: 60, completed: false, _completing: false }
  ]
  todayPlans.dietItems = [
    { id: 5, foodName: '燕麦', amount: '100g', mealType: '早餐', estimatedCalories: 380, completed: true, _completing: false },
    { id: 6, foodName: '鸡胸肉', amount: '150g', mealType: '午餐', estimatedCalories: 250, completed: false, _completing: false },
    { id: 7, foodName: '蔬菜沙拉', amount: '200g', mealType: '晚餐', estimatedCalories: 180, completed: false, _completing: false },
    { id: 8, foodName: '蛋白粉', amount: '30g', mealType: '加餐', estimatedCalories: 120, completed: false, _completing: false }
  ]
  calcCompletionRate()
}

function useMockRecommendations() {
  recommendations.value = [
    { id: 'r1', type: 'training', title: 'HIIT 燃脂训练', reason: '适合你的减脂目标和当前运动水平', score: 92 },
    { id: 'r2', type: 'food', title: '鸡胸肉', reason: '高蛋白、低脂肪，适合减脂期', score: 89 },
    { id: 'r3', type: 'plan', title: '7天减脂训练计划', reason: '结合你的 BMI、训练记录和饮食偏好生成', score: 95 }
  ]
}

function useMockAppointments() {
  appointments.value = [
    { id: 'a1', coachName: '李教练', appointmentTime: '2026-05-08 15:00', status: '待确认' }
  ]
}

onMounted(async () => {
  loadUserProfile()
  loadTodayPlans()
  loadWorkoutSummary()
  loadDietSummary()
  loadWeeklyWorkoutCount()
  loadRecommendations()
  loadAppointments()
})
</script>

<style scoped>
.home-container {
  padding: 0;
  min-height: 100%;
}

.section-row {
  margin-top: 20px;
}

:deep(.el-card) {
  border-radius: 16px;
  border: none;
}

.hero-card {
  position: relative;
  background: linear-gradient(135deg, #0d9488 0%, #2563eb 50%, #6366f1 100%);
  border-radius: 20px;
  padding: 40px 48px;
  color: #fff;
  overflow: hidden;
  min-height: 200px;
}

.hero-bg-shape {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.hero-bg-shape-1 {
  width: 300px;
  height: 300px;
  top: -80px;
  right: -40px;
  background: radial-gradient(circle, rgba(255,255,255,0.12) 0%, transparent 70%);
}

.hero-bg-shape-2 {
  width: 200px;
  height: 200px;
  bottom: -60px;
  left: 30%;
  background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
}

.hero-bg-shape-3 {
  width: 120px;
  height: 120px;
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
  font-size: 30px;
  font-weight: 800;
  margin: 0 0 12px;
  letter-spacing: 0.5px;
}

.hero-subtitle {
  font-size: 15px;
  opacity: 0.88;
  margin: 0 0 24px;
  max-width: 520px;
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

.hero-tag .el-icon {
  margin-right: 2px;
}

.hero-right {
  flex-shrink: 0;
}

.hero-metrics {
  display: flex;
  gap: 16px;
}

.hero-metric-item {
  background: rgba(255,255,255,0.12);
  backdrop-filter: blur(8px);
  border-radius: 14px;
  padding: 18px 22px;
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 130px;
  border: 1px solid rgba(255,255,255,0.15);
}

.metric-icon-wrap {
  width: 44px;
  height: 44px;
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
  margin-bottom: 4px;
}

.metric-value {
  font-size: 22px;
  font-weight: 800;
}

.metric-value small {
  font-size: 12px;
  font-weight: 400;
  opacity: 0.75;
  margin-left: 2px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 22px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.08);
}

.stat-card-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card-body {
  display: flex;
  flex-direction: column;
}

.stat-card-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-card-value {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
}

.stat-card-value small {
  font-size: 13px;
  font-weight: 400;
  color: #94a3b8;
}

.completion-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.completion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.completion-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
}

.completion-title .el-icon {
  color: #2563eb;
}

.completion-rate {
  font-size: 28px;
  font-weight: 800;
  color: #2563eb;
}

:deep(.completion-progress .el-progress-bar__outer) {
  border-radius: 10px;
}

:deep(.completion-progress .el-progress-bar__inner) {
  border-radius: 10px;
}

.completion-detail {
  display: flex;
  gap: 24px;
  margin-top: 12px;
  font-size: 13px;
  color: #64748b;
}

.module-card {
  height: 100%;
}

.module-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.module-card-title {
  font-size: 16px;
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

.plan-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #f8fafc;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.plan-item:hover {
  background: #f1f5f9;
  border-color: #e2e8f0;
}

.plan-item-done {
  opacity: 0.65;
}

.plan-item-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
  font-size: 18px;
  flex-shrink: 0;
}

.plan-item-icon.diet-icon {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.plan-item-icon-done {
  background: rgba(16, 185, 129, 0.15) !important;
  color: #10b981 !important;
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
  display: flex;
  align-items: center;
  gap: 6px;
}

.meal-tag {
  flex-shrink: 0;
}

.plan-item-meta {
  font-size: 12px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.plan-item-cal {
  color: #2563eb;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.plan-item-cal-orange {
  color: #f59e0b;
}

.plan-item-right {
  flex-shrink: 0;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.recommend-card {
  background: #f8fafc;
  border-radius: 14px;
  padding: 20px;
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
  margin-bottom: 12px;
}

.recommend-type-training {
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
}

.recommend-type-food {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.recommend-type-plan {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.recommend-card-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
}

.recommend-card-reason {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  margin-bottom: 16px;
  min-height: 40px;
}

.recommend-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.recommend-score {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.score-label {
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 2px;
}

.score-value {
  font-size: 26px;
  font-weight: 800;
  color: #2563eb;
}

.recommend-actions {
  display: flex;
  gap: 6px;
}

.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.appointment-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  transition: all 0.2s;
}

.appointment-item:hover {
  border-color: #e2e8f0;
}

.appointment-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(20, 184, 166, 0.1);
  color: #14b8a6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.appointment-info {
  flex: 1;
}

.appointment-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.appointment-meta {
  font-size: 12px;
  color: #64748b;
  display: flex;
  gap: 16px;
}

.appointment-meta .el-icon {
  margin-right: 3px;
}

.appointment-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.ai-card :deep(.el-card__body) {
  padding: 24px;
}

.ai-assistant-body {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.ai-greeting {
  display: flex;
  gap: 16px;
}

.ai-avatar {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: linear-gradient(135deg, #2563eb, #6366f1);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-message {
  flex: 1;
}

.ai-message p {
  margin: 0;
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
}

.ai-hint {
  margin-top: 8px !important;
  font-size: 13px !important;
  color: #94a3b8 !important;
}

.ai-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.ai-suggestion-tag {
  cursor: pointer;
  transition: all 0.2s;
  font-size: 12px;
}

.ai-suggestion-tag:hover {
  background: #2563eb !important;
  color: #fff !important;
  border-color: #2563eb !important;
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

.quick-entry-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.quick-entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
  border-radius: 14px;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.25s;
  border: 1px solid transparent;
  text-align: center;
}

.quick-entry-item:hover {
  background: #fff;
  border-color: #2563eb;
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.12);
}

.quick-entry-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.quick-entry-label {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.quick-entry-desc {
  font-size: 12px;
  color: #94a3b8;
}

@media (max-width: 1400px) {
  .hero-metrics {
    flex-direction: column;
    gap: 10px;
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
    margin-top: 24px;
    width: 100%;
  }

  .hero-metrics {
    flex-direction: row;
    gap: 12px;
  }

  .hero-metric-item {
    flex: 1;
  }

  .quick-entry-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
