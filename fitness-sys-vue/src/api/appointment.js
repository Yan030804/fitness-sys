import { request } from '../utils/request'

// 创建预约（用户）
export function createAppointment(data) {
  return request({
    url: '/api/v1/appointments',
    method: 'post',
    data
  })
}

// 查看我的预约列表（用户）
export function getMyAppointmentList(params) {
  return request({
    url: '/api/v1/appointments/my',
    method: 'get',
    params
  })
}

// 查看预约详情（用户）
export function getAppointmentDetail(id) {
  return request({
    url: `/api/v1/appointments/${id}`,
    method: 'get'
  })
}

// 修改预约信息（用户）
export function updateAppointment(id, data) {
  return request({
    url: `/api/v1/appointments/${id}`,
    method: 'put',
    data
  })
}

// 取消预约（用户）
export function cancelAppointment(id, data) {
  return request({
    url: `/api/v1/appointments/${id}/cancel`,
    method: 'put',
    data
  })
}

// 管理员查看全部预约
export function getAdminAppointmentList(params) {
  return request({
    url: '/api/v1/admin/appointments',
    method: 'get',
    params
  })
}

// 管理员查看预约详情
export function getAdminAppointmentDetail(id) {
  return request({
    url: `/api/v1/admin/appointments/${id}`,
    method: 'get'
  })
}

// 管理员修改预约状态
export function updateAdminAppointmentStatus(id, data) {
  return request({
    url: `/api/v1/admin/appointments/${id}/status`,
    method: 'put',
    data
  })
}
