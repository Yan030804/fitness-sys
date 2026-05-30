<template>
  <div class="admin-user-page">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryParams" label-width="68px" class="filter-form">
        <el-form-item label="用户名">
          <el-input
            v-model="queryParams.username"
            placeholder="请输入用户名"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input
            v-model="queryParams.realName"
            placeholder="请输入真实姓名"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="queryParams.phone"
            placeholder="请输入手机号"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input
            v-model="queryParams.email"
            placeholder="请输入邮箱"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryParams.roleCode" placeholder="全部角色" clearable style="width: 160px">
            <el-option label="管理员" value="ROLE_ADMIN" />
            <el-option label="普通用户" value="ROLE_USER" />
          </el-select>
        </el-form-item>
        <el-form-item class="action-group">
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <span class="title">用户列表</span>
            <span class="subtitle">支持按用户名、联系方式、角色和状态筛选</span>
          </div>
          <el-tag type="info" effect="plain">共 {{ total }} 条</el-tag>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe class="user-table">
        <el-table-column label="编号" min-width="90" align="center">
          <template #default="{ $index }">
            {{ getRowNumber($index) }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="130" show-overflow-tooltip />
        <el-table-column prop="realName" label="真实姓名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />
        <el-table-column label="角色" min-width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.roleCode)" effect="light">
              {{ row.roleName || formatRoleCode(row.roleCode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="140" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- <el-table-column label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column> -->
        <el-table-column label="操作" fixed="right" min-width="220">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="handleViewDetail(row)">查看详情</el-button>
              <el-popconfirm
                :title="row.status === 1 ? '确认禁用该用户吗？' : '确认启用该用户吗？'"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleToggleStatus(row)"
              >
                <template #reference>
                  <el-button
                    size="small"
                    :type="row.status === 1 ? 'danger' : 'success'"
                    plain
                    :loading="switchingUserId === row.id"
                  >
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无符合条件的用户数据" />

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-drawer
      v-model="detailVisible"
      title="用户详情"
      size="520px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-panel">
        <template v-if="detailData">
          <div class="detail-section">
            <div class="section-title">基础信息</div>
            <div class="detail-grid">
              <div class="detail-item"><span class="label">用户ID</span><span class="value">{{ detailData.id || '-' }}</span></div>
              <div class="detail-item"><span class="label">用户名</span><span class="value">{{ detailData.username || '-' }}</span></div>
              <div class="detail-item"><span class="label">真实姓名</span><span class="value">{{ detailData.realName || '-' }}</span></div>
              <div class="detail-item"><span class="label">昵称</span><span class="value">{{ detailData.nickname || '-' }}</span></div>
              <div class="detail-item"><span class="label">性别</span><span class="value">{{ detailData.gender || '-' }}</span></div>
              <div class="detail-item"><span class="label">年龄</span><span class="value">{{ detailData.age ?? '-' }}</span></div>
              <div class="detail-item"><span class="label">手机号</span><span class="value">{{ detailData.phone || '-' }}</span></div>
              <div class="detail-item"><span class="label">邮箱</span><span class="value">{{ detailData.email || '-' }}</span></div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">身体与目标</div>
            <div class="detail-grid">
              <div class="detail-item"><span class="label">身高</span><span class="value">{{ formatNumber(detailData.heightCm, 'cm') }}</span></div>
              <div class="detail-item"><span class="label">体重</span><span class="value">{{ formatNumber(detailData.weightKg, 'kg') }}</span></div>
              <div class="detail-item"><span class="label">BMI</span><span class="value">{{ formatNumber(detailData.bmi) }}</span></div>
              <div class="detail-item"><span class="label">健身目标</span><span class="value">{{ detailData.fitnessGoal || '-' }}</span></div>
              <div class="detail-item"><span class="label">运动水平</span><span class="value">{{ detailData.activityLevel || '-' }}</span></div>
              <div class="detail-item"><span class="label">饮食偏好</span><span class="value">{{ detailData.dietPreference || '-' }}</span></div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">账号信息</div>
            <div class="detail-grid">
              <div class="detail-item"><span class="label">角色ID</span><span class="value">{{ detailData.roleId ?? '-' }}</span></div>
              <div class="detail-item"><span class="label">角色编码</span><span class="value">{{ detailData.roleCode || '-' }}</span></div>
              <div class="detail-item"><span class="label">角色名称</span><span class="value">{{ detailData.roleName || '-' }}</span></div>
              <div class="detail-item">
                <span class="label">账号状态</span>
                <span class="value">
                  <el-tag :type="detailData.status === 1 ? 'success' : 'danger'" effect="light">
                    {{ detailData.status === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </span>
              </div>
              <div class="detail-item"><span class="label">创建时间</span><span class="value">{{ formatDateTime(detailData.createdAt) }}</span></div>
              <div class="detail-item"><span class="label">更新时间</span><span class="value">{{ formatDateTime(detailData.updatedAt) }}</span></div>
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
import { getAdminUserDetail, getAdminUserList, updateAdminUserStatus } from '../../api/user'

const createDefaultQueryParams = () => ({
  pageNum: 1,
  pageSize: 10,
  username: '',
  realName: '',
  phone: '',
  email: '',
  status: undefined,
  roleCode: ''
})

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)
const switchingUserId = ref(null)

const queryParams = reactive(createDefaultQueryParams())

const normalizePageData = (payload) => {
  return {
    list: payload?.list || payload?.records || payload?.rows || [],
    total: Number(payload?.total || payload?.count || 0)
  }
}

const buildQueryParams = () => {
  const params = {
    pageNum: queryParams.pageNum,
    pageSize: queryParams.pageSize
  }

  if (queryParams.username) params.username = queryParams.username.trim()
  if (queryParams.realName) params.realName = queryParams.realName.trim()
  if (queryParams.phone) params.phone = queryParams.phone.trim()
  if (queryParams.email) params.email = queryParams.email.trim()
  if (queryParams.roleCode) params.roleCode = queryParams.roleCode
  if (queryParams.status !== undefined && queryParams.status !== null && queryParams.status !== '') {
    params.status = queryParams.status
  }

  return params
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getAdminUserList(buildQueryParams())
    const { list, total: totalCount } = normalizePageData(res)
    tableData.value = list
    total.value = totalCount
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const handleReset = () => {
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

const handleViewDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getAdminUserDetail(row.id)
    detailData.value = res || null
  } catch (error) {
    console.error('获取用户详情失败:', error)
  } finally {
    detailLoading.value = false
  }
}

const handleToggleStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  switchingUserId.value = row.id
  try {
    const res = await updateAdminUserStatus(row.id, { status: nextStatus })
    const latestStatus = res?.status ?? nextStatus
    row.status = latestStatus

    if (detailData.value && detailData.value.id === row.id) {
      detailData.value.status = latestStatus
      detailData.value.updatedAt = detailData.value.updatedAt || new Date().toISOString()
    }

    ElMessage.success(latestStatus === 1 ? '用户已启用' : '用户已禁用')
  } catch (error) {
    console.error('更新用户状态失败:', error)
  } finally {
    switchingUserId.value = null
  }
}

const formatRoleCode = (roleCode) => {
  const roleMap = {
    ROLE_ADMIN: '管理员',
    ROLE_USER: '普通用户'
  }
  return roleMap[String(roleCode || '').toUpperCase()] || roleCode || '-'
}

const getRoleTagType = (roleCode) => {
  const code = String(roleCode || '').toUpperCase()
  if (code === 'ROLE_ADMIN') return 'danger'
  if (code === 'ROLE_USER') return 'primary'
  return 'info'
}

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  const pad = (num) => String(num).padStart(2, '0')
  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  const hour = pad(date.getHours())
  const minute = pad(date.getMinutes())
  const second = pad(date.getSeconds())
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

const formatNumber = (value, unit = '') => {
  if (value === null || value === undefined || value === '') return '-'
  return `${value}${unit}`
}

const getRowNumber = (index) => {
  return (queryParams.pageNum - 1) * queryParams.pageSize + index + 1
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.admin-user-page {
  min-height: calc(100vh - 120px);
}

.filter-card,
.table-card {
  border-radius: 14px;
}

.filter-card {
  margin-bottom: 18px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
}

.action-group {
  margin-left: auto;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.subtitle {
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
}

.user-table {
  width: 100%;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
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

@media (max-width: 768px) {
  .action-group {
    margin-left: 0;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .pagination-wrap {
    justify-content: center;
  }
}
</style>
