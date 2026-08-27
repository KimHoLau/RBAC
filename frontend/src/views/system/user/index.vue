<template>
  <el-card shadow="never" class="page-card">
    <!-- 搜索栏 -->
    <el-form inline @submit.prevent="handleSearch">
      <el-form-item label="关键字">
        <el-input
          v-model.trim="keyword"
          placeholder="用户名 / 昵称"
          clearable
          style="width: 220px"
          @clear="handleSearch"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button v-perms="'system:user:add'" type="primary" @click="openCreate">
        新增用户
      </el-button>
    </div>

    <!-- 用户表格 -->
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="330" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-perms="'system:user:edit'" link type="primary" @click="openEdit(row)">
            编辑
          </el-button>
          <el-button
            v-perms="'system:user:resetPwd'"
            link
            type="warning"
            @click="handleResetPwd(row)"
          >
            重置密码
          </el-button>
          <el-button
            v-perms="'system:user:assignRole'"
            link
            type="primary"
            @click="openRoles(row)"
          >
            分配角色
          </el-button>
          <el-button
            v-perms="'system:user:delete'"
            link
            type="danger"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadData"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 新增 / 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="520px"
      destroy-on-close
      @closed="formRef?.clearValidate()"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model.trim="form.username"
            :disabled="isEdit"
            placeholder="登录账号"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model.trim="form.nickname" maxlength="50" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model.trim="form.password" type="password" show-password maxlength="100" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model.trim="form.email" maxlength="100" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model.trim="form.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="420px" destroy-on-close>
      <p class="role-target">
        目标用户：<strong>{{ roleTarget?.username }}</strong>
      </p>
      <el-select v-model="roleIdModel" multiple placeholder="请选择角色" style="width: 100%">
        <el-option
          v-for="role in roleOptions"
          :key="role.id"
          :label="`${role.roleName}（${role.roleCode}）`"
          :value="role.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveRoles">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'

import {
  assignUserRoles,
  createUser,
  deleteUser,
  pageUsers,
  resetUserPassword,
  updateUser
} from '@/api/user'
import { listRoles } from '@/api/role'
import type { UserItem } from '@/types/api'

interface UserForm {
  username: string
  nickname: string
  email: string
  phone: string
  password: string
  status: number
}

/* ------------------------------ 列表与分页 ------------------------------ */
const loading = ref(false)
const submitting = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const list = ref<UserItem[]>([])

async function loadData(): Promise<void> {
  loading.value = true
  try {
    const result = await pageUsers({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined
    })
    list.value = result.list ?? []
    total.value = Number(result.total ?? 0)
  } finally {
    loading.value = false
  }
}

function handleSearch(): void {
  page.value = 1
  void loadData()
}

function handleSizeChange(): void {
  page.value = 1
  void loadData()
}

/* ---------------------------- 新增 / 编辑 ------------------------------- */
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<UserForm>({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  password: '',
  status: 1
})

const isEdit = computed(() => editingId.value !== null)

const rules = computed<FormRules>(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: isEdit.value
    ? []
    : [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}))

function openCreate(): void {
  editingId.value = null
  Object.assign(form, { username: '', nickname: '', email: '', phone: '', password: '', status: 1 })
  dialogVisible.value = true
  void nextTick(() => formRef.value?.clearValidate())
}

function openEdit(row: UserItem): void {
  editingId.value = row.id
  Object.assign(form, {
    username: row.username,
    nickname: row.nickname ?? '',
    email: row.email ?? '',
    phone: row.phone ?? '',
    password: '',
    status: row.status
  })
  dialogVisible.value = true
  void nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && editingId.value !== null) {
      await updateUser(editingId.value, {
        username: form.username,
        nickname: form.nickname || null,
        email: form.email || null,
        phone: form.phone || null,
        status: form.status
      })
      ElMessage.success('修改成功')
    } else {
      await createUser({
        username: form.username,
        nickname: form.nickname || null,
        email: form.email || null,
        phone: form.phone || null,
        password: form.password,
        status: form.status
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

/* -------------------------------- 重置密码 -------------------------------- */
async function handleResetPwd(row: UserItem): Promise<void> {
  try {
    await ElMessageBox.confirm(
      `确定将用户「${row.username}」的密码重置为 123456 吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  await resetUserPassword(row.id)
  ElMessage.success('密码已重置为 123456')
}

/* -------------------------------- 删除 ----------------------------------- */
async function handleDelete(row: UserItem): Promise<void> {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  // 若当前页删空则回退一页
  if (list.value.length === 1 && page.value > 1) {
    page.value -= 1
  }
  await loadData()
}

/* ------------------------------- 分配角色 -------------------------------- */
const roleDialogVisible = ref(false)
const roleTarget = ref<UserItem | null>(null)
const roleIdModel = ref<number[]>([])
const roleOptions = ref<Awaited<ReturnType<typeof listRoles>>>([])

async function openRoles(row: UserItem): Promise<void> {
  roleTarget.value = row
  roleIdModel.value = [...(row.roleIds ?? [])]
  roleDialogVisible.value = true
  if (roleOptions.value.length === 0) {
    roleOptions.value = await listRoles()
  }
}

async function saveRoles(): Promise<void> {
  if (!roleTarget.value) return
  submitting.value = true
  try {
    await assignUserRoles(roleTarget.value.id, roleIdModel.value)
    ElMessage.success('分配成功')
    roleDialogVisible.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void loadData()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.role-target {
  margin: 0 0 12px;
  color: #606266;
}
</style>
