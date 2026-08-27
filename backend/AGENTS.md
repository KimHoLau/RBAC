# BACKEND — Spring Boot 4 RBAC API

OVERVIEW: JDK 21 · Spring Boot 4.0.8 · Spring Security 7 · Spring Data JPA · PostgreSQL · jjwt 0.13 · Lombok · Hutool。无状态 RESTful，全部路由以 `/api` 开头。

## STRUCTURE

```
src/main/java/com/example/system/
├── SystemApplication.java   # 入口
├── common/                  # Result 信封、PageResult、BusinessException、全局异常处理器
├── config/                  # SecurityConfig（过滤链/AuthenticationManager/BCrypt）、CorsConfig
├── controller/              # Auth/User/Role/Menu —— 仅编组，逻辑在 service
├── dto/                     # LoginRequest、UserUpsertRequest… 命名混用 *Request/*VO/*DTO
├── entity/                  # JPA 实体 + 复合主键类（UserRole·UserRoleId, RoleMenu·RoleMenuId）
├── repository/              # Spring Data JPA 接口，每实体一个
├── security/                # JWT 全套：JwtUtil、JwtAuthenticationFilter、LoginUser(UserDetails)、
│                            #   UserDetailsServiceImpl、RestAuthenticationEntryPoint、SecurityUtils
└── service/                 # 接口 + impl/ 两层结构
db/init.sql                  # schema + 种子数据（repository 外唯一 schema 来源）
src/test/java/.../security/  # JwtUtilTest、PasswordHashTest —— 不依赖 DB 的单测
```

## 认证链路

登录 `AuthController` → `AuthenticationManager` → `JwtUtil.generateToken(uid, username, authorities)`；
后续请求 `JwtAuthenticationFilter`（仅当带 `Authorization: Bearer` 时）→ `parseToken` → `UserDetailsServiceImpl.loadUserByUsername` → 写入 `SecurityContext`。任何 JWT 异常静默清空上下文，由 `RestAuthenticationEntryPoint` 输出 `{code:401}` JSON。

## WHERE TO LOOK

| Task | Location |
|------|----------|
| 新增接口 | controller 加方法 + `@PreAuthorize("hasAuthority(...)")`，返回 `Result.success(data)`；同步更新 plans 文档的 API Contract |
| 业务异常 | throw `BusinessException(code, msg)`，由 `GlobalExceptionHandler` 统一转 Result |
| 当前用户信息 | `SecurityUtils` |
| 改 JWT 密钥/有效期 | `application.yml` jwt.* （支持 env `JWT_SECRET`/`JWT_EXPIRE_HOURS`） |

## CONVENTIONS

- 注入用显式构造器（虽引了 Lombok，安全相关类均手写构造器），字段 final。
- service 先写接口再写 impl（严格三层）。
- 方法级鉴权走 authority 字符串，不用角色名判断。
- CORS 由 `CorsConfig` 提供，过滤链里 `cors(withDefaults)` 引用。

## ANTI-PATTERNS (THIS PROJECT)

- **勿开 `ddl-auto` 非 none 值**；改表先改 `db/init.sql`。
- **实体里的 `Menu.children` 是 transient**：service 层组装、永不持久化。
- 密码只存 BCrypt；重置密码固定默认值 `123456`。
- 测试禁止连库（现有两测试即无 DB 单测范例）。

## GOTCHA

`PasswordHashTest` 断言种子管理员哈希 ↔ `admin123` 匹配：若改 `init.sql` 中 admin 口令哈希，必须同步该测试常量，否则 `mvn verify` 红。
