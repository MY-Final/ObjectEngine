<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CustomField } from '@/types/field'
import type { LayoutField as LayoutFieldConfig, LayoutSection as LayoutSectionConfig } from '@/types/layout'
import LayoutField from './LayoutField.vue'

const props = defineProps<{
  section: LayoutSectionConfig
  fields: CustomField[]
  unusedFields: CustomField[]
  isFirst: boolean
  isLast: boolean
}>()

const emit = defineEmits<{
  update: [section: LayoutSectionConfig]
  remove: []
  up: []
  down: []
}>()

const selectedApiName = ref('')

const fieldMetas = computed(() => {
  const map = new Map<string, CustomField>()
  for (const field of props.fields) {
    map.set(field.apiName, field)
  }
  return map
})

// 字段行：meta 可能为空（Layout 中残留了已删除/停用的字段），此时仍可渲染并移除
const rows = computed(() =>
  props.section.fields.map((layoutField, index) => ({
    index,
    layoutField,
    meta: fieldMetas.value.get(layoutField.fieldApiName),
  })),
)

function emitUpdate(fields: LayoutFieldConfig[]) {
  emit('update', { ...props.section, fields })
}

function addField() {
  const field = props.unusedFields.find((item) => item.apiName === selectedApiName.value)
  if (!field) return
  emitUpdate([
    ...props.section.fields,
    { fieldApiName: field.apiName, span: 6, sort: props.section.fields.length + 1 },
  ])
  selectedApiName.value = ''
}

function updateField(index: number, next: LayoutFieldConfig) {
  emitUpdate(props.section.fields.map((field, i) => (i === index ? next : field)))
}

function moveField(index: number, offset: -1 | 1) {
  const target = index + offset
  if (target < 0 || target >= props.section.fields.length) return
  const fields = [...props.section.fields]
  const moved = fields.splice(index, 1)[0]
  if (moved === undefined) return
  fields.splice(target, 0, moved)
  emitUpdate(fields)
}

function removeField(index: number) {
  emitUpdate(props.section.fields.filter((_, i) => i !== index))
}
</script>

<template>
  <el-card class="layout-section" shadow="never">
    <template #header>
      <div class="section-header">
        <el-input
          :model-value="section.title"
          size="small"
          style="width: 200px"
          @update:model-value="emit('update', { ...section, title: String($event) })"
        />
        <span class="section-hint">标题可直接编辑</span>
        <el-button-group>
          <el-button size="small" :disabled="isFirst" @click="emit('up')">↑</el-button>
          <el-button size="small" :disabled="isLast" @click="emit('down')">↓</el-button>
        </el-button-group>
        <el-button size="small" text type="danger" @click="emit('remove')">删除</el-button>
      </div>
    </template>

    <div v-for="rowItem in rows" :key="rowItem.layoutField.fieldApiName" class="layout-field-row">
      <LayoutField
        v-if="rowItem.meta"
        :meta="rowItem.meta"
        :layout-field="rowItem.layoutField"
        :is-first="rowItem.index === 0"
        :is-last="rowItem.index === section.fields.length - 1"
        @up="moveField(rowItem.index, -1)"
        @down="moveField(rowItem.index, 1)"
        @remove="removeField(rowItem.index)"
        @update:span="updateField(rowItem.index, { ...rowItem.layoutField, span: $event })"
      />
      <div v-else class="layout-field layout-field-invalid">
        <span>已失效字段：{{ rowItem.layoutField.fieldApiName }}（已自动忽略）</span>
        <el-button size="small" text type="danger" @click="removeField(rowItem.index)">移除</el-button>
      </div>
    </div>

    <div v-if="section.fields.length === 0" class="section-empty">此 Section 还没有字段</div>

    <div class="section-add">
      <el-select
        v-model="selectedApiName"
        size="small"
        filterable
        placeholder="选择未使用字段"
        style="width: 220px"
      >
        <el-option
          v-for="field in unusedFields"
          :key="field.apiName"
          :label="`${field.fieldName}（${field.apiName}）`"
          :value="field.apiName"
        />
      </el-select>
      <el-button size="small" type="primary" plain :disabled="!selectedApiName" @click="addField">
        添加到本 Section
      </el-button>
    </div>
  </el-card>
</template>

<style scoped>
.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-hint {
  flex: 1;
  font-size: 12px;
  color: #c0c4cc;
}

.layout-field-row:empty {
  display: none;
}

.layout-field-invalid {
  justify-content: space-between;
  color: #909399;
  background-color: #fdf6ec;
  border-color: #faecd8;
}

.section-empty {
  font-size: 12px;
  color: #909399;
  padding: 8px 0;
}

.section-add {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}
</style>
