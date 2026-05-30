import { request } from '../utils/request'

// 获取当前用户详细信息
export function getCurrentUserDetail() {
  return request({
    url: '/api/v1/users/me',
    method: 'get'
  })
}

// 修改当前用户信息
export function updateCurrentUser(data) {
  return request({
    url: '/api/v1/users/me',
    method: 'put',
    data
  })
}

// 修改当前用户密码
export function updateCurrentUserPassword(data) {
  return request({
    url: '/api/v1/users/me/password',
    method: 'put',
    data
  })
}

// 分页查询用户列表（管理端）
export function getAdminUserList(params) {
  return request({
    url: '/api/v1/admin/users',
    method: 'get',
    params
  })
}

// 查询用户详情（管理端）
export function getAdminUserDetail(id) {
  return request({
    url: `/api/v1/admin/users/${id}`,
    method: 'get'
  })
}

// 启用/禁用用户（管理端）
export function updateAdminUserStatus(id, data) {
  return request({
    url: `/api/v1/admin/users/${id}/status`,
    method: 'put',
    data
  })
}
