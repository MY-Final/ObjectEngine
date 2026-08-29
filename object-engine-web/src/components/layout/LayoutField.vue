<script setup lang="ts">
import type { CustomField } from '@/types/field'
import type { LayoutField as LayoutFieldConfig } from '@/types/layout'
import { ALLOWED_SPANS } from '@/utils/layout'

defineProps<{
  meta: CustomField
  layoutField: LayoutFieldConfig
  isFirst: boolean
  isLast: boolean
}>()

const emit = defineEmits<{
  up: []
  down: []
  remove: []
  'update:span': [span: number]
}>()

function onSpanChange(value: number | unknown) {
  emit('update:span', Number(value))
}
</script>

<template>
  <div class="layout-field">
    <el-button-group class="move-group">
      <el-button size="small" :disabled="isFirst" @click="emit('up')">↑</el-button>
      <el-button size="small" :disabled="isLast" @click="emit('down')">↓</el-button>
    </el-button-group>
    <span class="field-name">{{ meta.fieldName }}</span>
    <span class="field-api">{{ meta.apiName }}</span>
    <el-select
      :model-value="layoutField.span"
      size="small"
      style="width: 92px"
      @update:model-value="onSpanChange"
    >
      <el-option v-for="span in ALLOWED_SPANS" :key="span" :label="`span=${span}`" :value="span" />
    </el-select>
    <el-button size="small" text type="danger" @click="emit('remove')">移除</el-button>
  </div>
</template>

<style scoped>
.layout-field {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 8px;
  background-color: #fafafa;
}

.move-group {
  flex-shrink: 0;
}

.field-name {
  font-size: 13px;
  flex: 1;
}

.field-api {
  font-size: 12px;
  color: #909399;
}
</style>
