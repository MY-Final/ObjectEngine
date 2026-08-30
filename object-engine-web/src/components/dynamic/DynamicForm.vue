<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { CustomField } from '@/types/field'
import type { LayoutConfig } from '@/types/layout'
import { normalizeLayoutSections } from '@/utils/layout'
import { resolveReferenceValue } from '@/utils/record'
import FieldRenderer from './FieldRenderer.vue'

/**
 * 动态表单：
 * - 只传 fields 时按 sort 顺序平铺渲染
 * - 传入 layout 时按 layout.sections 分 Section / 分列（el-row + el-col span）渲染
 * - REFERENCE 字段只读展示（displays 为编辑时后端计算值，LOOKUP 变更时实时联动刷新）
 * required 规则与表单校验都在这里，父组件通过 validate() 触发校验
 */
const props = defineProps<{
  fields: CustomField[]
  modelValue: Record<string, unknown>
  layout?: LayoutConfig
  /** LOOKUP / REFERENCE 展示值（编辑时来自 Record 查询结果） */
  displays?: Record<string, unknown> | null
}>()

const emit = defineEmits<{ 'update:modelValue': [value: Record<string, unknown>] }>()

const formRef = ref<FormInstance>()

/** LOOKUP 变更后实时解析出的 REFERENCE 值（覆盖 displays） */
const liveDisplays = ref<Record<string, unknown>>({})
const mergedDisplays = computed(() => ({ ...(props.displays ?? {}), ...liveDisplays.value }))

watch(
  () => props.displays,
  () => {
    // 切换记录时丢弃联动缓存，以后端计算值为准
    liveDisplays.value = {}
  },
)

const rules = computed<FormRules>(() => {
  const result: FormRules = {}
  for (const field of props.fields) {
    const rulesForField: FormRules[string] = []
    if (field.fieldType === 'REFERENCE') {
      // 运行时计算字段，不可编辑也不做必填校验
      continue
    }
    if (field.requiredFlag === 1) {
    // 选择类控件变更即触发校验，提示语用「请选择」
    const isChoice =
      ['SELECT', 'MULTI_SELECT', 'DATE', 'TIME', 'BOOLEAN', 'LOOKUP'].includes(field.fieldType)
      rulesForField.push({
        required: true,
        message: isChoice ? `请选择${field.fieldName}` : `请输入${field.fieldName}`,
        trigger: isChoice ? 'change' : 'blur',
      })
    }
    if (field.fieldType === 'TEXT' || field.fieldType === 'TEXTAREA') {
      const { maxLength, minLength } = field.configJson ?? {}
      if (typeof maxLength === 'number' || typeof minLength === 'number') {
        rulesForField.push({
          min: typeof minLength === 'number' ? minLength : undefined,
          max: typeof maxLength === 'number' ? maxLength : undefined,
          message: `${field.fieldName}长度需在 ${minLength ?? 0} ~ ${maxLength ?? '不限'} 字之间`,
          trigger: 'blur',
        })
      }
    }
    if (rulesForField.length > 0) {
      result[field.apiName] = rulesForField
    }
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
  // LOOKUP 变更时实时刷新依赖它的 REFERENCE 字段展示值
  const changed = props.fields.find((field) => field.apiName === apiName)
  if (changed?.fieldType === 'LOOKUP') {
    void refreshReferences(changed, value)
  }
}

/** 实时解析 LOOKUP 指向记录的引用字段值（每条引用一次后端调用，仅在 LOOKUP 变更时触发） */
async function refreshReferences(lookupField: CustomField, value: unknown) {
  const recordId =
    typeof value === 'number'
      ? value
      : value && typeof value === 'object' && typeof (value as { recordId?: unknown }).recordId === 'number'
        ? (value as { recordId: number }).recordId
        : null
  for (const reference of props.fields.filter(
    (field) => field.fieldType === 'REFERENCE' && field.relationFieldId === lookupField.id,
  )) {
    if (recordId == null || !reference.relationObjectId || !reference.referenceFieldId) {
      liveDisplays.value = { ...liveDisplays.value, [reference.apiName]: null }
      continue
    }
    try {
      liveDisplays.value = {
        ...liveDisplays.value,
        [reference.apiName]: await resolveReferenceValue(
          reference.relationObjectId,
          recordId,
          reference.referenceFieldId,
        ),
      }
    } catch {
      liveDisplays.value = { ...liveDisplays.value, [reference.apiName]: null }
    }
  }
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
              <el-input
                v-if="renderField.field.fieldType === 'REFERENCE'"
                :model-value="String(mergedDisplays[renderField.field.apiName] ?? '')"
                disabled
                placeholder="选择关联记录后自动带出"
              />
              <FieldRenderer
                v-else
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
        <el-input
          v-if="field.fieldType === 'REFERENCE'"
          :model-value="String(mergedDisplays[field.apiName] ?? '')"
          disabled
          placeholder="选择关联记录后自动带出"
        />
        <FieldRenderer
          v-else
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
