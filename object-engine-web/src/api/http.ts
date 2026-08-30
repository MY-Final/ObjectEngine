import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/api'
import { clearToken, getToken } from '@/constants/auth'

/** 扩展配置：silent=true 时错误不弹 ElMessage，由调用方自行处理 */
export interface RequestConfig extends AxiosRequestConfig {
  silent?: boolean
}

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 15000,
})

// 请求统一携带登录 token（Sa-Token 按 Authorization 请求头读取）
instance.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

/**
 * 统一处理后端 { code, message, data } 响应：
 * - code=200 时把 data 直接返回给调用方
 * - 400/404/500 等 HTTP 错误通过 ElMessage 提示 message（silent 请求除外）
 */
instance.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return body.data as unknown as typeof response
      }
      if (!(response.config as RequestConfig | undefined)?.silent) {
        ElMessage.error(body.message || '请求失败')
      }
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return response
  },
  (error) => {
    // 登录过期：清 token 并跳登录页（当前已在登录页时仅提示，避免循环跳转）
    if (error.response?.status === 401) {
      clearToken()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }
    if (!(error.config as RequestConfig | undefined)?.silent) {
      const body = error.response?.data as ApiResponse<unknown> | undefined
      if (body && typeof body === 'object' && 'message' in body) {
        ElMessage.error(body.message || '请求失败')
      } else {
        ElMessage.error('网络异常，请稍后重试')
      }
    }
    return Promise.reject(error)
  },
)

/**
 * 响应拦截器已解包统一响应体，方法签名按“直接返回 data 部分”声明，
 * 业务侧调用形如 http.get<PageResult<CustomObject>>(...)
 */
interface Http {
  get<T>(url: string, config?: RequestConfig): Promise<T>
  post<T>(url: string, data?: unknown, config?: RequestConfig): Promise<T>
  put<T>(url: string, data?: unknown, config?: RequestConfig): Promise<T>
  delete<T>(url: string, config?: RequestConfig): Promise<T>
}

export default instance as unknown as Http
