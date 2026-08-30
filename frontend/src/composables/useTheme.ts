import { ref } from 'vue'

type Theme = 'light' | 'dark'

const THEME_KEY = 'rbac-theme'

const isDark = ref<boolean>(localStorage.getItem(THEME_KEY) === 'dark')

function apply(): void {
  document.documentElement.classList.toggle('dark', isDark.value)
}

function initTheme(): void {
  apply()
}

function toggleTheme(): void {
  isDark.value = !isDark.value
  localStorage.setItem(THEME_KEY, isDark.value ? 'dark' : 'light')
  apply()
}

export function useTheme() {
  return { isDark, toggleTheme }
}

export { initTheme }
export type { Theme }
