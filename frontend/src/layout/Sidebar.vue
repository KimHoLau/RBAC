<template>
  <el-menu
    class="sidebar-menu"
    :class="{ 'is-collapse': collapse }"
    :collapse="collapse"
    :default-active="route.path"
    router
    background-color="#001529"
    text-color="#bfcbd9"
    active-text-color="#ffffff"
    :collapse-transition="false"
  >
    <SidebarItem v-for="menu in visibleMenus" :key="menu.id" :item="menu" />
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import SidebarItem from './SidebarItem.vue'
import { usePermissionStore } from '@/stores/permission'

defineProps<{ collapse: boolean }>()

const route = useRoute()
const permissionStore = usePermissionStore()

const visibleMenus = computed(() => permissionStore.menus.filter(menu => menu.status === 1))
</script>

<style scoped>
.sidebar-menu {
  border-right: none;
}
.sidebar-menu:not(.is-collapse) {
  width: 210px;
}
</style>
