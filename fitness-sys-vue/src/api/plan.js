import { request } from '../utils/request'

// 生成计划（用户）
export function generatePlan(data) {
  return request({
    url: '/api/v1/plans/generate',
    method: 'post',
    data
  })
}

// 查询计划列表（用户）
export function getPlanList(params) {
  return request({
    url: '/api/v1/plans',
    method: 'get',
    params
  })
}

// 查看计划详情（用户）
export function getPlanDetail(id) {
  return request({
    url: `/api/v1/plans/${id}`,
    method: 'get'
  })
}

// 修改计划（用户）
export function updatePlan(id, data) {
  return request({
    url: `/api/v1/plans/${id}`,
    method: 'put',
    data
  })
}

// 删除计划（用户）
export function deletePlan(id) {
  return request({
    url: `/api/v1/plans/${id}`,
    method: 'delete'
  })
}

// 查询计划明细（用户）
export function getPlanItems(planId) {
  return request({
    url: `/api/v1/plans/${planId}/items`,
    method: 'get'
  })
}

// 新增计划明细项（用户）
export function createPlanItem(planId, data) {
  return request({
    url: `/api/v1/plans/${planId}/items`,
    method: 'post',
    data
  })
}

// 修改计划明细项（用户）
export function updatePlanItem(itemId, data) {
  return request({
    url: `/api/v1/plans/items/${itemId}`,
    method: 'put',
    data
  })
}

// 打卡完成计划项（用户）
export function completePlanItem(itemId, data) {
  return request({
    url: `/api/v1/plans/items/${itemId}/complete`,
    method: 'post',
    data
  })
}

// 删除计划明细项（用户）
export function deletePlanItem(itemId) {
  return request({
    url: `/api/v1/plans/items/${itemId}`,
    method: 'delete'
  })
}

// 管理员查看全部计划
export function getAdminPlanList(params) {
  return request({
    url: '/api/v1/admin/plans',
    method: 'get',
    params
  })
}
