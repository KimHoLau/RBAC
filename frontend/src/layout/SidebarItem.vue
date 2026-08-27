<template>
  <!-- 目录：渲染为可展开的子菜单 -->
  <el-sub-menu v-if="hasVisibleChildren" :index="String(item.id)">
    <template #title>
      <el-icon><component :is="iconComp" /></el-icon>
      <span>{{ item.menuName }}</span>
    </template>
    <SidebarItem v-for="child in visibleChildren" :key="child.id" :item="child" />
  </el-sub-menu>

  <!-- 菜单：渲染为叶子节点，点击跳转 -->
  <el-menu-item v-else-if="item.path" :index="item.path">
    <el-icon><component :is="iconComp" /></el-icon>
    <template #title>{{ item.menuName }}</template>
  </el-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Component } from 'vue'
import * as Icons from '@element-plus/icons-vue'

import type { MenuItem } from '@/types/api'

const props = defineProps<{ item: MenuItem }>()

const iconMap: Record<string, Component> = Object.fromEntries(
  Object.entries(Icons).map(([name, comp]) => [name, comp as Component])
)

const visibleChildren = computed(() =>
  (props.item.children ?? []).filter(child => child.type !== 2 && child.status === 1)
)

const hasVisibleChildren = computed(
  () => props.item.type === 0 && visibleChildren.value.length > 0
)

const iconComp = computed(() => iconMap[props.item.icon ?? ''] ?? iconMap['Document'])
</script>
