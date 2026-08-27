<template>
  <el-card shadow="never" class="page-card">
    <div class="toolbar">
      <el-button v-perms="'system:menu:add'" type="primary" @click="openCreate(0)">
        新增菜单
      </el-button>
      <el-button @click="loadTree">刷新</el-button>
    </div>

    <!-- 菜单树表格 -->
    <el-table
      v-loading="loading"
      :data="tree"
      row-key="id"
      border
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="menuName" label="名称" min-width="180" />
      <el-table-column label="类型" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.type)">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路由地址" min-width="140" show-overflow-tooltip />
      <el-table-column prop="component" label="组件路径" min-width="180" show-overflow-tooltip />
      <el-table-column prop="perms" label="权限标识" min-width="170" show-overflow-tooltip />
      <el-table-column prop="icon" label="图标" width="110" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="70" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.type !== 2"
            v-perms="'system:menu:add'"
            link
            type="primary"
            @click="openCreate(row.id)"
          >
            新增
          </el-button>
          <el-button v-perms="'system:menu:edit'" link type="primary" @click="openEdit(row)">
            编辑
          </el-button>
          <el-button v-perms="'system:menu:delete'" link type="danger" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增 / 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜单' : '新增菜单'"
      width="560px"
      destroy-on-close
      @closed="formRef?.clearValidate()"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="0">目录</el-radio>
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            node-key="id"
            check-strictly
            default-expand-all
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model.trim="form.menuName" maxlength="50" placeholder="显示名称" />
        </el-form-item>
        <el-form-item v-if="form.type !== 2" label="路由地址" prop="path">
          <el-input v-model.trim="form.path" maxlength="200" placeholder="如 /system/user" />
        </el-form-item>
        <el-form-item v-if="form.type === 1" label="组件路径" prop="component">
          <el-input
            v-model.trim="form.component"
            maxlength="200"
            placeholder="相对 src/views，不含 .vue，如 system/user/index"
          />
        </el-form-item>
        <el-form-item v-if="form.type !== 0" label="权限标识" prop="perms">
          <el-input v-model.trim="form.perms" maxlength="100" placeholder="如 system:user:list" />
        </el-form-item>
        <el-form-item v-if="form.type !== 2" label="图标" prop="icon">
          <el-input v-model.trim="form.icon" maxlength="50" placeholder="Element Plus 图标名，如 User" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" />
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
  </el-card>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'

import { createMenu, deleteMenu, getMenuTree, updateMenu } from '@/api/menu'
import type { MenuItem, MenuPayload } from '@/types/api'

interface OptionNode {
  id: number
  menuName: string
  children?: OptionNode[]
}

interface MenuForm {
  parentId: number
  menuName: string
  path: string
  component: string
  perms: string
  icon: string
  type: number
  sort: number
  status: number
}

/* -------------------------------- 树加载 ---------------------------------- */
const loading = ref(false)
const tree = ref<MenuItem[]>([])

async function loadTree(): Promise<void> {
  loading.value = true
  try {
    tree.value = (await getMenuTree()) ?? []
  } finally {
    loading.value = false
  }
}

function typeLabel(type: number): string {
  return type === 0 ? '目录' : type === 1 ? '菜单' : '按钮'
}

function typeTag(type: number): 'primary' | 'success' | 'warning' {
  return type === 0 ? 'primary' : type === 1 ? 'success' : 'warning'
}

/* ------------------------------- 表单与对话框 ------------------------------ */
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<MenuForm>({
  parentId: 0,
  menuName: '',
  path: '',
  component: '',
  perms: '',
  icon: '',
  type: 1,
  sort: 0,
  status: 1
})

const isEdit = computed(() => editingId.value !== null)

const rules: FormRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [
    {
      validator: (_rule, value: string, callback) => {
        if (form.type === 2 || value) callback()
        else callback(new Error('请输入路由地址'))
      },
      trigger: 'blur'
    }
  ]
}

/** 上级菜单选项：根目录 + 全部目录/菜单（排除自身，避免形成环） */
const parentOptions = computed<OptionNode[]>(() => {
  const excludeId = editingId.value
  function map(list: MenuItem[]): OptionNode[] {
    return list
      .filter(item => item.type !== 2 && item.id !== excludeId)
      .map(item => ({
        id: item.id,
        menuName: item.menuName,
        children: item.children?.length ? map(item.children) : undefined
      }))
  }
  return [{ id: 0, menuName: '根目录', children: map(tree.value) }]
})

function openCreate(parentId: number): void {
  editingId.value = null
  Object.assign(form, {
    parentId,
    menuName: '',
    path: '',
    component: '',
    perms: '',
    icon: '',
    type: parentId === 0 ? 0 : 1,
    sort: 0,
    status: 1
  })
  dialogVisible.value = true
  void nextTick(() => formRef.value?.clearValidate())
}

function openEdit(row: MenuItem): void {
  editingId.value = row.id
  Object.assign(form, {
    parentId: row.parentId,
    menuName: row.menuName,
    path: row.path ?? '',
    component: row.component ?? '',
    perms: row.perms ?? '',
    icon: row.icon ?? '',
    type: row.type,
    sort: row.sort,
    status: row.status
  })
  dialogVisible.value = true
  void nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const payload: MenuPayload = {
    parentId: form.parentId,
    menuName: form.menuName,
    path: form.type === 2 ? null : form.path || null,
    component: form.type === 1 ? form.component || null : null,
    perms: form.type === 0 ? null : form.perms || null,
    icon: form.type === 2 ? null : form.icon || null,
    type: form.type,
    sort: form.sort,
    status: form.status
  }

  submitting.value = true
  try {
    if (isEdit.value && editingId.value !== null) {
      await updateMenu(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createMenu(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadTree()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: MenuItem): Promise<void> {
  try {
    await ElMessageBox.confirm(
      `确定删除「${row.menuName}」吗？（存在子节点或已被角色关联时无法删除）`,
      '警告',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  await deleteMenu(row.id)
  ElMessage.success('删除成功')
  await loadTree()
}

onMounted(() => {
  void loadTree()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
</style>
