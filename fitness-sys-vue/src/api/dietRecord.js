import { request } from '../utils/request'

// 新增饮食记录
export function createDietRecord(data) {
  return request({
    url: '/api/v1/diet-records',
    method: 'post',
    data
  })
}

// 分页查询饮食记录列表
export function getDietRecordList(params) {
  return request({
    url: '/api/v1/diet-records',
    method: 'get',
    params
  })
}

// 查询饮食记录详情
export function getDietRecordDetail(id) {
  return request({
    url: `/api/v1/diet-records/${id}`,
    method: 'get'
  })
}

// 修改饮食记录
export function updateDietRecord(id, data) {
  return request({
    url: `/api/v1/diet-records/${id}`,
    method: 'put',
    data
  })
}

// 删除饮食记录
export function deleteDietRecord(id) {
  return request({
    url: `/api/v1/diet-records/${id}`,
    method: 'delete'
  })
}
