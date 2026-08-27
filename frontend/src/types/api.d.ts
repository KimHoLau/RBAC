/** 统一响应包装：后端 Result<T> */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

/** 菜单/目录/按钮节点（树形） */
export interface MenuItem {
  id: number
  parentId: number
  menuName: string
  path?: string | null
  component?: string | null
  perms?: string | null
  icon?: string | null
  /** 0 目录 1 菜单 2 按钮 */
  type: number
  sort: number
  status: number
  createTime?: string | null
  children: MenuItem[]
}

/** 分页结果 */
export interface PageResult<T> {
  list: T[]
  total: number
}

/** 用户列表项（对应后端 UserDTO） */
export interface UserItem {
  id: number
  username: string
  nickname?: string | null
  email?: string | null
  phone?: string | null
  status: number
  createTime?: string | null
  roleIds?: number[] | null
}

/** 登录响应 */
export interface LoginResponse {
  token: string
  userInfo: UserItem
}

/** 当前用户信息（对应后端 AuthInfoVO） */
export interface AuthInfo {
  userId: number
  username: string
  nickname?: string | null
  email?: string | null
  phone?: string | null
  roles: string[]
  perms: string[]
}

/** 角色选项 */
export interface RoleItem {
  id: number
  roleName: string
  roleCode: string
  description?: string | null
  status: number
}

/** 菜单新增/编辑表单（对应后端 MenuRequest） */
export interface MenuPayload {
  parentId: number
  menuName: string
  path?: string | null
  component?: string | null
  perms?: string | null
  icon?: string | null
  type: number
  sort?: number | null
  status?: number | null
}

/** 用户新增表单 */
export interface UserCreatePayload {
  username: string
  nickname?: string | null
  email?: string | null
  phone?: string | null
  password: string
  status?: number | null
}

/** 用户编辑表单（不含密码，username 仅回显） */
export interface UserUpdatePayload {
  username: string
  nickname?: string | null
  email?: string | null
  phone?: string | null
  status?: number | null
}
