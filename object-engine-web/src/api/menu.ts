import http from './http'
import type { MenuTreeItem } from '@/types/menu'

/** 导航菜单树：GET /api/v1/menus/tree（叶子菜单按 routePath 前缀区分前台/后台，布局层按当前区过滤） */
export function getMenuTree() {
  return http.get<MenuTreeItem[]>('/menus/tree', { silent: true })
}
