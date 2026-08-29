export type FieldType = 'TEXT' | 'TEXTAREA' | 'NUMBER' | 'MONEY' | 'DATE' | 'SELECT'

export interface FieldOption {
  label: string
  value: string
}

export interface FieldConfig {
  options?: FieldOption[]
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
