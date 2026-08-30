<template>
  <el-menu
    class="sidebar-menu"
    :class="{ 'is-collapse': collapse }"
    :collapse="collapse"
    :default-active="route.path"
    router
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

