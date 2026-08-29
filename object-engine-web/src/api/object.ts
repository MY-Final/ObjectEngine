import http from './http'
import type { PageQuery, PageResult } from '@/types/api'
import type {
  CreateObjectPayload,
  CustomObject,
  ObjectMetadata,
  UpdateObjectPayload,
} from '@/types/object'

export function createObject(data: CreateObjectPayload) {
  return http.post<CustomObject>('/custom-objects', data)
}

export function listObjects(params: PageQuery & { keyword?: string }) {
  return http.get<PageResult<CustomObject>>('/custom-objects', { params })
}

export function getObject(apiName: string) {
  return http.get<CustomObject>(`/custom-objects/${apiName}`)
}

export function updateObject(apiName: string, data: UpdateObjectPayload) {
  return http.put<CustomObject>(`/custom-objects/${apiName}`, data)
}

export function deleteObject(apiName: string) {
  return http.delete<void>(`/custom-objects/${apiName}`)
}

/** Metadata API：前端动态渲染的唯一数据来源 */
export function getMetadata(apiName: string) {
  return http.get<ObjectMetadata>(`/objects/${apiName}/metadata`)
}
