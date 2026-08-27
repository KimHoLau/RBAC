import { request } from '@/utils/request'
import type { RoleItem } from '@/types/api'

/** 角色下拉选项 */
export function listRoles(): Promise<RoleItem[]> {
  return request<RoleItem[]>({ url: '/api/roles', method: 'get' })
}
