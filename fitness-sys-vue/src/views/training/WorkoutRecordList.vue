<template>
  <div class="workout-page">
    <el-card class="filter-card" shadow="never">
      <div class="page-header">
        <div>
          <div class="page-title">训练记录</div>
          <div class="page-subtitle">记录每次训练的动作、时长、组数、消耗热量和完成反馈</div>
        </div>
        <el-button type="primary" @click="handleAdd">新增训练记录</el-button>
      </div>

      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="训练日期">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>

        <el-form-item label="训练动作">
          <el-select
            v-model="queryParams.exerciseId"
            placeholder="全部动作"
            clearable
            filterable
            style="width: 220px"
          >
            <el-option
              v-for="item in exerciseOptions"
              :key="item.id"
              :label="item.exerciseName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="完成状态">
          <el-select v-model="queryParams.completionStatus" placeholder="全部状态" clearable style="width: 160px">
            <el-option
              v-for="item in completionStatusOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

        <el-form-item class="action-buttons">
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="section-header">
          <span>训练记录列表</span>
          <el-tag type="info" effect="plain">共 {{ total }} 条</el-tag>
        </div>
      </template>

      <el-table v-loading="loading" :data="recordList" border stripe class="record-table">
        <el-table-column label="编号" width="80" align="center">
          <template #default="{ $index }">
            {{ getRowNumber($index) }}
          </template>
        </el-table-column>
        <el-table-column prop="exerciseName" label="动作名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="训练日期" min-width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.workoutDate) }}
          </template>
        </el-table-column>
        <el-table-column label="训练时长" min-width="100" align="center">
          <template #default="{ row }">
            {{ formatMinutes(row.durationMin) }}
          </template>
        </el-table-column>
        <el-table-column label="完成组数" min-width="100" align="center">
          <template #default="{ row }">
            {{ formatCount(row.setsCount, '组') }}
          </template>
        </el-table-column>
        <el-table-column label="完成次数" min-width="100" align="center">
          <template #default="{ row }">
            {{ formatCount(row.repsCount, '次') }}
          </template>
        </el-table-column>
        <el-table-column label="消耗热量" min-width="110" align="center">
          <template #default="{ row }">
            {{ formatCount(row.caloriesBurned, 'kcal') }}
          </template>
        </el-table-column>
        <el-table-column label="完成状态" min-width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getCompletionTagType(row.completionStatus)" effect="light">
              {{ row.completionStatus || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评分" min-width="120" align="center">
          <template #default="{ row }">
            <el-rate
              :model-value="Number(row.feedbackScore || 0)"
              disabled
              show-score
              text-color="#f59e0b"
              score-template="{value} 分"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="240" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="handleViewDetail(row.id)">查看详情</el-button>
              <el-button size="small" type="primary" plain @click="handleEdit(row)">编辑</el-button>
              <el-popconfirm
                title="确认删除该训练记录吗？"
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

      <el-empty v-if="!loading && recordList.length === 0" description="暂无训练记录" />
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="780px"
      append-to-body
      destroy-on-close
      @closed="resetForm"
    >
      <el-form
        ref="recordFormRef"
        :model="recordForm"
        :rules="rules"
        label-width="100px"
        class="record-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="训练动作" prop="exerciseId">
              <el-select
                v-model="recordForm.exerciseId"
                placeholder="请选择动作"
                filterable
                class="w-full"
              >
                <el-option
                  v-for="item in exerciseOptions"
                  :key="item.id"
                  :label="item.exerciseName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="训练日期" prop="workoutDate">
              <el-date-picker
                v-model="recordForm.workoutDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择训练日期"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="训练时长">
              <el-input-number v-model="recordForm.durationMin" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="消耗热量">
              <el-input-number v-model="recordForm.caloriesBurned" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="完成组数">
              <el-input-number v-model="recordForm.setsCount" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="完成次数">
              <el-input-number v-model="recordForm.repsCount" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="完成状态">
              <el-select v-model="recordForm.completionStatus" placeholder="请选择状态" clearable class="w-full">
                <el-option
                  v-for="item in completionStatusOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="训练评分">
              <el-rate v-model="recordForm.feedbackScore" :max="5" show-score />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="recordForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入训练备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="detailVisible"
      title="训练记录详情"
      size="520px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-panel">
        <template v-if="detailData">
          <div class="detail-section">
            <div class="section-title">基础信息</div>
            <div class="detail-grid">
              <div class="detail-item"><span class="label">记录ID</span><span class="value">{{ detailData.id || '-' }}</span></div>
              <div class="detail-item"><span class="label">动作名称</span><span class="value">{{ detailData.exerciseName || '-' }}</span></div>
              <div class="detail-item"><span class="label">训练日期</span><span class="value">{{ formatDate(detailData.workoutDate) }}</span></div>
              <div class="detail-item"><span class="label">完成状态</span><span class="value">{{ detailData.completionStatus || '-' }}</span></div>
              <div class="detail-item"><span class="label">训练时长</span><span class="value">{{ formatMinutes(detailData.durationMin) }}</span></div>
              <div class="detail-item"><span class="label">消耗热量</span><span class="value">{{ formatCount(detailData.caloriesBurned, 'kcal') }}</span></div>
              <div class="detail-item"><span class="label">完成组数</span><span class="value">{{ formatCount(detailData.setsCount, '组') }}</span></div>
              <div class="detail-item"><span class="label">完成次数</span><span class="value">{{ formatCount(detailData.repsCount, '次') }}</span></div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">训练反馈</div>
            <div class="detail-grid single-column">
              <div class="detail-item">
                <span class="label">训练评分</span>
                <span class="value">
                  <el-rate :model-value="Number(detailData.feedbackScore || 0)" disabled show-score />
                </span>
              </div>
              <div class="detail-item">
                <span class="label">备注</span>
                <span class="value">{{ detailData.remark || '暂无备注' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">创建时间</span>
                <span class="value">{{ formatDateTime(detailData.createdAt) }}</span>
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getExerciseList } from '../../api/exercise'
import {
  createWorkoutRecord,
  deleteWorkoutRecord,
  getWorkoutRecordDetail,
  getWorkoutRecordList,
  updateWorkoutRecord
} from '../../api/workoutRecord'

const completionStatusOptions = ['已完成', '部分完成', '未完成']

const createDefaultQueryParams = () => ({
  pageNum: 1,
  pageSize: 10,
  dateRange: [],
  exerciseId: undefined,
  completionStatus: ''
})

const createDefaultRecordForm = () => ({
  id: undefined,
  exerciseId: undefined,
  workoutDate: '',
  durationMin: undefined,
  setsCount: undefined,
  repsCount: undefined,
  caloriesBurned: undefined,
  completionStatus: '',
  feedbackScore: 0,
  remark: ''
})

const loading = ref(false)
const submitLoading = ref(false)
const detailLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增训练记录')
const recordFormRef = ref(null)
const recordList = ref([])
const total = ref(0)
const detailData = ref(null)
const exerciseOptions = ref([])

const queryParams = reactive(createDefaultQueryParams())
const recordForm = reactive(createDefaultRecordForm())

const rules = {
  exerciseId: [{ required: true, message: '请选择训练动作', trigger: 'change' }],
  workoutDate: [{ required: true, message: '请选择训练日期', trigger: 'change' }]
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

  if (queryParams.exerciseId) params.exerciseId = queryParams.exerciseId
  if (queryParams.completionStatus) params.completionStatus = queryParams.completionStatus

  if (Array.isArray(queryParams.dateRange) && queryParams.dateRange.length === 2) {
    params.workoutDateStart = queryParams.dateRange[0]
    params.workoutDateEnd = queryParams.dateRange[1]
  }

  return params
}

const buildSubmitPayload = () => {
  const payload = {
    exerciseId: recordForm.exerciseId,
    workoutDate: recordForm.workoutDate
  }

  if (recordForm.durationMin !== undefined && recordForm.durationMin !== null) payload.durationMin = recordForm.durationMin
  if (recordForm.setsCount !== undefined && recordForm.setsCount !== null) payload.setsCount = recordForm.setsCount
  if (recordForm.repsCount !== undefined && recordForm.repsCount !== null) payload.repsCount = recordForm.repsCount
  if (recordForm.caloriesBurned !== undefined && recordForm.caloriesBurned !== null) payload.caloriesBurned = recordForm.caloriesBurned
  if (recordForm.completionStatus) payload.completionStatus = recordForm.completionStatus
  if (recordForm.feedbackScore) payload.feedbackScore = recordForm.feedbackScore
  if (recordForm.remark.trim()) payload.remark = recordForm.remark.trim()

  return payload
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getWorkoutRecordList(buildQueryParams())
    const { list, total: totalCount } = normalizePageData(res)
    recordList.value = list
    total.value = totalCount
  } catch (error) {
    console.error('获取训练记录失败:', error)
  } finally {
    loading.value = false
  }
}

const getExerciseOptions = async () => {
  try {
    const res = await getExerciseList({
      pageNum: 1,
      pageSize: 1000
    })
    const data = normalizePageData(res)
    exerciseOptions.value = data.list
  } catch (error) {
    console.error('获取动作选项失败:', error)
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

const getRowNumber = (index) => {
  return (queryParams.pageNum - 1) * queryParams.pageSize + index + 1
}

const resetForm = () => {
  Object.assign(recordForm, createDefaultRecordForm())
  if (recordFormRef.value) {
    recordFormRef.value.clearValidate()
  }
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增训练记录'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(recordForm, {
    id: row.id,
    exerciseId: row.exerciseId,
    workoutDate: row.workoutDate || '',
    durationMin: row.durationMin ?? undefined,
    setsCount: row.setsCount ?? undefined,
    repsCount: row.repsCount ?? undefined,
    caloriesBurned: row.caloriesBurned ?? undefined,
    completionStatus: row.completionStatus || '',
    feedbackScore: Number(row.feedbackScore || 0),
    remark: row.remark || ''
  })
  dialogTitle.value = '编辑训练记录'
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!recordFormRef.value) return

  try {
    await recordFormRef.value.validate()
    submitLoading.value = true

    const payload = buildSubmitPayload()
    if (recordForm.id) {
      await updateWorkoutRecord(recordForm.id, payload)
    } else {
      await createWorkoutRecord(payload)
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
  try {
    await deleteWorkoutRecord(row.id)
    ElMessage.success('删除成功')

    if (recordList.value.length === 1 && queryParams.pageNum > 1) {
      queryParams.pageNum -= 1
    }

    await getList()
  } catch (error) {
    console.error('删除训练记录失败:', error)
  }
}

const handleViewDetail = async (id) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null

  try {
    const res = await getWorkoutRecordDetail(id)
    detailData.value = res || null
  } catch (error) {
    console.error('获取训练记录详情失败:', error)
  } finally {
    detailLoading.value = false
  }
}

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const formatMinutes = (value) => {
  if (value === null || value === undefined || value === '') return '-'
  return `${value} 分钟`
}

const formatCount = (value, unit) => {
  if (value === null || value === undefined || value === '') return '-'
  return `${value} ${unit}`
}

const getCompletionTagType = (status) => {
  if (status === '已完成') return 'success'
  if (status === '部分完成') return 'warning'
  if (status === '未完成') return 'danger'
  return 'info'
}

onMounted(() => {
  getExerciseOptions()
  getList()
})
</script>

<style scoped>
.workout-page {
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

.record-table {
  width: 100%;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.record-form {
  padding-right: 12px;
}

.w-full {
  width: 100%;
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
}
</style>
