<template>
  <div class="plan-page">
    <el-card class="filter-card" shadow="never">
      <div class="page-header">
        <div>
          <div class="page-title">{{ isAdmin ? '计划总览' : '计划管理' }}</div>
          <div class="page-subtitle">
            {{ isAdmin ? '查看全部用户的训练计划与饮食计划' : '统一管理训练计划、饮食计划以及对应的执行明细' }}
          </div>
        </div>
        <el-button v-if="!isAdmin" type="primary" @click="handleGeneratePlan">生成计划</el-button>
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

        <el-form-item label="计划类型">
          <el-select v-model="queryParams.planType" placeholder="全部类型" clearable style="width: 160px">
            <el-option label="训练计划" value="TRAINING" />
            <el-option label="饮食计划" value="DIET" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="全部状态"
            clearable
            filterable
            allow-create
            default-first-option
            style="width: 180px"
          >
            <el-option
              v-for="item in planStatusOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="!isAdmin" label="计划周期">
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
          <span>{{ isAdmin ? '全部计划列表' : '我的计划列表' }}</span>
          <el-tag type="info" effect="plain">共 {{ total }} 条</el-tag>
        </div>
      </template>

      <el-table v-loading="loading" :data="planList" border stripe class="plan-table">
        <el-table-column label="编号" width="80" align="center">
          <template #default="{ $index }">
            {{ getRowNumber($index) }}
          </template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="用户姓名" width="120" align="center">
          <template #default="{ row }">
            {{ userNameMap[row.userId] || row.userId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="planName" label="计划名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="计划类型" min-width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.planType === 'TRAINING' ? 'primary' : 'success'" effect="light">
              {{ formatPlanType(row.planType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="周期类型" min-width="110" align="center">
          <template #default="{ row }">
            {{ formatCycleType(row.cycleType) }}
          </template>
        </el-table-column>
        <el-table-column label="计划周期" min-width="220">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }} 至 {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="targetGoal" label="目标" min-width="140" show-overflow-tooltip />
        <el-table-column prop="sourceType" label="来源" min-width="110" align="center" />
        <el-table-column prop="status" label="状态" min-width="120" align="center" />
        <el-table-column label="操作" min-width="300" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="handleViewPlan(row)">查看详情</el-button>
              <el-button v-if="!isAdmin" size="small" type="primary" plain @click="handleManageItems(row)">
                管理明细
              </el-button>
              <el-button v-if="!isAdmin" size="small" type="primary" plain @click="handleEditPlan(row)">编辑</el-button>
              <el-popconfirm
                v-if="!isAdmin"
                title="确认删除该计划吗？"
                confirm-button-text="确认"
                cancel-button-text="取消"
                @confirm="handleDeletePlan(row)"
              >
                <template #reference>
                  <el-button size="small" type="danger" plain>删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && planList.length === 0" description="暂无计划数据" />
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
      v-model="planDialogVisible"
      :title="planDialogTitle"
      width="760px"
      append-to-body
      destroy-on-close
      @closed="resetPlanForm"
    >
      <el-form
        ref="planFormRef"
        :model="planForm"
        :rules="planRules"
        label-width="100px"
        class="plan-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划类型" prop="planType">
              <el-select v-model="planForm.planType" placeholder="请选择类型" class="w-full" :disabled="isEditingPlan">
                <el-option label="训练计划" value="TRAINING" />
                <el-option label="饮食计划" value="DIET" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划名称" prop="planName">
              <el-input v-model="planForm.planName" placeholder="请输入计划名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="周期类型" prop="cycleType">
              <el-select v-model="planForm.cycleType" placeholder="请选择周期" class="w-full">
                <el-option label="每日计划" value="DAILY" />
                <el-option label="每周计划" value="WEEKLY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源类型">
              <el-select
                v-model="planForm.sourceType"
                placeholder="请选择来源"
                clearable
                filterable
                allow-create
                default-first-option
                class="w-full"
              >
                <el-option label="系统生成" value="SYSTEM" />
                <el-option label="手动创建" value="MANUAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="planForm.startDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择开始日期"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="planForm.endDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择结束日期"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="目标">
              <el-select
                v-model="planForm.targetGoal"
                placeholder="请选择或输入目标"
                clearable
                filterable
                allow-create
                default-first-option
                class="w-full"
              >
                <el-option
                  v-for="item in targetGoalOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="isEditingPlan" :span="12">
            <el-form-item label="状态">
              <el-select
                v-model="planForm.status"
                placeholder="请选择或输入状态"
                clearable
                filterable
                allow-create
                default-first-option
                class="w-full"
              >
                <el-option
                  v-for="item in planStatusOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="planForm.notes"
            type="textarea"
            :rows="4"
            placeholder="请输入计划备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="planSubmitLoading" @click="submitPlanForm">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="planDetailVisible"
      title="计划详情"
      size="540px"
      destroy-on-close
    >
      <div v-loading="planDetailLoading" class="detail-panel">
        <template v-if="planDetailData">
          <div class="detail-section">
            <div class="section-title">基础信息</div>
            <div class="detail-grid">
              <div class="detail-item"><span class="label">计划ID</span><span class="value">{{ planDetailData.id || '-' }}</span></div>
              <div class="detail-item"><span class="label">用户ID</span><span class="value">{{ planDetailData.userId || '-' }}</span></div>
              <div class="detail-item"><span class="label">计划名称</span><span class="value">{{ planDetailData.planName || '-' }}</span></div>
              <div class="detail-item"><span class="label">计划类型</span><span class="value">{{ formatPlanType(planDetailData.planType) }}</span></div>
              <div class="detail-item"><span class="label">周期类型</span><span class="value">{{ formatCycleType(planDetailData.cycleType) }}</span></div>
              <div class="detail-item"><span class="label">计划状态</span><span class="value">{{ planDetailData.status || '-' }}</span></div>
              <div class="detail-item"><span class="label">开始日期</span><span class="value">{{ formatDate(planDetailData.startDate) }}</span></div>
              <div class="detail-item"><span class="label">结束日期</span><span class="value">{{ formatDate(planDetailData.endDate) }}</span></div>
              <div class="detail-item"><span class="label">目标</span><span class="value">{{ planDetailData.targetGoal || '-' }}</span></div>
              <div class="detail-item"><span class="label">来源</span><span class="value">{{ planDetailData.sourceType || '-' }}</span></div>
              <div class="detail-item"><span class="label">创建时间</span><span class="value">{{ formatDateTime(planDetailData.createdAt) }}</span></div>
              <div class="detail-item"><span class="label">更新时间</span><span class="value">{{ formatDateTime(planDetailData.updatedAt) }}</span></div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">备注信息</div>
            <div class="detail-grid single-column">
              <div class="detail-item">
                <span class="label">备注</span>
                <span class="value">{{ planDetailData.notes || '暂无备注' }}</span>
              </div>
            </div>
          </div>
        </template>

        <el-empty v-else-if="!planDetailLoading" description="暂无详情数据" />
      </div>
    </el-drawer>

    <el-drawer
      v-model="itemsDrawerVisible"
      :title="`${currentPlan?.planName || ''} - 计划明细`"
      size="80%"
      destroy-on-close
    >
      <div class="items-container">
        <div class="items-toolbar">
          <div class="items-summary">
            <span>计划类型：{{ formatPlanType(currentPlan?.planType) }}</span>
            <span>周期：{{ formatDate(currentPlan?.startDate) }} 至 {{ formatDate(currentPlan?.endDate) }}</span>
          </div>
          <el-button type="primary" @click="handleAddItem">新增明细项</el-button>
        </div>

        <el-table v-loading="itemsLoading" :data="planItems" border stripe class="items-table">
          <el-table-column label="排序" width="80" align="center">
            <template #default="{ row }">
              {{ row.sortNo ?? '-' }}
            </template>
          </el-table-column>
          <el-table-column label="类型" min-width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.itemType === 'EXERCISE' ? 'primary' : 'success'" effect="light">
                {{ formatItemType(row.itemType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetName" label="对象名称" min-width="150" show-overflow-tooltip />
          <el-table-column label="执行日期" min-width="120" align="center">
            <template #default="{ row }">
              {{ formatDate(row.itemDate) }}
            </template>
          </el-table-column>
          <el-table-column label="执行时间" min-width="100" align="center">
            <template #default="{ row }">
              {{ row.itemTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="训练/饮食指标" min-width="230">
            <template #default="{ row }">
              <span v-if="row.itemType === 'EXERCISE'">
                {{ formatCount(row.setsCount, '组') }} / {{ formatCount(row.repsCount, '次') }} / {{ formatCount(row.durationMin, '分钟') }}
              </span>
              <span v-else>
                {{ formatDecimal(row.intakeGrams, 'g') }} / {{ formatCount(row.calories, 'kcal') }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="完成状态" min-width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="getItemCompletionType(row.completionStatus)" effect="light">
                {{ row.completionStatus || '待执行' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" min-width="260" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button size="small" type="success" plain @click="handleOpenComplete(row)">打卡</el-button>
                <el-button size="small" type="primary" plain @click="handleEditItem(row)">编辑</el-button>
                <el-popconfirm
                  title="确认删除该计划项吗？"
                  confirm-button-text="确认"
                  cancel-button-text="取消"
                  @confirm="handleDeleteItem(row)"
                >
                  <template #reference>
                    <el-button size="small" type="danger" plain>删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!itemsLoading && planItems.length === 0" description="当前计划暂无明细项" />
      </div>
    </el-drawer>

    <el-dialog
      v-model="itemDialogVisible"
      :title="itemDialogTitle"
      width="820px"
      append-to-body
      destroy-on-close
      @closed="resetItemForm"
    >
      <el-form
        ref="itemFormRef"
        :model="itemForm"
        :rules="itemRules"
        label-width="100px"
        class="plan-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="明细类型" prop="itemType">
              <el-select v-model="itemForm.itemType" placeholder="请选择类型" class="w-full" @change="handleItemTypeChange">
                <el-option label="训练项" value="EXERCISE" />
                <el-option label="饮食项" value="FOOD" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联对象" prop="targetId">
              <el-select v-model="itemForm.targetId" placeholder="请选择对象" filterable class="w-full">
                <el-option
                  v-for="item in currentTargetOptions"
                  :key="item.id"
                  :label="item.label"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="执行日期">
              <el-date-picker
                v-model="itemForm.itemDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择执行日期"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="执行时间">
              <el-time-picker
                v-model="itemForm.itemTime"
                value-format="HH:mm:ss"
                placeholder="请选择执行时间"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="itemForm.itemType === 'EXERCISE'">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="组数">
                <el-input-number v-model="itemForm.setsCount" :min="0" class="w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="次数">
                <el-input-number v-model="itemForm.repsCount" :min="0" class="w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="时长">
                <el-input-number v-model="itemForm.durationMin" :min="0" class="w-full" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <template v-else>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="摄入克数">
                <el-input-number v-model="itemForm.intakeGrams" :precision="2" :step="1" :min="0" class="w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="热量">
                <el-input-number v-model="itemForm.calories" :min="0" class="w-full" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序号">
              <el-input-number v-model="itemForm.sortNo" :min="0" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="完成状态">
              <el-select
                v-model="itemForm.completionStatus"
                placeholder="请选择或输入状态"
                clearable
                filterable
                allow-create
                default-first-option
                class="w-full"
              >
                <el-option
                  v-for="item in itemCompletionOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="itemForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入明细备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="itemSubmitLoading" @click="submitItemForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="completeDialogVisible"
      title="计划项打卡"
      width="520px"
      append-to-body
      destroy-on-close
      @closed="resetCompleteForm"
    >
      <el-form ref="completeFormRef" :model="completeForm" :rules="completeRules" label-width="100px">
        <el-form-item label="完成状态" prop="completionStatus">
          <el-radio-group v-model="completeForm.completionStatus">
            <el-radio label="已完成">已完成</el-radio>
            <el-radio label="未完成">未完成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="打卡备注">
          <el-input
            v-model="completeForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入打卡备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="completeSubmitLoading" @click="submitCompleteForm">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '../../store/auth'
import { isAdminByRole } from '../../utils/auth'
import { getExerciseList } from '../../api/exercise'
import { getFoodList } from '../../api/food'
import { getAdminUserDetail } from '../../api/user'
import {
  completePlanItem,
  createPlanItem,
  deletePlan,
  deletePlanItem,
  generatePlan,
  getAdminPlanList,
  getPlanDetail,
  getPlanItems,
  getPlanList,
  updatePlan,
  updatePlanItem
} from '../../api/plan'

const authStore = useAuthStore()
const isAdmin = computed(() => isAdminByRole(authStore.roleCode))

const targetGoalOptions = ['增肌', '减脂', '塑形', '提高体能', '维持体重', '健康饮食']
const planStatusOptions = ['待开始', '进行中', '已完成', '已取消']
const itemCompletionOptions = ['待执行', '已完成', '未完成']

const createDefaultQueryParams = () => ({
  pageNum: 1,
  pageSize: 10,
  userId: '',
  phone: '',
  planType: '',
  status: '',
  dateRange: []
})

const createDefaultPlanForm = () => ({
  id: undefined,
  planType: 'TRAINING',
  planName: '',
  cycleType: 'WEEKLY',
  startDate: '',
  endDate: '',
  targetGoal: '',
  sourceType: 'SYSTEM',
  status: '',
  notes: ''
})

const createDefaultItemForm = () => ({
  id: undefined,
  itemType: 'EXERCISE',
  targetId: undefined,
  itemDate: '',
  itemTime: '',
  setsCount: undefined,
  repsCount: undefined,
  durationMin: undefined,
  intakeGrams: undefined,
  calories: undefined,
  completionStatus: '',
  sortNo: undefined,
  remark: ''
})

const createDefaultCompleteForm = () => ({
  itemId: undefined,
  completionStatus: '已完成',
  remark: ''
})

const loading = ref(false)
const planSubmitLoading = ref(false)
const planDetailLoading = ref(false)
const itemsLoading = ref(false)
const itemSubmitLoading = ref(false)
const completeSubmitLoading = ref(false)

const planDialogVisible = ref(false)
const planDetailVisible = ref(false)
const itemsDrawerVisible = ref(false)
const itemDialogVisible = ref(false)
const completeDialogVisible = ref(false)

const planDialogTitle = ref('生成计划')
const planList = ref([])
const userNameMap = ref({})
const total = ref(0)
const planDetailData = ref(null)
const currentPlan = ref(null)
const planItems = ref([])
const exerciseOptions = ref([])
const foodOptions = ref([])

const planFormRef = ref(null)
const itemFormRef = ref(null)
const completeFormRef = ref(null)

const queryParams = reactive(createDefaultQueryParams())
const planForm = reactive(createDefaultPlanForm())
const itemForm = reactive(createDefaultItemForm())
const completeForm = reactive(createDefaultCompleteForm())

const isEditingPlan = computed(() => Boolean(planForm.id))
const currentTargetOptions = computed(() => {
  const source = itemForm.itemType === 'FOOD' ? foodOptions.value : exerciseOptions.value
  return source.map((item) => ({
    id: item.id,
    label: item.foodName || item.exerciseName || item.targetName || `ID:${item.id}`
  }))
})

const planRules = {
  planType: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  cycleType: [{ required: true, message: '请选择周期类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

const itemRules = {
  itemType: [{ required: true, message: '请选择明细类型', trigger: 'change' }],
  targetId: [{ required: true, message: '请选择关联对象', trigger: 'change' }]
}

const completeRules = {
  completionStatus: [{ required: true, message: '请选择完成状态', trigger: 'change' }]
}

const normalizePageData = (payload) => {
  return {
    list: payload?.list || payload?.records || payload?.rows || [],
    total: Number(payload?.total || 0)
  }
}

const normalizeListData = (payload) => {
  return payload?.list || payload?.records || payload?.rows || payload || []
}

const formatUserDisplayName = (user, userId) => {
  return user?.realName || user?.nickname || user?.username || String(userId)
}

const loadUserNameMap = async (list) => {
  if (!isAdmin.value) return

  const userIds = [...new Set(list.map((item) => item.userId).filter(Boolean))]
  const needLoadIds = userIds.filter((id) => !userNameMap.value[id])

  await Promise.all(
    needLoadIds.map(async (id) => {
      try {
        const user = await getAdminUserDetail(id)
        userNameMap.value[id] = formatUserDisplayName(user, id)
      } catch (error) {
        userNameMap.value[id] = String(id)
      }
    })
  )
}

const buildQueryParams = () => {
  const params = {
    pageNum: queryParams.pageNum,
    pageSize: queryParams.pageSize
  }

  if (queryParams.planType) params.planType = queryParams.planType
  if (queryParams.status) params.status = queryParams.status

  if (isAdmin.value) {
    const phone = queryParams.phone.trim()
    const userId = queryParams.userId.trim()
    if (phone) params.phone = phone
    if (userId) params.userId = userId
  } else if (Array.isArray(queryParams.dateRange) && queryParams.dateRange.length === 2) {
    params.startDate = queryParams.dateRange[0]
    params.endDate = queryParams.dateRange[1]
  }

  return params
}

const buildPlanPayload = () => {
  const payload = {
    planName: planForm.planName.trim(),
    cycleType: planForm.cycleType,
    startDate: planForm.startDate,
    endDate: planForm.endDate
  }

  if (!isEditingPlan.value) payload.planType = planForm.planType
  if (planForm.targetGoal) payload.targetGoal = planForm.targetGoal
  if (planForm.sourceType) payload.sourceType = planForm.sourceType
  if (planForm.status) payload.status = planForm.status
  if (planForm.notes.trim()) payload.notes = planForm.notes.trim()

  return payload
}

const buildItemPayload = () => {
  const payload = {
    itemType: itemForm.itemType,
    targetId: itemForm.targetId
  }

  if (itemForm.itemDate) payload.itemDate = itemForm.itemDate
  if (itemForm.itemTime) payload.itemTime = itemForm.itemTime
  if (itemForm.setsCount !== undefined && itemForm.setsCount !== null) payload.setsCount = itemForm.setsCount
  if (itemForm.repsCount !== undefined && itemForm.repsCount !== null) payload.repsCount = itemForm.repsCount
  if (itemForm.durationMin !== undefined && itemForm.durationMin !== null) payload.durationMin = itemForm.durationMin
  if (itemForm.intakeGrams !== undefined && itemForm.intakeGrams !== null) payload.intakeGrams = itemForm.intakeGrams
  if (itemForm.calories !== undefined && itemForm.calories !== null) payload.calories = itemForm.calories
  if (itemForm.completionStatus) payload.completionStatus = itemForm.completionStatus
  if (itemForm.sortNo !== undefined && itemForm.sortNo !== null) payload.sortNo = itemForm.sortNo
  if (itemForm.remark.trim()) payload.remark = itemForm.remark.trim()

  return payload
}

const getPlans = async () => {
  loading.value = true
  try {
    const requestApi = isAdmin.value ? getAdminPlanList : getPlanList
    const res = await requestApi(buildQueryParams())
    const { list, total: totalCount } = normalizePageData(res)
    planList.value = list
    total.value = totalCount
    loadUserNameMap(list)
  } catch (error) {
    console.error('获取计划列表失败:', error)
  } finally {
    loading.value = false
  }
}

const getPlanItemsData = async (planId) => {
  itemsLoading.value = true
  try {
    const res = await getPlanItems(planId)
    planItems.value = normalizeListData(res)
  } catch (error) {
    console.error('获取计划明细失败:', error)
  } finally {
    itemsLoading.value = false
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
  getPlans()
}

const resetQuery = () => {
  Object.assign(queryParams, createDefaultQueryParams())
  getPlans()
}

const handleSizeChange = (value) => {
  queryParams.pageSize = value
  queryParams.pageNum = 1
  getPlans()
}

const handleCurrentChange = (value) => {
  queryParams.pageNum = value
  getPlans()
}

const getRowNumber = (index) => {
  return (queryParams.pageNum - 1) * queryParams.pageSize + index + 1
}

const resetPlanForm = () => {
  Object.assign(planForm, createDefaultPlanForm())
  if (planFormRef.value) {
    planFormRef.value.clearValidate()
  }
}

const handleGeneratePlan = () => {
  resetPlanForm()
  planDialogTitle.value = '生成计划'
  planDialogVisible.value = true
}

const handleEditPlan = (row) => {
  resetPlanForm()
  Object.assign(planForm, {
    id: row.id,
    planType: row.planType || 'TRAINING',
    planName: row.planName || '',
    cycleType: row.cycleType || 'WEEKLY',
    startDate: row.startDate || '',
    endDate: row.endDate || '',
    targetGoal: row.targetGoal || '',
    sourceType: row.sourceType || 'SYSTEM',
    status: row.status || '',
    notes: row.notes || ''
  })
  planDialogTitle.value = '编辑计划'
  planDialogVisible.value = true
}

const submitPlanForm = async () => {
  if (!planFormRef.value) return

  try {
    await planFormRef.value.validate()
    planSubmitLoading.value = true
    const payload = buildPlanPayload()

    if (isEditingPlan.value) {
      await updatePlan(planForm.id, payload)
    } else {
      await generatePlan(payload)
    }

    ElMessage.success(`${planDialogTitle.value}成功`)
    planDialogVisible.value = false
    await getPlans()
  } catch (error) {
    if (error?.name !== 'Error') {
      console.error(`${planDialogTitle.value}失败:`, error)
    }
  } finally {
    planSubmitLoading.value = false
  }
}

const handleDeletePlan = async (row) => {
  try {
    await deletePlan(row.id)
    ElMessage.success('删除成功')
    if (planList.value.length === 1 && queryParams.pageNum > 1) {
      queryParams.pageNum -= 1
    }
    await getPlans()
  } catch (error) {
    console.error('删除计划失败:', error)
  }
}

const handleViewPlan = async (row) => {
  planDetailVisible.value = true
  planDetailLoading.value = true
  planDetailData.value = null

  try {
    if (isAdmin.value) {
      planDetailData.value = row
    } else {
      const res = await getPlanDetail(row.id)
      planDetailData.value = res || null
    }
  } catch (error) {
    console.error('获取计划详情失败:', error)
  } finally {
    planDetailLoading.value = false
  }
}

const handleManageItems = async (row) => {
  currentPlan.value = row
  itemsDrawerVisible.value = true
  await getPlanItemsData(row.id)
}

const resetItemForm = () => {
  Object.assign(itemForm, createDefaultItemForm())
  if (itemFormRef.value) {
    itemFormRef.value.clearValidate()
  }
}

const handleItemTypeChange = () => {
  itemForm.targetId = undefined
  itemForm.setsCount = undefined
  itemForm.repsCount = undefined
  itemForm.durationMin = undefined
  itemForm.intakeGrams = undefined
  itemForm.calories = undefined
}

const handleAddItem = () => {
  resetItemForm()
  if (currentPlan.value?.planType === 'DIET') {
    itemForm.itemType = 'FOOD'
  }
  if (currentPlan.value?.planType === 'TRAINING') {
    itemForm.itemType = 'EXERCISE'
  }
  itemDialogTitle.value = '新增明细项'
  itemDialogVisible.value = true
}

const itemDialogTitle = ref('新增明细项')

const handleEditItem = (row) => {
  resetItemForm()
  Object.assign(itemForm, {
    id: row.id,
    itemType: row.itemType || 'EXERCISE',
    targetId: row.targetId,
    itemDate: row.itemDate || '',
    itemTime: row.itemTime || '',
    setsCount: row.setsCount ?? undefined,
    repsCount: row.repsCount ?? undefined,
    durationMin: row.durationMin ?? undefined,
    intakeGrams: row.intakeGrams ?? undefined,
    calories: row.calories ?? undefined,
    completionStatus: row.completionStatus || '',
    sortNo: row.sortNo ?? undefined,
    remark: row.remark || ''
  })
  itemDialogTitle.value = '编辑明细项'
  itemDialogVisible.value = true
}

const submitItemForm = async () => {
  if (!itemFormRef.value || !currentPlan.value) return

  try {
    await itemFormRef.value.validate()
    itemSubmitLoading.value = true
    const payload = buildItemPayload()

    if (itemForm.id) {
      await updatePlanItem(itemForm.id, payload)
    } else {
      await createPlanItem(currentPlan.value.id, payload)
    }

    ElMessage.success(`${itemDialogTitle.value}成功`)
    itemDialogVisible.value = false
    await getPlanItemsData(currentPlan.value.id)
  } catch (error) {
    if (error?.name !== 'Error') {
      console.error(`${itemDialogTitle.value}失败:`, error)
    }
  } finally {
    itemSubmitLoading.value = false
  }
}

const handleDeleteItem = async (row) => {
  if (!currentPlan.value) return
  try {
    await deletePlanItem(row.id)
    ElMessage.success('删除成功')
    await getPlanItemsData(currentPlan.value.id)
  } catch (error) {
    console.error('删除计划项失败:', error)
  }
}

const resetCompleteForm = () => {
  Object.assign(completeForm, createDefaultCompleteForm())
  if (completeFormRef.value) {
    completeFormRef.value.clearValidate()
  }
}

const handleOpenComplete = (row) => {
  resetCompleteForm()
  completeForm.itemId = row.id
  completeForm.completionStatus = row.completionStatus === '已完成' ? '已完成' : '未完成'
  completeDialogVisible.value = true
}

const submitCompleteForm = async () => {
  if (!completeFormRef.value || !currentPlan.value) return

  try {
    await completeFormRef.value.validate()
    completeSubmitLoading.value = true
    await completePlanItem(completeForm.itemId, {
      completionStatus: completeForm.completionStatus,
      remark: completeForm.remark.trim()
    })
    ElMessage.success('打卡成功')
    completeDialogVisible.value = false
    await getPlanItemsData(currentPlan.value.id)
  } catch (error) {
    if (error?.name !== 'Error') {
      console.error('计划项打卡失败:', error)
    }
  } finally {
    completeSubmitLoading.value = false
  }
}

const formatPlanType = (value) => {
  if (value === 'TRAINING') return '训练计划'
  if (value === 'DIET') return '饮食计划'
  return value || '-'
}

const formatCycleType = (value) => {
  if (value === 'DAILY') return '每日'
  if (value === 'WEEKLY') return '每周'
  return value || '-'
}

const formatItemType = (value) => {
  if (value === 'EXERCISE') return '训练项'
  if (value === 'FOOD') return '饮食项'
  return value || '-'
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

const getItemCompletionType = (value) => {
  if (value === '已完成') return 'success'
  if (value === '未完成') return 'danger'
  return 'warning'
}

onMounted(() => {
  getExerciseOptions()
  getFoodOptions()
  getPlans()
})
</script>

<style scoped>
.plan-page {
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

.plan-table,
.items-table {
  width: 100%;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.plan-form {
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

.items-container {
  min-height: 300px;
}

.items-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.items-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  color: #606266;
  font-size: 14px;
}

@media (max-width: 992px) {
  .action-buttons {
    margin-left: 0;
  }
}

@media (max-width: 768px) {
  .page-header,
  .section-header,
  .items-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
