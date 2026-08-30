import http from './http'
import type { PageQuery, PageResult } from '@/types/api'
import type {
  CreateOptionPayload,
  CreateOptionSetPayload,
  OptionSet,
  OptionSetItem,
  UpdateOptionPayload,
  UpdateOptionSetPayload,
} from '@/types/optionSet'

/** 选项集分页列表（keyword 匹配名称 / API 名称） */
export function listOptionSets(params: PageQuery & { keyword?: string; status?: number }) {
  return http.get<PageResult<OptionSet>>('/option-sets', { params })
}

export function getOptionSet(id: number) {
  return http.get<OptionSet>(`/option-sets/${id}`)
}

export function createOptionSet(data: CreateOptionSetPayload) {
  return http.post<number>('/option-sets', data)
}

export function updateOptionSet(id: number, data: UpdateOptionSetPayload) {
  return http.put<void>(`/option-sets/${id}`, data)
}

export function deleteOptionSet(id: number) {
  return http.delete<void>(`/option-sets/${id}`)
}

/** 运行时使用：仅启用状态的选项，按 sort 升序 */
export function listEnabledOptions(optionSetId: number) {
  return http.get<OptionSetItem[]>(`/option-sets/${optionSetId}/options`)
}

export function createOption(optionSetId: number, data: CreateOptionPayload) {
  return http.post<number>(`/option-sets/${optionSetId}/options`, data)
}

export function updateOption(optionSetId: number, optionId: number, data: UpdateOptionPayload) {
  return http.put<void>(`/option-sets/${optionSetId}/options/${optionId}`, data)
}

export function deleteOption(optionSetId: number, optionId: number) {
  return http.delete<void>(`/option-sets/${optionSetId}/options/${optionId}`)
}
