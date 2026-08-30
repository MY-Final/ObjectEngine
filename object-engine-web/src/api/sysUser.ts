import http from './http'
import type { PageQuery, PageResult } from '@/types/api'
import type { CreateSysUserPayload, SysUserItem, UpdateSysUserPayload } from '@/types/sysUser'

/** 用户分页列表（keyword 匹配用户名 / 姓名） */
export function listSysUsers(params: PageQuery & { keyword?: string; status?: number }) {
  return http.get<PageResult<SysUserItem>>('/sys-users', { params })
}

export function createSysUser(data: CreateSysUserPayload) {
  return http.post<SysUserItem>('/sys-users', data)
}

export function updateSysUser(id: number, data: UpdateSysUserPayload) {
  return http.put<SysUserItem>(`/sys-users/${id}`, data)
}

export function updateSysUserStatus(id: number, status: number) {
  return http.put<void>(`/sys-users/${id}/status`, { status })
}

export function resetSysUserPassword(id: number, password: string) {
  return http.put<void>(`/sys-users/${id}/password`, { password })
}

export function deleteSysUser(id: number) {
  return http.delete<void>(`/sys-users/${id}`)
}
