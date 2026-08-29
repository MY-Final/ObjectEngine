/** 后端统一响应体：{ code, message, data } */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

/** 后端统一分页结构：{ records, total, page, pageSize } */
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

/** 分页查询参数 */
export interface PageQuery {
  page?: number
  pageSize?: number
}
