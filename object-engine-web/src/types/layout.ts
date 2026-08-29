import type { CustomField } from './field'
import type { CustomObject } from './object'

export type LayoutType = 'FRONTEND' | 'BACKEND'

/** 布局中的字段引用：只描述「放在哪里」，字段定义仍在 Field Metadata */
export interface LayoutField {
  fieldApiName: string
  span: number
  sort: number
}

export interface LayoutSection {
  id: string
  title: string
  sort: number
  columns: 12
  fields: LayoutField[]
}

export interface LayoutConfig {
  sections: LayoutSection[]
}

export interface ObjectLayout {
  id: number
  objectApiName: string
  layoutName: string
  layoutType: LayoutType
  configJson: LayoutConfig
  status: number
  sort: number
  remark?: string
}

export interface SaveLayoutPayload {
  layoutName?: string
  layoutType?: LayoutType
  status?: number
  sort?: number
  remark?: string
  configJson: LayoutConfig
}

/** Runtime 布局响应：GET /api/v1/objects/{apiName}/layout */
export interface ObjectLayoutRuntimeResponse {
  object: CustomObject
  layout: LayoutConfig
  fields: CustomField[]
}
