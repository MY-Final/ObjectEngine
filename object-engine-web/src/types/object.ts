import type { CustomField } from './field'

export interface CustomObject {
  id: number
  apiName: string
  objectName: string
  description?: string
  remark?: string
  icon?: string
  status: number
  sort: number
  createdAt: string
  updatedAt: string
}

/** 完整 Metadata：前端动态渲染的核心类型 */
export interface ObjectMetadata {
  object: CustomObject
  fields: CustomField[]
}

export interface CreateObjectPayload {
  apiName: string
  objectName: string
  description?: string
  remark?: string
  icon?: string
  sort?: number
}

export interface UpdateObjectPayload {
  objectName?: string
  description?: string
  remark?: string
  icon?: string
  sort?: number
  status?: number
}
