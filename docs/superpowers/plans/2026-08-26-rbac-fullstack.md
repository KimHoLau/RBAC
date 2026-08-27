# RBAC 全栈系统 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按 `design.md` 搭建用户管理/登录/菜单管理的 RBAC 前后端分离系统（后端 API + 前端管理界面）。

**Architecture:** 前后端分离。后端 Spring Boot 4 无状态 RESTful API，JWT 认证，Spring Data JPA 访问 PostgreSQL；前端 Vue3 SPA，Pinia 存 Token/菜单，路由守卫按后端菜单树 `router.addRoute` 动态挂路由。

**Tech Stack:** Spring Boot 4.0.8 · JDK 21 · Spring Security 7 · Spring Data JPA · Hibernate · PostgreSQL · jjwt 0.13.0 · Lombok · Hutool 5.8.47 ‖ Vue 3 · TypeScript · Vite · Element Plus · Pinia · Vue Router 4 · Axios

**Spec:** `/mnt/d/openCodeProject/RBAC/design.md`（本计划在其基础上做了 3 处用户指定的调整：① 数据库 MySQL → **PostgreSQL**；② Spring Boot 3.2 → **4.0**；③ MyBatis-Plus → **Spring Data JPA**。冲突处以本计划为准。）

## Global Constraints

- 后端基础包名：`com.example.system`，工程目录：`backend/`
- 前端工程目录：`frontend/`，Node 22 / npm 10 可用
- JDK 21（`<java.version>21</java.version>`），Maven 3.9.16
- Spring Boot parent：`spring-boot-starter-parent:4.0.8`（Maven Central 已验证存在）
- 数据库：PostgreSQL 14+，库名 `rbac_system`；驱动 `org.postgresql:postgresql`（BOM 管理版本）
- ORM：Spring Data JPA（`spring-boot-starter-data-jpa`），`ddl-auto: none`，表结构由 `db/init.sql` 手工初始化
- 所有响应统一 `Result<T>`：`{ code:number, message:string, data:T }`；成功 code=200；401 未认证、403 无权限、400 参数错误、500 服务器错误
- 认证：请求头 `Authorization: Bearer <token>`；JWT 含 uid/username/authorities，有效期配置化（默认 24h）
- 接口路径与设计文档 §4 表格完全一致（全部以 `/api` 开头）
- 密码：BCrypt；初始管理员 `admin/admin123`；重置密码默认值 `123456`
- 权限标识格式 `system:user:list|add|edit|delete|resetPwd|assignRole`、`system:menu:list|add|edit|delete`；方法级控制用 `@PreAuthorize("hasAuthority('...')")`
- 不使用 Redis（设计文档标记可选）；不引入其他未列依赖
- 验证门槛（无本地数据库）：后端 `mvn test` 全绿且不依赖 DB 连接；前端 `npm run build`（vue-tsc + vite build）零错误

## API Contract（前端/后端唯一对接面，双方不得偏离）

| 功能 | 方法 | 路径 | 权限标识 | 请求体 | 响应 data |
|---|---|---|---|---|---|
| 登录 | POST | `/api/auth/login` | 匿名 | `{username,password}` | `{token, userInfo}` |
| 当前用户信息 | GET | `/api/auth/info` | 登录即可 | - | `{userId,username,nickname,roles:string[],perms:string[]}` |
| 用户菜单树 | GET | `/api/menus/user` | 登录即可 | - | `MenuItem[]`（树，仅目录+菜单） |
| 用户分页 | GET | `/api/users?page=1&size=10&keyword=` | `system:user:list` | - | `PageResult<UserItem>{list,total}` |
| 新增用户 | POST | `/api/users` | `system:user:add` | `{username,nickname,email,phone,password,status}` | `null` |
| 修改用户 | PUT | `/api/users/{id}` | `system:user:edit` | 同上（无 password） | `null` |
| 删除用户 | DELETE | `/api/users/{id}` | `system:user:delete` | - | `null` |
| 重置密码 | PUT | `/api/users/{id}/password` | `system:user:resetPwd` | - | `null` |
| 分配角色 | PUT | `/api/users/{id}/roles` | `system:user:assignRole` | `[1,2]`（角色ID数组） | `null` |
| 角色列表 | GET | `/api/roles` | 登录即可 | - | `[{id,roleName,roleCode}]`（供分配角色下拉） |
| 菜单全量树 | GET | `/api/menus` | `system:menu:list` | - | `MenuItem[]`（含按钮） |
| 新增菜单 | POST | `/api/menus` | `system:menu:add` | MenuItem 字段 | `null` |
| 修改菜单 | PUT | `/api/menus/{id}` | `system:menu:edit` | MenuItem 字段 | `null` |
| 删除菜单 | DELETE | `/api/menus/{id}` | `system:menu:delete` | - | `null` |

共享类型：
```typescript
interface MenuItem { id:number; parentId:number; menuName:string; path?:string|null;
  component?:string|null; perms?:string|null; icon?:string|null;
  type:0|1|2; sort:number; status:0|1; children:MenuItem[] }
interface PageResult<T> { list:T[]; total:number }
```

---

## Track A：后端（backend/）

### 文件清单

```
backend/
├── pom.xml
├── db/init.sql                                  # PG 建库建表 + 种子数据
└── src/main/java/com/example/system/
    ├── SystemApplication.java
    ├── config/    SecurityConfig.java  CorsConfig.java
    ├── security/  JwtUtil.java  JwtAuthenticationFilter.java  RestAuthenticationEntryPoint.java
    │              UserDetailsServiceImpl.java  SecurityUtils.java  LoginUser.java
    ├── common/    Result.java  PageResult.java  BusinessException.java  GlobalExceptionHandler.java
    ├── entity/    User.java  Role.java  Menu.java  UserRole.java  RoleMenu.java
    ├── repository/ UserRepository.java  RoleRepository.java  MenuRepository.java
    │               UserRoleRepository.java  RoleMenuRepository.java
    ├── dto/       LoginRequest.java  LoginResponse.java  UserDTO.java  AssignRolesRequest.java
    │              UserUpsertRequest.java  MenuRequest.java  AuthInfoVO.java
    ├── service/   UserService.java  MenuService.java  RoleService.java
    │   └── impl/  UserServiceImpl.java  MenuServiceImpl.java  RoleServiceImpl.java
    └── controller/ AuthController.java  UserController.java  MenuController.java  RoleController.java
backend/src/main/resources/application.yml
backend/src/test/java/com/example/system/security/JwtUtilTest.java
backend/src/test/java/com/example/system/security/PasswordHashTest.java
```

### Task A1: 工程骨架 + 配置

**Files:** pom.xml、SystemApplication.java、application.yml、CorsConfig.java

- [ ] pom.xml：parent `spring-boot-starter-parent:4.0.8`；依赖 `starter-web/starter-security/starter-validation/starter-data-jpa/starter-test`、`org.postgresql:postgresql`(runtime)、`io.jsonwebtoken:jjwt-api:0.13.0` + `jjwt-impl:0.13.0`(runtime) + `jjwt-jackson:0.13.0`(runtime)、`cn.hutool:hutool-all:5.8.47`、lombok(optional)；`maven-surefire-plugin` 由 BOM 管理
- [ ] application.yml：port 8080；datasource `jdbc:postgresql://localhost:5432/rbac_system`（user/pass 默认 postgres/postgres，可用环境变量覆盖）；jpa `ddl-auto: none`、`open-in-view: false`；自定义 `jwt.secret`（≥32字符）、`jwt.expire-hours: 24`
- [ ] CorsConfig：允许 `http://localhost:5173`，方法全放行，允许 Authorization 头

**Gate:** `mvn -q compile` 绿。

### Task A2: 通用层 + 安全层

**Files:** common/* 、security/*

关键签名（后续任务依赖）：
```java
public class Result<T> { private int code; private String message; private T data;
  public static <T> Result<T> success(T data); public static <T> Result<T> error(int code,String msg); }
public class BusinessException extends RuntimeException { private final int code; } // 默认500
public class JwtUtil {
  public String generateToken(Long userId, String username, Collection<String> authorities);
  public Claims parseToken(String token);            // 失效/伪造抛 JwtException
  public boolean isExpired(String token);
}
public class LoginUser implements UserDetails {     // 持有 User 实体 + Set<String> perms
  public Long getUserId();
}
public final class SecurityUtils { public static Long getCurrentUserId(); public static String getCurrentUsername(); }
```

- [ ] GlobalExceptionHandler：`MethodArgumentNotValidException`→400 取第一条字段消息；`BadCredentialsException`/`UsernameNotFoundException`→401“用户名或密码错误”；`BusinessException`→自带 code；`AccessDeniedException`→403“没有权限”；兜底 `Exception`→500
- [ ] RestAuthenticationEntryPoint：未认证访问受保护接口返回 200 包裹体 `{code:401,...}`（前端拦截器据此跳登录）
- [ ] JwtAuthenticationFilter extends OncePerRequestFilter：取 Bearer→parseToken→loadUserByUsername 构建 `UsernamePasswordAuthenticationToken(perms 为 authorities)` 放入 context；解析失败不阻断链路（由 EntryPoint 兜底）
- [ ] SecurityConfig：`@EnableMethodSecurity`；`SecurityFilterChain`：csrf disable、cors 引用、session STATELESS、`/api/auth/login` permitAll、其余 authenticated；`AuthenticationManager` 来自 `AuthenticationConfiguration`；`PasswordEncoder` = BCryptPasswordEncoder

### Task A3: 实体 + 仓储 + DTO

- [ ] 五个实体映射 `sys_*` 表；主键 `@GeneratedValue(strategy=IDENTITY)`；时间戳用 Hibernate `@CreationTimestamp/@UpdateTimestamp`；`User.password` 加 `@JsonIgnore`（响应永不泄露密码）
- [ ] 关联表复合主键：`UserRole(@IdClass)` 或 `@EmbeddedId`（userId,roleId），`RoleMenu` 同理 —— 与 `db/init.sql` 的联合主键一致
- [ ] UserRepository：`Optional<User> findByUsername(String)`、`boolean existsByUsername(String)`；
  分页关键字查询用 `@Query`：`select u from User u where :keyword is null or lower(u.username) like lower(concat('%',:keyword,'%')) or lower(coalesce(u.nickname,'')) like lower(concat('%',:keyword,'%'))`，返回 `Page<User>`
- [ ] MenuRepository：`List<Menu> findByStatusOrderBySortAsc()`；
  按用户查菜单：`@Query("select distinct m from Menu m join RoleMenu rm on rm.menuId=m.id where rm.roleId in (select ur.roleId from UserRole ur where ur.userId=:userId) and m.status=1 order by m.sort")`
- [ ] RoleMenuRepository：`List<RoleMenu> findByRoleId(Long)`、`deleteByRoleId(Long)`；UserRoleRepository 对称
- [ ] DTO 校验注解齐全（LoginRequest/UserUpsertRequest `@NotBlank` 等）

### Task A4: 服务层 + 控制器

- [ ] UserService：page(keyword,page,size)（页码转 `PageRequest.of(page-1,size)`）、create（查重 username→BCrypt 加密）、update（不改密码/用户名）、delete、assignRoles(id,List<Long>)（先删后插 sys_user_role）、resetPassword(id,"123456")
- [ ] MenuService：getUserMenuTree(userId)（过滤 type=2 按钮→buildTree）、getFullTree()、create/update/remove（有子节点或已关联角色时禁止删除→BusinessException）
- [ ] buildTree 私有工具：按 parentId 分组递归，根为 parentId=0，children 按 sort 升序
- [ ] 四个 Controller 按【API Contract】表逐行实现；写操作全部 `@PreAuthorize`；AuthController.login 流程遵循 design.md §2.4.1（authenticationManager.authenticate → generateToken → 返回 LoginResponse）

**Gate:** `mvn -q compile` 绿。

### Task A5: 初始化 SQL + 测试

- [ ] `db/init.sql`：`CREATE DATABASE rbac_system;` 注释形式给出 psql 命令；5 张表 PG 方言——`BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY`（替代 AUTO_INCREMENT）、`SMALLINT`（替代 TINYINT）、`TIMESTAMP`（替代 DATETIME）；关联表联合主键；索引 `sys_menu(parent_id)`
- [ ] 种子数据：角色 ROLE_ADMIN/ROLE_USER；菜单树（仪表盘 /dashboard/dashboard/index；系统管理目录；用户管理 system/user/index + 6 个按钮权限；菜单管理 system/menu/index + 4 个按钮权限）；admin 用户（BCrypt 常量哈希）+ 全量授权关联
- [ ] `PasswordHashTest`：断言 init.sql 中硬编码的哈希 `matches("admin123")`（先在测试中生成一次，回填 init.sql，再固化常量断言）
- [ ] `JwtUtilTest`：生成→解析 round-trip 断言 claims；篡改 token 抛异常；过期判定正确（构造 expire=-1h 的 util 实例）

**Gate:** `mvn -q test` 全绿（不连数据库）。

---

## Track B：前端（frontend/）

### 文件清单

```
frontend/
├── package.json  vite.config.ts  tsconfig.json  tsconfig.node.json  index.html  .env.development
├── src/main.ts  src/App.vue
├── src/types/api.d.ts                    # Result<T>/MenuItem/PageResult/UserItem/RoleItem/LoginResponse/AuthInfo
├── src/utils/auth.ts                     # getToken/setToken/removeToken (localStorage 'rbac_token')
├── src/utils/request.ts                  # axios 实例：请求头带 token；resp.code!==200→ElMessage.error+reject；
│                                         # code===401→清 token→router.push('/login')
├── src/api/auth.ts  src/api/user.ts  src/api/menu.ts  src/api/role.ts
├── src/stores/user.ts                    # token/userInfo/roles/perms；actions: login/logout/fetchUserInfo
├── src/stores/permission.ts              # menus 树 + routesGenerated 标志 + generateRoutes()
├── src/router/index.ts                   # 静态路由 + 全局守卫 + 动态挂载
├── src/layout/index.vue  Sidebar.vue  Navbar.vue
└── src/views/login/index.vue  dashboard/index.vue  system/user/index.vue  system/menu/index.vue
```

### Task B1: 骨架 + 基础设施

- [ ] `npm create vite`（vue-ts 模板手写等价文件亦可）；依赖：element-plus、pinia、vue-router、axios；devDeps 保持模板 + `@types/node`；scripts：`dev/build/preview`，build = `vue-tsc -b && vite build`
- [ ] vite.config.ts：`base:'/'`；server.port 5173、host true；proxy `'/api'→http://localhost:8080` 不重写
- [ ] main.ts：createPinia、ElementPlus（完整引入+中文 locale）、router、全局样式
- [ ] request.ts：`timeout:15000`；请求拦截器注入 `Authorization`；响应拦截器按 Global Constraints 处理 code!==200 / HTTP 错误 / 401 跳登录

### Task B2: 类型 + API + Store + Router

- [ ] types/api.d.ts 与【API Contract】逐字段对齐（字段名完全一致，camelCase）
- [ ] api/*.ts：函数名 `login/getAuthInfo/getUserMenus/pageUsers/createUser/updateUser/deleteUser/resetUserPassword/assignUserRoles/listRoles/getMenuTree/createMenu/updateMenu/deleteMenu`，路径严格按 Contract
- [ ] stores/user.ts：login→存 token→fetchUserInfo；logout→清 pinia+localStorage+强制跳 /login
- [ ] permission.ts 动态路由核心（注意 Vite 下不能用模板字符串动态 import，必须用 glob 映射）：
```typescript
const modules = import.meta.glob('@/views/**/*.vue')
export function generateRoutes(menus: MenuItem[]): RouteRecordRaw[] {
  // 仅处理 type 0/1；type 0 且 children 非空→带 Layout 子路由结构；
  // component: modules[`/src/views/${menu.component}.vue`] ?? NotFound 占位；
  // meta:{title,icon,perms}；children 递归；name 用 path 驼峰化保证唯一
}
```
- [ ] router/index.ts：静态：`/login`；`/`→Layout(children: /dashboard 静态声明)；catch-all 404 在动态路由添加完成后再 push；全局守卫：白名单 /login；有 token 无 userInfo→fetchUserInfo+getUserMenus→generateRoutes→`forEach(addRoute)`→`next({...to,replace:true})`；失败→登出

### Task B3: 布局 + 登录页 + 仪表盘

- [ ] layout/index.vue：el-container（aside 侧边栏可折叠 + header + main router-view 过渡动画）
- [ ] Sidebar.vue：el-menu（router 模式，default-active=当前路由）+ 递归子组件渲染目录/菜单；图标用 `<component :is="icon"/>` 注册的 @element-plus/icons-vue 动态渲染
- [ ] Navbar.vue：面包屑（route.meta.title）、右侧 el-dropdown（昵称头像、退出登录二次确认）
- [ ] login/index.vue：居中卡片表单、rules 校验、loading 态、回车提交、成功 ElMessage+跳转
- [ ] dashboard/index.vue：欢迎卡片 + 用户信息展示（演示用，简洁即可）

### Task B4: 用户管理页 + 菜单管理页

- [ ] system/user/index.vue：搜索栏（关键字/重置）；el-table（分页 el-pagination current-page/total/page-size 对接 PageResult）；新增/编辑共用 el-dialog 表单（校验、status 开关）；删除气泡确认；重置密码确认框；分配角色 dialog（el-select multiple，选项来自 listRoles()）；按钮 v-perms 自定义指令按 `perms` 数组隐藏无权按钮
- [ ] system/menu/index.vue：el-table 树形（row-key=id、tree-props children、默认全展开）；类型列 tag（0目录/1菜单/2按钮）；新增/编辑 dialog：类型 radio、上级菜单 el-tree-select、path/component/perms/icon/sort/status 按类型联动显示；删除确认（提示含子节点不可删）
- [ ] 指令 `v-perms`：directives/perms.ts 读 stores/user.perms，无权限则 removeChild

**Gate:** `npm run install` 成功；`npm run build`（vue-tsc 类型检查 + vite build）零错误。

---

## Self-Review 结论

- 设计覆盖：§2 后端全部要点（登录/CRUD/重置密码/分配角色/菜单树/@PreAuthorize/无状态安全）✓；§3 前端全部要点（拦截器/Pinia/动态路由/布局/页面）✓；§4 接口表逐行落在 Contract ✓；偏离项 3 个已注明（PG/SB4/JPA）+ 新增 `/api/roles`（分配角色功能必需）✓
- 类型一致性：Contract 中 `MenuItem/PageResult` 与前端 types、后端 VO 字段一致 ✓
- 占位符扫描：无 TBD/TODO；BCrypt 哈希通过测试生成回填（Task A5 明确流程）✓

## Execution Handoff

Track A 与 Track B 无文件交集，仅靠【API Contract】耦合 → 并行派发两个实现代理；完成后分别跑 Gate 验证；最后补根 README。
