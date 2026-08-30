<template>
  <el-card shadow="never" class="page-card">
    <!-- 角色表格 -->
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="roleName" label="角色名称" min-width="120" />
      <el-table-column prop="roleCode" label="角色编码" min-width="120" />
      <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button
            v-perms="'system:role:assignMenu'"
            link
            type="primary"
            :loading="assigningId === row.id"
            @click="openAssign(row)"
          >
            分配菜单
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分配菜单对话框 -->
    <el-dialog v-model="assignVisible" title="分配菜单" width="420px" destroy-on-close>
      <p class="assign-target">
        目标角色：<strong>{{ assignTarget?.roleName }}</strong>
      </p>
      <div v-loading="treeLoading" class="menu-tree">
        <el-tree
          ref="treeRef"
          :data="menuTree"
          :props="{ label: 'menuName', children: 'children' }"
          node-key="id"
          show-checkbox
          default-expand-all
          :check-strictly="checkStrictly"
        />
      </div>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveAssign">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import type { TreeInstance } from 'element-plus'
import { nextTick, onMounted, ref } from 'vue'

import { listRoles, assignRoleMenus, getRoleMenuIds } from '@/api/role'
import { getMenuTree } from '@/api/menu'
import type { MenuItem, RoleItem } from '@/types/api'

/* ------------------------------ 列表 ------------------------------ */
const loading = ref(false)
const list = ref<RoleItem[]>([])

async function loadData(): Promise<void> {
  loading.value = true
  try {
    list.value = await listRoles()
  } finally {
    loading.value = false
  }
}

/* ----------------------------- 分配菜单 ---------------------------- */
const assignVisible = ref(false)
const assigningId = ref<number | null>(null)
const assignTarget = ref<RoleItem | null>(null)
const treeLoading = ref(false)
const submitting = ref(false)
const menuTree = ref<MenuItem[]>([])
const treeRef = ref<TreeInstance>()
// 回显阶段用严格模式精确勾选已授权节点，随后切回级联模式供交互
const checkStrictly = ref(false)

function collectCheckedIds(): number[] {
  const keys = [
    ...(treeRef.value?.getCheckedKeys() ?? []),
    ...(treeRef.value?.getHalfCheckedKeys() ?? [])
  ]
  return keys.filter((key): key is number => typeof key === 'number')
}

async function openAssign(row: RoleItem): Promise<void> {
  assignTarget.value = row
  assigningId.value = row.id
  treeLoading.value = true
  try {
    if (menuTree.value.length === 0) {
      menuTree.value = await getMenuTree()
    }
    const granted = await getRoleMenuIds(row.id)
    checkStrictly.value = true
    assignVisible.value = true
    await nextTick()
    treeRef.value?.setCheckedKeys(granted)
    await nextTick()
    checkStrictly.value = false
  } finally {
    treeLoading.value = false
    assigningId.value = null
  }
}

async function saveAssign(): Promise<void> {
  if (!assignTarget.value) return
  submitting.value = true
  try {
    await assignRoleMenus(assignTarget.value.id, collectCheckedIds())
    ElMessage.success('分配成功')
    assignVisible.value = false
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void loadData()
})
</script>

<style scoped>
.assign-target {
  margin: 0 0 12px;
  color: #606266;
}
.menu-tree {
  max-height: 420px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 8px;
}
</style>
