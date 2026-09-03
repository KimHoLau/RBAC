<template>
  <div class="navbar-left">
    <el-icon class="fold-btn" @click="toggleCollapse">
      <component :is="collapse ? Expand : Fold" />
    </el-icon>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item v-for="(crumb, index) in breadcrumbs" :key="index">
        {{ crumb }}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>

  <div class="navbar-right">
    <el-icon class="theme-btn" :title="isDark ? '切换到白天' : '切换到黑夜'" @click="toggleTheme">
      <component :is="isDark ? Sunny : Moon" />
    </el-icon>
    <el-dropdown trigger="click" @command="handleCommand">
      <span class="user-info">
        <el-avatar :size="28" class="avatar">{{ avatarText }}</el-avatar>
        <span class="nickname">{{ userStore.displayName }}</span>
        <el-icon><ArrowDown /></el-icon>
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="logout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { ArrowDown, Fold, Expand, SwitchButton, Moon, Sunny } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useTheme } from '@/composables/useTheme'
import { usePermissionStore } from '@/stores/permission'
import { useUserStore } from '@/stores/user'

const collapse = defineModel<boolean>('collapse', { default: false })

const { isDark, toggleTheme } = useTheme()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const breadcrumbs = computed(() =>
  route.matched.filter(record => record.meta?.title).map(record => String(record.meta.title))
)

const avatarText = computed(() => userStore.displayName.charAt(0).toUpperCase() || '?')

function toggleCollapse(): void {
  collapse.value = !collapse.value
}

async function handleCommand(command: string): Promise<void> {
  if (command !== 'logout') return
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  userStore.logoutLocal()
  permissionStore.reset()
  router.replace('/login')
}
</script>

<style scoped>
.navbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.fold-btn,
.theme-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--ink-500);
  transition: color 0.16s var(--ease-fluid);
}
.fold-btn:hover,
.theme-btn:hover {
  color: var(--accent);
}
.navbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.navbar-right .user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 999px;
  transition: background-color 0.16s var(--ease-fluid);
}
.navbar-right .user-info:hover {
  background-color: var(--hover-fill);
}
.avatar {
  background-color: var(--accent);
}
.nickname {
  color: var(--ink-900);
  font-weight: 500;
}
</style>
