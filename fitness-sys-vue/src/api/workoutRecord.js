import { request } from '../utils/request'

// 新增训练记录
export function createWorkoutRecord(data) {
  return request({
    url: '/api/v1/workout-records',
    method: 'post',
    data
  })
}

// 分页查询训练记录列表
export function getWorkoutRecordList(params) {
  return request({
    url: '/api/v1/workout-records',
    method: 'get',
    params
  })
}

// 查询训练记录详情
export function getWorkoutRecordDetail(id) {
  return request({
    url: `/api/v1/workout-records/${id}`,
    method: 'get'
  })
}

// 修改训练记录
export function updateWorkoutRecord(id, data) {
  return request({
    url: `/api/v1/workout-records/${id}`,
    method: 'put',
    data
  })
}

// 删除训练记录
export function deleteWorkoutRecord(id) {
  return request({
    url: `/api/v1/workout-records/${id}`,
    method: 'delete'
  })
}
