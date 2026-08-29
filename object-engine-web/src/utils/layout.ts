import type { CustomField } from '@/types/field'
import type { LayoutConfig } from '@/types/layout'

export const ALLOWED_SPANS = [1, 2, 3, 4, 6, 8, 12]

export interface RenderableLayoutField {
  field: CustomField
  span: number
}

export interface RenderableLayoutSection {
  id: string
  title: string
  fields: RenderableLayoutField[]
}

export function enabledFieldsOf(fields: CustomField[]): CustomField[] {
  return fields.filter((field) => field.status !== 0)
}

function sortBySort<T extends { sort: number }>(items: T[]): T[] {
  return [...items].sort((a, b) => a.sort - b.sort)
}

export function clampSpan(span: number): number {
  return (ALLOWED_SPANS as readonly number[]).includes(span) ? span : 6
}

/** 无布局时的默认布局：启用字段 sort ASC，span=6，单个「基本信息」Section */
export function buildDefaultLayoutConfig(fields: CustomField[]): LayoutConfig {
  const enabled = sortBySort(enabledFieldsOf(fields))
  return {
    sections: [
      {
        id: 'default',
        title: '基本信息',
        sort: 1,
        columns: 12,
        fields: enabled.map((field, index) => ({
          fieldApiName: field.apiName,
          span: 6,
          sort: index + 1,
        })),
      },
    ],
  }
}

/**
 * Layout Metadata → 渲染结构：
 * 过滤停用 / 不存在的字段、跨 Section 去重、修正非法 span、跳过空 Section
 */
export function normalizeLayoutSections(
  config: LayoutConfig | undefined,
  fields: CustomField[],
): { sections: RenderableLayoutSection[]; droppedApiNames: string[] } {
  const fieldMap = new Map(enabledFieldsOf(fields).map((field) => [field.apiName, field]))
  const used = new Set<string>()
  const droppedApiNames: string[] = []
  const sections: RenderableLayoutSection[] = []

  for (const section of sortBySort(config?.sections ?? [])) {
    const renderFields: RenderableLayoutField[] = []
    for (const layoutField of sortBySort(section.fields ?? [])) {
      const field = fieldMap.get(layoutField.fieldApiName)
      if (!field || used.has(layoutField.fieldApiName)) {
        droppedApiNames.push(layoutField.fieldApiName)
        continue
      }
      used.add(layoutField.fieldApiName)
      renderFields.push({ field, span: clampSpan(layoutField.span) })
    }
    if (renderFields.length > 0) {
      sections.push({ id: section.id, title: section.title, fields: renderFields })
    }
  }
  return { sections, droppedApiNames }
}

/** 保存前清洗：删除无效字段与跨 Section 重复字段，并重新分配 sort */
export function prepareLayoutForSave(config: LayoutConfig, fields: CustomField[]): LayoutConfig {
  const validApiNames = new Set(enabledFieldsOf(fields).map((field) => field.apiName))
  const used = new Set<string>()
  const sections = config.sections.map((section, sectionIndex) => ({
    id: section.id,
    title: section.title.trim(),
    sort: sectionIndex + 1,
    columns: 12 as const,
    fields: section.fields
      .filter((layoutField) => validApiNames.has(layoutField.fieldApiName) && !used.has(layoutField.fieldApiName))
      .map((layoutField, fieldIndex) => {
        used.add(layoutField.fieldApiName)
        return {
          fieldApiName: layoutField.fieldApiName,
          span: clampSpan(layoutField.span),
          sort: fieldIndex + 1,
        }
      }),
  }))
  return { sections }
}

/** 保存前校验，返回错误列表（空数组代表通过）；清洗请在调用 validate 之前完成 */
export function validateLayoutForSave(config: LayoutConfig): string[] {
  const issues: string[] = []
  if (config.sections.length === 0) {
    issues.push('布局必须包含至少一个 Section')
  }
  const sectionIds = new Set<string>()
  for (const section of config.sections) {
    if (sectionIds.has(section.id)) {
      issues.push(`Section id 重复：${section.id}`)
    }
    sectionIds.add(section.id)
    if (!section.title.trim()) {
      issues.push(`Section 标题不能为空`)
    }
    for (const layoutField of section.fields) {
      if (layoutField.span < 1 || layoutField.span > 12) {
        issues.push(`字段 ${layoutField.fieldApiName} 的 span 必须在 1～12 之间`)
      }
    }
  }
  return issues
}
