import { request } from '../utils/request'

// 生成个性化推荐（用户）
export function generateRecommendations(data) {
  return request({
    url: '/api/v1/recommendations/generate',
    method: 'post',
    data
  })
}

// 查询最新推荐结果（用户）
export function getLatestRecommendations(params) {
  return request({
    url: '/api/v1/recommendations/latest',
    method: 'get',
    params
  })
}

// 查询推荐历史（用户）
export function getRecommendationHistory(params) {
  return request({
    url: '/api/v1/recommendations/history',
    method: 'get',
    params
  })
}

// 查看推荐详情（用户）
export function getRecommendationDetail(id) {
  return request({
    url: `/api/v1/recommendations/${id}`,
    method: 'get'
  })
}

// 管理员查看全部推荐记录
export function getAdminRecommendationList(params) {
  return request({
    url: '/api/v1/admin/recommendations',
    method: 'get',
    params
  })
}
