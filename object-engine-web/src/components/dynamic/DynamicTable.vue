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

const emit = defineEmits<{ delete: [record: CustomRecord]; edit: [record: CustomRecord] }>()
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
        <FieldValue :field="field" :value="row.data[field.apiName]" :record="row" />
      </template>
    </el-table-column>
    <el-table-column label="操作" width="120" fixed="right">
      <template #default="{ row }">
        <!-- 默认提供 编辑/删除；需要扩展复制等操作时用 #actions 覆盖 -->
        <slot name="actions" :row="row">
          <el-button link type="primary" @click="emit('edit', row)">编辑</el-button>
          <el-button
            link
            type="danger"
            :loading="deletingId === row.id"
            @click="emit('delete', row)"
          >
            删除
          </el-button>
        </slot>
      </template>
    </el-table-column>
  </el-table>
</template>
