<script setup lang="ts">
import { computed, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { CustomField } from '@/types/field'
import type { LayoutConfig } from '@/types/layout'
import { normalizeLayoutSections } from '@/utils/layout'
import FieldRenderer from './FieldRenderer.vue'

/**
 * 动态表单：
 * - 只传 fields 时按 sort 顺序平铺渲染
 * - 传入 layout 时按 layout.sections 分 Section / 分列（el-row + el-col span）渲染
 * required 规则与表单校验都在这里，父组件通过 validate() 触发校验
 */
const props = defineProps<{
  fields: CustomField[]
  modelValue: Record<string, unknown>
  layout?: LayoutConfig
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

// Layout → 渲染结构：过滤停用/不存在字段、跨 Section 去重、修正非法 span
const layoutSections = computed(() => {
  if (!props.layout) return null
  return normalizeLayoutSections(props.layout, props.fields).sections
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
    <template v-if="layoutSections">
      <div v-for="section in layoutSections" :key="section.id" class="dyn-section">
        <div class="dyn-section-title">{{ section.title }}</div>
        <el-row :gutter="16">
          <el-col
            v-for="renderField in section.fields"
            :key="renderField.field.apiName"
            :span="renderField.span"
          >
            <el-form-item :label="renderField.field.fieldName" :prop="renderField.field.apiName">
              <FieldRenderer
                :field="renderField.field"
                :model-value="modelValue[renderField.field.apiName]"
                @update:model-value="updateValue(renderField.field.apiName, $event)"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </template>
    <template v-else>
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
    </template>
  </el-form>
</template>

<style scoped>
.dyn-section {
  margin-bottom: 8px;
}

.dyn-section-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid var(--el-color-primary);
}
</style>
