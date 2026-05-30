import { request } from '../utils/request'

// 分页查询食材列表（用户端）
export function getFoodList(params) {
  return request({
    url: '/api/v1/foods',
    method: 'get',
    params
  })
}

// 查询食材详情
export function getFoodDetail(id) {
  return request({
    url: `/api/v1/foods/${id}`,
    method: 'get'
  })
}

// 分页查询食材列表（管理端）
export function getAdminFoodList(params) {
  return request({
    url: '/api/v1/admin/foods',
    method: 'get',
    params
  })
}

// 新增食材（管理端）
export function createFood(data) {
  return request({
    url: '/api/v1/admin/foods',
    method: 'post',
    data
  })
}

// 修改食材（管理端）
export function updateFood(id, data) {
  return request({
    url: `/api/v1/admin/foods/${id}`,
    method: 'put',
    data
  })
}

// 删除食材（管理端）
export function deleteFood(id) {
  return request({
    url: `/api/v1/admin/foods/${id}`,
    method: 'delete'
  })
}
