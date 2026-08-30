/** 登录请求 */
export interface LoginPayload {
  username: string
  password: string
}

/** 当前登录用户 */
export interface AuthUser {
  id: number
  username: string
  name: string
}

/** 登录结果：token + 用户信息 */
export interface LoginResult {
  token: string
  user: AuthUser
}
