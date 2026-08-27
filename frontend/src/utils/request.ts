import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

import router from '@/router'
import type { ApiResult } from '@/types/api'
import { getToken, removeToken } from './auth'

const service = axios.create({
  timeout: 15000
})

// 请求拦截器：自动携带 Token
service.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function redirectToLogin(): void {
  removeToken()
  if (router.currentRoute.value.path !== '/login') {
    router.replace('/login')
  }
}

/**
 * 响应处理：将 AxiosResponse 解包为业务 data（本项目请求层约定，
 * 与 axios 默认契约不同，故注册处存在类型边界转换）。
 */
function onResponse(response: AxiosResponse): unknown {
  const body = response.data as ApiResult<unknown>
  if (!body || typeof body.code !== 'number') {
    return body
  }
  if (body.code === 200) {
    return body.data
  }
  if (body.code === 401) {
    ElMessage.error(body.message || '登录已过期，请重新登录')
    redirectToLogin()
    throw new Error(body.message || '登录已过期')
  }
  ElMessage.error(body.message || '请求失败')
  throw new Error(body.message || '请求失败')
}

interface HttpErrorBody {
  message?: string
}

function onError(error: unknown): Promise<never> {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status
    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      redirectToLogin()
    } else {
      const body = error.response?.data as HttpErrorBody | undefined
      ElMessage.error(
        body?.message || (status ? `请求失败（HTTP ${status}）` : '网络异常，请稍后重试')
      )
    }
    return Promise.reject(error)
  }
  ElMessage.error('网络异常，请稍后重试')
  return Promise.reject(error instanceof Error ? error : new Error('请求失败'))
}

// 注册边界：解包型拦截器返回的是业务数据而非 AxiosResponse
service.interceptors.response.use(
  onResponse as unknown as (response: AxiosResponse) => AxiosResponse | Promise<AxiosResponse>,
  onError
)

/** 返回值已被响应拦截器解包为后端 Result.data */
export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const data: unknown = await service.request(config)
  return data as T
}
