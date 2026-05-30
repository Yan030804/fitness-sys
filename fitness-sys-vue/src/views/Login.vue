<template>
  <div class="auth-page auth-page--login">
    <div class="auth-shell auth-shell--login">
      <section class="auth-brand-panel" aria-label="系统亮点">
        <div class="auth-badge">FITNESS OPS</div>
        <h1 class="auth-brand-title">健身管理系统</h1>
        <p class="auth-brand-desc">
          汇聚会员训练、饮食记录、课程预约与个性化推荐，让健身房运营和用户成长都更清晰。
        </p>

        <div class="auth-metrics">
          <div class="auth-metric">
            <strong>24h</strong>
            <span>训练数据同步</span>
          </div>
          <div class="auth-metric">
            <strong>AI</strong>
            <span>计划智能推荐</span>
          </div>
          <div class="auth-metric">
            <strong>360</strong>
            <span>会员画像管理</span>
          </div>
        </div>

        <div class="auth-feature-list">
          <span>课程预约</span>
          <span>饮食追踪</span>
          <span>运动记录</span>
        </div>
      </section>

      <section class="auth-card auth-card--login">
        <div class="auth-card-kicker">欢迎回来</div>
        <h2 class="auth-card-title">登录账户</h2>
        <p class="auth-card-desc">请输入用户名和密码进入系统</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="auth-form"
          label-position="top"
          size="large"
          @submit.prevent
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model.trim="form.username"
              :prefix-icon="User"
              autocomplete="username"
              placeholder="请输入用户名"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              :prefix-icon="Lock"
              autocomplete="current-password"
              placeholder="请输入密码"
              show-password
              type="password"
              @keyup.enter="onSubmit"
            />
          </el-form-item>

          <div class="auth-actions">
            <el-button :loading="loading" class="auth-submit" type="primary" @click="onSubmit">
              登录
            </el-button>
            <el-button class="auth-secondary" @click="goRegister">注册</el-button>
          </div>
        </el-form>

        <div class="auth-foot">
          <span>还没有账号？</span>
          <a class="auth-link" href="#" @click.prevent="goRegister">立即注册</a>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { isAdminByRole } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function goRegister() {
  router.push('/register')
}

async function onSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    await authStore.login({
      username: form.username,
      password: form.password
    })
    ElMessage.success('登录成功')
    const redirect = route.query.redirect ? String(route.query.redirect) : '/home'
    const target =
      isAdminByRole(authStore.roleCode) && (!route.query.redirect || redirect === '/home')
        ? '/admin/dashboard'
        : redirect
    router.replace(target)
  } catch (e) {
    return
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
@import '../styles/auth.css';
</style>
