import type { CustomField } from '@/types/field'

/**
 * 字段集合 → 动态表单模型的桥接，新建与编辑共用：
 * - 编辑（传 data）：把存储值转换成控件需要的类型——布尔转 boolean、多选转数组、数字类转 Number
 * - 新建（不传 data）：按字段 defaultValue 初始化，空默认值按类型给 [] / false / null
 */
export function buildFormModel(
  fields: CustomField[],
  data?: Record<string, unknown>,
): Record<string, unknown> {
  const model: Record<string, unknown> = {}
  for (const field of fields) {
    const stored = data?.[field.apiName]
    const hasStored = stored !== null && stored !== undefined && stored !== ''
    const defaultText = field.defaultValue
    const hasDefault = defaultText !== null && defaultText !== undefined && defaultText !== ''
    switch (field.fieldType) {
      case 'MULTI_SELECT':
        model[field.apiName] = hasStored
          ? Array.isArray(stored)
            ? stored
            : String(stored).split(',').map((item) => item.trim())
          : hasDefault
            ? defaultText.split(',').map((item) => item.trim())
            : []
        break
      case 'BOOLEAN':
        model[field.apiName] = hasStored
          ? stored === true || stored === 'true'
          : hasDefault
            ? defaultText === 'true'
            : false
        break
      case 'NUMBER':
      case 'MONEY':
      case 'PERCENT':
        model[field.apiName] = hasStored ? Number(stored) : hasDefault ? Number(defaultText) : null
        break
      default:
        model[field.apiName] = hasStored ? stored : hasDefault ? defaultText : null
    }
  }
  return model
}
