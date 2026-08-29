import type { Component } from 'vue'
import * as ElementPlusIcons from '@element-plus/icons-vue'

/** 菜单 icon 存的是 Element Plus 图标名，按名解析，解析不到返回 undefined */
export function resolveIcon(name?: string | null): Component | undefined {
  if (!name) return undefined
  return (ElementPlusIcons as unknown as Record<string, Component | undefined>)[name]
}
