import http from './http'
import type { LoginPayload } from '@/types/auth'
import type { AuthUser, LoginResult } from '@/types/auth'

export function login(data: LoginPayload) {
  return http.post<LoginResult>('/auth/login', data)
}

export function logout() {
  return http.post<void>('/auth/logout')
}

export function getMe() {
  return http.get<AuthUser>('/auth/me')
}
