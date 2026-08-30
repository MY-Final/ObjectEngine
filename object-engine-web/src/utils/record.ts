import { getMetadata, listObjects } from '@/api/object'
import { listFields } from '@/api/field'
import { getRecord } from '@/api/record'
import type { CustomObject, ObjectMetadata } from '@/types/object'
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

/** 启用中的自定义对象列表（模块级缓存） */
let objectsPromise: Promise<CustomObject[]> | null = null

export function fetchObjectsCached(): Promise<CustomObject[]> {
  if (!objectsPromise) {
    objectsPromise = listObjects({ page: 1, pageSize: 100 }).then(
      (result) => result.records.filter((object) => object.status === 1),
      (error) => {
        objectsPromise = null
        throw error
      },
    )
  }
  return objectsPromise
}

/** 按对象 ID 解析 API 名称（配合 relationObjectId 使用） */
export async function objectApiNameById(objectId: number): Promise<string | undefined> {
  const objects = await fetchObjectsCached()
  return objects.find((object) => object.id === objectId)?.apiName
}

/** 对象字段列表（模块级缓存，按 apiName） */
const fieldsCache = new Map<string, Promise<CustomField[]>>()

export function fetchFieldsCached(apiName: string): Promise<CustomField[]> {
  let promise = fieldsCache.get(apiName)
  if (!promise) {
    promise = listFields(apiName)
    fieldsCache.set(apiName, promise)
    promise.catch(() => fieldsCache.delete(apiName))
  }
  return promise
}

/**
 * 实时解析 REFERENCE 字段值：关联记录 ID → 目标对象 → 目标字段值。
 * 供 DynamicForm 在 LOOKUP 变更时联动刷新
 */
export async function resolveReferenceValue(
  relationObjectId: number,
  relationRecordId: number,
  referenceFieldId: number,
): Promise<unknown> {
  const apiName = await objectApiNameById(relationObjectId)
  if (!apiName) return null
  const fields = await fetchFieldsCached(apiName)
  const referenceField = fields.find((field) => field.id === referenceFieldId)
  if (!referenceField) return null
  const record = await getRecord(apiName, relationRecordId)
  return record.data?.[referenceField.apiName] ?? null
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
      case 'LOOKUP':
        // 存储为关联记录 ID；兼容历史 { recordId } 快照结构
        model[field.apiName] = hasStored
          ? typeof stored === 'number'
            ? stored
            : typeof stored === 'object' &&
                (stored as { recordId?: unknown }).recordId !== undefined &&
                (stored as { recordId?: unknown }).recordId !== null
              ? (stored as { recordId: number }).recordId
              : null
          : null
        break
      case 'REFERENCE':
        // 运行时计算字段，不参与表单模型（表单只读展示走 displays）
        model[field.apiName] = null
        break
      default:
        model[field.apiName] = hasStored ? stored : hasDefault ? defaultText : null
    }
  }
  return model
}
