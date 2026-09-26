import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getMenuTree } from '@/api/menu'
import type { MenuTreeItem } from '@/types/menu'

/** 主题在 localStorage 的键 */
const THEME_KEY = 'oe_theme'

export type ThemeMode = 'light' | 'dark'

function readStoredTheme(): ThemeMode | null {
  const raw = localStorage.getItem(THEME_KEY)
  return raw === 'light' || raw === 'dark' ? raw : null
}

/** 全局 UI 状态 */
export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)

  /** 侧边栏导航菜单树；接口不可用时置空，侧边栏仅显示静态项 */
  const menuTree = ref<MenuTreeItem[]>([])

  /** 亮 / 暗主题，实际生效的类名挂在 <html> 上 */
  const theme = ref<ThemeMode>('light')

  const isDark = computed(() => theme.value === 'dark')

  async function loadMenus() {
    try {
      menuTree.value = await getMenuTree()
    } catch {
      menuTree.value = []
    }
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  /**
   * 初始化主题：优先用户上次的选择，其次跟随系统。
   * 在 main.ts 挂载前调用，避免深色用户先闪一下白屏。
   */
  function initTheme() {
    theme.value = readStoredTheme() ?? (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
    applyTheme()
  }

  function toggleTheme() {
    theme.value = isDark.value ? 'light' : 'dark'
    localStorage.setItem(THEME_KEY, theme.value)
    applyTheme()
  }

  /** 唯一改 html class 的地方：tokens.css 里的 html.dark 负责实际换色 */
  function applyTheme() {
    document.documentElement.classList.toggle('dark', isDark.value)
  }

  return {
    sidebarCollapsed,
    menuTree,
    theme,
    isDark,
    loadMenus,
    toggleSidebar,
    initTheme,
    toggleTheme,
  }
})
