import http from './http'
import type { PageQuery, PageResult } from '@/types/api'
import type { CustomRecord } from '@/types/record'

/** 请求体直接是 fieldApiName -> value，不需要包一层 data */
export function createRecord(apiName: string, data: Record<string, unknown>) {
  return http.post<CustomRecord>(`/objects/${apiName}/records`, data)
}

export function listRecords(apiName: string, params: PageQuery & { keyword?: string }) {
  return http.get<PageResult<CustomRecord>>(`/objects/${apiName}/records`, { params })
}

export function getRecord(apiName: string, id: number) {
  return http.get<CustomRecord>(`/objects/${apiName}/records/${id}`)
}

/** 更新为全量替换，请求体与创建一致：fieldApiName -> value */
export function updateRecord(apiName: string, id: number, data: Record<string, unknown>) {
  return http.put<CustomRecord>(`/objects/${apiName}/records/${id}`, data)
}

export function deleteRecord(apiName: string, id: number) {
  return http.delete<void>(`/objects/${apiName}/records/${id}`)
}
