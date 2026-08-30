/** 动态数据记录：data 的 key 是字段 apiName，结构由 Metadata 决定，不能写死 */
export interface CustomRecord {
  id: number
  objectId: number
  data: Record<string, unknown>
  remark?: string
  createdAt: string
  updatedAt: string
  /** LOOKUP / REFERENCE 字段的展示值（字段API名称 → 展示文本/引用值），由后端批量计算 */
  displays?: Record<string, unknown> | null
}

/** 列表筛选条件：第一版只有关键字，后续按需扩展字段级筛选 */
export interface RecordFilterQuery {
  keyword: string
}
