# FRONTEND — Vue 3 RBAC Admin SPA

OVERVIEW: Vue 3.5 + TypeScript + Vite 7 + Element Plus + Pinia + Vue Router 4 + Axios。Node 22/npm 10。

## STRUCTURE

```
src/
├── main.ts               # 入口
├── api/                  # auth/user/role/menu.ts —— 一个后端域一个模块
├── router/index.ts       # 静态路由 + beforeEach 守卫（核心机制见下）
├── stores/               # user.ts(Token/info)、permission.ts(菜单树/动态路由)
├── layout/               # index.vue + Navbar/Sidebar/SidebarItem —— 同一菜单树递归渲染侧边栏
├── views/                # login、dashboard、error/404、system/{user,menu}
├── utils/                # auth.ts(localStorage Token)、request.ts(axios 封装)
├── directives/           # v-perms 按钮级权限指令
└── types/api.d.ts        # ApiResult 等共享类型
```

## 核心机制：动态路由

守卫白名单仅 `/login`。已登录且 `infoLoaded=false` 时：拉 `/api/auth/info` → 拉 `/api/menus/user` 菜单树 → `permissionStore.generateRoutes()`（经 `import.meta.glob` 把菜单 component 字符串映射为组件）→ 逐条 `router.addRoute('Layout', route)`（同名静态路由优先，`router.hasRoute` 去重）→ `{ ...to, replace: true }` 重解析。失败则本地登出回登录页。

## 关键约定（新代码必读）

- **request.ts 会解包信封**：响应拦截器剥掉 `Result{code,message,data}` 外壳，所有 `api/*` 函数直接返回业务 `data`；code≠200 抛 Error 并弹 ElMessage，401 自动清 Token 跳登录。**业务代码不要再碰 `response.data.code`。**
- 按钮/元素显隐控制用 `v-perms="'system:user:add'"`（依据 auth/info 返回的 perms 集合）。
- Token 存 localStorage（`utils/auth.ts`），请求拦截器自动加 `Bearer` 头。
- 构建即类型门禁：`npm run build` 先跑 `vue-tsc --noEmit`，零容忍错误。

## ANTI-PATTERNS (THIS PROJECT)

- **注册边界已有类型转换注释**：request.ts 里拦截器签名是刻意 `as unknown as` 转换（解包型），别"修好"它，也别在业务层模仿此转换。
- 新页面组件路径必须能被 `import.meta.glob` 规则命中且与菜单表 component 字段一致，否则动态路由挂载失败表现为空白/404。
- 不要把新路由写死进静态表——属于权限的页面一律走菜单树下发。

## GOTCHA

开发期请同时起后端 :8080（vite proxy `/api` 指向它）；无后端时任何页面刷新都会落到登录页（info 加载失败即登出）。
