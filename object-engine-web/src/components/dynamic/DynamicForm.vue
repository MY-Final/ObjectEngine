<script setup lang="ts">
import { computed, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { CustomField } from '@/types/field'
import FieldRenderer from './FieldRenderer.vue'

/**
 * 动态表单：fields → ElFormItem → FieldRenderer，
 * required 规则与表单校验都在这里，父组件通过 validate() 触发校验
 */
const props = defineProps<{
  fields: CustomField[]
  modelValue: Record<string, unknown>
}>()

const emit = defineEmits<{ 'update:modelValue': [value: Record<string, unknown>] }>()

const formRef = ref<FormInstance>()

const rules = computed<FormRules>(() => {
  const result: FormRules = {}
  for (const field of props.fields) {
    if (field.requiredFlag !== 1) continue
    const isChoice = field.fieldType === 'SELECT' || field.fieldType === 'DATE'
    result[field.apiName] = [
      {
        required: true,
        message: isChoice ? `请选择${field.fieldName}` : `请输入${field.fieldName}`,
        trigger: isChoice ? 'change' : 'blur',
      },
    ]
  }
  return result
})

function updateValue(apiName: string, value: unknown) {
  emit('update:modelValue', { ...props.modelValue, [apiName]: value })
}

async function validate(): Promise<boolean> {
  return (await formRef.value?.validate().then(() => true).catch(() => false)) ?? false
}

defineExpose({ validate })
</script>

<template>
  <el-form ref="formRef" :model="modelValue" :rules="rules" label-width="auto">
    <el-form-item
      v-for="field in fields"
      :key="field.apiName"
      :label="field.fieldName"
      :prop="field.apiName"
    >
      <FieldRenderer
        :field="field"
        :model-value="modelValue[field.apiName]"
        @update:model-value="updateValue(field.apiName, $event)"
      />
    </el-form-item>
  </el-form>
</template>
