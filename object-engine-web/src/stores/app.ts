import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMenuTree } from '@/api/menu'
import type { MenuTreeItem } from '@/types/menu'

/** 全局 UI 状态 */
export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)

  /** 侧边栏导航菜单树；接口不可用时置空，侧边栏仅显示静态项 */
  const menuTree = ref<MenuTreeItem[]>([])

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

  return { sidebarCollapsed, menuTree, loadMenus, toggleSidebar }
})
