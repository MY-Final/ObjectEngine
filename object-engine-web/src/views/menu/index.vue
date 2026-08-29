<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteMenu, listMenus, updateMenuStatus, updateMenuVisible } from '@/api/menu'
import type { MenuItem, MenuParentOption, MenuTree, MenuType } from '@/types/menu'
import { useAppStore } from '@/stores/app'
import { resolveIcon } from '@/utils/icon'
import MenuFormDialog from './components/MenuFormDialog.vue'

const appStore = useAppStore()

const MENU_TYPE_LABELS: Record<MenuType, string> = {
  DIRECTORY: '目录',
  OBJECT: '对象',
  LINK: '链接',
}

const MENU_TYPE_TAG_TYPES: Record<MenuType, 'info' | 'warning' | 'success'> = {
  DIRECTORY: 'info',
  OBJECT: 'warning',
  LINK: 'success',
}

const loading = ref(false)
const items = ref<MenuItem[]>([])
const query = reactive({
  menuName: '',
  menuType: undefined as MenuType | undefined,
  status: undefined as number | undefined,
})

async function load() {
  loading.value = true
  try {
    items.value = await listMenus({
      menuName: query.menuName || undefined,
      menuType: query.menuType,
      status: query.status,
    })
  } finally {
    loading.value = false
  }
}

/** 列表变更后同时刷新侧边栏导航 */
async function refresh() {
  await load()
  void appStore.loadMenus()
}

/** 后端列表不分页，前端按 parentId 组树，兄弟节点按 sort 升序 */
function buildTree(list: MenuItem[], parentId: number): MenuTree[] {
  return list
    .filter((item) => item.parentId === parentId)
    .sort((a, b) => a.sort - b.sort || a.id - b.id)
    .map((item) => ({ ...item, children: buildTree(list, item.id) }))
}

const treeData = computed(() => buildTree(items.value, 0))

/** 默认全部展开：把所有节点 key 交给 expand-row-keys */
const expandedKeys = computed(() => {
  const keys: string[] = []
  const walk = (nodes: MenuTree[]) => {
    for (const node of nodes) {
      keys.push(String(node.id))
      walk(node.children)
    }
  }
  walk(treeData.value)
  return keys
})

/** 上级菜单候选：顶级 + 目录节点 */
function collectDirectories(nodes: MenuTree[]): MenuParentOption[] {
  const result: MenuParentOption[] = []
  for (const node of nodes) {
    if (node.menuType === 'DIRECTORY') {
      result.push({
        value: node.id,
        label: node.menuName,
        children: collectDirectories(node.children),
      })
    }
  }
  return result
}

const parentOptions = computed<MenuParentOption[]>(() => [
  { value: 0, label: '顶级菜单', children: collectDirectories(treeData.value) },
])

/** 新建时默认排在最后 */
const defaultSort = computed(() => Math.max(0, ...items.value.map((item) => item.sort)) + 1)

async function handleStatusChange(row: MenuTree) {
  try {
    await updateMenuStatus(row.id, row.status)
    ElMessage.success(row.status === 1 ? '已启用' : '已停用')
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

async function handleVisibleChange(row: MenuTree) {
  try {
    await updateMenuVisible(row.id, row.visible)
    ElMessage.success(row.visible === 1 ? '已设为显示' : '已设为隐藏')
  } catch {
    row.visible = row.visible === 1 ? 0 : 1
  }
}

async function handleDelete(row: MenuTree) {
  try {
    await ElMessageBox.confirm(`确定删除菜单「${row.menuName}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  await deleteMenu(row.id)
  ElMessage.success('删除成功')
  await refresh()
}

function handleSearch() {
  void load()
}

function handleReset() {
  query.menuName = ''
  query.menuType = undefined
  query.status = undefined
  void load()
}

const dialogVisible = ref(false)
const dialogTarget = ref<MenuItem | null>(null)

function openCreate() {
  dialogTarget.value = null
  dialogVisible.value = true
}

function openEdit(row: MenuItem) {
  dialogTarget.value = row
  dialogVisible.value = true
}

async function handleSaved() {
  await refresh()
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="page">
    <div class="page-toolbar">
      <h2>菜单管理</h2>
      <el-button type="primary" @click="openCreate">新建菜单</el-button>
    </div>

    <div class="search-bar">
      <span class="search-label">菜单名称：</span>
      <el-input
        v-model="query.menuName"
        placeholder="菜单名称"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-select
        v-model="query.menuType"
        placeholder="类型"
        clearable
        style="width: 130px"
        @change="handleSearch"
      >
        <el-option label="目录" value="DIRECTORY" />
        <el-option label="对象" value="OBJECT" />
        <el-option label="链接" value="LINK" />
      </el-select>
      <el-select
        v-model="query.status"
        placeholder="状态"
        clearable
        style="width: 110px"
        @change="handleSearch"
      >
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="treeData"
      row-key="id"
      :tree-props="{ children: 'children' }"
      :expand-row-keys="expandedKeys"
      border
      empty-text="暂无菜单"
    >
      <el-table-column label="菜单名称" min-width="220">
        <template #default="{ row }">
          <el-icon v-if="resolveIcon(row.icon)" class="menu-icon">
            <component :is="resolveIcon(row.icon)" />
          </el-icon>
          {{ row.menuName }}
        </template>
      </el-table-column>
      <el-table-column label="类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="MENU_TYPE_TAG_TYPES[row.menuType as MenuType]" size="small">
            {{ MENU_TYPE_LABELS[row.menuType as MenuType] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="路由 / 对象" min-width="220">
        <template #default="{ row }">
          <span v-if="row.menuType === 'OBJECT'">
            {{ row.objectApiName }}（{{ row.routePath }}）
          </span>
          <span v-else>{{ row.routePath || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="70" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="显示" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.visible"
            :active-value="1"
            :inactive-value="0"
            @change="handleVisibleChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="170" />
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <MenuFormDialog
      v-model="dialogVisible"
      :menu="dialogTarget"
      :parent-options="parentOptions"
      :default-sort="defaultSort"
      @saved="handleSaved"
    />
  </div>
</template>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.search-label {
  font-size: 14px;
  color: #606266;
}

.menu-icon {
  margin-right: 6px;
  vertical-align: -2px;
}
</style>
