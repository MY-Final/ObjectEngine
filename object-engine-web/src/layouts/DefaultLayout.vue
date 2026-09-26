<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  Expand,
  Fold,
  HomeFilled,
  Moon,
  Search,
  Setting,
  Sunny,
  SwitchButton,
  UserFilled,
} from '@element-plus/icons-vue'
import { logout } from '@/api/auth'
import { clearLoginState, getLoginUser } from '@/constants/auth'
import type { MenuTreeItem } from '@/types/menu'
import { useAppStore } from '@/stores/app'
import { resolveIcon } from '@/utils/icon'
import BrandLogo from '@/components/common/BrandLogo.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const keyword = ref('')

/** 启动时拉取一次导航菜单；菜单管理页变更后会调用 store 重新加载 */
void appStore.loadMenus()

const loginUser = getLoginUser()

/** 当前是否处于后台区（/admin/* 路由）：前台与后台各自只显示本区菜单 */
const isAdminZone = computed(() => route.path.startsWith('/admin'))

/** 首页属于前台区：前台页面（首页 / 自定义对象页）都显示，进入后台区后隐藏；回首页走左上角品牌区 */
const showHome = computed(() => !isAdminZone.value)

/** 顶栏面包屑的路径段文案；未收录的段按原样展示（如对象的 apiName） */
const SEGMENT_LABELS: Record<string, string> = {
  dashboard: '首页',
  admin: '后台管理',
  objects: '对象管理',
  menus: '菜单管理',
  'option-sets': '通用选项集',
  users: '用户管理',
  fields: '字段配置',
  layout: '布局配置',
  custom: '自定义对象',
}

/** 在菜单树里按 routePath 反查菜单名，用来把 apiName 显示成对象名 */
function findMenuByPath(items: MenuTreeItem[], path: string): MenuTreeItem | undefined {
  for (const item of items) {
    if (item.routePath === path) return item
    const hit = findMenuByPath(item.children ?? [], path)
    if (hit) return hit
  }
  return undefined
}

/** 面包屑：路径分段逐级展开，未知分段用等宽字体显示（多半是对象的 apiName） */
const breadcrumbs = computed(() => {
  const segments = route.path.split('/').filter(Boolean)
  if (segments.length === 0) return []
  return segments.map((segment, index) => {
    const isLeaf = index === segments.length - 1
    const path = `/${segments.slice(0, index + 1).join('/')}`
    const label =
      SEGMENT_LABELS[segment] ?? findMenuByPath(appStore.menuTree, path)?.menuName ?? segment
    return { path, label, known: segment in SEGMENT_LABELS, isLeaf }
  })
})

/** 面包屑中间层可点击回退，最后一层是当前位置不可点 */
function goCrumb(path: string, isLeaf: boolean) {
  if (isLeaf) return
  if (path === '/') return
  void router.push(path)
}

/**
 * 按区过滤菜单树：叶子菜单以 routePath 前缀归属（/admin/* 为后台菜单，其余为前台菜单），
 * 目录跟随子菜单——子菜单全部不在本区时整个目录隐藏
 */
function filterByZone(items: MenuTreeItem[], admin: boolean): MenuTreeItem[] {
  const result: MenuTreeItem[] = []
  for (const item of items) {
    if (item.children && item.children.length > 0) {
      const children = filterByZone(item.children, admin)
      if (children.length > 0) {
        result.push({ ...item, children })
      }
      continue
    }
    if ((item.routePath ?? '').startsWith('/admin') === admin) {
      result.push(item)
    }
  }
  return result
}

/** 先按关键词过滤，再按当前区（前台/后台）过滤 */
const filteredTree = computed(() =>
  filterByZone(filterMenuTree(appStore.menuTree, keyword.value.trim()), isAdminZone.value),
)

function filterMenuTree(items: MenuTreeItem[], keyword: string): MenuTreeItem[] {
  if (!keyword) return items
  const result: MenuTreeItem[] = []
  for (const item of items) {
    const children = filterMenuTree(item.children ?? [], keyword)
    if (item.menuName.includes(keyword) || children.length > 0) {
      result.push({ ...item, children })
    }
  }
  return result
}

/** 前台 / 后台 分区切换：进后台落到对象管理，回前台落到首页 */
function switchZone(zone: 'front' | 'admin') {
  keyword.value = ''
  void router.push(zone === 'admin' ? '/admin/objects' : '/dashboard')
}

async function handleUserCommand(command: string) {
  if (command !== 'logout') return
  try {
    await logout()
  } catch {
    // 接口失败也照常清理本地登录态
  }
  clearLoginState()
  ElMessage.success('已退出登录')
  await router.push('/login')
}

function go(menu: MenuTreeItem) {
  const path = menu.routePath ?? ''
  if (menu.menuType === 'LINK' && /^https?:\/\//.test(path)) {
    window.open(path, menu.target === '_blank' ? '_blank' : '_self')
    return
  }
  if (path) router.push(path)
}
</script>

<template>
  <el-container class="app-shell">
    <el-header class="app-header" :height="'var(--oe-header-height)'">
      <div class="app-header__left">
        <BrandLogo
          class="app-header__brand"
          @brand-click="router.push('/dashboard')"
        />
        <el-breadcrumb v-if="breadcrumbs.length > 0" class="app-breadcrumb" separator="/">
          <el-breadcrumb-item v-for="crumb in breadcrumbs" :key="crumb.path">
            <span
              class="app-breadcrumb__link"
              :class="{ 'is-leaf': crumb.isLeaf, 'is-raw': !crumb.known }"
              @click="goCrumb(crumb.path, crumb.isLeaf)"
            >
              {{ crumb.label }}
            </span>
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="app-header__right">
        <el-tooltip :content="appStore.isDark ? '切换到亮色主题' : '切换到暗色主题'" placement="bottom">
          <el-switch
            :model-value="appStore.isDark"
            inline-prompt
            :active-action-icon="Sunny"
            :inactive-action-icon="Moon"
            @change="appStore.toggleTheme"
          />
        </el-tooltip>

        <el-dropdown trigger="click" @command="handleUserCommand">
          <div class="user-chip">
            <el-avatar :size="28" :icon="UserFilled" class="user-chip__avatar" />
            <span class="user-chip__name">{{ loginUser?.name || loginUser?.username || '未登录' }}</span>
            <el-icon class="user-chip__caret" :size="12"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                {{ loginUser?.username || '-' }}
              </el-dropdown-item>
              <el-dropdown-item command="logout" :icon="SwitchButton" divided>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="app-body">
      <el-aside
        class="app-aside"
        :width="appStore.sidebarCollapsed ? 'var(--oe-rail-width-collapsed)' : 'var(--oe-rail-width)'"
      >
        <div class="rail-zone" :class="{ 'is-collapsed': appStore.sidebarCollapsed }">
          <el-radio-group
            v-if="!appStore.sidebarCollapsed"
            :model-value="isAdminZone ? 'admin' : 'front'"
            size="small"
            class="rail-zone__switch"
            @update:model-value="switchZone($event as 'front' | 'admin')"
          >
            <el-radio-button value="front">前台</el-radio-button>
            <el-radio-button value="admin">后台</el-radio-button>
          </el-radio-group>
          <el-tooltip v-else :content="isAdminZone ? '当前：后台' : '当前：前台'" placement="right">
            <el-icon class="rail-zone__collapsed" :size="18">
              <Setting v-if="isAdminZone" />
              <HomeFilled v-else />
            </el-icon>
          </el-tooltip>
        </div>

        <div v-if="!appStore.sidebarCollapsed" class="rail-search">
          <el-input
            v-model="keyword"
            :prefix-icon="Search"
            placeholder="搜索菜单"
            clearable
          />
        </div>

        <el-scrollbar class="rail-scroll">
          <el-menu
            class="rail-menu"
            :class="{ 'is-collapsed': appStore.sidebarCollapsed }"
            :default-active="route.path"
            :collapse="appStore.sidebarCollapsed"
            :collapse-transition="false"
          >
            <el-menu-item
              v-if="showHome"
              index="/dashboard"
              @click="router.push('/dashboard')"
            >
              <el-icon><HomeFilled /></el-icon>
              <template #title>首页</template>
            </el-menu-item>

            <template v-for="menu in filteredTree" :key="menu.id">
              <el-sub-menu v-if="menu.children.length > 0" :index="`dir-${menu.id}`">
                <template #title>
                  <el-icon v-if="resolveIcon(menu.icon)">
                    <component :is="resolveIcon(menu.icon)" />
                  </el-icon>
                  <span>{{ menu.menuName }}</span>
                </template>
                <el-menu-item
                  v-for="child in menu.children"
                  :key="child.id"
                  :index="child.routePath || `menu-${child.id}`"
                  @click="go(child)"
                >
                  <el-icon v-if="resolveIcon(child.icon)">
                    <component :is="resolveIcon(child.icon)" />
                  </el-icon>
                  <template #title>{{ child.menuName }}</template>
                </el-menu-item>
              </el-sub-menu>

              <el-menu-item
                v-else
                :index="menu.routePath || `menu-${menu.id}`"
                @click="go(menu)"
              >
                <el-icon v-if="resolveIcon(menu.icon)">
                  <component :is="resolveIcon(menu.icon)" />
                </el-icon>
                <template #title>{{ menu.menuName }}</template>
              </el-menu-item>
            </template>

            <el-empty
              v-if="filteredTree.length === 0 && !showHome"
              description="暂无菜单"
              :image-size="52"
              class="rail-empty"
            />
          </el-menu>
        </el-scrollbar>

        <div class="rail-footer">
          <el-tooltip :content="appStore.sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'" placement="right">
            <el-button
              text
              class="rail-footer__toggle"
              :icon="appStore.sidebarCollapsed ? Expand : Fold"
              @click="appStore.toggleSidebar"
            />
          </el-tooltip>
          <span v-if="!appStore.sidebarCollapsed" class="rail-footer__version">v0.1.0</span>
        </div>
      </el-aside>

      <el-main class="app-main">
        <RouterView v-slot="{ Component }">
          <component :is="Component" />
        </RouterView>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-shell {
  height: 100vh;
}

/* —— 顶栏：亮色玻璃质感，与深色侧栏拉开对比 —— */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--oe-space-4);
  padding: 0 var(--oe-space-5);
  background-color: var(--oe-surface);
  border-bottom: 1px solid var(--oe-border);
  backdrop-filter: saturate(180%) blur(12px);
}

.app-header__left {
  display: flex;
  align-items: center;
  gap: var(--oe-space-5);
  min-width: 0;
}

.app-header__brand {
  flex-shrink: 0;
}

.app-header__right {
  display: flex;
  align-items: center;
  gap: var(--oe-space-4);
  flex-shrink: 0;
}

.app-breadcrumb {
  font-size: var(--el-font-size-small);
  min-width: 0;
}

.app-breadcrumb__link {
  color: var(--oe-text-3);
  cursor: pointer;
  border-radius: var(--oe-radius-sm);
  transition: color 0.15s ease;
}

.app-breadcrumb__link:hover {
  color: var(--oe-brand-500);
}

.app-breadcrumb__link.is-leaf {
  color: var(--oe-text-1);
  font-weight: 500;
  cursor: default;
}

/* 未收录的路径段一般是对象的 apiName，用等宽字体区分 */
.app-breadcrumb__link.is-raw {
  font-family: var(--oe-font-mono);
}

.user-chip {
  display: flex;
  align-items: center;
  gap: var(--oe-space-2);
  height: 38px;
  padding: 0 var(--oe-space-2) 0 var(--oe-space-1);
  border-radius: var(--oe-radius-md);
  cursor: pointer;
  outline: none;
  transition: background-color 0.15s ease;
}

.user-chip:hover {
  background-color: var(--oe-surface-2);
}

.user-chip__avatar {
  background-color: var(--oe-brand-100);
  color: var(--oe-brand-600);
}

.user-chip__name {
  font-size: var(--el-font-size-base);
  color: var(--oe-text-1);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-chip__caret {
  color: var(--oe-text-3);
}

.app-body {
  height: calc(100vh - var(--oe-header-height));
  overflow: hidden;
}

/* —— 侧边栏：深色轨道，菜单色全部走局部 --el-menu-* 变量 —— */
.app-aside {
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, var(--oe-rail), var(--oe-rail-2));
  border-right: 1px solid var(--oe-rail-border);
  transition: width 0.2s ease;

  /* 深色底上把 EP 菜单变量换掉，菜单文字/悬停/激活色全部跟着走 */
  --el-menu-bg-color: transparent;
  --el-menu-text-color: var(--oe-rail-text);
  --el-menu-hover-bg-color: var(--oe-rail-hover);
  --el-menu-hover-text-color: var(--oe-rail-text-strong);
  --el-menu-active-color: var(--oe-rail-text-strong);
  --el-menu-border-color: transparent;

  /* 分区切换按钮组在深色底上的配色 */
  --el-radio-button-bg-color: transparent;
  --el-radio-button-text-color: var(--oe-rail-text);
  --el-radio-button-border-color: var(--oe-rail-border);
  --el-radio-button-hover-text-color: var(--oe-rail-text-strong);
  --el-radio-button-checked-bg-color: var(--oe-brand-500);
  --el-radio-button-checked-text-color: var(--oe-on-brand);
  --el-radio-button-checked-border-color: var(--oe-brand-500);
}

.rail-zone {
  padding: var(--oe-space-3) var(--oe-space-3) var(--oe-space-2);
}

.rail-zone.is-collapsed {
  display: flex;
  justify-content: center;
  padding: var(--oe-space-3) 0 var(--oe-space-2);
}

.rail-zone__switch {
  display: flex;
  width: 100%;
}

.rail-zone__switch :deep(.el-radio-button) {
  flex: 1;
}

.rail-zone__switch :deep(.el-radio-button__inner) {
  width: 100%;
  font-weight: 500;
}

.rail-zone__collapsed {
  color: var(--oe-rail-text);
  cursor: pointer;
}

.rail-search {
  padding: 0 var(--oe-space-3) var(--oe-space-2);
}

.rail-search :deep(.el-input__wrapper) {
  background-color: var(--oe-rail-input-bg);
  box-shadow: none inset;
  border-radius: var(--oe-radius-md);
  padding: 1px 10px;
}

.rail-search :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--oe-brand-500) inset;
}

.rail-search :deep(.el-input__inner) {
  color: var(--oe-rail-text-strong);
}

.rail-search :deep(.el-input__inner::placeholder) {
  color: var(--oe-rail-text);
  opacity: 0.7;
}

.rail-search :deep(.el-input__clear) {
  color: var(--oe-rail-text);
}

.rail-scroll {
  flex: 1;
  min-height: 0;
}

.rail-menu {
  border-right: none;
  padding: 0 var(--oe-space-2) var(--oe-space-4);
  background-color: transparent;
}

.rail-menu.is-collapsed {
  padding: 0;
  --el-menu-collapse-width: var(--oe-rail-width-collapsed);
}

/*
 * 以下 4 条必须用 :deep() 覆写 Element Plus 菜单的内部结构：
 * 选中项背景、左边指示条、圆角与间距都不在 --el-menu-* 变量的可覆盖范围内，
 * EP 只给 .el-menu-item.is-active 设了 color，没有 background。
 */
.rail-menu :deep(.el-menu-item),
.rail-menu :deep(.el-sub-menu__title) {
  height: 42px;
  line-height: 42px;
  margin-bottom: 2px;
  border-radius: var(--oe-radius-md);
  border-bottom: none;
  font-weight: 500;
}

.rail-menu :deep(.el-menu-item:hover),
.rail-menu :deep(.el-sub-menu__title:hover) {
  background-color: var(--oe-rail-hover);
  color: var(--oe-rail-text-strong);
}

.rail-menu :deep(.el-menu-item.is-active) {
  background-color: var(--oe-rail-active);
  color: var(--oe-rail-text-strong);
  font-weight: 600;
}

.rail-menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  border-radius: 0 3px 3px 0;
  background-color: var(--oe-brand-400);
}

/* 目录含子菜单时父项也用主色文字，提示当前位置在哪个目录下 */
.rail-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--oe-rail-text-strong);
}

/* 子菜单层：压暗一档并内缩，形成层级 */
.rail-menu :deep(.el-sub-menu .el-menu) {
  background-color: rgb(0 0 0 / 18%);
  border-radius: var(--oe-radius-md);
  padding: var(--oe-space-1) var(--oe-space-1) var(--oe-space-1) var(--oe-space-2);
  margin: 2px 0 6px;
}

.rail-menu :deep(.el-menu-item .el-icon) {
  color: inherit;
}

.rail-empty {
  padding: var(--oe-space-6) 0;
}

.rail-empty :deep(.el-empty__description p) {
  color: var(--oe-rail-text);
  font-size: var(--el-font-size-small);
}

.rail-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--oe-space-2);
  padding: var(--oe-space-2) var(--oe-space-2) var(--oe-space-3);
  border-top: 1px solid var(--oe-rail-border);
}

.rail-footer__toggle {
  color: var(--oe-rail-text);
}

.rail-footer__toggle:hover {
  color: var(--oe-rail-text-strong);
  background-color: var(--oe-rail-hover);
}

.rail-footer__version {
  font-size: var(--el-font-size-extra-small);
  color: var(--oe-rail-text);
  opacity: 0.7;
}

.app-main {
  padding: 0;
  overflow: auto;
  background-color: var(--oe-bg);
}
</style>
