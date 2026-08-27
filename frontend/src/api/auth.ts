import { request } from '@/utils/request'
import type { AuthInfo, LoginResponse } from '@/types/api'

export function login(data: { username: string; password: string }): Promise<LoginResponse> {
  return request<LoginResponse>({ url: '/api/auth/login', method: 'post', data })
}

export function getAuthInfo(): Promise<AuthInfo> {
  return request<AuthInfo>({ url: '/api/auth/info', method: 'get' })
}
