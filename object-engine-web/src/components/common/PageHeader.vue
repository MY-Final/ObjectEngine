<script setup lang="ts">
/**
 * 页面头部：标题 + 说明 + 右侧操作区。
 * 此前 6 个页面各自手搓 .page-toolbar，样式和结构都不一致（fields 页甚至叠了两个），
 * 这里统一定义，页面只往 #actions 里塞按钮。
 */
defineProps<{
  title: string
  /** 标题下方的一行说明，对象描述、API 名称等 */
  description?: string
}>()
</script>

<template>
  <header class="page-header">
    <div class="page-header__main">
      <!-- 面包屑由调用方通过 default slot 前置插槽提供 -->
      <slot name="breadcrumb" />
      <h2 class="page-header__title">{{ title }}</h2>
      <p v-if="description || $slots.default" class="page-header__desc">
        <slot>{{ description }}</slot>
      </p>
    </div>
    <div v-if="$slots.actions" class="page-header__actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--oe-space-4);
  margin-bottom: var(--oe-space-4);
}

.page-header__main {
  min-width: 0;
}

.page-header__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--oe-text-1);
}

.page-header__desc {
  margin: var(--oe-space-1) 0 0;
  font-size: var(--el-font-size-small);
  line-height: 1.6;
  color: var(--oe-text-3);
}

.page-header__actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: var(--oe-space-2);
}

@media (width <= 768px) {
  .page-header {
    flex-direction: column;
  }
}
</style>
