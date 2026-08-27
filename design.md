# 前后端分离系统搭建方案

**项目名称**：用户管理、登录与菜单管理系统  
**技术栈**：Spring Boot 3 + JDK21 + Vue3 + Element Plus + MySQL  

---

## 1. 总体架构

系统采用前后端分离架构，后端提供 RESTful API，前端负责页面渲染与交互。认证方式采用 JWT 无状态令牌。
┌─────────────┐ HTTP/JSON ┌─────────────┐
│ Vue3 前端 │ ───────────────▶ │ Spring Boot │
│ (Vite + │ │ 后端服务 │
│ Element+) │ ◀─────────────── │ (JDK21) │
└─────────────┘ └──────┬──────┘
│
┌──────▼──────┐
│ MySQL │
│ (可加Redis) │
└─────────────┘

text

- **前端**：Vue3 + TypeScript + Vite + Element Plus + Pinia + Vue Router + Axios  
- **后端**：Spring Boot 3.x + Spring Security + JWT + MyBatis-Plus + MySQL + Redis（可选）  
- **认证方式**：JWT 无状态认证，前端存储 Token，请求头携带 `Authorization: Bearer <token>`

---

## 2. 后端设计

### 2.1 技术选型

| 技术               | 说明                                   |
|--------------------|----------------------------------------|
| Spring Boot 3.2.x  | 基础框架，要求 JDK17+，我们使用 JDK21   |
| Spring Security    | 安全框架，处理认证和授权                |
| JWT (jjwt)         | 生成和解析 Token                        |
| MyBatis-Plus       | ORM 框架，简化 CRUD                    |
| MySQL 8.x          | 主数据库                               |
| Redis（可选）      | 缓存 Token、验证码等                   |
| Lombok             | 简化实体类代码                         |
| Hutool             | 常用工具类库                           |

### 2.2 项目结构
backend/
├── pom.xml
├── src/main/java/com/example/system
│ ├── SystemApplication.java
│ ├── config/ # 配置类
│ │ ├── SecurityConfig.java
│ │ ├── MybatisPlusConfig.java
│ │ └── CorsConfig.java
│ ├── controller/ # 控制层
│ │ ├── AuthController.java
│ │ ├── UserController.java
│ │ └── MenuController.java
│ ├── service/ # 业务层
│ │ ├── UserService.java
│ │ ├── MenuService.java
│ │ └── impl/...
│ ├── mapper/ # 数据访问层
│ ├── entity/ # 实体类
│ │ ├── User.java
│ │ ├── Role.java
│ │ ├── Menu.java
│ │ ├── UserRole.java
│ │ └── RoleMenu.java
│ ├── dto/ # 数据传输对象
│ │ ├── LoginRequest.java
│ │ ├── LoginResponse.java
│ │ └── UserDTO.java
│ ├── security/ # 安全相关
│ │ ├── JwtUtil.java
│ │ ├── JwtAuthenticationFilter.java
│ │ └── UserDetailsServiceImpl.java
│ └── common/ # 通用返回结果、异常处理
│ ├── Result.java
│ └── GlobalExceptionHandler.java
└── src/main/resources
├── application.yml
└── mapper/*.xml # MyBatis XML 映射文件

text

### 2.3 数据库设计（RBAC 5张表）

```sql
-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,        -- BCrypt加密
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    status TINYINT DEFAULT 1,              -- 1启用 0禁用
    create_time DATETIME,
    update_time DATETIME
);

-- 角色表
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

-- 菜单表（含权限标识）
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    menu_name VARCHAR(50) NOT NULL,
    path VARCHAR(200),                     -- 前端路由地址
    component VARCHAR(200),                -- 前端组件路径
    perms VARCHAR(100),                    -- 权限标识，如 system:user:list
    icon VARCHAR(50),
    type TINYINT,                          -- 0目录 1菜单 2按钮
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME
);

-- 用户角色关联表
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);
2.4 核心功能实现要点
2.4.1 用户登录（认证）
使用 Spring Security 的 AuthenticationManager 进行用户名密码验证。

验证成功后，生成 JWT Token，包含用户ID、用户名、角色权限等信息。

返回 Token 及用户基本信息给前端。

java
// AuthController
@PostMapping("/login")
public Result<LoginResponse> login(@RequestBody LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String token = jwtUtil.generateToken(userDetails);
    // 查询用户信息
    User user = userService.getByUsername(userDetails.getUsername());
    return Result.success(new LoginResponse(token, user));
}
2.4.2 用户管理（CRUD）
提供用户的增删改查、分页查询、重置密码、分配角色等功能。

密码使用 BCryptPasswordEncoder 加密存储。

分配角色时，操作 sys_user_role 表。

2.4.3 菜单管理
菜单分为目录、菜单、按钮三种类型。

前端根据用户角色动态生成路由和菜单（通过接口返回菜单树）。

后端提供菜单的增删改查，以及根据用户ID查询其可见菜单树的接口。

java
// MenuController
@GetMapping("/user/menus")
public Result<List<Menu>> getUserMenus() {
    // 从 SecurityContext 获取当前用户ID
    Long userId = SecurityUtils.getCurrentUserId();
    return Result.success(menuService.selectMenusByUserId(userId));
}
2.4.4 权限控制
使用 @PreAuthorize("hasAuthority('system:user:list')") 注解进行方法级权限控制。

在 SecurityConfig 中配置无状态会话、放行登录接口、其他接口需认证。

自定义 JwtAuthenticationFilter 解析 Token，将用户权限信息设置到 SecurityContext。

3. 前端设计
3.1 技术选型
Vue 3 + TypeScript

Vite 构建工具

Element Plus UI组件库

Pinia 状态管理（存储用户信息、Token、动态路由等）

Vue Router 路由管理（动态路由）

Axios 请求封装（拦截器添加Token、统一错误处理）

3.2 项目结构
text
frontend/
├── package.json
├── vite.config.ts
├── index.html
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── api/                  # 接口定义
│   │   ├── auth.ts
│   │   ├── user.ts
│   │   └── menu.ts
│   ├── router/
│   │   ├── index.ts          # 静态路由
│   │   └── dynamic.ts        # 动态路由生成逻辑
│   ├── stores/
│   │   ├── user.ts           # 用户信息、Token
│   │   └── permission.ts     # 动态路由/菜单
│   ├── layout/               # 布局组件
│   │   ├── index.vue
│   │   ├── Sidebar.vue
│   │   ├── Navbar.vue
│   │   └── ...
│   ├── views/                # 页面
│   │   ├── login/index.vue
│   │   ├── dashboard/index.vue
│   │   ├── system/user/index.vue
│   │   └── system/menu/index.vue
│   ├── utils/
│   │   ├── request.ts        # Axios 封装
│   │   └── auth.ts           # Token 存取
│   └── styles/
└── ...
3.3 关键实现
3.3.1 登录流程
用户输入用户名密码，调用 /api/auth/login 接口。

获取 Token 和用户信息，存入 Pinia 和 localStorage。

跳转到首页，并拉取用户菜单树。

根据菜单树动态添加路由（router.addRoute）。

3.3.2 动态路由与菜单
后端返回菜单树结构（目录、菜单），前端递归生成 RouteRecordRaw[]。

将动态路由添加到路由实例，并生成侧边栏菜单。

typescript
// 动态路由生成
function generateRoutes(menus: Menu[]): RouteRecordRaw[] {
  return menus.map(menu => {
    const route: RouteRecordRaw = {
      path: menu.path,
      name: menu.menuName,
      component: () => import(`@/views/${menu.component}.vue`),
      meta: { title: menu.menuName, icon: menu.icon }
    };
    if (menu.children && menu.children.length > 0) {
      route.children = generateRoutes(menu.children);
    }
    return route;
  });
}
3.3.3 请求拦截器
每次请求自动在 Header 中加入 Authorization: Bearer <token>。

响应拦截器处理 401（跳转登录页）、500 等错误。

4. 接口联调示例
功能	方法	路径	说明
登录	POST	/api/auth/login	返回Token
获取当前用户信息	GET	/api/auth/info	用户基本信息、角色、权限
获取用户菜单	GET	/api/menus/user	当前用户可见菜单树
用户分页查询	GET	/api/users?page=1&size=10	分页参数
新增用户	POST	/api/users	用户数据
修改用户	PUT	/api/users/{id}	
删除用户	DELETE	/api/users/{id}	
分配角色	PUT	/api/users/{id}/roles	角色ID列表
菜单列表	GET	/api/menus	全量菜单树
新增菜单	POST	/api/menus	
修改菜单	PUT	/api/menus/{id}	
删除菜单	DELETE	/api/menus/{id}	
