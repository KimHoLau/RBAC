import { request } from '@/utils/request'
import type { MenuPayload, MenuItem } from '@/types/api'

/** 当前用户可见的菜单树（仅目录+菜单） */
export function getUserMenus(): Promise<MenuItem[]> {
  return request<MenuItem[]>({ url: '/api/menus/user', method: 'get' })
}

/** 全量菜单树（含按钮，管理用） */
export function getMenuTree(): Promise<MenuItem[]> {
  return request<MenuItem[]>({ url: '/api/menus', method: 'get' })
}

export function createMenu(data: MenuPayload): Promise<void> {
  return request<void>({ url: '/api/menus', method: 'post', data })
}

export function updateMenu(id: number, data: MenuPayload): Promise<void> {
  return request<void>({ url: `/api/menus/${id}`, method: 'put', data })
}

export function deleteMenu(id: number): Promise<void> {
  return request<void>({ url: `/api/menus/${id}`, method: 'delete' })
}
