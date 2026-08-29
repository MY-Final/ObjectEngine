import http from './http'
import type { PageQuery, PageResult } from '@/types/api'
import type { CustomRecord } from '@/types/record'

/** 请求体直接是 fieldApiName -> value，不需要包一层 data */
export function createRecord(apiName: string, data: Record<string, unknown>) {
  return http.post<CustomRecord>(`/objects/${apiName}/records`, data)
}

export function listRecords(apiName: string, params: PageQuery) {
  return http.get<PageResult<CustomRecord>>(`/objects/${apiName}/records`, { params })
}

export function getRecord(apiName: string, id: number) {
  return http.get<CustomRecord>(`/objects/${apiName}/records/${id}`)
}

export function deleteRecord(apiName: string, id: number) {
  return http.delete<void>(`/objects/${apiName}/records/${id}`)
}
