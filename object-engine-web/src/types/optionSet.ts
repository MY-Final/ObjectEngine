/** 通用选项集选项 */
export interface OptionSetItem {
  id: number
  optionSetId?: number
  label: string
  value: string
  sort: number
  status: number
  isDefault?: boolean
  description?: string | null
  remark?: string | null
  createdAt?: string
  updatedAt?: string
}

/** 通用选项集：detail 场景携带 options，分页列表为 undefined */
export interface OptionSet {
  id: number
  name: string
  apiName: string
  description?: string | null
  status: number
  sort: number
  remark?: string | null
  createdAt: string
  updatedAt: string
  options?: OptionSetItem[]
  /** 启用选项的 label 顿号拼接（分页列表预览用） */
  optionsSummary?: string | null
}

export interface CreateOptionSetPayload {
  name: string
  apiName: string
  description?: string
  remark?: string
  sort?: number
  status?: number
}

export interface UpdateOptionSetPayload {
  name?: string
  description?: string
  remark?: string
  sort?: number
  status?: number
}

export interface CreateOptionPayload {
  label: string
  value: string
  sort?: number
  status?: number
  isDefault?: number
  description?: string
  remark?: string
}

/** value 创建后不可修改，更新只允许调整展示与排序属性 */
export interface UpdateOptionPayload {
  label?: string
  sort?: number
  status?: number
  isDefault?: number
  description?: string
  remark?: string
}
