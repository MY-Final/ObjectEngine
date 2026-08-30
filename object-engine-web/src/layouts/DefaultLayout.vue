<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Expand, Fold, HomeFilled, Search, Setting, SwitchButton, UserFilled } from '@element-plus/icons-vue'
import { logout } from '@/api/auth'
import { clearLoginState } from '@/constants/auth'
import type { MenuTreeItem } from '@/types/menu'
import { useAppStore } from '@/stores/app'
import { resolveIcon } from '@/utils/icon'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const keyword = ref('')

/** 启动时拉取一次导航菜单；菜单管理页变更后会调用 store 重新加载 */
void appStore.loadMenus()

/** 当前是否处于后台区（/admin/* 路由）：前台与后台各自只显示本区菜单 */
const isAdminZone = computed(() => route.path.startsWith('/admin'))

/** 首页属于前台区：前台页面（首页 / 自定义对象页）都显示，进入后台区后隐藏；回首页走左上角品牌区 */
const showHome = computed(() => !isAdminZone.value)

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
    <el-header class="app-header" height="50px">
      <div class="brand" title="返回首页" @click="router.push('/dashboard')">
        <span class="brand-mark">OE</span>
        <span class="brand-name">Object Engine</span>
      </div>
      <div class="header-right">
        <el-tooltip content="进入后台" placement="bottom">
          <el-icon class="header-action" :size="18" @click="router.push('/admin/objects')">
            <Setting />
          </el-icon>
        </el-tooltip>
        <el-dropdown @command="handleUserCommand">
          <el-avatar :size="30" :icon="UserFilled" class="header-avatar" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="app-body">
      <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="app-aside">
        <div class="sidebar-tools" :class="{ 'is-collapsed': appStore.sidebarCollapsed }">
          <el-input
            v-if="!appStore.sidebarCollapsed"
            v-model="keyword"
            class="sidebar-search"
            :prefix-icon="Search"
            placeholder="搜索菜单..."
            size="small"
            clearable
          />
          <el-icon class="collapse-toggle" @click="appStore.toggleSidebar">
            <Expand v-if="appStore.sidebarCollapsed" />
            <Fold v-else />
          </el-icon>
        </div>

        <el-menu
          class="sidebar-menu"
          :class="{ 'is-collapsed': appStore.sidebarCollapsed }"
          :default-active="route.path"
          :collapse="appStore.sidebarCollapsed"
          :collapse-transition="false"
        >
          <el-menu-item v-if="showHome" index="/dashboard" @click="router.push('/dashboard')">
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
                {{ child.menuName }}
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
        </el-menu>
      </el-aside>

      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-shell {
  height: 100vh;
}

/* 顶栏：白底、底部细分隔线，右侧预留头像位 */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 6px;
  background-color: var(--el-color-primary);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.brand-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-action {
  color: #606266;
  cursor: pointer;
}

.header-action:hover {
  color: var(--el-color-primary);
}

.header-avatar {
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  cursor: pointer;
}

.app-body {
  height: calc(100vh - 50px);
}

/* 侧边栏：白底黑字，选中蓝色高亮 */
.app-aside {
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  transition: width 0.2s;
  overflow-x: hidden;
}

.sidebar-tools {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 10px 6px;
}

/* 折叠态：隐藏搜索后按钮与菜单图标同轴居中（aside 宽 64px，与折叠菜单项对齐） */
.sidebar-tools.is-collapsed {
  justify-content: center;
  padding: 10px 0 6px;
}

.collapse-toggle {
  flex-shrink: 0;
  color: #909399;
  cursor: pointer;
  font-size: 16px;
}

.collapse-toggle:hover {
  color: var(--el-color-primary);
}

.sidebar-menu {
  border-right: none;
  padding: 4px 8px;
}

/* 折叠态去掉水平 padding，并统一折叠宽度为 64px，菜单图标与顶部折叠按钮同轴居中 */
.sidebar-menu.is-collapsed {
  padding: 4px 0;
  --el-menu-collapse-width: 64px;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  color: #303133;
  border-radius: 6px;
  margin-bottom: 2px;
  height: 40px;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background-color: #f5f7fa;
  color: #303133;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 500;
}

.sidebar-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--el-color-primary);
}

.sidebar-menu :deep(.el-sub-menu .el-menu) {
  background-color: transparent;
}

.app-main {
  background-color: #f5f7fa;
  padding: 0;
  overflow: auto;
}
</style>
