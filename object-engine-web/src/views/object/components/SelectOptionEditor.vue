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
    <div v-for="(option, index) in options" :key="index" class="option-row">
      <el-input v-model="option.label" placeholder="选项名称，如：待处理" />
      <el-input v-model="option.value" placeholder="选项值，如：pending" />
      <el-button text type="danger" @click="remove(index)">删除</el-button>
    </div>
    <el-button text type="primary" @click="add">+ 添加选项</el-button>
  </div>
</template>

<style scoped>
.option-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.option-row .el-input {
  flex: 1;
}
</style>
