/** 菜单类型：目录 / 自定义对象 / 链接 */
export type MenuType = 'DIRECTORY' | 'OBJECT' | 'LINK'

/** 菜单树节点：GET /api/v1/menus/tree */
export interface MenuTreeItem {
  id: number
  parentId: number
  menuName: string
  menuType: MenuType
  objectApiName?: string | null
  routePath?: string | null
  icon?: string | null
  target?: string | null
  sort: number
  children: MenuTreeItem[]
}

/** 菜单管理项（完整字段）：GET /api/v1/menus */
export interface MenuItem {
  id: number
  parentId: number
  menuName: string
  menuType: MenuType
  objectApiName?: string | null
  routePath?: string | null
  component?: string | null
  icon?: string | null
  sort: number
  status: number
  visible: number
  target?: string | null
  remark?: string | null
  createdAt: string
  updatedAt: string
  /** 关联对象状态（仅 OBJECT 菜单有值）：0停用 1启用，对象不存在时为 null */
  objectStatus?: number | null
}

/** 管理端菜单树：扁平列表按 parentId 组装 */
export interface MenuTree extends MenuItem {
  children: MenuTree[]
}

/** 新增 / 编辑菜单请求（编辑为全量更新） */
export interface MenuPayload {
  parentId: number
  menuName: string
  menuType: MenuType
  objectApiName?: string | null
  routePath?: string | null
  component?: string | null
  icon?: string | null
  sort?: number
  status?: number
  visible?: number
  target?: string | null
  remark?: string | null
}

/** 菜单列表查询参数 */
export interface MenuListQuery {
  menuName?: string
  menuType?: MenuType
  status?: number
  visible?: number
}

/** 上级菜单树选项（el-tree-select 数据格式） */
export interface MenuParentOption {
  value: number
  label: string
  children: MenuParentOption[]
}
