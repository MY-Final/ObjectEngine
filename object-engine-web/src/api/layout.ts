import http from './http'
import type {
  LayoutType,
  ObjectLayout,
  ObjectLayoutRuntimeResponse,
  SaveLayoutPayload,
} from '@/types/layout'

export function listLayouts(apiName: string) {
  return http.get<ObjectLayout[]>(`/custom-objects/${apiName}/layouts`)
}

export function getLayout(apiName: string, layoutType: LayoutType) {
  return http.get<ObjectLayout | null>(`/custom-objects/${apiName}/layouts/${layoutType}`)
}

export function createLayout(apiName: string, data: SaveLayoutPayload) {
  return http.post<ObjectLayout>(`/custom-objects/${apiName}/layouts`, data)
}

/** 按布局类型保存，不存在则创建 */
export function saveLayout(apiName: string, layoutType: LayoutType, data: SaveLayoutPayload) {
  return http.put<ObjectLayout>(`/custom-objects/${apiName}/layouts/${layoutType}`, data)
}

export function deleteLayout(apiName: string, layoutType: LayoutType) {
  return http.delete<void>(`/custom-objects/${apiName}/layouts/${layoutType}`)
}

/** Runtime 布局：无布局时后端返回默认布局 */
export function getRuntimeLayout(apiName: string) {
  return http.get<ObjectLayoutRuntimeResponse>(`/objects/${apiName}/layout`, { silent: true })
}
