<script setup lang="ts">
import type { CustomField } from '@/types/field'
import type { CustomRecord } from '@/types/record'
import FieldValue from './FieldValue.vue'

withDefaults(
  defineProps<{
    fields: CustomField[]
    records: CustomRecord[]
    loading?: boolean
    deletingId?: number | null
  }>(),
  { loading: false, deletingId: null },
)

const emit = defineEmits<{ delete: [record: CustomRecord] }>()
</script>

<template>
  <!-- 动态列：只渲染 metadata.fields，禁止写死任何业务字段 -->
  <el-table v-loading="loading" :data="records" border empty-text="暂无数据">
    <el-table-column
      v-for="field in fields"
      :key="field.apiName"
      :label="field.fieldName"
      :min-width="150"
      show-overflow-tooltip
    >
      <template #default="{ row }">
        <FieldValue :field="field" :value="row.data[field.apiName]" />
      </template>
    </el-table-column>
    <el-table-column label="操作" width="90" fixed="right">
      <template #default="{ row }">
        <el-button
          link
          type="danger"
          :loading="deletingId === row.id"
          @click="emit('delete', row)"
        >
          删除
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>
