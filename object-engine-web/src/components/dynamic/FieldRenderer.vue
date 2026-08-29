<script setup lang="ts">
import { computed } from 'vue'
import type { CustomField } from '@/types/field'
import { FIELD_COMPONENT_MAP } from '@/constants/field'

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

// 各字段类型的组件附加属性（placeholder / precision / value-format 等）
const extraProps = computed<Record<string, unknown>>(() => {
  const name = props.field.fieldName
  switch (props.field.fieldType) {
    case 'TEXT':
      return { placeholder: `请输入${name}`, clearable: true }
    case 'TEXTAREA':
      return { type: 'textarea', rows: 4, placeholder: `请输入${name}` }
    case 'NUMBER':
      return { controlsPosition: 'right', style: 'width: 100%' }
    case 'MONEY':
      return { precision: 2, controlsPosition: 'right', style: 'width: 100%', placeholder: `请输入${name}` }
    case 'DATE':
      return { type: 'date', valueFormat: 'YYYY-MM-DD', placeholder: `请选择${name}`, style: 'width: 100%' }
    case 'SELECT':
      return { placeholder: `请选择${name}`, clearable: true, style: 'width: 100%' }
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
  <component
    :is="component"
    :model-value="modelValue"
    v-bind="extraProps"
    @update:model-value="handleUpdate"
  >
    <template v-if="field.fieldType === 'SELECT'">
      <el-option
        v-for="option in options"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </template>
  </component>
</template>
