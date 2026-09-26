<script setup lang="ts">
import type { FieldOption } from '@/types/field'

const options = defineModel<FieldOption[]>({ required: true })

function add() {
  options.value = [...options.value, { label: '', value: '' }]
}

function remove(index: number) {
  options.value = options.value.filter((_, i) => i !== index)
}
</script>

<template>
  <div class="select-option-editor">
    <div class="option-header">
      <span class="col-label">选项名称</span>
      <span class="col-value">选项值</span>
      <span class="col-action">操作</span>
    </div>
    <div v-for="(option, index) in options" :key="index" class="option-row">
      <el-input v-model="option.label" placeholder="如：待处理" />
      <el-input v-model="option.value" placeholder="如：pending" />
      <el-button link type="danger" @click="remove(index)">删除</el-button>
    </div>
    <el-button class="option-add" text type="primary" @click="add">+ 添加选项</el-button>
  </div>
</template>

<style scoped>
.select-option-editor {
  width: 100%;
  padding: var(--oe-space-2) var(--oe-space-3);
  border: 1px solid var(--oe-border-soft);
  border-radius: var(--oe-radius-md);
  background-color: var(--oe-surface-2);
}

.option-header {
  display: grid;
  grid-template-columns: 1fr 1fr 48px;
  gap: var(--oe-space-2);
  padding-bottom: var(--oe-space-2);
  margin-bottom: var(--oe-space-1);
  border-bottom: 1px solid var(--oe-border-soft);
  font-size: var(--el-font-size-extra-small);
  color: var(--oe-text-3);
}

.option-row {
  display: grid;
  grid-template-columns: 1fr 1fr 48px;
  align-items: center;
  gap: var(--oe-space-2);
  margin-bottom: var(--oe-space-2);
}

.option-row:last-of-type {
  margin-bottom: 0;
}

.option-add {
  padding-left: 0;
}
</style>
