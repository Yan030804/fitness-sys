<template>
  <div class="layout-container">
    <!-- 左侧侧边栏 -->
    <aside class="sidebar">
      <div class="logo-area">
        <el-icon class="logo-icon"><Odometer /></el-icon>
        <span class="logo-text">健身管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
      >
        <el-menu-item v-if="!isAdmin" index="/home">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/admin/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>管理员工作台</span>
        </el-menu-item>
        <el-menu-item index="/exercise/list">
          <el-icon><Reading /></el-icon>
          <span>动作库</span>
        </el-menu-item>
        <el-menu-item v-if="!isAdmin" index="/training">
          <el-icon><Bicycle /></el-icon>
          <span>训练记录</span>
        </el-menu-item>
        <el-menu-item v-if="!isAdmin" index="/diet">
          <el-icon><Memo /></el-icon>
          <span>饮食记录</span>
        </el-menu-item>
        <el-menu-item index="/food/list">
          <el-icon><ForkSpoon /></el-icon>
          <span>食材库</span>
        </el-menu-item>
        <el-menu-item index="/recommendation">
          <el-icon><DataAnalysis /></el-icon>
          <span>个性化推荐</span>
        </el-menu-item>
        <el-menu-item index="/plan">
          <el-icon><Document /></el-icon>
          <span>计划管理</span>
        </el-menu-item>
        <el-menu-item index="/appointment">
          <el-icon><Calendar /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
        <el-menu-item v-if="!isAdmin" index="/ai-chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能问答</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/admin/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户列表</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 右侧主体 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <header class="header">
        <div class="header-left">
          <!-- 这里可以放面包屑或收起菜单按钮 -->
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-info">
              <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              <span class="username">{{ authStore.username || '用户' }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容主体容器 -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { isAdminByRole } from '../utils/auth'
import {
  Odometer,
  House,
  Bicycle,
  ForkSpoon,
  Document,
  ChatDotRound,
  User,
  CaretBottom,
  SwitchButton,
  Reading,
  UserFilled,
  Memo,
  DataAnalysis,
  Calendar,
  Monitor
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)
const isAdmin = computed(() => isAdminByRole(authStore.roleCode))

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    authStore.logout().then(() => {
      router.push('/login')
    })
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: #f1f5f9;
}

.sidebar {
  width: 240px;
  background-color: #fff;
  border-right: 1px solid var(--line);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  box-shadow: 2px 0 8px rgba(0,0,0,0.05);
  z-index: 10;
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-size: 18px;
  font-weight: 800;
  color: var(--primary);
  border-bottom: 1px solid var(--line);
}

.logo-icon {
  font-size: 24px;
  margin-right: 10px;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background-color: #fff;
  border-bottom: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
  z-index: 9;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f1f5f9;
}

.username {
  margin: 0 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-0);
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 页面切换过渡动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s ease;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(20px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
