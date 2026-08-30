import { ElDatePicker, ElInput, ElInputNumber, ElSelect, ElSwitch, ElTimePicker } from 'element-plus'
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
  { label: '百分比', value: 'PERCENT' },
  { label: '日期', value: 'DATE' },
  { label: '时间', value: 'TIME' },
  { label: '下拉选择', value: 'SELECT' },
  { label: '多选', value: 'MULTI_SELECT' },
  { label: '布尔', value: 'BOOLEAN' },
  { label: '电话', value: 'PHONE' },
  { label: '邮箱', value: 'EMAIL' },
  { label: '网址', value: 'URL' },
  { label: '关联关系', value: 'REFERENCE' },
]

export const FIELD_TYPE_LABEL_MAP: Record<FieldType, string> = {
  TEXT: '文本',
  TEXTAREA: '多行文本',
  NUMBER: '数字',
  MONEY: '金额',
  PERCENT: '百分比',
  DATE: '日期',
  TIME: '时间',
  SELECT: '下拉选择',
  MULTI_SELECT: '多选',
  REFERENCE: '关联关系',
  BOOLEAN: '布尔',
  PHONE: '电话',
  EMAIL: '邮箱',
  URL: '网址',
}

/**
 * FieldType → Element Plus 组件映射，FieldRenderer 依据它渲染，
 * 新增字段类型时只需要在这里扩展，不需要修改渲染组件。
 * REFERENCE 是自定义组件（远程搜索关联记录），由 FieldRenderer 模板特殊处理
 */
export const FIELD_COMPONENT_MAP: Partial<Record<FieldType, Component>> = {
  TEXT: ElInput,
  TEXTAREA: ElInput,
  PHONE: ElInput,
  EMAIL: ElInput,
  URL: ElInput,
  NUMBER: ElInputNumber,
  MONEY: ElInputNumber,
  PERCENT: ElInputNumber,
  DATE: ElDatePicker,
  TIME: ElTimePicker,
  SELECT: ElSelect,
  MULTI_SELECT: ElSelect,
  BOOLEAN: ElSwitch,
}
