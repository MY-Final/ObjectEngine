import { getMetadata } from '@/api/object'
import type { ObjectMetadata } from '@/types/object'
import type { CustomField } from '@/types/field'

/** 关联对象元数据的模块级缓存：ReferenceSelect 多实例共享 */
const metadataCache = new Map<string, Promise<ObjectMetadata>>()

function fetchMetadataCached(apiName: string): Promise<ObjectMetadata> {
  let promise = metadataCache.get(apiName)
  if (!promise) {
    promise = getMetadata(apiName)
    metadataCache.set(apiName, promise)
    promise.catch(() => metadataCache.delete(apiName))
  }
  return promise
}

/**
 * 关联记录的展示文本：优先 name 字段，其次第一个启用中的文本字段，兜底 #记录ID。
 * 与后端 resolveDisplayText 的启发式保持一致
 */
export async function recordDisplayText(
  apiName: string,
  data: Record<string, unknown>,
  recordId: number,
): Promise<string> {
  try {
    const metadata = await fetchMetadataCached(apiName)
    const fields = (metadata.fields ?? [])
      .filter((field) => field.status === 1)
      .sort((a, b) => a.sort - b.sort)
    const displayField =
      fields.find((field) => field.apiName === 'name') ??
      fields.find((field) => field.fieldType === 'TEXT') ??
      fields.find((field) => field.fieldType === 'TEXTAREA')
    const text = displayField ? data[displayField.apiName] : undefined
    if (text !== null && text !== undefined && text !== '') {
      return String(text)
    }
  } catch {
    // 元数据加载失败时退化为 #记录ID
  }
  return `#${recordId}`
}

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
      case 'REFERENCE':
        // 存储结构 { recordId, text } 原样回填，无默认值语义
        model[field.apiName] = hasStored ? stored : null
        break
      default:
        model[field.apiName] = hasStored ? stored : hasDefault ? defaultText : null
    }
  }
  return model
}
