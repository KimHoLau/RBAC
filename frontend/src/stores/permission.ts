import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteComponent, RouteRecordRaw } from 'vue-router'

import type { MenuItem } from '@/types/api'

// Vite 要求静态可分析的 glob：此处收集全部视图组件，运行时按 component 字符串匹配
const viewModules = import.meta.glob('../views/**/*.vue')

function resolveComponent(component?: string | null): (() => Promise<RouteComponent>) | undefined {
  if (!component) return undefined
  for (const [key, loader] of Object.entries(viewModules)) {
    if (key.endsWith(`/views/${component}.vue`)) {
      return loader as () => Promise<RouteComponent>
    }
  }
  return undefined
}

function routeName(path: string): string {
  return path
    .replace(/^\//, '')
    .split('/')
    .filter(Boolean)
    .map(seg => seg.charAt(0).toUpperCase() + seg.slice(1))
    .join('')
}

function collect(list: MenuItem[], acc: RouteRecordRaw[]): void {
  for (const menu of list) {
    if (menu.status === 1 && menu.type !== 0 && menu.path && menu.component) {
      const component = resolveComponent(menu.component)
      if (component) {
        acc.push({
          path: menu.path,
          name: routeName(menu.path),
          component,
          meta: { title: menu.menuName, icon: menu.icon ?? undefined }
        })
      }
    }
    if (menu.children?.length) {
      collect(menu.children, acc)
    }
  }
}

export const usePermissionStore = defineStore('permission', () => {
  const menus = ref<MenuItem[]>([])
  const routesGenerated = ref(false)

  function setMenus(tree: MenuItem[]): void {
    menus.value = tree ?? []
    routesGenerated.value = true
  }

  /** 菜单树 → Layout 下的扁平子路由（绝对路径） */
  function generateRoutes(): RouteRecordRaw[] {
    const acc: RouteRecordRaw[] = []
    collect(menus.value, acc)
    return acc
  }

  function reset(): void {
    menus.value = []
    routesGenerated.value = false
  }

  return { menus, routesGenerated, setMenus, generateRoutes, reset }
})
