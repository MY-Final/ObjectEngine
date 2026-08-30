<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { listRecords } from '@/api/record'
import type { CustomField } from '@/types/field'
import { objectApiNameById, recordDisplayText } from '@/utils/record'

/**
 * 关联字段（LOOKUP）控件：远程搜索目标对象的记录，v-model 为关联记录 ID（数字）。
 * 展示文本由目标对象元数据决定（name 字段 → 第一个启用文本字段 → #id），与后端展示启发式一致
 */
const props = defineProps<{ field: CustomField }>()
const model = defineModel<unknown>({ required: true })

const targetApiName = ref<string | undefined>()
const resolvingTarget = ref(false)

const options = ref<Array<{ value: number; label: string }>>([])
const loading = ref(false)
const selected = ref<number | undefined>()

onMounted(async () => {
  resolvingTarget.value = true
  try {
    targetApiName.value = props.field.relationObjectId
      ? await objectApiNameById(props.field.relationObjectId)
      : undefined
  } finally {
    resolvingTarget.value = false
  }
})

function ensureOption(value: number, label?: string) {
  if (!options.value.some((option) => option.value === value)) {
    options.value = [{ value, label: label ?? `#${value}` }, ...options.value]
  }
}

// 回显当前值（新建无值 / 编辑有值；兼容历史 { recordId } 快照结构）
watch(
  model,
  async (value) => {
    let recordId: number | undefined
    if (typeof value === 'number') {
      recordId = value
    } else if (value && typeof value === 'object') {
      const recordIdRaw = (value as { recordId?: unknown }).recordId
      if (typeof recordIdRaw === 'number') {
        recordId = recordIdRaw
      }
    }
    selected.value = recordId
    if (recordId != null && targetApiName.value) {
      const existing = options.value.find((option) => option.value === recordId)
      if (!existing || existing.label.startsWith('#')) {
        const label = await recordDisplayText(targetApiName.value, {}, recordId)
        // recordDisplayText 拿不到数据时返回 #id，尝试用列表搜索补全一次
        if (!label.startsWith('#')) {
          ensureOption(recordId, label)
        } else if (!existing) {
          ensureOption(recordId, label)
        }
      }
    }
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
        label: await recordDisplayText(targetApiName.value as string, record.data, record.id),
      })),
    )
  } finally {
    loading.value = false
  }
}

function handleChange(value: number | undefined) {
  // 提交的是关联记录 ID，不是展示文本
  model.value = value ?? null
}
</script>

<template>
  <el-select
    v-model="selected"
    filterable
    remote
    clearable
    remote-show-suffix
    :loading="loading"
    :remote-method="handleSearch"
    :disabled="resolvingTarget || !targetApiName"
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
