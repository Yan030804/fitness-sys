import { request } from '../utils/request'

/**
 * 获取动作列表 (用户端)
 * @param {Object} params - pageNum, pageSize, category, bodyPart, difficulty
 */
export const getExerciseList = (params) => {
  return request({
    url: '/api/v1/exercises',
    method: 'get',
    params
  })
}

/**
 * 获取动作详情
 * @param {number|string} id 
 */
export const getExerciseDetail = (id) => {
  return request({
    url: `/api/v1/exercises/${id}`,
    method: 'get'
  })
}

/**
 * 分页查询动作列表 (管理端)
 * @param {Object} params - pageNum, pageSize, exerciseName, category, bodyPart, difficulty, status
 */
export const getAdminExerciseList = (params) => {
  return request({
    url: '/api/v1/admin/exercises',
    method: 'get',
    params
  })
}

/**
 * 新增动作 (管理端)
 * @param {Object} data 
 */
export const createExercise = (data) => {
  return request({
    url: '/api/v1/admin/exercises',
    method: 'post',
    data
  })
}

/**
 * 修改动作 (管理端)
 * @param {number|string} id 
 * @param {Object} data 
 */
export const updateExercise = (id, data) => {
  return request({
    url: `/api/v1/admin/exercises/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除动作 (管理端)
 * @param {number|string} id 
 */
export const deleteExercise = (id) => {
  return request({
    url: `/api/v1/admin/exercises/${id}`,
    method: 'delete'
  })
}
