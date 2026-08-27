import { request } from '@/utils/request'
import type { RoleItem } from '@/types/api'

/** 角色下拉选项 */
export function listRoles(): Promise<RoleItem[]> {
  return request<RoleItem[]>({ url: '/api/roles', method: 'get' })
}

/** 角色已授权的菜单 ID（分配对话框回显用） */
export function getRoleMenuIds(id: number): Promise<number[]> {
  return request<number[]>({ url: `/api/roles/${id}/menus`, method: 'get' })
}

/** 覆盖式保存角色授权菜单（以传入集合为最终状态） */
export function assignRoleMenus(id: number, menuIds: number[]): Promise<void> {
  return request<void>({ url: `/api/roles/${id}/menus`, method: 'put', data: menuIds })
}
