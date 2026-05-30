<template>
  <div class="appointment-page">
    <el-card class="filter-card" shadow="never">
      <div class="page-header">
        <div>
          <div class="page-title">{{ isAdmin ? '预约总览' : '预约管理' }}</div>
          <div class="page-subtitle">
            {{ isAdmin ? '查看并处理全部用户预约记录' : '在线提交预约、查看我的预约记录并跟踪预约状态' }}
          </div>
        </div>
        <el-button v-if="!isAdmin" type="primary" @click="handleAdd">创建预约</el-button>
      </div>

      <el-form :inline="true" :model="queryParams" class="filter-form">
        <!-- <el-form-item v-if="isAdmin" label="用户ID">
          <el-input
            v-model="queryParams.userId"
            placeholder="请输入用户ID"
            clearable
            style="width: 160px"
            @keyup.enter="handleQuery"
          />
        </el-form-item> -->

        <el-form-item v-if="isAdmin">
          <template #label>
            <span class="label-with-tip">
              手机号
              <el-tooltip content="填写手机号时优先按手机号查询" placement="top">
                <el-icon class="label-tip-icon"><InfoFilled /></el-icon>
              </el-tooltip>
            </span>
          </template>
          <el-input
            v-model="queryParams.phone"
            placeholder="请输入手机号"
            clearable
            maxlength="20"
            style="width: 160px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item v-if="isAdmin" label="预约类型">
          <el-select
            v-model="queryParams.appointmentType"
            placeholder="全部类型"
            clearable
            filterable
            allow-create
            default-first-option
            style="width: 180px"
          >
            <el-option
              v-for="item in appointmentTypeOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="全部状态"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="item in appointmentStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="isAdmin" label="预约日期">
          <el-date-picker
            v-model="queryParams.reserveDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择预约日期"
            style="width: 180px"
          />
        </el-form-item>

        <el-form-item v-else label="预约日期">
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

        <el-form-item class="action-buttons">
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="section-header">
          <span>{{ isAdmin ? '全部预约列表' : '我的预约列表' }}</span>
          <el-tag type="info" effect="plain">共 {{ total }} 条</el-tag>
        </div>
      </template>

      <el-table v-loading="loading" :data="appointmentList" border stripe class="appointment-table">
        <el-table-column label="编号" width="80" align="center">
          <template #default="{ $index }">
            {{ getRowNumber($index) }}
          </template>
        </el-table-column>
        <!-- <el-table-column v-if="isAdmin" prop="userId" label="用户ID" width="100" align="center" /> -->
        <el-table-column prop="name" label="预约人" min-width="120" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" min-width="140" show-overflow-tooltip />
        <el-table-column label="预约类型" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.appointmentType || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="预约日期" min-width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.reserveDate) }}
          </template>
        </el-table-column>
        <el-table-column label="预约时间" min-width="100" align="center">
          <template #default="{ row }">
            {{ row.reserveTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ formatAppointmentStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" min-width="320" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="handleViewDetail(row.id)">查看详情</el-button>
              <el-button
                v-if="!isAdmin && canEditOrCancel(row.status)"
                size="small"
                type="primary"
                plain
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                v-if="isAdmin"
                size="small"
                type="primary"
                plain
                @click="handleOpenStatusDialog(row)"
              >
                处理状态
              </el-button>
              <el-popconfirm
                v-if="!isAdmin && canEditOrCancel(row.status)"
                title="确认取消该预约吗？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleCancel(row)"
              >
                <template #reference>
                  <el-button size="small" type="danger" plain>取消预约</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && appointmentList.length === 0" description="暂无预约数据" />
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
      v-if="!isAdmin"
      v-model="formDialogVisible"
      :title="formDialogTitle"
      width="760px"
      append-to-body
      destroy-on-close
      @closed="resetForm"
    >
      <el-form
        ref="appointmentFormRef"
        :model="appointmentForm"
        :rules="appointmentRules"
        label-width="100px"
        class="appointment-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预约人姓名" prop="name">
              <el-input v-model="appointmentForm.name" placeholder="请输入预约人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="appointmentForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="appointmentForm.gender" placeholder="请选择性别" clearable class="w-full">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input-number v-model="appointmentForm.age" :min="0" :max="150" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预约类型" prop="appointmentType">
              <el-select
                v-model="appointmentForm.appointmentType"
                placeholder="请选择预约类型"
                clearable
                filterable
                allow-create
                default-first-option
                class="w-full"
              >
                <el-option
                  v-for="item in appointmentTypeOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预约日期" prop="reserveDate">
              <el-date-picker
                v-model="appointmentForm.reserveDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择预约日期"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预约时间" prop="reserveTime">
              <el-time-picker
                v-model="appointmentForm.reserveTime"
                value-format="HH:mm:ss"
                placeholder="请选择预约时间"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="appointmentForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-if="isAdmin"
      v-model="statusDialogVisible"
      title="处理预约状态"
      width="560px"
      append-to-body
      destroy-on-close
      @closed="resetStatusForm"
    >
      <el-form ref="statusFormRef" :model="statusForm" :rules="statusRules" label-width="100px">
        <el-form-item label="预约状态" prop="status">
          <el-select v-model="statusForm.status" placeholder="请选择状态" class="w-full">
            <el-option
              v-for="item in adminStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="statusForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusSubmitLoading" @click="submitStatusForm">提交</el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="detailVisible"
      title="预约详情"
      size="520px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-panel">
        <template v-if="detailData">
          <div class="detail-section">
            <div class="section-title">基础信息</div>
            <div class="detail-grid">
              <div class="detail-item"><span class="label">预约ID</span><span class="value">{{ detailData.id || '-' }}</span></div>
              <div class="detail-item"><span class="label">用户ID</span><span class="value">{{ detailData.userId || '-' }}</span></div>
              <div class="detail-item"><span class="label">预约人姓名</span><span class="value">{{ detailData.name || '-' }}</span></div>
              <div class="detail-item"><span class="label">联系电话</span><span class="value">{{ detailData.phone || '-' }}</span></div>
              <div class="detail-item"><span class="label">性别</span><span class="value">{{ detailData.gender || '-' }}</span></div>
              <div class="detail-item"><span class="label">年龄</span><span class="value">{{ detailData.age ?? '-' }}</span></div>
              <div class="detail-item"><span class="label">预约类型</span><span class="value">{{ detailData.appointmentType || '-' }}</span></div>
              <div class="detail-item"><span class="label">预约状态</span><span class="value">{{ formatAppointmentStatus(detailData.status) }}</span></div>
              <div class="detail-item"><span class="label">预约日期</span><span class="value">{{ formatDate(detailData.reserveDate) }}</span></div>
              <div class="detail-item"><span class="label">预约时间</span><span class="value">{{ detailData.reserveTime || '-' }}</span></div>
              <div class="detail-item"><span class="label">创建时间</span><span class="value">{{ formatDateTime(detailData.createdAt) }}</span></div>
              <div class="detail-item"><span class="label">更新时间</span><span class="value">{{ formatDateTime(detailData.updatedAt) }}</span></div>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '../../store/auth'
import { isAdminByRole } from '../../utils/auth'
import {
  cancelAppointment,
  createAppointment,
  getAdminAppointmentDetail,
  getAdminAppointmentList,
  getAppointmentDetail,
  getMyAppointmentList,
  updateAdminAppointmentStatus,
  updateAppointment
} from '../../api/appointment'

const authStore = useAuthStore()
const isAdmin = computed(() => isAdminByRole(authStore.roleCode))

const appointmentTypeOptions = ['私教咨询', '体测评估', '营养咨询', '训练指导', '课程体验']
const appointmentStatusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已完成', value: 'DONE' }
]
const adminStatusOptions = appointmentStatusOptions

const createDefaultQueryParams = () => ({
  pageNum: 1,
  pageSize: 10,
  userId: '',
  phone: '',
  appointmentType: '',
  status: '',
  reserveDate: '',
  dateRange: []
})

const createDefaultAppointmentForm = () => ({
  id: undefined,
  name: '',
  phone: '',
  gender: '',
  age: undefined,
  appointmentType: '',
  reserveDate: '',
  reserveTime: '',
  remark: ''
})

const createDefaultStatusForm = () => ({
  id: undefined,
  status: 'PENDING',
  remark: ''
})

const loading = ref(false)
const submitLoading = ref(false)
const detailLoading = ref(false)
const statusSubmitLoading = ref(false)
const formDialogVisible = ref(false)
const detailVisible = ref(false)
const statusDialogVisible = ref(false)
const formDialogTitle = ref('创建预约')
const total = ref(0)
const appointmentList = ref([])
const detailData = ref(null)

const appointmentFormRef = ref(null)
const statusFormRef = ref(null)

const queryParams = reactive(createDefaultQueryParams())
const appointmentForm = reactive(createDefaultAppointmentForm())
const statusForm = reactive(createDefaultStatusForm())

const appointmentRules = {
  name: [{ required: true, message: '请输入预约人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  appointmentType: [{ required: true, message: '请选择预约类型', trigger: 'change' }],
  reserveDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  reserveTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
}

const statusRules = {
  status: [{ required: true, message: '请选择预约状态', trigger: 'change' }]
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

  if (queryParams.status) params.status = queryParams.status

  if (isAdmin.value) {
    const phone = queryParams.phone.trim()
    const userId = queryParams.userId.trim()
    if (phone) params.phone = phone
    if (userId) params.userId = userId
    if (queryParams.appointmentType) params.appointmentType = queryParams.appointmentType
    if (queryParams.reserveDate) params.reserveDate = queryParams.reserveDate
  } else if (Array.isArray(queryParams.dateRange) && queryParams.dateRange.length === 2) {
    params.reserveDateStart = queryParams.dateRange[0]
    params.reserveDateEnd = queryParams.dateRange[1]
  }

  return params
}

const buildAppointmentPayload = () => {
  const payload = {
    name: appointmentForm.name.trim(),
    phone: appointmentForm.phone.trim(),
    appointmentType: appointmentForm.appointmentType,
    reserveDate: appointmentForm.reserveDate,
    reserveTime: appointmentForm.reserveTime
  }

  if (appointmentForm.gender) payload.gender = appointmentForm.gender
  if (appointmentForm.age !== undefined && appointmentForm.age !== null) payload.age = appointmentForm.age
  if (appointmentForm.remark.trim()) payload.remark = appointmentForm.remark.trim()

  return payload
}

const getList = async () => {
  loading.value = true
  try {
    const requestApi = isAdmin.value ? getAdminAppointmentList : getMyAppointmentList
    const res = await requestApi(buildQueryParams())
    const { list, total: totalCount } = normalizePageData(res)
    appointmentList.value = list
    total.value = totalCount
  } catch (error) {
    console.error('获取预约列表失败:', error)
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

const getRowNumber = (index) => {
  return (queryParams.pageNum - 1) * queryParams.pageSize + index + 1
}

const resetForm = () => {
  Object.assign(appointmentForm, createDefaultAppointmentForm())
  if (appointmentFormRef.value) {
    appointmentFormRef.value.clearValidate()
  }
}

const handleAdd = () => {
  resetForm()
  appointmentForm.name = authStore.currentUser?.realName || authStore.currentUser?.nickname || authStore.username || ''
  appointmentForm.phone = authStore.currentUser?.phone || ''
  appointmentForm.gender = authStore.currentUser?.gender || ''
  appointmentForm.age = authStore.currentUser?.age ?? undefined
  formDialogTitle.value = '创建预约'
  formDialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(appointmentForm, {
    id: row.id,
    name: row.name || '',
    phone: row.phone || '',
    gender: row.gender || '',
    age: row.age ?? undefined,
    appointmentType: row.appointmentType || '',
    reserveDate: row.reserveDate || '',
    reserveTime: row.reserveTime || '',
    remark: row.remark || ''
  })
  formDialogTitle.value = '编辑预约'
  formDialogVisible.value = true
}

const submitForm = async () => {
  if (!appointmentFormRef.value) return

  try {
    await appointmentFormRef.value.validate()
    submitLoading.value = true
    const payload = buildAppointmentPayload()

    if (appointmentForm.id) {
      await updateAppointment(appointmentForm.id, payload)
    } else {
      await createAppointment(payload)
    }

    ElMessage.success(`${formDialogTitle.value}成功`)
    formDialogVisible.value = false
    await getList()
  } catch (error) {
    if (error?.name !== 'Error') {
      console.error(`${formDialogTitle.value}失败:`, error)
    }
  } finally {
    submitLoading.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await cancelAppointment(row.id, {})
    ElMessage.success('预约已取消')
    await getList()
  } catch (error) {
    console.error('取消预约失败:', error)
  }
}

const handleViewDetail = async (id) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null

  try {
    const requestApi = isAdmin.value ? getAdminAppointmentDetail : getAppointmentDetail
    const res = await requestApi(id)
    detailData.value = res || null
  } catch (error) {
    console.error('获取预约详情失败:', error)
  } finally {
    detailLoading.value = false
  }
}

const resetStatusForm = () => {
  Object.assign(statusForm, createDefaultStatusForm())
  if (statusFormRef.value) {
    statusFormRef.value.clearValidate()
  }
}

const handleOpenStatusDialog = (row) => {
  resetStatusForm()
  statusForm.id = row.id
  statusForm.status = row.status || 'PENDING'
  statusForm.remark = row.remark || ''
  statusDialogVisible.value = true
}

const submitStatusForm = async () => {
  if (!statusFormRef.value) return

  try {
    await statusFormRef.value.validate()
    statusSubmitLoading.value = true
    await updateAdminAppointmentStatus(statusForm.id, {
      status: statusForm.status,
      remark: statusForm.remark.trim()
    })
    ElMessage.success('预约状态更新成功')
    statusDialogVisible.value = false
    await getList()
  } catch (error) {
    if (error?.name !== 'Error') {
      console.error('更新预约状态失败:', error)
    }
  } finally {
    statusSubmitLoading.value = false
  }
}

const canEditOrCancel = (status) => {
  return status !== 'CANCELLED' && status !== 'DONE'
}

const formatAppointmentStatus = (status) => {
  const statusMap = {
    PENDING: '待处理',
    CONFIRMED: '已确认',
    CANCELLED: '已取消',
    DONE: '已完成'
  }
  return statusMap[status] || status || '-'
}

const getStatusTagType = (status) => {
  if (status === 'PENDING') return 'warning'
  if (status === 'CONFIRMED') return 'primary'
  if (status === 'DONE') return 'success'
  if (status === 'CANCELLED') return 'danger'
  return 'info'
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

onMounted(() => {
  getList()
})
</script>

<style scoped>
.appointment-page {
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

.label-with-tip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.label-tip-icon {
  cursor: help;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.action-buttons {
  margin-left: auto;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.appointment-table {
  width: 100%;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.appointment-form {
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
