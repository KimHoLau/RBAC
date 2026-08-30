<template>
  <el-container class="layout-root">
    <el-aside :width="collapse ? '64px' : '210px'" class="layout-aside">
      <div class="logo-bar">
        <span v-if="!collapse" class="logo-title">RBAC 管理系统</span>
        <span v-else class="logo-mini">RB</span>
      </div>
      <Sidebar :collapse="collapse" />
    </el-aside>

    <el-container class="layout-body">
      <el-header height="56px" class="layout-header">
        <Navbar v-model:collapse="collapse" />
      </el-header>
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import Navbar from './Navbar.vue'
import Sidebar from './Sidebar.vue'

const collapse = ref(false)
</script>

<style scoped>
.layout-root {
  height: 100%;
}
/* 顶栏在其上悬浮，内容从底部穿过 */
.layout-body {
  position: relative;
}
.layout-main {
  padding: 72px 16px 20px;
}
.logo-bar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--ink-900);
  font-weight: 700;
  letter-spacing: 1px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--hairline);
}
.logo-bar::before {
  content: '';
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), #66b3f0);
  box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.15);
  flex-shrink: 0;
}
.logo-mini {
  font-size: 18px;
}
</style>
