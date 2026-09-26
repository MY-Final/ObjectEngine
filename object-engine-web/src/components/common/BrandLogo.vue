<script setup lang="ts">
/**
 * 品牌标识。此前 DefaultLayout 与 Login 各自复制了一份 .brand / .brand-mark /
 * .brand-name 的结构和样式，改一处要改两处，这里抽成组件。
 */
withDefaults(
  defineProps<{
    /** 尺寸：sm 顶栏 / md 登录页 / lg 登录页品牌面 */
    size?: 'sm' | 'md' | 'lg'
    /** 配色：light 用于深色底（侧栏、登录页品牌面），dark 用于亮色底 */
    variant?: 'light' | 'dark'
    /** 点击行为；不传则纯展示 */
    onBrandClick?: () => void
  }>(),
  { size: 'sm', variant: 'dark' },
)
</script>

<template>
  <div
    class="brand-logo"
    :class="[`is-${size}`, `is-${variant}`, { 'is-clickable': !!onBrandClick }]"
    @click="onBrandClick?.()"
  >
    <span class="brand-logo__mark">OE</span>
    <span class="brand-logo__text">
      <span class="brand-logo__name">Object Engine</span>
      <span v-if="size !== 'sm'" class="brand-logo__slogan">元数据驱动的动态业务对象引擎</span>
    </span>
  </div>
</template>

<style scoped>
.brand-logo {
  display: flex;
  align-items: center;
  gap: var(--oe-space-3);
  min-width: 0;
}

.brand-logo.is-clickable {
  cursor: pointer;
}

.brand-logo__mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: var(--oe-radius-md);
  background: var(--oe-gradient-mark);
  color: var(--oe-on-brand);
  font-weight: 700;
  letter-spacing: 0.02em;
  box-shadow: var(--oe-shadow-brand);
}

.brand-logo__text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.brand-logo__name {
  font-weight: 600;
  color: var(--oe-text-1);
  white-space: nowrap;
}

.brand-logo__slogan {
  font-size: var(--el-font-size-extra-small);
  color: var(--oe-text-3);
  white-space: nowrap;
}

.brand-logo.is-light .brand-logo__name {
  color: var(--oe-rail-text-strong);
}

.brand-logo.is-light .brand-logo__slogan {
  color: var(--oe-rail-text);
}

/* —— 尺寸 —— */
.is-sm .brand-logo__mark {
  width: 28px;
  height: 28px;
  font-size: 12px;
}

.is-sm .brand-logo__name {
  font-size: 15px;
}

.is-md .brand-logo__mark {
  width: 34px;
  height: 34px;
  font-size: 14px;
}

.is-md .brand-logo__name {
  font-size: 19px;
}

.is-lg .brand-logo__mark {
  width: 48px;
  height: 48px;
  font-size: 19px;
  border-radius: var(--oe-radius-lg);
}

.is-lg .brand-logo__name {
  font-size: 26px;
}

.is-lg .brand-logo__slogan {
  margin-top: 2px;
  font-size: var(--el-font-size-base);
}
</style>
