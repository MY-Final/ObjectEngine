<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { deleteObject, listObjects, updateObject } from '@/api/object'
import type { CustomObject } from '@/types/object'
import { useAppStore } from '@/stores/app'
import PageHeader from '@/components/common/PageHeader.vue'
import SearchBar from '@/components/common/SearchBar.vue'
import ObjectFormDialog from './components/ObjectFormDialog.vue'

const router = useRouter()
const appStore = useAppStore()

const loading = ref(false)
const records = ref<CustomObject[]>([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 20, keyword: '' })
const deletingApiName = ref('')

async function load() {
  loading.value = true
  try {
    const result = await listObjects({
      page: query.page,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
    })
    records.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  void load()
}

function handleReset() {
  query.keyword = ''
  query.page = 1
  void load()
}

function handlePageChange() {
  void load()
}

function handleSizeChange() {
  query.page = 1
  void load()
}

/** 行内启停：更新请求字段均可选，从行数据带全量字段提交，失败回弹开关 */
async function handleStatusChange(row: CustomObject) {
  try {
    await updateObject(row.apiName, {
      objectName: row.objectName,
      description: row.description,
      remark: row.remark,
      icon: row.icon,
      sort: row.sort,
      status: row.status,
    })
    ElMessage.success(row.status === 1 ? '已启用' : '已停用')
    // 对象菜单随对象状态联动显示，同步刷新侧边栏
    void appStore.loadMenus()
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

onMounted(() => {
  void load()
})

function goDetail(apiName: string) {
  router.push(`/admin/objects/${apiName}`)
}

function goFields(apiName: string) {
  router.push(`/admin/objects/${apiName}/fields`)
}

const dialogVisible = ref(false)
const dialogTarget = ref<CustomObject | null>(null)

function openCreate() {
  dialogTarget.value = null
  dialogVisible.value = true
}

function openEdit(row: CustomObject) {
  dialogTarget.value = row
  dialogVisible.value = true
}

async function handleSaved() {
  await load()
  // 新建对象会自动注册导航菜单，同步刷新侧边栏
  void appStore.loadMenus()
}

async function handleDelete(row: CustomObject) {
  try {
    await ElMessageBox.confirm(
      h('div', null, [
        h('p', { style: 'margin: 0 0 8px' }, `确定要删除对象「${row.objectName}」吗？`),
        h(
          'p',
          { class: 'form-hint' },
          '删除对象后，该对象的字段和数据也会被删除，此操作不可恢复。',
        ),
      ]),
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  deletingApiName.value = row.apiName
  try {
    await deleteObject(row.apiName)
    ElMessage.success('删除成功')
    await load()
  } finally {
    deletingApiName.value = ''
  }
}
</script>

<template>
  <div class="page">
    <PageHeader title="对象管理" description="业务对象是引擎的核心单元，一个对象对应一张动态页面与一份元数据。">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openCreate">新建对象</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="table-card is-flush">
      <template #header>
        <SearchBar label="关键词：" :loading="loading" @search="handleSearch" @reset="handleReset">
          <el-input
            v-model="query.keyword"
            placeholder="对象名称 / API 名称"
            clearable
            class="w-220"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </SearchBar>
      </template>

      <el-table v-loading="loading" :data="records" empty-text="暂无对象">
        <el-table-column prop="objectName" label="对象名称" min-width="130">
          <template #default="{ row }">
            <div class="cell-main">{{ row.objectName }}</div>
            <div class="text-api">{{ row.apiName }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
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
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.apiName)">查看</el-button>
            <el-button link type="primary" @click="goFields(row.apiName)">字段</el-button>
            <el-button
              link
              type="primary"
              :disabled="row.status !== 1"
              @click="router.push(`/custom/${row.apiName}`)"
            >
              打开
            </el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              link
              type="danger"
              :loading="deletingApiName === row.apiName"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          class="page-pagination"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </template>
    </el-card>

    <ObjectFormDialog v-model="dialogVisible" :object="dialogTarget" @saved="handleSaved" />
  </div>
</template>

<style scoped>
/* 表格首列：主标题 + 等宽 API 名称 */
.cell-main {
  font-weight: 500;
  color: var(--oe-text-1);
}
</style>

