<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <span>仪表盘</span>
    </template>

    <el-row :gutter="16">
      <el-col :span="12">
        <h2 class="greeting">{{ greeting }}，{{ userStore.displayName }}</h2>
        <p class="sub">欢迎使用 RBAC 管理系统，左侧菜单由后端权限动态生成。</p>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="info-row">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-label">我的角色</div>
          <div>
            <el-tag v-for="role in userStore.roles" :key="role" class="tag">{{ role }}</el-tag>
            <span v-if="userStore.roles.length === 0" class="muted">（暂无）</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-label">权限标识数量</div>
          <div class="stat-value">{{ userStore.perms.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-label">当前登录账号</div>
          <div class="stat-value">{{ userStore.authInfo?.username ?? '-' }}</div>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})
</script>

<style scoped>
.greeting {
  margin: 8px 0;
  color: var(--ink-900);
  letter-spacing: -0.02em;
}
.sub {
  color: var(--ink-500);
}
.info-row {
  margin-top: 16px;
}
.stat-label {
  color: var(--ink-500);
  font-size: 13px;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: var(--ink-900);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}
.tag {
  margin-right: 8px;
}
.muted {
  color: var(--ink-300);
}
</style>
