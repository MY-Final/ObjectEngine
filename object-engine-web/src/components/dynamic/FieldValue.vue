<script setup lang="ts">
import { computed } from 'vue'
import type { CustomField } from '@/types/field'

/**
 * 列表中的字段值展示：
 * - 空值显示 "-"
 * - MONEY 按千分位 + 两位小数展示
 * - SELECT 把 value 转成 options 里的 label，匹配不到时展示原始 value
 */
const props = defineProps<{
  field: CustomField
  value: unknown
}>()

const EMPTY_TEXT = '-'

const display = computed<string>(() => {
  const { value, field } = props
  if (value === null || value === undefined || value === '') {
    return EMPTY_TEXT
  }
  if (field.fieldType === 'MONEY') {
    const amount = Number(value)
    return Number.isNaN(amount)
      ? String(value)
      : amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  }
  if (field.fieldType === 'SELECT') {
    const option = field.configJson?.options?.find((item) => item.value === String(value))
    return option ? option.label : String(value)
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return String(value)
})
</script>

<template>
  <span>{{ display }}</span>
</template>
