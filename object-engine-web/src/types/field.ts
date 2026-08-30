export type FieldType =
  | 'TEXT'
  | 'TEXTAREA'
  | 'NUMBER'
  | 'MONEY'
  | 'DATE'
  | 'SELECT'
  | 'MULTI_SELECT'
  | 'BOOLEAN'
  | 'TIME'
  | 'PHONE'
  | 'EMAIL'
  | 'URL'
  | 'PERCENT'

export interface FieldOption {
  label: string
  value: string
}

export interface FieldConfig {
  options?: FieldOption[]
  /** 文本类：长度限制 */
  maxLength?: number
  minLength?: number
  /** 数字类：整数位数与小数位数 */
  integerLength?: number
  scale?: number
  [key: string]: unknown
}

export interface CustomField {
  id: number
  objectId: number
  apiName: string
  fieldName: string
  fieldType: FieldType
  description?: string
  remark?: string
  requiredFlag: number
  uniqueFlag: number
  searchableFlag: number
  sortableFlag: number
  sort: number
  defaultValue?: string | null
  configJson?: FieldConfig | null
  status: number
  createdAt: string
  updatedAt: string
}

export interface CreateFieldPayload {
  apiName: string
  fieldName: string
  fieldType: FieldType
  description?: string
  remark?: string
  requiredFlag?: number
  uniqueFlag?: number
  searchableFlag?: number
  sortableFlag?: number
  sort?: number
  defaultValue?: string | null
  configJson?: FieldConfig | null
}

export interface UpdateFieldPayload {
  fieldName?: string
  description?: string
  remark?: string
  requiredFlag?: number
  uniqueFlag?: number
  searchableFlag?: number
  sortableFlag?: number
  sort?: number
  defaultValue?: string | null
  configJson?: FieldConfig | null
  status?: number
}
