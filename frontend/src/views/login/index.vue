<template>
  <div class="login-container">
    <!-- 深空动效层：叠加在静态 login-bg.svg 之上（background-image 里的 SVG 无法被 CSS 驱动），
         仅 transform/opacity 动画，prefers-reduced-motion 下随全局规则静止 -->
    <div class="login-fx" aria-hidden="true">
      <svg viewBox="0 0 1920 1080" preserveAspectRatio="xMidYMid slice">
        <defs>
          <radialGradient id="fx-nebula-blue" cx="0.5" cy="0.5" r="0.5">
            <stop offset="0" stop-color="#0071e3" stop-opacity="0.32" />
            <stop offset="1" stop-color="#0071e3" stop-opacity="0" />
          </radialGradient>
          <radialGradient id="fx-nebula-violet" cx="0.5" cy="0.5" r="0.5">
            <stop offset="0" stop-color="#8b5cf6" stop-opacity="0.26" />
            <stop offset="1" stop-color="#8b5cf6" stop-opacity="0" />
          </radialGradient>
          <radialGradient id="fx-star" cx="0.5" cy="0.5" r="0.5">
            <stop offset="0" stop-color="#bcd9ff" stop-opacity="0.9" />
            <stop offset="0.3" stop-color="#7db2ff" stop-opacity="0.3" />
            <stop offset="1" stop-color="#7db2ff" stop-opacity="0" />
          </radialGradient>
          <!-- 流星头部亮、尾部渐隐：userSpaceOnUse 避免线条零高包围盒破坏渐变 -->
          <linearGradient id="fx-meteor-a" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="72" y2="-35">
            <stop offset="0" stop-color="#cfe4ff" stop-opacity="0.95" />
            <stop offset="1" stop-color="#cfe4ff" stop-opacity="0" />
          </linearGradient>
          <linearGradient id="fx-meteor-b" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="52" y2="-22">
            <stop offset="0" stop-color="#cfe4ff" stop-opacity="0.75" />
            <stop offset="1" stop-color="#cfe4ff" stop-opacity="0" />
          </linearGradient>
          <filter id="fx-blur6" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur stdDeviation="6" />
          </filter>
        </defs>

        <!-- 星云慢漂移（60s 级，alternate） -->
        <ellipse class="fx-drift-a" cx="1560" cy="330" rx="640" ry="400" fill="url(#fx-nebula-blue)" opacity="0.55" />
        <ellipse class="fx-drift-b" cx="380" cy="260" rx="560" ry="360" fill="url(#fx-nebula-violet)" opacity="0.55" />

        <!-- 流星：尾部朝运动反方向，7s / 9.5s 两颗错峰 -->
        <g class="fx-meteor fx-meteor-a">
          <line x1="0" y1="0" x2="72" y2="-35" stroke="url(#fx-meteor-a)" stroke-width="2" stroke-linecap="round" />
        </g>
        <g class="fx-meteor fx-meteor-b">
          <line x1="0" y1="0" x2="52" y2="-22" stroke="url(#fx-meteor-b)" stroke-width="1.5" stroke-linecap="round" />
        </g>

        <!-- 星闪：错峰呼吸（负延迟去同步） -->
        <g fill="url(#fx-star)">
          <circle class="fx-twinkle" style="animation-duration: 4.6s; animation-delay: -1.2s" cx="180" cy="180" r="11" />
          <circle class="fx-twinkle" style="animation-duration: 6.2s; animation-delay: -3.4s" cx="420" cy="640" r="9" />
          <circle class="fx-twinkle" style="animation-duration: 5.1s; animation-delay: -0.6s" cx="900" cy="140" r="12" />
          <circle class="fx-twinkle" style="animation-duration: 7.1s; animation-delay: -2.8s" cx="1700" cy="420" r="10" />
          <circle class="fx-twinkle" style="animation-duration: 4.4s; animation-delay: -5.2s" cx="1450" cy="760" r="9" />
          <circle class="fx-twinkle" style="animation-duration: 6.6s; animation-delay: -1.9s" cx="300" cy="880" r="11" />
          <circle class="fx-twinkle" style="animation-duration: 5.4s; animation-delay: -4.1s" cx="1100" cy="950" r="9" />
          <circle class="fx-twinkle" style="animation-duration: 6.9s; animation-delay: -0.3s" cx="1830" cy="220" r="10" />
          <circle class="fx-twinkle" style="animation-duration: 4.9s; animation-delay: -2.2s" cx="640" cy="330" r="8" />
          <circle class="fx-twinkle" style="animation-duration: 5.8s; animation-delay: -3.7s" cx="1240" cy="860" r="9" />
        </g>

        <!-- 地平线辉光缓慢脉动 -->
        <line class="fx-horizon" x1="260" y1="700" x2="1660" y2="700" stroke="#4da3ff" stroke-width="3" filter="url(#fx-blur6)" />
      </svg>
    </div>

    <el-card class="login-card" shadow="always">
      <svg class="login-logo" viewBox="0 0 64 64" aria-hidden="true">
        <defs>
          <linearGradient id="fx-logo-stroke" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0" stop-color="#66b3f0" />
            <stop offset="1" stop-color="#0071e3" />
          </linearGradient>
          <radialGradient id="fx-logo-glow" cx="0.5" cy="0.5" r="0.5">
            <stop offset="0" stop-color="#4da3ff" stop-opacity="0.35" />
            <stop offset="1" stop-color="#4da3ff" stop-opacity="0" />
          </radialGradient>
        </defs>
        <circle cx="32" cy="32" r="26" fill="url(#fx-logo-glow)" />
        <polygon points="32,4 56,18 56,46 32,60 8,46 8,18" fill="rgba(77, 163, 255, 0.08)" stroke="url(#fx-logo-stroke)" stroke-width="2" />
        <polygon points="32,12 49,22 49,42 32,52 15,42 15,22" fill="none" stroke="rgba(102, 179, 240, 0.35)" stroke-width="1" />
        <text x="32" y="42" text-anchor="middle" font-size="30" font-weight="600" fill="#e8f2ff">R</text>
      </svg>
      <h2 class="login-title">RBAC 管理系统</h2>
      <p class="login-subtitle">企业级权限管理平台</p>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input v-model.trim="form.username" placeholder="用户名" clearable>
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model.trim="form.password"
            type="password"
            placeholder="密码"
            show-password
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="login-footer">© 2026 RBAC 管理系统 · v1.0.0</div>
  </div>
</template>

<script setup lang="ts">
import { Lock, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function resolveRedirect(): string {
  const redirect = route.query.redirect
  return typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/'
}

async function handleLogin(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.replace(resolveRedirect())
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-btn {
  width: 100%;
}
</style>
