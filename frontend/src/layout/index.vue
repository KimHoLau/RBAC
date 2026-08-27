<template>
  <el-container class="layout-root">
    <el-aside :width="collapse ? '64px' : '210px'" class="layout-aside">
      <div class="logo-bar">
        <span v-if="!collapse" class="logo-title">RBAC 管理系统</span>
        <span v-else class="logo-mini">RB</span>
      </div>
      <Sidebar :collapse="collapse" />
    </el-aside>

    <el-container>
      <el-header height="56px" class="layout-header">
        <Navbar v-model:collapse="collapse" />
      </el-header>
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
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
.layout-aside {
  display: flex;
  flex-direction: column;
  background-color: #001529;
}
.logo-bar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  letter-spacing: 1px;
  flex-shrink: 0;
}
.logo-mini {
  font-size: 18px;
}
.layout-main {
  padding: 16px;
}
.sidebar-menu {
  border-right: none;
  flex: 1;
}
</style>
