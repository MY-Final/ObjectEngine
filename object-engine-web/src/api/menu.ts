import http from './http'
import type { MenuTreeItem } from '@/types/menu'

/** 前台导航菜单树：仅启用且显示的菜单（接口暂不可用时不弹错，侧边栏只显示静态项） */
export function getMenuTree() {
  return http.get<MenuTreeItem[]>('/menus/tree', { silent: true })
}
