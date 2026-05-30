import { createRouter, createWebHistory } from 'vue-router'
import { getToken, clearAuth, getAuthSnapshot, isAdminByRole } from '../utils/auth'
import { useAuthStore } from '../store/auth'
import Layout from '../components/Layout.vue'

const routes = [
  { path: '/', redirect: '/login' },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/dashboard/index.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/home',
        redirect: '/admin/dashboard'
      },
      {
        path: 'admin/workout-records',
        name: 'AdminWorkoutRecords',
        component: () => import('../views/training/WorkoutRecordList.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/diet-records',
        name: 'AdminDietRecords',
        component: () => import('../views/diet/DietRecordList.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/UserDashboard.vue')
      },
      {
        path: 'dashboard',
        name: 'UserDashboard',
        redirect: '/home'
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/UserProfile.vue')
      },
      {
        path: 'exercise/list',
        name: 'ExerciseList',
        component: () => import('../views/exercise/ExerciseList.vue')
      },
      {
        path: 'exercise/detail/:id',
        name: 'ExerciseDetail',
        component: () => import('../views/exercise/ExerciseDetail.vue')
      },
      {
        path: 'training',
        name: 'WorkoutRecordList',
        component: () => import('../views/training/WorkoutRecordList.vue'),
        meta: { requiresUser: true }
      },
      {
        path: 'diet',
        name: 'DietRecordList',
        component: () => import('../views/diet/DietRecordList.vue'),
        meta: { requiresUser: true }
      },
      {
        path: 'recommendation',
        name: 'RecommendationCenter',
        component: () => import('../views/recommendation/RecommendationCenter.vue')
      },
      {
        path: 'qa',
        redirect: '/ai-chat'
      },
      {
        path: 'ai-chat',
        name: 'AiChat',
        component: () => import('../views/ai/AiChat.vue'),
        meta: { requiresUser: true }
      },
      {
        path: 'appointment',
        name: 'AppointmentManage',
        component: () => import('../views/appointment/AppointmentManage.vue')
      },
      {
        path: 'plan',
        name: 'PlanManage',
        component: () => import('../views/plan/PlanManage.vue')
      },
      {
        path: 'food/list',
        name: 'FoodList',
        component: () => import('../views/food/FoodList.vue')
      },
      {
        path: 'food/detail/:id',
        name: 'FoodDetail',
        component: () => import('../views/food/FoodDetail.vue')
      },
      {
        path: 'admin/users',
        name: 'AdminUserList',
        component: () => import('../views/admin/AdminUserList.vue'),
        meta: { requiresAdmin: true }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const token = getToken()
  const isPublic = Boolean(to.meta?.public)

  if (isPublic) {
    if (token && (to.path === '/login' || to.path === '/register')) {
      const { roleCode } = getAuthSnapshot()
      return isAdminByRole(roleCode) ? '/admin/dashboard' : '/home'
    }
    return true
  }

  if (!token) return { path: '/login', query: { redirect: to.fullPath } }

  const authStore = useAuthStore()
  if (!authStore.currentUser) {
    try {
      await authStore.fetchCurrentUser()
    } catch (error) {
      clearAuth()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  if (to.meta?.requiresAdmin) {
    if (!isAdminByRole(authStore.roleCode)) return '/home'
  }

  if (to.path === '/home' && isAdminByRole(authStore.roleCode)) {
    return '/admin/dashboard'
  }

  if (to.meta?.requiresUser) {
    if (isAdminByRole(authStore.roleCode)) return '/admin/dashboard'
  }

  return true
})

export default router
