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
    <!-- 字段名与 API 名称竖排：横向空间有限，竖排与各列表页的单元格写法保持一致 -->
    <div class="field-text">
      <span class="field-name">{{ meta.fieldName }}</span>
      <span class="field-api">{{ meta.apiName }}</span>
    </div>
    <el-select
      :model-value="layoutField.span"
      size="small"
      class="w-92"
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
  gap: var(--oe-space-2);
  margin-bottom: var(--oe-space-2);
  padding: 6px var(--oe-space-2);
  border: 1px solid var(--oe-border-soft);
  border-radius: var(--oe-radius-md);
  background-color: var(--oe-surface-2);
}

.move-group {
  flex-shrink: 0;
}

/* 中文可在任意字符间断行，必须显式 nowrap + 省略号，否则窄列里会被压成一列一个字 */
.field-text {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
}

.field-name {
  overflow: hidden;
  font-size: var(--el-font-size-small);
  color: var(--oe-text-1);
  white-space: nowrap;
  text-overflow: ellipsis;
}

.field-api {
  overflow: hidden;
  font-family: var(--oe-font-mono);
  font-size: var(--el-font-size-extra-small);
  color: var(--oe-text-3);
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
