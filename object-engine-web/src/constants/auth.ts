/** 登录态在 localStorage 的键 */
export const TOKEN_KEY = 'oe_token'

/** 当前登录用户信息在 localStorage 的键 */
export const AUTH_USER_KEY = 'oe_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export interface LoginUser {
  id: number
  username: string
  name: string
}

export function setLoginUser(user: LoginUser): void {
  localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user))
}

export function getLoginUser(): LoginUser | null {
  const raw = localStorage.getItem(AUTH_USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as LoginUser
  } catch {
    return null
  }
}

/** 清理全部登录态（token + 用户信息） */
export function clearLoginState(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(AUTH_USER_KEY)
}
