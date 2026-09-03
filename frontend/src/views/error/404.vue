<template>
  <div class="not-found">
    <div class="not-found-card">
      <p class="code">404</p>
      <p class="hint">抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="router.replace('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.not-found {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.not-found-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 48px 64px;
  border-radius: 16px;
  border: 1px solid var(--glass-rim);
  background: var(--card-bg);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  box-shadow:
    inset 0 1px 0 var(--glass-sheen),
    var(--elev-2);
}
.code {
  margin: 0;
  font-size: 64px;
  font-weight: 700;
  line-height: 1;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
  color: var(--accent);
}
.hint {
  margin: 0 0 16px;
  color: var(--ink-500);
}
</style>
