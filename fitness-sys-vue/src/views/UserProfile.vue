<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2 class="page-title">个人信息</h2>
      <div class="actions">
        <el-button v-if="!isEditMode" type="primary" @click="enterEditMode">
          <el-icon><Edit /></el-icon>编辑资料
        </el-button>
        <template v-else>
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveProfile">
            <el-icon><Check /></el-icon>保存修改
          </el-button>
        </template>
        <el-button type="warning" plain @click="openPasswordDialog">
          <el-icon><Lock /></el-icon>修改密码
        </el-button>
      </div>
    </div>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :disabled="!isEditMode"
      label-width="100px"
      label-position="right"
      class="profile-form"
    >
      <!-- 基本信息 -->
      <div class="card-section">
        <div class="section-title">基本信息</div>
        <el-row :gutter="30">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="formData.username" disabled placeholder="自动获取" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="formData.nickname" placeholder="请输入昵称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="formData.gender" placeholder="请选择性别" class="w-full">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
                <el-option label="保密" value="保密" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="formData.age" :min="1" :max="120" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <!-- 身体与健身信息 -->
      <div class="card-section">
        <div class="section-title">身体与健身信息</div>
        <el-row :gutter="30">
          <el-col :span="12">
            <el-form-item label="身高(cm)" prop="heightCm">
              <el-input-number v-model="formData.heightCm" :min="1" :precision="1" class="w-full" @change="calculateBMI" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)" prop="weightKg">
              <el-input-number v-model="formData.weightKg" :min="1" :precision="1" class="w-full" @change="calculateBMI" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="BMI指数">
              <el-input v-model="formData.bmi" disabled>
                <template #append>
                  <span :class="bmiStatusClass">{{ bmiStatusText }}</span>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="健身目标" prop="fitnessGoal">
              <el-select v-model="formData.fitnessGoal" placeholder="请选择健身目标" class="w-full">
                <el-option label="减脂" value="减脂" />
                <el-option label="增肌" value="增肌" />
                <el-option label="塑形" value="塑形" />
                <el-option label="保持健康" value="保持健康" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="运动水平" prop="activityLevel">
              <el-select v-model="formData.activityLevel" placeholder="请选择运动水平" class="w-full">
                <el-option label="初级（缺乏运动）" value="初级" />
                <el-option label="中级（每周1-3次）" value="中级" />
                <el-option label="高级（每周3次以上）" value="高级" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="饮食偏好" prop="dietPreference">
              <el-select v-model="formData.dietPreference" placeholder="请选择饮食偏好" class="w-full">
                <el-option label="无特殊偏好" value="无特殊偏好" />
                <el-option label="高蛋白" value="高蛋白" />
                <el-option label="低碳水" value="低碳水" />
                <el-option label="素食" value="素食" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>

    <ChangePasswordDialog ref="passwordDialogRef" @success="handlePasswordSuccess" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Check, Lock } from '@element-plus/icons-vue'
import { getCurrentUserDetail, updateCurrentUser } from '../api/user'
import { useAuthStore } from '../store/auth'
import { useRouter } from 'vue-router'
import ChangePasswordDialog from '../components/ChangePasswordDialog.vue'

const authStore = useAuthStore()
const router = useRouter()

const isEditMode = ref(false)
const saving = ref(false)
const formRef = ref(null)
const passwordDialogRef = ref(null)

let originalData = {}

const formData = reactive({
  username: '',
  realName: '',
  nickname: '',
  gender: '',
  age: null,
  phone: '',
  email: '',
  heightCm: null,
  weightKg: null,
  bmi: null,
  fitnessGoal: '',
  activityLevel: '',
  dietPreference: ''
})

const phoneReg = /^1[3-9]\d{9}$/
const emailReg = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/

const rules = {
  realName: [{ required: true, message: '真实姓名不能为空', trigger: 'blur' }],
  nickname: [{ required: true, message: '昵称不能为空', trigger: 'blur' }],
  age: [
    { required: true, message: '年龄不能为空', trigger: 'blur' },
    { type: 'number', min: 1, message: '年龄必须为正整数', trigger: 'blur' }
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
  ],
  heightCm: [{ required: true, message: '身高不能为空', trigger: 'blur' }],
  weightKg: [{ required: true, message: '体重不能为空', trigger: 'blur' }]
}

// 动态计算 BMI
const calculateBMI = () => {
  if (formData.heightCm && formData.weightKg) {
    const heightM = formData.heightCm / 100
    formData.bmi = Number((formData.weightKg / (heightM * heightM)).toFixed(2))
  } else {
    formData.bmi = null
  }
}

const bmiStatusText = computed(() => {
  const bmi = formData.bmi
  if (!bmi) return '-'
  if (bmi < 18.5) return '偏瘦'
  if (bmi >= 18.5 && bmi < 24) return '正常'
  if (bmi >= 24 && bmi < 28) return '偏胖'
  return '肥胖'
})

const bmiStatusClass = computed(() => {
  const bmi = formData.bmi
  if (!bmi) return ''
  if (bmi < 18.5) return 'text-warning'
  if (bmi >= 18.5 && bmi < 24) return 'text-success'
  if (bmi >= 24 && bmi < 28) return 'text-warning'
  return 'text-danger'
})

const fetchUserInfo = async () => {
  try {
    const res = await getCurrentUserDetail()
    Object.assign(formData, res)
    originalData = JSON.parse(JSON.stringify(formData))
    
    // 如果后端没返回 username，使用登录存下来的保底
    if (!formData.username) {
      formData.username = authStore.username
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

onMounted(() => {
  fetchUserInfo()
})

const enterEditMode = () => {
  isEditMode.value = true
}

const cancelEdit = () => {
  Object.assign(formData, originalData)
  isEditMode.value = false
  if (formRef.value) formRef.value.clearValidate()
}

const saveProfile = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  
  saving.value = true
  try {
    await updateCurrentUser({
      realName: formData.realName,
      nickname: formData.nickname,
      gender: formData.gender,
      age: formData.age,
      phone: formData.phone,
      email: formData.email,
      heightCm: formData.heightCm,
      weightKg: formData.weightKg,
      bmi: formData.bmi,
      fitnessGoal: formData.fitnessGoal,
      activityLevel: formData.activityLevel,
      dietPreference: formData.dietPreference
    })
    ElMessage.success('个人资料修改成功')
    originalData = JSON.parse(JSON.stringify(formData))
    isEditMode.value = false
    authStore.fetchCurrentUser() // 同步刷新顶部用户名
  } catch (error) {
  } finally {
    saving.value = false
  }
}

const openPasswordDialog = () => {
  passwordDialogRef.value?.open()
}

const handlePasswordSuccess = () => {
  authStore.logout().then(() => {
    router.push('/login')
  })
}
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  animation: slideUp 0.4s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: var(--text-0);
}

.card-section {
  background: #fff;
  border-radius: var(--radius);
  padding: 24px 30px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
  border: 1px solid var(--line);
  transition: box-shadow 0.3s;
}

.card-section:hover {
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--line);
  color: var(--text-0);
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 40px;
  height: 2px;
  background-color: var(--primary);
}

.w-full {
  width: 100%;
}

.text-success {
  color: #67c23a;
  font-weight: bold;
}
.text-warning {
  color: #e6a23c;
  font-weight: bold;
}
.text-danger {
  color: #f56c6c;
  font-weight: bold;
}
</style>
