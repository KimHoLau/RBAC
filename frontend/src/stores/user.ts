import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import { getAuthInfo, login as loginApi } from '@/api/auth'
import type { AuthInfo } from '@/types/api'
import { getToken, removeToken, setToken } from '@/utils/auth'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref<string>(getToken() ?? '')
    const authInfo = ref<AuthInfo | null>(null)
    const roles = ref<string[]>([])
    const perms = ref<string[]>([])
    const infoLoaded = ref(false)

    const displayName = computed(
      () => authInfo.value?.nickname || authInfo.value?.username || ''
    )

    async function login(form: { username: string; password: string }): Promise<void> {
      const res = await loginApi(form)
      setToken(res.token)
      token.value = res.token
    }

    async function fetchUserInfo(): Promise<void> {
      const info = await getAuthInfo()
      authInfo.value = info
      roles.value = info.roles
      perms.value = info.perms
      infoLoaded.value = true
    }

    /** 清空本地登录态（不调用后端） */
    function logoutLocal(): void {
      removeToken()
      token.value = ''
      authInfo.value = null
      roles.value = []
      perms.value = []
      infoLoaded.value = false
    }

    return {
      token,
      authInfo,
      roles,
      perms,
      infoLoaded,
      displayName,
      login,
      fetchUserInfo,
      logoutLocal
    }
  }
)
