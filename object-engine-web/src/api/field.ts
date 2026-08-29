import http from './http'
import type { CreateFieldPayload, CustomField, UpdateFieldPayload } from '@/types/field'

export function createField(apiName: string, data: CreateFieldPayload) {
  return http.post<CustomField>(`/custom-objects/${apiName}/fields`, data)
}

export function listFields(apiName: string) {
  return http.get<CustomField[]>(`/custom-objects/${apiName}/fields`)
}

export function updateField(apiName: string, fieldApiName: string, data: UpdateFieldPayload) {
  return http.put<CustomField>(`/custom-objects/${apiName}/fields/${fieldApiName}`, data)
}

export function deleteField(apiName: string, fieldApiName: string) {
  return http.delete<void>(`/custom-objects/${apiName}/fields/${fieldApiName}`)
}
