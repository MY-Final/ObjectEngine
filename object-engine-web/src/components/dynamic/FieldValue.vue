<script setup lang="ts">
import { computed } from 'vue'
import type { CustomField } from '@/types/field'
import type { CustomRecord } from '@/types/record'

/**
 * 列表中的字段值展示：
 * - 空值显示 "-"
 * - LOOKUP / REFERENCE 优先读后端批量计算的 displays（记录展示文本 / 引用值），data 里是记录 ID
 * - MONEY 按千分位 + 两位小数展示，NUMBER / PERCENT 按千分位展示（PERCENT 追加 %）
 * - SELECT 把 value 转成 options 里的 label，MULTI_SELECT 逐项转换后用「、」连接
 * - BOOLEAN 显示 是 / 否，DATE 兼容带时间的 ISO 字符串，URL 渲染成新窗口链接
 */
const props = defineProps<{
  field: CustomField
  value: unknown
  /** 所在记录（LOOKUP / REFERENCE 的展示值从 record.displays 取） */
  record?: CustomRecord
}>()

const EMPTY_TEXT = '-'

/** 关联/引用字段的展示值：后端 enrich 的 displays 优先 */
const relationDisplay = computed<string | null>(() => {
  if (props.field.fieldType !== 'LOOKUP' && props.field.fieldType !== 'REFERENCE') {
    return null
  }
  const display = props.record?.displays?.[props.field.apiName]
  if (display === null || display === undefined || display === '') {
    return EMPTY_TEXT
  }
  return String(display)
})

function toOptionLabel(item: unknown): string {
  const option = props.field.configJson?.options?.find((candidate) => candidate.value === String(item))
  return option ? option.label : String(item)
}

/** 小数位数：配置了 scale 用配置值，否则用类型默认（MONEY 2，其余不定位数） */
function scaleOf(fallback: number | null): number | null {
  const scale = props.field.configJson?.scale
  return typeof scale === 'number' ? scale : fallback
}

const display = computed<string>(() => {
  if (relationDisplay.value !== null) {
    return relationDisplay.value
  }
  const { value, field } = props
  if (value === null || value === undefined || value === '') {
    return EMPTY_TEXT
  }
  if (field.fieldType === 'LOOKUP') {
    // displays 缺失时兜底显示 #记录ID，不把 ID 假装成文本
    return typeof value === 'number' ? `#${value}` : String(value)
  }
  if (field.fieldType === 'MONEY') {
    const amount = Number(value)
    if (Number.isNaN(amount)) {
      return String(value)
    }
    const scale = scaleOf(2) ?? 2
    return amount.toLocaleString('zh-CN', { minimumFractionDigits: scale, maximumFractionDigits: scale })
  }
  if (field.fieldType === 'NUMBER') {
    const amount = Number(value)
    if (Number.isNaN(amount)) {
      return String(value)
    }
    const scale = scaleOf(null)
    return scale == null
      ? amount.toLocaleString('zh-CN')
      : amount.toLocaleString('zh-CN', { minimumFractionDigits: scale, maximumFractionDigits: scale })
  }
  if (field.fieldType === 'PERCENT') {
    const amount = Number(value)
    if (Number.isNaN(amount)) {
      return String(value)
    }
    const scale = scaleOf(0) ?? 0
    return `${amount.toLocaleString('zh-CN', { minimumFractionDigits: scale, maximumFractionDigits: scale })}%`
  }
  if (field.fieldType === 'SELECT') {
    return toOptionLabel(value)
  }
  if (field.fieldType === 'MULTI_SELECT') {
    if (!Array.isArray(value) || value.length === 0) {
      return EMPTY_TEXT
    }
    return value.map(toOptionLabel).join('、')
  }
  if (field.fieldType === 'BOOLEAN') {
    return value === true || value === 'true' ? '是' : '否'
  }
  if (field.fieldType === 'REFERENCE') {
    if (typeof value === 'object' && value !== null) {
      const { recordId, text } = value as { recordId?: unknown; text?: unknown }
      if (text !== null && text !== undefined && text !== '') {
        return String(text)
      }
      if (recordId !== null && recordId !== undefined) {
        return `#${String(recordId)}`
      }
    }
    if (typeof value === 'number') {
      return `#${value}`
    }
    return String(value)
  }
  if (field.fieldType === 'DATE') {
    return String(value).replace(/T.*$/, '')
  }
  if (field.fieldType === 'DATETIME') {
    return String(value).replace('T', ' ')
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return String(value)
})

/** URL 字段渲染成新窗口链接 */
const isLink = computed(() => props.field.fieldType === 'URL' && display.value !== EMPTY_TEXT)
</script>

<template>
  <a v-if="isLink" :href="display" target="_blank" rel="noopener noreferrer">{{ display }}</a>
  <span v-else>{{ display }}</span>
</template>
