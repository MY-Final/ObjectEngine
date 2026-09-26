<script setup lang="ts">
import type { Component } from 'vue'

/**
 * 首页统计卡：图标 + 数值 + 说明 + 趋势/备注。
 * 纯展示组合 el-card + el-icon + el-statistic，不含数据请求。
 */
withDefaults(
  defineProps<{
    label: string
    value: number | string
    /** 卡片图标 */
    icon: Component
    /** 图标颜色 */
    tone?: string
    /** 图标底色 */
    toneBg?: string
    /** 数值下方的一行补充说明 */
    hint?: string
    loading?: boolean
  }>(),
  { tone: 'var(--oe-brand-500)', toneBg: 'var(--oe-brand-50)' },
)
</script>

<template>
  <el-card v-loading="loading" shadow="never" class="stat-card" body-class="stat-card__body">
    <div class="stat-card__icon" :style="{ backgroundColor: toneBg, color: tone }">
      <el-icon :size="20"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-card__content">
      <div class="stat-card__label">{{ label }}</div>
      <el-statistic :value="value" class="stat-card__value" />
      <div v-if="hint" class="stat-card__hint">{{ hint }}</div>
    </div>
  </el-card>
</template>

<style scoped>
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: var(--oe-space-4);
  padding: var(--oe-space-5);
}

.stat-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 46px;
  height: 46px;
  border-radius: var(--oe-radius-lg);
}

.stat-card__content {
  min-width: 0;
}

.stat-card__label {
  font-size: var(--el-font-size-small);
  color: var(--oe-text-3);
}

.stat-card__value {
  margin-top: 2px;
}

.stat-card__value :deep(.el-statistic__content) {
  font-size: 26px;
  font-weight: 600;
  color: var(--oe-text-1);
  line-height: 1.2;
}

.stat-card__hint {
  margin-top: 2px;
  font-size: var(--el-font-size-extra-small);
  color: var(--oe-text-3);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
