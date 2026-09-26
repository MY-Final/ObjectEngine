<script setup lang="ts">
import type { CustomField } from '@/types/field'
import type { RecordFilterQuery } from '@/types/record'
import SearchBar from '@/components/common/SearchBar.vue'

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
  <SearchBar @search="emit('search')" @reset="handleReset">
    <el-input
      v-model="query.keyword"
      placeholder="搜索关键字"
      clearable
      class="w-240"
      @keyup.enter="emit('search')"
      @clear="emit('search')"
    />
  </SearchBar>
</template>
