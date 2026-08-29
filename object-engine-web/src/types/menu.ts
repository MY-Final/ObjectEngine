/** 菜单树节点：GET /api/v1/menus/tree */
export interface MenuTreeItem {
  id: number
  parentId: number
  menuName: string
  menuType: 'DIRECTORY' | 'OBJECT' | 'LINK'
  objectApiName?: string | null
  routePath?: string | null
  icon?: string | null
  target?: string | null
  sort: number
  children: MenuTreeItem[]
}
