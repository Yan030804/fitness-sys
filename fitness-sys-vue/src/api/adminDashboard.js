import { request } from '../utils/request'

export function fetchAdminMe() {
  return request({ url: '/api/v1/auth/me', method: 'get' })
}

export function fetchAdminUsersPage(params) {
  return request({ url: '/api/v1/admin/users', method: 'get', params })
}

export function fetchAdminExercisesPage(params) {
  return request({ url: '/api/v1/admin/exercises', method: 'get', params })
}

export function fetchAdminFoodsPage(params) {
  return request({ url: '/api/v1/admin/foods', method: 'get', params })
}

export function fetchAdminPlansPage(params) {
  return request({ url: '/api/v1/admin/plans', method: 'get', params })
}

export function fetchAdminRecommendationsPage(params) {
  return request({ url: '/api/v1/admin/recommendations', method: 'get', params })
}

export function fetchAdminAppointmentsPage(params) {
  return request({ url: '/api/v1/admin/appointments', method: 'get', params })
}

export function updateAppointmentStatus(id, data) {
  return request({ url: `/api/v1/admin/appointments/${id}/status`, method: 'put', data })
}

export function normalizePagePayload(payload) {
  return {
    list: payload?.list || payload?.records || payload?.rows || [],
    total: Number(payload?.total ?? payload?.count ?? 0)
  }
}
