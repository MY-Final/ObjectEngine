<script setup lang="ts">
import { ref, watch } from 'vue'
import type { CustomField } from '@/types/field'
import type { LayoutConfig } from '@/types/layout'
import DynamicForm from '@/components/dynamic/DynamicForm.vue'

/**
 * 布局实时预览：必须复用 Runtime 的 DynamicForm（内含 FieldRenderer），
 * 保证后台预览效果与前台 /custom/:apiName 一致
 */
const props = defineProps<{
  fields: CustomField[]
  layout: LayoutConfig
}>()

const previewModel = ref<Record<string, unknown>>({})

function rebuild() {
  const model: Record<string, unknown> = {}
  for (const field of props.fields) {
    const defaultValue = field.defaultValue
    if (defaultValue == null || defaultValue === '') {
      model[field.apiName] = null
    } else if (field.fieldType === 'NUMBER' || field.fieldType === 'MONEY') {
      model[field.apiName] = Number(defaultValue)
    } else {
      model[field.apiName] = defaultValue
    }
  }
  previewModel.value = model
}

watch(
  () => [props.fields, props.layout],
  () => rebuild(),
  { immediate: true },
)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <span>Layout 预览</span>
    </template>
    <div class="layout-preview">
      <DynamicForm v-model="previewModel" :fields="fields" :layout="layout" />
    </div>
  </el-card>
</template>

<style scoped>
.layout-preview {
  max-height: 60vh;
  overflow: auto;
  padding: 4px;
}
</style>
