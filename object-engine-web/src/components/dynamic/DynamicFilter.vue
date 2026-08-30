<script setup lang="ts">
import type { CustomField } from '@/types/field'
import type { RecordFilterQuery } from '@/types/record'

defineProps<{
  /** 预留：字段级筛选项按 metadata 生成，第一版仅关键字 */
  fields?: CustomField[]
}>()

const query = defineModel<RecordFilterQuery>({ required: true })
const emit = defineEmits<{ search: []; reset: [] }>()

function handleReset() {
  query.value = { keyword: '' }
  emit('reset')
}
</script>

<template>
  <div class="filter-bar">
    <el-input
      v-model="query.keyword"
      class="filter-input"
      placeholder="搜索关键字"
      clearable
      @keyup.enter="emit('search')"
      @clear="emit('search')"
    />
    <el-button @click="emit('search')">搜索</el-button>
    <el-button @click="handleReset">重置</el-button>
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.filter-input {
  width: 240px;
}
</style>
