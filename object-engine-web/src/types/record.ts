/** 动态数据记录：data 的 key 是字段 apiName，结构由 Metadata 决定，不能写死 */
export interface CustomRecord {
  id: number
  objectId: number
  data: Record<string, unknown>
  remark?: string
  createdAt: string
  updatedAt: string
}
