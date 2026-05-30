<template>
  <div class="food-page">
    <el-card class="filter-card" shadow="never">
      <div class="page-header">
        <div>
          <div class="page-title">{{ isAdmin ? '食材库管理' : '食材库' }}</div>
          <div class="page-subtitle">
            {{ isAdmin ? '支持食材查询、维护、新增和逻辑删除' : '快速查找适合当前目标与饮食时段的食材推荐' }}
          </div>
        </div>
        <el-tag v-if="isAdmin" type="warning" effect="light">管理员视图</el-tag>
      </div>

      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item v-if="isAdmin" label="食材名称">
          <el-input
            v-model="queryParams.foodName"
            placeholder="请输入食材名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="食材分类">
          <el-select
            v-model="queryParams.category"
            placeholder="全部分类"
            clearable
            filterable
            allow-create
            default-first-option
            style="width: 160px"
          >
            <el-option
              v-for="item in categoryOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="适用目标">
          <el-select
            v-model="queryParams.suitableGoal"
            placeholder="全部目标"
            clearable
            filterable
            allow-create
            default-first-option
            style="width: 160px"
          >
            <el-option
              v-for="item in goalOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="适用时间">
          <el-select
            v-model="queryParams.suitableTime"
            placeholder="全部时间"
            clearable
            filterable
            allow-create
            default-first-option
            style="width: 160px"
          >
            <el-option
              v-for="item in timeOptions"
              :key="item"
              :label="item"
              :value="item"
            />
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
          <el-button v-if="isAdmin" type="success" @click="handleAdd">新增食材</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="isAdmin" class="table-card" shadow="never">
      <template #header>
        <div class="section-header">
          <span>食材列表</span>
          <el-tag type="info" effect="plain">共 {{ total }} 条</el-tag>
        </div>
      </template>

      <el-table v-loading="loading" :data="foodList" border stripe class="admin-table">
        <el-table-column label="编号" width="80" align="center">
          <template #default="{ $index }">
            {{ getRowNumber($index) }}
          </template>
        </el-table-column>
        <el-table-column prop="foodName" label="食材名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" min-width="110" />
        <el-table-column label="热量/100g" min-width="120" align="center">
          <template #default="{ row }">
            {{ formatDecimal(row.caloriesPer100g, 'kcal') }}
          </template>
        </el-table-column>
        <el-table-column label="蛋白质" min-width="110" align="center">
          <template #default="{ row }">
            {{ formatDecimal(row.proteinPer100g, 'g') }}
          </template>
        </el-table-column>
        <el-table-column label="脂肪" min-width="100" align="center">
          <template #default="{ row }">
            {{ formatDecimal(row.fatPer100g, 'g') }}
          </template>
        </el-table-column>
        <el-table-column label="碳水" min-width="100" align="center">
          <template #default="{ row }">
            {{ formatDecimal(row.carbPer100g, 'g') }}
          </template>
        </el-table-column>
        <el-table-column prop="suitableGoal" label="适用目标" min-width="120" show-overflow-tooltip />
        <el-table-column prop="suitableTime" label="适用时间" min-width="120" show-overflow-tooltip />
        <el-table-column label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- <el-table-column label="更新时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt || row.createdAt) }}
          </template>
        </el-table-column> -->
        <el-table-column label="操作" min-width="240" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="goToDetail(row.id)">查看详情</el-button>
              <el-button size="small" type="primary" plain @click="handleEdit(row)">编辑</el-button>
              <el-popconfirm
                title="确认将该食材逻辑删除吗？"
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

      <el-empty v-if="!loading && foodList.length === 0" description="暂无食材数据" />
    </el-card>

    <div v-else v-loading="loading" class="user-list-content">
      <el-row v-if="foodList.length > 0" :gutter="20">
        <el-col
          v-for="item in foodList"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="card-col"
        >
          <el-card class="food-card" shadow="hover">
            <div class="card-top">
              <div class="food-name-row">
                <span class="food-name">{{ item.foodName }}</span>
                <el-tag type="success" effect="light" size="small">{{ item.category || '未分类' }}</el-tag>
              </div>
              <p class="food-desc">{{ item.description || '暂无食材说明' }}</p>
            </div>

            <div class="nutrition-grid">
              <div class="nutrition-item">
                <div class="nutrition-label">热量</div>
                <div class="nutrition-value">{{ formatDecimal(item.caloriesPer100g, 'kcal') }}</div>
              </div>
              <div class="nutrition-item">
                <div class="nutrition-label">蛋白质</div>
                <div class="nutrition-value">{{ formatDecimal(item.proteinPer100g, 'g') }}</div>
              </div>
              <div class="nutrition-item">
                <div class="nutrition-label">脂肪</div>
                <div class="nutrition-value">{{ formatDecimal(item.fatPer100g, 'g') }}</div>
              </div>
              <div class="nutrition-item">
                <div class="nutrition-label">碳水</div>
                <div class="nutrition-value">{{ formatDecimal(item.carbPer100g, 'g') }}</div>
              </div>
            </div>

            <div class="meta-list">
              <div class="meta-item">
                <span class="meta-label">适用目标</span>
                <span class="meta-value">{{ item.suitableGoal || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">适用时间</span>
                <span class="meta-value">{{ item.suitableTime || '-' }}</span>
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

      <el-empty v-else description="暂无相关食材，换个筛选条件试试吧" />
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
      width="780px"
      append-to-body
      destroy-on-close
      @closed="resetForm"
    >
      <el-form
        ref="foodFormRef"
        :model="foodForm"
        :rules="rules"
        label-width="110px"
        class="food-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="食材名称" prop="foodName">
              <el-input v-model="foodForm.foodName" placeholder="请输入食材名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="食材分类">
              <el-select
                v-model="foodForm.category"
                placeholder="请选择分类"
                clearable
                filterable
                allow-create
                default-first-option
                class="w-full"
              >
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
            <el-form-item label="适用目标">
              <el-select
                v-model="foodForm.suitableGoal"
                placeholder="请选择适用目标"
                clearable
                filterable
                allow-create
                default-first-option
                class="w-full"
              >
                <el-option
                  v-for="item in goalOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用时间">
              <el-select
                v-model="foodForm.suitableTime"
                placeholder="请选择适用时间"
                clearable
                filterable
                allow-create
                default-first-option
                class="w-full"
              >
                <el-option
                  v-for="item in timeOptions"
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
            <el-form-item label="每100g热量">
              <el-input-number v-model="foodForm.caloriesPer100g" :precision="2" :step="1" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每100g蛋白质">
              <el-input-number v-model="foodForm.proteinPer100g" :precision="2" :step="0.1" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="每100g脂肪">
              <el-input-number v-model="foodForm.fatPer100g" :precision="2" :step="0.1" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每100g碳水">
              <el-input-number v-model="foodForm.carbPer100g" :precision="2" :step="0.1" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="食材说明">
          <el-input
            v-model="foodForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入食材说明"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="foodForm.status">
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
  createFood,
  deleteFood,
  getAdminFoodList,
  getFoodList,
  updateFood
} from '../../api/food'

const categoryOptions = ['主食', '蛋白质', '蔬菜', '水果', '乳制品', '坚果', '饮品']
const goalOptions = ['增肌', '减脂', '维持体重', '塑形', '高蛋白', '低脂']
const timeOptions = ['早餐', '午餐', '晚餐', '加餐', '训练前', '训练后']

const createDefaultQueryParams = () => ({
  pageNum: 1,
  pageSize: 8,
  foodName: '',
  category: '',
  suitableGoal: '',
  suitableTime: '',
  status: undefined
})

const createDefaultFoodForm = () => ({
  id: undefined,
  foodName: '',
  category: '',
  caloriesPer100g: 0,
  proteinPer100g: 0,
  fatPer100g: 0,
  carbPer100g: 0,
  suitableGoal: '',
  suitableTime: '',
  description: '',
  status: 1
})

const router = useRouter()
const authStore = useAuthStore()
const isAdmin = computed(() => isAdminByRole(authStore.roleCode))

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增食材')
const foodFormRef = ref(null)
const foodList = ref([])
const total = ref(0)

const queryParams = reactive(createDefaultQueryParams())
const foodForm = reactive(createDefaultFoodForm())

const rules = {
  foodName: [{ required: true, message: '请输入食材名称', trigger: 'blur' }]
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
  if (queryParams.suitableGoal) params.suitableGoal = queryParams.suitableGoal
  if (queryParams.suitableTime) params.suitableTime = queryParams.suitableTime

  if (isAdmin.value) {
    if (queryParams.foodName.trim()) params.foodName = queryParams.foodName.trim()
    if (queryParams.status !== undefined && queryParams.status !== null && queryParams.status !== '') {
      params.status = queryParams.status
    }
  }

  return params
}

const getList = async () => {
  loading.value = true
  try {
    const requestApi = isAdmin.value ? getAdminFoodList : getFoodList
    const res = await requestApi(buildQueryParams())
    const { list, total: totalCount } = normalizePageData(res)
    foodList.value = list
    total.value = totalCount
  } catch (error) {
    console.error('获取食材列表失败:', error)
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
  router.push(`/food/detail/${id}`)
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

const getRowNumber = (index) => {
  return (queryParams.pageNum - 1) * queryParams.pageSize + index + 1
}

const resetForm = () => {
  Object.assign(foodForm, createDefaultFoodForm())
  if (foodFormRef.value) {
    foodFormRef.value.clearValidate()
  }
}

const handleAdd = () => {
  if (!isAdmin.value) return
  resetForm()
  dialogTitle.value = '新增食材'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  if (!isAdmin.value) return
  resetForm()
  Object.assign(foodForm, {
    id: row.id,
    foodName: row.foodName || '',
    category: row.category || '',
    caloriesPer100g: Number(row.caloriesPer100g ?? 0),
    proteinPer100g: Number(row.proteinPer100g ?? 0),
    fatPer100g: Number(row.fatPer100g ?? 0),
    carbPer100g: Number(row.carbPer100g ?? 0),
    suitableGoal: row.suitableGoal || '',
    suitableTime: row.suitableTime || '',
    description: row.description || '',
    status: row.status ?? 1
  })
  dialogTitle.value = '编辑食材'
  dialogVisible.value = true
}

const buildSubmitPayload = () => {
  return {
    foodName: foodForm.foodName.trim(),
    category: foodForm.category || '',
    caloriesPer100g: foodForm.caloriesPer100g ?? 0,
    proteinPer100g: foodForm.proteinPer100g ?? 0,
    fatPer100g: foodForm.fatPer100g ?? 0,
    carbPer100g: foodForm.carbPer100g ?? 0,
    suitableGoal: foodForm.suitableGoal || '',
    suitableTime: foodForm.suitableTime || '',
    description: foodForm.description.trim(),
    status: foodForm.status ?? 1
  }
}

const submitForm = async () => {
  if (!isAdmin.value || !foodFormRef.value) return

  try {
    await foodFormRef.value.validate()
    submitLoading.value = true

    const payload = buildSubmitPayload()
    if (foodForm.id) {
      await updateFood(foodForm.id, payload)
    } else {
      await createFood(payload)
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
    await deleteFood(row.id)
    ElMessage.success('删除成功')

    if (foodList.value.length === 1 && queryParams.pageNum > 1) {
      queryParams.pageNum -= 1
    }

    await getList()
  } catch (error) {
    console.error('删除食材失败:', error)
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.food-page {
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

.food-card {
  height: 100%;
  border-radius: 16px;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.food-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 24px rgba(15, 23, 42, 0.08);
}

.card-top {
  margin-bottom: 16px;
}

.food-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.food-name {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.food-desc {
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

.nutrition-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.nutrition-item,
.meta-item {
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
}

.nutrition-label,
.meta-label {
  display: block;
  margin-bottom: 6px;
  color: #909399;
  font-size: 12px;
}

.nutrition-value,
.meta-value {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.meta-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
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

.food-form {
  padding-right: 12px;
}

.w-full {
  width: 100%;
}

@media (max-width: 992px) {
  .action-buttons {
    margin-left: 0;
  }

  .nutrition-grid,
  .meta-list {
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
