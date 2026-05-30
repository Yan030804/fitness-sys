<template>
  <div class="exercise-page">
    <el-card class="filter-card" shadow="never">
      <div class="page-header">
        <div>
          <div class="page-title">{{ isAdmin ? '动作库管理' : '动作库' }}</div>
          <div class="page-subtitle">
            {{ isAdmin ? '支持动作查询、维护、新增和逻辑删除' : '按分类、部位和难度快速查找适合你的训练动作' }}
          </div>
        </div>
        <el-tag v-if="isAdmin" type="warning" effect="light">管理员视图</el-tag>
      </div>

      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item v-if="isAdmin" label="动作名称">
          <el-input
            v-model="queryParams.exerciseName"
            placeholder="请输入动作名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="动作分类">
          <el-select v-model="queryParams.category" placeholder="全部分类" clearable style="width: 160px">
            <el-option
              v-for="item in categoryOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="训练部位">
          <el-select v-model="queryParams.bodyPart" placeholder="全部部位" clearable style="width: 160px">
            <el-option
              v-for="item in bodyPartOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="难度等级">
          <el-select v-model="queryParams.difficulty" placeholder="全部难度" clearable style="width: 160px">
            <el-option label="初级" value="1" />
            <el-option label="中级" value="2" />
            <el-option label="高级" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isAdmin" label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item class="action-buttons">
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button v-if="isAdmin" type="success" @click="handleAdd">新增动作</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="isAdmin" class="table-card" shadow="never">
      <template #header>
        <div class="section-header">
          <span>动作列表</span>
          <el-tag type="info" effect="plain">共 {{ total }} 条</el-tag>
        </div>
      </template>

      <el-table v-loading="loading" :data="exerciseList" border stripe class="admin-table">
        <el-table-column label="编号" width="80" align="center">
          <template #default="{ $index }">
            {{ getRowNumber($index) }}
          </template>
        </el-table-column>
        <el-table-column prop="exerciseName" label="动作名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" min-width="110" />
        <el-table-column prop="bodyPart" label="部位" min-width="110" />
        <el-table-column label="难度" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getDifficultyType(row.difficulty)" effect="light">
              {{ formatDifficulty(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="热量" min-width="110" align="center">
          <template #default="{ row }">
            {{ row.caloriesPerHour ?? 0 }} kcal/h
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt || row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="240" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="goToDetail(row.id)">查看详情</el-button>
              <el-button size="small" type="primary" plain @click="handleEdit(row)">编辑</el-button>
              <el-popconfirm
                title="确认将该动作逻辑删除吗？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button size="small" type="danger" plain>删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && exerciseList.length === 0" description="暂无动作数据" />
    </el-card>

    <div v-else v-loading="loading" class="user-list-content">
      <el-row v-if="exerciseList.length > 0" :gutter="20">
        <el-col
          v-for="item in exerciseList"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="card-col"
        >
          <el-card class="exercise-card" shadow="hover">
            <div class="card-top">
              <div class="exercise-name-row">
                <span class="exercise-name">{{ item.exerciseName }}</span>
                <el-tag :type="getDifficultyType(item.difficulty)" effect="light" size="small">
                  {{ formatDifficulty(item.difficulty) }}
                </el-tag>
              </div>
              <p class="exercise-desc">{{ item.description || '暂无动作说明' }}</p>
            </div>

            <div class="meta-list">
              <div class="meta-item">
                <span class="meta-label">分类</span>
                <span class="meta-value">{{ item.category || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">部位</span>
                <span class="meta-value">{{ item.bodyPart || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">器械</span>
                <span class="meta-value">{{ item.equipment || '无需器械' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">热量</span>
                <span class="meta-value highlight">{{ item.caloriesPerHour ?? 0 }} kcal/h</span>
              </div>
            </div>

            <div class="suggest-grid">
              <div class="suggest-box">
                <div class="suggest-title">默认组数</div>
                <div class="suggest-value">{{ item.defaultSets ?? 0 }} 组</div>
              </div>
              <div class="suggest-box">
                <div class="suggest-title">默认次数</div>
                <div class="suggest-value">{{ formatReps(item.defaultReps) }}</div>
              </div>
              <div class="suggest-box">
                <div class="suggest-title">建议时长</div>
                <div class="suggest-value">{{ item.durationMin ?? 0 }} 分钟</div>
              </div>
            </div>

            <div class="card-footer">
              <el-button type="primary" plain class="detail-btn" @click="goToDetail(item.id)">
                查看详情
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-else description="暂无相关动作，换个筛选条件试试吧" />
    </div>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[8, 16, 24, 32, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
      v-if="isAdmin"
      v-model="dialogVisible"
      :title="dialogTitle"
      width="760px"
      append-to-body
      destroy-on-close
      @closed="resetForm"
    >
      <el-form
        ref="exerciseFormRef"
        :model="exerciseForm"
        :rules="rules"
        label-width="100px"
        class="exercise-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="动作名称" prop="exerciseName">
              <el-input v-model="exerciseForm.exerciseName" placeholder="请输入动作名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="动作分类" prop="category">
              <el-select v-model="exerciseForm.category" placeholder="请选择分类" class="w-full">
                <el-option
                  v-for="item in categoryOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="训练部位">
              <el-select v-model="exerciseForm.bodyPart" placeholder="请选择训练部位" clearable class="w-full">
                <el-option
                  v-for="item in bodyPartOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度等级">
              <el-select v-model="exerciseForm.difficulty" placeholder="请选择难度等级" clearable class="w-full">
                <el-option label="初级" value="1" />
                <el-option label="中级" value="2" />
                <el-option label="高级" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="器械要求">
              <el-input v-model="exerciseForm.equipment" placeholder="如：哑铃、杠铃" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每小时热量">
              <el-input-number v-model="exerciseForm.caloriesPerHour" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="默认组数">
              <el-input-number v-model="exerciseForm.defaultSets" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="默认次数">
              <el-input-number v-model="exerciseForm.defaultReps" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="建议时长">
              <el-input-number v-model="exerciseForm.durationMin" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="动作说明">
          <el-input
            v-model="exerciseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入动作说明"
          />
        </el-form-item>

        <el-form-item label="注意事项">
          <el-input
            v-model="exerciseForm.caution"
            type="textarea"
            :rows="4"
            placeholder="请输入注意事项"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="exerciseForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../store/auth'
import { isAdminByRole } from '../../utils/auth'
import {
  createExercise,
  deleteExercise,
  getAdminExerciseList,
  getExerciseList,
  updateExercise
} from '../../api/exercise'

const categoryOptions = ['有氧', '力量', '拉伸', 'HIIT']
const bodyPartOptions = ['全身', '胸部', '背部', '肩部', '手臂', '核心', '腿部', '臀部']

const createDefaultQueryParams = () => ({
  pageNum: 1,
  pageSize: 8,
  exerciseName: '',
  category: '',
  bodyPart: '',
  difficulty: '',
  status: undefined
})

const createDefaultExerciseForm = () => ({
  id: undefined,
  exerciseName: '',
  category: '',
  bodyPart: '',
  difficulty: '',
  equipment: '',
  caloriesPerHour: 0,
  defaultSets: 0,
  defaultReps: 0,
  durationMin: 0,
  description: '',
  caution: '',
  status: 1
})

const router = useRouter()
const authStore = useAuthStore()
const isAdmin = computed(() => isAdminByRole(authStore.roleCode))

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增动作')
const exerciseFormRef = ref(null)
const exerciseList = ref([])
const total = ref(0)

const queryParams = reactive(createDefaultQueryParams())
const exerciseForm = reactive(createDefaultExerciseForm())

const rules = {
  exerciseName: [{ required: true, message: '请输入动作名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择动作分类', trigger: 'change' }]
}

const normalizePageData = (payload) => {
  return {
    list: payload?.list || payload?.records || payload?.rows || [],
    total: Number(payload?.total || 0)
  }
}

const buildQueryParams = () => {
  const params = {
    pageNum: queryParams.pageNum,
    pageSize: queryParams.pageSize
  }

  if (queryParams.category) params.category = queryParams.category
  if (queryParams.bodyPart) params.bodyPart = queryParams.bodyPart
  if (queryParams.difficulty) params.difficulty = queryParams.difficulty

  if (isAdmin.value) {
    if (queryParams.exerciseName.trim()) params.exerciseName = queryParams.exerciseName.trim()
    if (queryParams.status !== undefined && queryParams.status !== null && queryParams.status !== '') {
      params.status = queryParams.status
    }
  }

  return params
}

const getList = async () => {
  loading.value = true
  try {
    const requestApi = isAdmin.value ? getAdminExerciseList : getExerciseList
    const res = await requestApi(buildQueryParams())
    const { list, total: totalCount } = normalizePageData(res)
    exerciseList.value = list
    total.value = totalCount
  } catch (error) {
    console.error('获取动作列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  Object.assign(queryParams, createDefaultQueryParams())
  getList()
}

const handleSizeChange = (value) => {
  queryParams.pageSize = value
  queryParams.pageNum = 1
  getList()
}

const handleCurrentChange = (value) => {
  queryParams.pageNum = value
  getList()
}

const goToDetail = (id) => {
  router.push(`/exercise/detail/${id}`)
}

const formatDifficulty = (difficulty) => {
  if (difficulty === null || difficulty === undefined || difficulty === '') return '-'
  const map = {
    1: '初级',
    2: '中级',
    3: '高级',
    '1': '初级',
    '2': '中级',
    '3': '高级'
  }
  return map[difficulty] || String(difficulty)
}

const difficultyToCode = (difficulty) => {
  if (difficulty === null || difficulty === undefined || difficulty === '') return ''
  const map = {
    初级: '1',
    中级: '2',
    高级: '3',
    1: '1',
    2: '2',
    3: '3',
    '1': '1',
    '2': '2',
    '3': '3'
  }
  return map[difficulty] || String(difficulty)
}

const formatReps = (reps) => {
  if (reps === null || reps === undefined || reps === '') return '-'
  return `${reps} 次`
}

const getDifficultyType = (difficulty) => {
  const label = formatDifficulty(difficulty)
  switch (label) {
    case '初级':
      return 'success'
    case '中级':
      return 'warning'
    case '高级':
      return 'danger'
    default:
      return 'info'
  }
}

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const getRowNumber = (index) => {
  return (queryParams.pageNum - 1) * queryParams.pageSize + index + 1
}

const resetForm = () => {
  Object.assign(exerciseForm, createDefaultExerciseForm())
  if (exerciseFormRef.value) {
    exerciseFormRef.value.clearValidate()
  }
}

const handleAdd = () => {
  if (!isAdmin.value) return
  resetForm()
  dialogTitle.value = '新增动作'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  if (!isAdmin.value) return
  resetForm()
  Object.assign(exerciseForm, {
    id: row.id,
    exerciseName: row.exerciseName || '',
    category: row.category || '',
    bodyPart: row.bodyPart || '',
    difficulty: difficultyToCode(row.difficulty),
    equipment: row.equipment || '',
    caloriesPerHour: row.caloriesPerHour ?? 0,
    defaultSets: row.defaultSets ?? 0,
    defaultReps: row.defaultReps ?? 0,
    durationMin: row.durationMin ?? 0,
    description: row.description || '',
    caution: row.caution || '',
    status: row.status ?? 1
  })
  dialogTitle.value = '编辑动作'
  dialogVisible.value = true
}

const buildSubmitPayload = () => {
  return {
    exerciseName: exerciseForm.exerciseName.trim(),
    category: exerciseForm.category,
    bodyPart: exerciseForm.bodyPart || '',
    difficulty: exerciseForm.difficulty || '',
    equipment: exerciseForm.equipment.trim(),
    caloriesPerHour: exerciseForm.caloriesPerHour ?? 0,
    defaultSets: exerciseForm.defaultSets ?? 0,
    defaultReps: exerciseForm.defaultReps ?? 0,
    durationMin: exerciseForm.durationMin ?? 0,
    description: exerciseForm.description.trim(),
    caution: exerciseForm.caution.trim(),
    status: exerciseForm.status ?? 1
  }
}

const submitForm = async () => {
  if (!isAdmin.value || !exerciseFormRef.value) return

  try {
    await exerciseFormRef.value.validate()
    submitLoading.value = true

    const payload = buildSubmitPayload()
    if (exerciseForm.id) {
      await updateExercise(exerciseForm.id, payload)
    } else {
      await createExercise(payload)
    }

    ElMessage.success(`${dialogTitle.value}成功`)
    dialogVisible.value = false
    await getList()
  } catch (error) {
    if (error?.name !== 'Error') {
      console.error(`${dialogTitle.value}失败:`, error)
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  if (!isAdmin.value) return

  try {
    await deleteExercise(row.id)
    ElMessage.success('删除成功')

    if (exerciseList.value.length === 1 && queryParams.pageNum > 1) {
      queryParams.pageNum -= 1
    }

    await getList()
  } catch (error) {
    console.error('删除动作失败:', error)
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.exercise-page {
  min-height: calc(100vh - 120px);
}

.filter-card,
.table-card {
  border-radius: 14px;
}

.filter-card {
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

.action-buttons {
  margin-left: auto;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.admin-table {
  width: 100%;
}

.user-list-content {
  min-height: 320px;
}

.card-col {
  margin-bottom: 20px;
}

.exercise-card {
  height: 100%;
  border-radius: 16px;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.exercise-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 24px rgba(15, 23, 42, 0.08);
}

.card-top {
  margin-bottom: 16px;
}

.exercise-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.exercise-name {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.exercise-desc {
  margin: 0;
  min-height: 42px;
  line-height: 1.6;
  color: #606266;
  font-size: 14px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.meta-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.meta-item {
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
}

.meta-label {
  display: block;
  margin-bottom: 6px;
  color: #909399;
  font-size: 12px;
}

.meta-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.highlight {
  color: #f56c6c;
}

.suggest-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.suggest-box {
  padding: 12px 10px;
  text-align: center;
  border-radius: 12px;
  background: linear-gradient(180deg, #eff6ff 0%, #f8fbff 100%);
}

.suggest-title {
  color: #909399;
  font-size: 12px;
  margin-bottom: 6px;
}

.suggest-value {
  color: #2563eb;
  font-size: 14px;
  font-weight: 700;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.detail-btn {
  width: 100%;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.exercise-form {
  padding-right: 12px;
}

.w-full {
  width: 100%;
}

@media (max-width: 992px) {
  .action-buttons {
    margin-left: 0;
  }

  .meta-list {
    grid-template-columns: 1fr;
  }

  .suggest-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header,
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
