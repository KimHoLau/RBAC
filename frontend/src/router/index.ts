import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

import { getUserMenus } from '@/api/menu'
import { usePermissionStore } from '@/stores/permission'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 白名单：无需登录即可访问
const WHITE_LIST = new Set(['/login'])

router.beforeEach(async to => {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()

  if (!getToken()) {
    return WHITE_LIST.has(to.path) ? true : { path: '/login', query: { redirect: to.fullPath } }
  }

  if (to.path === '/login') {
    return true
  }

  // 已登录但尚未加载用户信息：拉取信息 + 菜单树，并动态注册路由
  if (!userStore.infoLoaded) {
    try {
      await userStore.fetchUserInfo()
      permissionStore.setMenus(await getUserMenus())
      permissionStore.generateRoutes().forEach(route => {
        // 与静态路由同名（如种子数据中的仪表盘）时以静态声明为准，避免重复注册
        if (route.name && !router.hasRoute(route.name)) {
          router.addRoute('Layout', route)
        }
      })
      // 重新解析当前目标，确保新注册的路由生效
      return { ...to, replace: true }
    } catch {
      userStore.logoutLocal()
      permissionStore.reset()
      return { path: '/login' }
    }
  }

  return true
})

router.afterEach(to => {
  document.title = to.meta?.title ? `${String(to.meta.title)} - RBAC 管理系统` : 'RBAC 管理系统'
})

export default router
