export interface SysUserItem {
  id: number
  username: string
  name: string
  email?: string | null
  phone?: string | null
  status: number
  remark?: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateSysUserPayload {
  username: string
  password: string
  name: string
  email?: string
  phone?: string
  remark?: string
}

export interface UpdateSysUserPayload {
  name?: string
  email?: string
  phone?: string
  remark?: string
}
