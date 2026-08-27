import { request } from '@/utils/request'
import type {
  PageResult,
  UserCreatePayload,
  UserItem,
  UserUpdatePayload
} from '@/types/api'

export function pageUsers(params: {
  page: number
  size: number
  keyword?: string
}): Promise<PageResult<UserItem>> {
  return request<PageResult<UserItem>>({ url: '/api/users', method: 'get', params })
}

export function createUser(data: UserCreatePayload): Promise<void> {
  return request<void>({ url: '/api/users', method: 'post', data })
}

export function updateUser(id: number, data: UserUpdatePayload): Promise<void> {
  return request<void>({ url: `/api/users/${id}`, method: 'put', data })
}

export function deleteUser(id: number): Promise<void> {
  return request<void>({ url: `/api/users/${id}`, method: 'delete' })
}

export function resetUserPassword(id: number): Promise<void> {
  return request<void>({ url: `/api/users/${id}/password`, method: 'put' })
}

export function assignUserRoles(id: number, roleIds: number[]): Promise<void> {
  return request<void>({ url: `/api/users/${id}/roles`, method: 'put', data: roleIds })
}
