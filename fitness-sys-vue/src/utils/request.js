import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth, getToken } from './auth'

const service = axios.create({
  baseURL: import.meta.env.DEV ? '' : import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const res = response?.data ?? {}
    const code = Number(res.code)
    const message = res.message || '请求失败'
    if (code === 200) return res
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
  (error) => {
    const status = error?.response?.status
    const message = error?.response?.data?.message || error?.message || '网络错误'

    if (status === 401) {
      clearAuth()
      if (location.pathname !== '/login') location.href = '/login'
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service

export function request(config) {
  return service(config).then((res) => res.data)
}
