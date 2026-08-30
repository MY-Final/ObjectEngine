<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { listRecords } from '@/api/record'
import type { CustomField } from '@/types/field'
import { recordDisplayText } from '@/utils/record'

/**
 * 关联关系字段控件：远程搜索目标对象的记录，v-model 值为
 * { recordId, text } 快照（text 由选择时的展示文本带入，后端保存时会重新校验并解析）
 */
const props = defineProps<{ field: CustomField }>()
const model = defineModel<unknown>({ required: true })

const targetApiName = computed(
  () => (props.field.configJson?.targetObjectApiName as string | undefined) ?? '',
)

interface ReferenceOption {
  value: number
  label: string
}

const options = ref<ReferenceOption[]>([])
const loading = ref(false)
const selected = ref<number | undefined>()

// 把当前值同步进下拉（已选快照文本直接展示，不依赖搜索）
watch(
  model,
  (value) => {
    if (value && typeof value === 'object') {
      const { recordId, text } = value as { recordId?: unknown; text?: unknown }
      if (typeof recordId === 'number') {
        const label = typeof text === 'string' && text !== '' ? text : `#${recordId}`
        selected.value = recordId
        if (!options.value.some((option) => option.value === recordId)) {
          options.value = [{ value: recordId, label }, ...options.value]
        }
      }
      return
    }
    if (typeof value === 'number') {
      selected.value = value
      const label = `#${value}`
      if (!options.value.some((option) => option.value === value)) {
        options.value = [{ value, label }, ...options.value]
      }
      return
    }
    selected.value = undefined
  },
  { immediate: true },
)

async function handleSearch(keyword: string) {
  if (!targetApiName.value) return
  loading.value = true
  try {
    const result = await listRecords(targetApiName.value, {
      page: 1,
      pageSize: 20,
      keyword: keyword.trim() || undefined,
    })
    options.value = await Promise.all(
      result.records.map(async (record) => ({
        value: record.id,
        label: await recordDisplayText(targetApiName.value, record.data, record.id),
      })),
    )
  } finally {
    loading.value = false
  }
}

function handleChange(value: number | undefined) {
  if (value == null) {
    model.value = null
    return
  }
  const option = options.value.find((item) => item.value === value)
  model.value = { recordId: value, text: option ? option.label : null }
}

onMounted(() => {
  // 预加载前 20 条，让下拉不输入关键字也有候选
  void handleSearch('')
})
</script>

<template>
  <el-select
    v-model="selected"
    filterable
    remote
    clearable
    remote-show-suffix
    :remote-method="handleSearch"
    :loading="loading"
    :disabled="!targetApiName"
    :placeholder="targetApiName ? '输入关键字搜索记录' : '该字段未配置关联对象'"
    style="width: 100%"
    @change="handleChange"
    @clear="model = null"
  >
    <el-option
      v-for="option in options"
      :key="option.value"
      :label="option.label"
      :value="option.value"
    />
  </el-select>
</template>
