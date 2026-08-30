export type FieldType =
  | 'TEXT'
  | 'TEXTAREA'
  | 'NUMBER'
  | 'MONEY'
  | 'DATE'
  | 'SELECT'
  | 'MULTI_SELECT'
  | 'LOOKUP'
  | 'REFERENCE'
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

/** 选项来源：LOCAL 字段自有选项，GLOBAL 引用通用选项集 */
export type OptionSource = 'LOCAL' | 'GLOBAL'

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
  optionSource?: OptionSource | null
  optionSetId?: number | null
  /** 关联对象ID（LOOKUP：目标对象；REFERENCE：由关联的 LOOKUP 字段推导） */
  relationObjectId?: number | null
  /** 关联的 LOOKUP 字段ID（REFERENCE 使用） */
  relationFieldId?: number | null
  /** 引用的目标字段ID（REFERENCE 使用） */
  referenceFieldId?: number | null
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
  optionSource?: OptionSource
  optionSetId?: number | null
  relationObjectId?: number | null
  relationFieldId?: number | null
  referenceFieldId?: number | null
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
  optionSource?: OptionSource
  optionSetId?: number | null
  relationObjectId?: number | null
  relationFieldId?: number | null
  referenceFieldId?: number | null
  status?: number
}
