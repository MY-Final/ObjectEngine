import { ElDatePicker, ElInput, ElInputNumber, ElSelect } from 'element-plus'
import type { Component } from 'vue'
import type { FieldType } from '@/types/field'

/** 对象 / 字段 API 名称格式：小写字母开头，仅小写字母、数字、下划线 */
export const API_NAME_PATTERN = /^[a-z][a-z0-9_]*$/

export const API_NAME_HINT = 'API 名称只能以小写字母开头，只允许小写字母、数字和下划线'

export const FIELD_TYPE_OPTIONS: Array<{ label: string; value: FieldType }> = [
  { label: '文本', value: 'TEXT' },
  { label: '多行文本', value: 'TEXTAREA' },
  { label: '数字', value: 'NUMBER' },
  { label: '金额', value: 'MONEY' },
  { label: '日期', value: 'DATE' },
  { label: '下拉选择', value: 'SELECT' },
]

export const FIELD_TYPE_LABEL_MAP: Record<FieldType, string> = {
  TEXT: '文本',
  TEXTAREA: '多行文本',
  NUMBER: '数字',
  MONEY: '金额',
  DATE: '日期',
  SELECT: '下拉选择',
}

/**
 * FieldType → Element Plus 组件映射，FieldRenderer 依据它渲染，
 * 新增字段类型时只需要在这里扩展，不需要修改渲染组件
 */
export const FIELD_COMPONENT_MAP: Record<FieldType, Component> = {
  TEXT: ElInput,
  TEXTAREA: ElInput,
  NUMBER: ElInputNumber,
  MONEY: ElInputNumber,
  DATE: ElDatePicker,
  SELECT: ElSelect,
}
