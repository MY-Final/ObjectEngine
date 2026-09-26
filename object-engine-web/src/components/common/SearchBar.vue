<script setup lang="ts">
/**
 * 搜索 / 筛选条。内部只组合 el-* 控件，页面通过默认插槽放筛选项，
 * 右侧的「搜索 / 重置」由本组件统一提供，避免 4 个列表页各写一遍。
 */
withDefaults(
  defineProps<{
    /** 左侧标签文案，如「关键词：」；不传则不渲染 */
    label?: string
    /** 搜索按钮文案 */
    searchText?: string
    resetText?: string
    loading?: boolean
  }>(),
  { searchText: '搜索', resetText: '重置' },
)

const emit = defineEmits<{ search: []; reset: [] }>()
</script>

<template>
  <div class="search-bar">
    <span v-if="label" class="search-label">{{ label }}</span>
    <slot />
    <el-button type="primary" :loading="loading" @click="emit('search')">{{ searchText }}</el-button>
    <el-button @click="emit('reset')">{{ resetText }}</el-button>
  </div>
</template>
