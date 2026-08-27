# PROJECT KNOWLEDGE BASE

**Generated:** 2026-08-27
**Commit:** 32b5102 (main)
**Branch:** main

## OVERVIEW

前后端分离的 RBAC 权限管理系统：Spring Boot 4 无状态 JWT API（`backend/`）+ Vue 3 SPA（`frontend/`）。实现以 [design.md](./design.md) 为蓝本，但 3 处技术选型以实施计划为准：PostgreSQL（非 MySQL）、Spring Boot 4.0.8（非 3.2）、Spring Data JPA（非 MyBatis-Plus）。

## STRUCTURE

```
RBAC/
├── design.md                          # 原始设计文档（与 plans 冲突时以后者为准）
├── docs/superpowers/plans/            # 实施计划；"API Contract" 节是前后端唯一对接契约
├── backend/                           # Spring Boot 工程，JDK 21，包根 com.example.system
│   ├── db/init.sql                    # PostgreSQL 建表+种子数据；schema 唯一来源
│   └── src/main/java/com/example/system/
└── frontend/                          # Vue 3 + TS + Vite 7 SPA
    └── src/{api,router,stores,layout,views,...}
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| 接口路径/请求体/响应契约 | `docs/superpowers/plans/2026-08-26-rbac-fullstack.md` → "API Contract" | 双方不得偏离 |
| 权限标识（perms）定义 | 同上 Global Constraints 节 | `system:user:add` 等格式 |
| 数据库表结构/种子数据 | `backend/db/init.sql` | 建库后手工执行一次 |
| 认证链路 | `backend/src/.../security/` + `config/SecurityConfig.java` | 见 backend/AGENTS.md |
| 动态路由机制 | `frontend/src/router/index.ts` 守卫 | 见 frontend/AGENTS.md |

## CODE MAP

来自 codegraph 索引（`.codegraph/` 存在，可用 `codegraph_explore` 查询本项目）。

| Symbol | Type | Location | Refs | Role |
|--------|------|----------|------|------|
| SystemApplication | class | backend/.../SystemApplication.java | - | 后端入口 |
| SecurityFilterChain bean | method | backend/.../config/SecurityConfig.java | 高 | STATELESS 安全链，仅放行 `/api/auth/login` |
| JwtAuthenticationFilter | class | backend/.../security/JwtAuthenticationFilter.java | 3 callers | Bearer 解析 → SecurityContext |
| JwtUtil | class | backend/.../security/JwtUtil.java | 核心依赖 | jjwt 0.13 签发/校验 |
| RestAuthenticationEntryPoint | class | backend/.../security/RestAuthenticationEntryPoint.java | SecurityConfig | 未认证返回 `{code:401}` JSON |
| router.beforeEach | guard | frontend/src/router/index.ts | 高 | 拉菜单树 + `router.addRoute('Layout', ...)` |
| request() | fn | frontend/src/utils/request.ts | 所有 api/* | 解包 Result 信封的业务数据 |
| Result<T> | class | backend/.../common/Result.java | 全部 controller | `{code,message,data}` 统一响应 |

## CONVENTIONS

- 响应信封：`Result{code:number, message:string, data}`；成功=200，未认证=401，无权限=403，参数错=400，服务器错=500。HTTP 层不用状态码表达业务错误语义。
- 鉴权：方法级 `@PreAuthorize("hasAuthority('system:user:list')")`；权限串格式 `<域>:<资源>:<动作>`。
- 配置外置：`DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD`、`JWT_SECRET/JWT_EXPIRE_HOURS` 均可环境变量覆盖。
- 注释、界面文案、提交说明多为中文。

## ANTI-PATTERNS (THIS PROJECT)

- **不要启用 Hibernate 建表**：`ddl-auto: none` 是刻意的，schema 由 `init.sql` 管理。
- **不要引入计划外依赖**（计划明令：不使用 Redis，不加未列依赖）。
- **测试不得依赖数据库连接**——验证门槛是 `mvn verify` 与 `npm run build` 在无 DB 环境全绿。
- 无 ESLint/Prettier/Checkstyle 配置：改动须贴合既有风格而非依赖 linter 兜底。

## COMMANDS

```bash
# 数据库初始化（先执行）
psql -U postgres -c "CREATE DATABASE rbac_system;"
psql -U postgres -d rbac_system -f backend/db/init.sql

# 后端 :8080（要求 JDK 21）
cd backend && mvn spring-boot:run     # 运行
cd backend && mvn clean verify        # 构建+测试（无需数据库）

# 前端 :5173（dev 已配 /api → localhost:8080 代理）
cd frontend && npm install && npm run dev
cd frontend && npm run build          # vue-tsc 类型检查 + vite build
```

种子账号：`admin / admin123`。

## NOTES

- **README 与实际默认值不一致**：README 称数据库默认口令 `postgres/postgres`，但 `application.yml` 中 `DB_PASSWORD` 默认实为 `123456`。连不上库先查这里。
- 动态路由与静态路由同名（如仪表盘）时，静态声明优先，守卫用 `router.hasRoute(name)` 去重。
