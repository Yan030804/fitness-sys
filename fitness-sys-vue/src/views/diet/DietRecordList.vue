<template>
  <div class="diet-page">
    <el-card class="filter-card" shadow="never">
      <div class="page-header">
        <div>
          <div class="page-title">饮食记录</div>
          <div class="page-subtitle">记录每日饮食摄入情况，跟踪餐次、食材、摄入克数与热量</div>
        </div>
        <el-button type="primary" @click="handleAdd">新增饮食记录</el-button>
      </div>

      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="饮食日期">
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

        <el-form-item label="餐次">
          <el-select v-model="queryParams.mealType" placeholder="全部餐次" clearable style="width: 160px">
            <el-option
              v-for="item in mealTypeOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="食材名称">
          <el-select
            v-model="queryParams.foodId"
            placeholder="全部食材"
            clearable
            filterable
            style="width: 220px"
          >
            <el-option
              v-for="item in foodOptions"
              :key="item.id"
              :label="item.foodName"
              :value="item.id"
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
          <span>饮食记录列表</span>
          <el-tag type="info" effect="plain">共 {{ total }} 条</el-tag>
        </div>
      </template>

      <el-table v-loading="loading" :data="recordList" border stripe class="record-table">
        <el-table-column label="编号" width="80" align="center">
          <template #default="{ $index }">
            {{ getRowNumber($index) }}
          </template>
        </el-table-column>
        <el-table-column prop="foodName" label="食材名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="饮食日期" min-width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.dietDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="mealType" label="餐次" min-width="100" align="center" />
        <el-table-column label="摄入克数" min-width="110" align="center">
          <template #default="{ row }">
            {{ formatDecimal(row.intakeGrams, 'g') }}
          </template>
        </el-table-column>
        <el-table-column label="摄入热量" min-width="110" align="center">
          <template #default="{ row }">
            {{ formatCount(row.caloriesIntake, 'kcal') }}
          </template>
        </el-table-column>
        <el-table-column label="来源" min-width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="Number(row.isRecommended) === 1 ? 'success' : 'info'" effect="light">
              {{ Number(row.isRecommended) === 1 ? '系统推荐' : '手动记录' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" min-width="240" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="handleViewDetail(row.id)">查看详情</el-button>
              <el-button size="small" type="primary" plain @click="handleEdit(row)">编辑</el-button>
              <el-popconfirm
                title="确认删除该饮食记录吗？"
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

      <el-empty v-if="!loading && recordList.length === 0" description="暂无饮食记录" />
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
            <el-form-item label="食材名称" prop="foodId">
              <el-select
                v-model="recordForm.foodId"
                placeholder="请选择食材"
                filterable
                class="w-full"
              >
                <el-option
                  v-for="item in foodOptions"
                  :key="item.id"
                  :label="item.foodName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="饮食日期" prop="dietDate">
              <el-date-picker
                v-model="recordForm.dietDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择饮食日期"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="餐次">
              <el-select v-model="recordForm.mealType" placeholder="请选择餐次" clearable class="w-full">
                <el-option
                  v-for="item in mealTypeOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="摄入克数">
              <el-input-number
                v-model="recordForm.intakeGrams"
                :precision="2"
                :step="1"
                :min="0"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="摄入热量">
              <el-input-number v-model="recordForm.caloriesIntake" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="推荐来源">
              <el-radio-group v-model="recordForm.isRecommended">
                <el-radio :label="0">手动记录</el-radio>
                <el-radio :label="1">系统推荐</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="recordForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入饮食备注"
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
      title="饮食记录详情"
      size="520px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-panel">
        <template v-if="detailData">
          <div class="detail-section">
            <div class="section-title">基础信息</div>
            <div class="detail-grid">
              <div class="detail-item"><span class="label">记录ID</span><span class="value">{{ detailData.id || '-' }}</span></div>
              <div class="detail-item"><span class="label">食材名称</span><span class="value">{{ detailData.foodName || '-' }}</span></div>
              <div class="detail-item"><span class="label">饮食日期</span><span class="value">{{ formatDate(detailData.dietDate) }}</span></div>
              <div class="detail-item"><span class="label">餐次</span><span class="value">{{ detailData.mealType || '-' }}</span></div>
              <div class="detail-item"><span class="label">摄入克数</span><span class="value">{{ formatDecimal(detailData.intakeGrams, 'g') }}</span></div>
              <div class="detail-item"><span class="label">摄入热量</span><span class="value">{{ formatCount(detailData.caloriesIntake, 'kcal') }}</span></div>
              <div class="detail-item"><span class="label">推荐来源</span><span class="value">{{ Number(detailData.isRecommended) === 1 ? '系统推荐' : '手动记录' }}</span></div>
              <div class="detail-item"><span class="label">创建时间</span><span class="value">{{ formatDateTime(detailData.createdAt) }}</span></div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">备注信息</div>
            <div class="detail-grid single-column">
              <div class="detail-item">
                <span class="label">备注</span>
                <span class="value">{{ detailData.remark || '暂无备注' }}</span>
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
import { getFoodList } from '../../api/food'
import {
  createDietRecord,
  deleteDietRecord,
  getDietRecordDetail,
  getDietRecordList,
  updateDietRecord
} from '../../api/dietRecord'

const mealTypeOptions = ['早餐', '午餐', '晚餐', '加餐']

const createDefaultQueryParams = () => ({
  pageNum: 1,
  pageSize: 10,
  dateRange: [],
  mealType: '',
  foodId: undefined
})

const createDefaultRecordForm = () => ({
  id: undefined,
  foodId: undefined,
  dietDate: '',
  mealType: '',
  intakeGrams: undefined,
  caloriesIntake: undefined,
  isRecommended: 0,
  remark: ''
})

const loading = ref(false)
const submitLoading = ref(false)
const detailLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增饮食记录')
const recordFormRef = ref(null)
const recordList = ref([])
const total = ref(0)
const detailData = ref(null)
const foodOptions = ref([])

const queryParams = reactive(createDefaultQueryParams())
const recordForm = reactive(createDefaultRecordForm())

const rules = {
  foodId: [{ required: true, message: '请选择食材', trigger: 'change' }],
  dietDate: [{ required: true, message: '请选择饮食日期', trigger: 'change' }]
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

  if (queryParams.foodId) params.foodId = queryParams.foodId
  if (queryParams.mealType) params.mealType = queryParams.mealType

  if (Array.isArray(queryParams.dateRange) && queryParams.dateRange.length === 2) {
    params.dietDateStart = queryParams.dateRange[0]
    params.dietDateEnd = queryParams.dateRange[1]
  }

  return params
}

const buildSubmitPayload = () => {
  const payload = {
    foodId: recordForm.foodId,
    dietDate: recordForm.dietDate
  }

  if (recordForm.mealType) payload.mealType = recordForm.mealType
  if (recordForm.intakeGrams !== undefined && recordForm.intakeGrams !== null) payload.intakeGrams = recordForm.intakeGrams
  if (recordForm.caloriesIntake !== undefined && recordForm.caloriesIntake !== null) payload.caloriesIntake = recordForm.caloriesIntake
  payload.isRecommended = recordForm.isRecommended ?? 0
  if (recordForm.remark.trim()) payload.remark = recordForm.remark.trim()

  return payload
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getDietRecordList(buildQueryParams())
    const { list, total: totalCount } = normalizePageData(res)
    recordList.value = list
    total.value = totalCount
  } catch (error) {
    console.error('获取饮食记录失败:', error)
  } finally {
    loading.value = false
  }
}

const getFoodOptions = async () => {
  try {
    const res = await getFoodList({
      pageNum: 1,
      pageSize: 1000
    })
    const data = normalizePageData(res)
    foodOptions.value = data.list
  } catch (error) {
    console.error('获取食材选项失败:', error)
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
  dialogTitle.value = '新增饮食记录'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(recordForm, {
    id: row.id,
    foodId: row.foodId,
    dietDate: row.dietDate || '',
    mealType: row.mealType || '',
    intakeGrams: row.intakeGrams ?? undefined,
    caloriesIntake: row.caloriesIntake ?? undefined,
    isRecommended: Number(row.isRecommended ?? 0),
    remark: row.remark || ''
  })
  dialogTitle.value = '编辑饮食记录'
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!recordFormRef.value) return

  try {
    await recordFormRef.value.validate()
    submitLoading.value = true

    const payload = buildSubmitPayload()
    if (recordForm.id) {
      await updateDietRecord(recordForm.id, payload)
    } else {
      await createDietRecord(payload)
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
    await deleteDietRecord(row.id)
    ElMessage.success('删除成功')

    if (recordList.value.length === 1 && queryParams.pageNum > 1) {
      queryParams.pageNum -= 1
    }

    await getList()
  } catch (error) {
    console.error('删除饮食记录失败:', error)
  }
}

const handleViewDetail = async (id) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null

  try {
    const res = await getDietRecordDetail(id)
    detailData.value = res || null
  } catch (error) {
    console.error('获取饮食记录详情失败:', error)
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

const formatDecimal = (value, unit = '') => {
  if (value === null || value === undefined || value === '') return '-'
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) return `${value}${unit ? ` ${unit}` : ''}`.trim()
  const formatted = Number.isInteger(numberValue) ? String(numberValue) : numberValue.toFixed(2)
  return `${formatted}${unit ? ` ${unit}` : ''}`.trim()
}

const formatCount = (value, unit) => {
  if (value === null || value === undefined || value === '') return '-'
  return `${value} ${unit}`
}

onMounted(() => {
  getFoodOptions()
  getList()
})
</script>

<style scoped>
.diet-page {
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
