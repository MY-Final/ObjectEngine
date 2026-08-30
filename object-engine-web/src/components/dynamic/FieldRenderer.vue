<script setup lang="ts">
import { computed } from 'vue'
import type { CustomField } from '@/types/field'
import { FIELD_COMPONENT_MAP } from '@/constants/field'
import LookupSelect from './LookupSelect.vue'

/**
 * Runtime 核心组件：根据 field.fieldType 从 FIELD_COMPONENT_MAP
 * 选择 Element Plus 组件渲染，并向上抛出 update:modelValue
 */
const props = defineProps<{
  field: CustomField
  modelValue?: unknown
}>()

const emit = defineEmits<{ 'update:modelValue': [value: unknown] }>()

const component = computed(() => FIELD_COMPONENT_MAP[props.field.fieldType])

/** 数字类约束：scale 映射为 precision，integerLength 换算成对称的 min/max；scale 取默认值时强制 precision */
function digitProps(defaultScale?: number): Record<string, unknown> {
  const config = props.field.configJson ?? {}
  const scale = typeof config.scale === 'number' ? config.scale : defaultScale
  const result: Record<string, unknown> = { controlsPosition: 'right', style: 'width: 100%' }
  if (scale != null) {
    result.precision = scale
  }
  if (typeof config.integerLength === 'number') {
    const s = scale ?? 0
    const bound = 10 ** (config.integerLength - s) - 10 ** -s
    result.max = bound
    result.min = -bound
  }
  return result
}

// 各字段类型的组件附加属性（placeholder / precision / value-format 等）
const extraProps = computed<Record<string, unknown>>(() => {
  const name = props.field.fieldName
  const config = props.field.configJson ?? {}
  switch (props.field.fieldType) {
    case 'TEXT':
    case 'TEXTAREA': {
      const result: Record<string, unknown> = { placeholder: `请输入${name}`, clearable: true }
      if (typeof config.maxLength === 'number') {
        result.maxlength = config.maxLength
        result.showWordLimit = true
      }
      if (props.field.fieldType === 'TEXTAREA') {
        result.type = 'textarea'
        result.rows = 4
      }
      return result
    }
    case 'PHONE':
    case 'EMAIL':
      return { placeholder: `请输入${name}`, clearable: true }
    case 'URL':
      return { placeholder: `请输入${name}（以 http(s):// 开头）`, clearable: true }
    case 'NUMBER':
      return digitProps(undefined)
    case 'PERCENT':
      return digitProps(0)
    case 'MONEY':
      return { ...digitProps(2), placeholder: `请输入${name}` }
    case 'DATE':
      return { type: 'date', valueFormat: 'YYYY-MM-DD', placeholder: `请选择${name}`, style: 'width: 100%' }
    case 'DATETIME':
      return {
        type: 'datetime',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        placeholder: `请选择${name}`,
        style: 'width: 100%',
      }
    case 'AUTO_NUMBER':
      return { disabled: true, placeholder: '保存后由系统按格式自动生成' }
    case 'TIME':
      return { valueFormat: 'HH:mm:ss', placeholder: `请选择${name}`, style: 'width: 100%' }
    case 'SELECT':
      return { placeholder: `请选择${name}`, clearable: true, style: 'width: 100%' }
    case 'MULTI_SELECT':
      return { multiple: true, placeholder: `请选择${name}`, clearable: true, style: 'width: 100%' }
    default:
      return {}
  }
})

const options = computed(() => props.field.configJson?.options ?? [])

function handleUpdate(value: unknown) {
  // 输入组件清空时抛出 undefined，统一归一化为 null
  emit('update:modelValue', value ?? null)
}
</script>

<template>
  <!-- LOOKUP 走自定义远程搜索组件，其余按类型映射到 Element Plus 组件 -->
  <LookupSelect
    v-if="field.fieldType === 'LOOKUP'"
    :field="field"
    :model-value="modelValue"
    @update:model-value="handleUpdate"
  />
  <component
    v-else-if="component"
    :is="component"
    :model-value="modelValue"
    v-bind="extraProps"
    @update:model-value="handleUpdate"
  >
    <template v-if="field.fieldType === 'SELECT' || field.fieldType === 'MULTI_SELECT'">
      <el-option
        v-for="option in options"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </template>
  </component>
</template>
