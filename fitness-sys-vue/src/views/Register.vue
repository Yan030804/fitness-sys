<template>
  <div class="auth-page">
    <div class="auth-shell auth-shell--wide">
      <header class="auth-header">
        <h1 class="auth-heading">健身管理系统</h1>
        <p class="auth-tagline">创建账号，开启你的健身数据之旅</p>
      </header>

      <section class="auth-card">
        <h2 class="auth-card-title">创建账号</h2>
        <p class="auth-card-desc">请填写基础信息完成注册</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          style="margin-top: 14px"
          @submit.prevent
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model.trim="form.username" placeholder="请输入用户名" autocomplete="username" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              placeholder="不少于 6 位"
              autocomplete="new-password"
              show-password
              type="password"
            />
          </el-form-item>

          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model.trim="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model.trim="form.nickname" placeholder="请输入昵称" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone" required>
            <el-input v-model.trim="form.phone" placeholder="例如：13800000000" autocomplete="tel" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model.trim="form.email" placeholder="例如：user01@qq.com" autocomplete="email" />
          </el-form-item>

          <div class="auth-actions">
            <el-button :loading="loading" type="primary" style="flex: 1" @click="onSubmit">
              注册
            </el-button>
            <el-button style="flex: 1" @click="goLogin">返回登录</el-button>
          </div>
        </el-form>

        <div class="auth-foot">
          <span>已经有账号？</span>
          <a class="auth-link" href="#" @click.prevent="goLogin">去登录</a>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  realName: '',
  nickname: '',
  phone: '',
  email: ''
})

const phoneReg = /^1[3-9]\d{9}$/
const emailReg = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/

const rules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度不少于 6 位', trigger: 'blur' }
  ],
  phone: [
    {
      validator: (_, value, cb) => {
        if (!value) return cb()
        if (!phoneReg.test(value)) return cb(new Error('手机号格式不正确'))
        cb()
      },
      trigger: 'blur'
    }
  ],
  email: [
    {
      validator: (_, value, cb) => {
        if (!value) return cb()
        if (!emailReg.test(value)) return cb(new Error('邮箱格式不正确'))
        cb()
      },
      trigger: 'blur'
    }
  ]
}

function goLogin() {
  router.push('/login')
}

async function onSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    await authStore.register({
      username: form.username,
      password: form.password,
      realName: form.realName,
      nickname: form.nickname,
      phone: form.phone,
      email: form.email
    })
    ElMessage.success('注册成功')
    router.replace('/login')
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
