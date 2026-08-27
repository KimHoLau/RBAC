import type { Directive } from 'vue'

import { useUserStore } from '@/stores/user'

/**
 * v-perms="`system:user:add`" 或 v-perms="[`system:user:add`, `system:user:edit`]":
 * 当前用户不持有任一权限标识时移除该元素。
 */
export const perms: Directive<HTMLElement, string | string[]> = {
  mounted(el, binding) {
    const store = useUserStore()
    const needed = Array.isArray(binding.value) ? binding.value : [binding.value]
    if (needed.length === 0) return
    const allowed = needed.some(perm => store.perms.includes(perm))
    if (!allowed && el.parentNode) {
      el.parentNode.removeChild(el)
    }
  }
}
