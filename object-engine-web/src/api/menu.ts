import http from './http'
import type { MenuListQuery, MenuPayload, MenuTreeItem, MenuItem } from '@/types/menu'

/** 导航菜单树：GET /api/v1/menus/tree（叶子菜单按 routePath 前缀区分前台/后台，布局层按当前区过滤） */
export function getMenuTree() {
  return http.get<MenuTreeItem[]>('/menus/tree', { silent: true })
}

/** 菜单管理列表：全量扁平结构，前端按 parentId 组树 */
export function listMenus(params?: MenuListQuery) {
  return http.get<MenuItem[]>('/menus', { params })
}

export function createMenu(data: MenuPayload) {
  return http.post<number>('/menus', data)
}

/** 编辑为全量更新 */
export function updateMenu(id: number, data: MenuPayload) {
  return http.put<void>(`/menus/${id}`, data)
}

export function deleteMenu(id: number) {
  return http.delete<void>(`/menus/${id}`)
}

/** 启用 / 禁用 */
export function updateMenuStatus(id: number, status: number) {
  return http.put<void>(`/menus/${id}/status`, { status })
}

/** 显示 / 隐藏 */
export function updateMenuVisible(id: number, visible: number) {
  return http.put<void>(`/menus/${id}/visible`, { visible })
}

/** 单独调整排序值 */
export function updateMenuSort(id: number, sort: number) {
  return http.put<void>(`/menus/${id}/sort`, { sort })
}

/** 移动到目标父菜单下并设置排序 */
export function moveMenu(id: number, parentId: number, sort: number) {
  return http.put<void>(`/menus/${id}/move`, { parentId, sort })
}
