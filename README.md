# RBAC(Role-Based Access Control) 管理系统

## RBAC
RBAC权限模型（Role-Based Access Control）即：基于角色的权限控制。模型中有几个关键的术语：
- **用户**：系统接口及访问的操作者
- **权限**：能够访问某接口或者做某操作的授权资格
- **角色**：具有一类相同操作权限的用户的总称

## 用户角色权限关系
### 一个用户有一个或多个角色
### 一个角色包含多个用户
### 一个角色有多种权限
### 一个权限属于多个角色

前后端分离的 RBAC 权限管理系统。基于 [design.md](./design.md) 实现，含三处技术调整：

| 调整项 | 设计文档 | 实际采用 |
|---|---|---|
| 数据库 | MySQL 8.x | **PostgreSQL 14+** |
| 后端框架 | Spring Boot 3.2.x | **Spring Boot 4.0.8**（Spring Framework 7 / Security 7）|
| ORM | MyBatis-Plus | **Spring Data JPA** |

## 技术栈

- **后端**：JDK 21 · Spring Boot 4.0.8 · Spring Security 7（JWT 无状态认证，jjwt 0.13）· Spring Data JPA · PostgreSQL · Lombok · Hutool
- **前端**：Vue 3 · TypeScript · Vite 7 · Element Plus · Pinia · Vue Router 4 · Axios
- **认证方式**：登录签发 JWT；前端存 localStorage；请求头携带 `Authorization: Bearer <token>`；后端按菜单表中的权限标识做方法级鉴权（`@PreAuthorize`）

## 目录结构

```
RBAC/
├── design.md                          # 原始设计文档
├── docs/superpowers/plans/            # 实施计划（API 契约以此为准）
├── backend/                           # Spring Boot 工程
│   ├── db/init.sql                    # PostgreSQL 建表 + 种子数据（先执行）
│   └── src/main/java/com/example/system/
└── frontend/                          # Vue3 工程
    └── src/{api,router,stores,layout,views,...}
```

## 快速开始

### 1. 初始化数据库（PostgreSQL）

```bash
psql -U postgres -c "CREATE DATABASE rbac_system;"
psql -U postgres -d rbac_system -f backend/db/init.sql
```

连接信息可用环境变量覆盖（默认 `localhost:5432/rbac_system`，用户/密码 `postgres/postgres`）：
`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD`。

### 2. 启动后端（端口 8080）

```bash
cd backend
mvn spring-boot:run
```

要求 JDK 21。JWT 密钥/有效期同样支持环境变量：`JWT_SECRET`、`JWT_EXPIRE_HOURS`。

### 3. 启动前端（端口 5173）

```bash
cd frontend
npm install
npm run dev
```

开发服务器已配置代理：`/api → http://localhost:8080`。

### 4. 登录

打开 http://localhost:5173 ，使用种子账号：

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 系统管理员（全部权限）|

## 生产构建验证

```bash
# 后端（不依赖数据库即可跑测试）
cd backend && mvn clean verify

# 前端（vue-tsc 类型检查 + vite 构建）
cd frontend && npm run build
```

## 接口一览

完整契约见实施计划文档（docs/superpowers/plans/2026-08-26-rbac-fullstack.md 的 "API Contract"），核心接口：

| 功能 | 方法 | 路径 |
|---|---|---|
| 登录 | POST | `/api/auth/login` |
| 当前用户信息 | GET | `/api/auth/info` |
| 用户菜单树 | GET | `/api/menus/user` |
| 用户分页 | GET | `/api/users?page=1&size=10&keyword=` |
| 新增/修改/删除用户 | POST/PUT/DELETE | `/api/users[/{id}]` |
| 重置密码 | PUT | `/api/users/{id}/password` |
| 分配角色 | PUT | `/api/users/{id}/roles` |
| 角色列表 | GET | `/api/roles` |
| 菜单树管理 | GET/POST/PUT/DELETE | `/api/menus[/{id}]` |

## 关键实现说明

- **动态路由**：登录后前端拉取 `/api/menus/user` 菜单树，经 `import.meta.glob` 映射组件路径，`router.addRoute('Layout', route)` 挂载为布局子路由；侧边栏由同一棵菜单树递归渲染。
- **按钮级权限**：自定义指令 `v-perms="'system:user:add'"`，依据 `/api/auth/info` 返回的 perms 集合控制元素显隐。
- **无状态安全**：`SecurityFilterChain` 全局 STATELESS，仅放行登录接口；未认证请求由 `RestAuthenticationEntryPoint` 返回 `{code:401}` JSON，前端拦截器统一跳转登录页。
- **密码存储**：BCrypt；种子管理员密码哈希与 `PasswordHashTest` 中常量保持一致，测试保证其与 `admin123` 匹配。
