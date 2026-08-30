<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import {
  createOption,
  createOptionSet,
  deleteOption,
  deleteOptionSet,
  getOptionSet,
  listOptionSets,
  updateOption,
  updateOptionSet,
} from '@/api/optionSet'
import type { OptionSet } from '@/types/optionSet'

const MAX_OPTIONS = 1000

const loading = ref(false)
const records = ref<OptionSet[]>([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 20, keyword: '', status: undefined as number | undefined })

async function load() {
  loading.value = true
  try {
    const result = await listOptionSets({
      page: query.page,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status,
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
  query.status = undefined
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

interface OptionRow {
  key: string
  /** 已保存的选项 id；新增行为空 */
  id?: number
  label: string
  value: string
  status: 0 | 1
  /** 装载时来自后端的属性，用于保存时做变更检测 */
  snapshot?: { label: string; sort: number; status: number }
}

// —— 新建 / 编辑弹窗：基本信息 + 选项内联管理（拖拽排序） ——
const dialogVisible = ref(false)
const dialogTarget = ref<OptionSet | null>(null)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ name: '', apiName: '', description: '', status: 1 })

const rows = ref<OptionRow[]>([])
const snapshotOptions = ref<{ id: number; label: string; sort: number; status: number }[]>([])
const sortByName = ref(false)
const dragIndex = ref<number | null>(null)
let rowKeySeed = 0

const rules = {
  name: [{ required: true, message: '请输入选项集名称', trigger: 'blur' }],
  apiName: [{ required: true, message: '请输入 API 名称', trigger: 'blur' }],
}

const optionsFull = computed(() => rows.value.length >= MAX_OPTIONS)

function nextRowKey() {
  rowKeySeed += 1
  return `row-${rowKeySeed}`
}

function openCreate() {
  dialogTarget.value = null
  form.name = ''
  form.apiName = ''
  form.description = ''
  form.status = 1
  rows.value = []
  snapshotOptions.value = []
  sortByName.value = false
  dialogVisible.value = true
}

async function openEdit(row: OptionSet) {
  dialogTarget.value = row
  form.name = row.name
  form.apiName = row.apiName
  form.description = row.description ?? ''
  form.status = row.status
  rows.value = []
  snapshotOptions.value = []
  sortByName.value = false
  dialogVisible.value = true
  // 弹窗内直接管理选项，取详情（含停用选项）
  const detail = await getOptionSet(row.id)
  snapshotOptions.value = (detail.options ?? []).map((option) => ({
    id: option.id,
    label: option.label,
    sort: option.sort,
    status: option.status,
  }))
  rows.value = (detail.options ?? []).map((option) => ({
    key: nextRowKey(),
    id: option.id,
    label: option.label,
    value: option.value,
    status: option.status === 1 ? 1 : 0,
  }))
}

function addRow() {
  if (optionsFull.value) {
    ElMessage.warning(`一个选项集最多 ${MAX_OPTIONS} 个选项`)
    return
  }
  rows.value = [...rows.value, { key: nextRowKey(), label: '', value: '', status: 1 }]
}

function removeRow(row: OptionRow) {
  if (row.id) {
    // 已保存的选项在保存弹窗时才会真正删除，先确认一次
    ElMessageBox.confirm(`确定移除选项「${row.label}」吗？保存后生效`, '移除确认', {
      type: 'warning',
      confirmButtonText: '移除',
      cancelButtonText: '取消',
    })
      .then(() => {
        rows.value = rows.value.filter((item) => item.key !== row.key)
      })
      .catch(() => {})
    return
  }
  rows.value = rows.value.filter((item) => item.key !== row.key)
}

function toggleStatus(row: OptionRow) {
  row.status = row.status === 1 ? 0 : 1
}

// —— 拖拽排序 ——
function onDragStart(index: number, event: DragEvent) {
  dragIndex.value = index
  event.dataTransfer?.setData('text/plain', String(index))
}

function onDrop(index: number) {
  if (dragIndex.value == null || dragIndex.value === index) return
  const [moved] = rows.value.splice(dragIndex.value, 1)
  if (!moved) return
  rows.value.splice(index, 0, moved)
  dragIndex.value = null
}

// —— 批量添加：每行一条，格式「名称,值」（省略值时与名称相同） ——
const batchVisible = ref(false)
const batchText = ref('')

function applyBatch() {
  const lines = batchText.value
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
  for (const line of lines) {
    if (rows.value.length >= MAX_OPTIONS) {
      ElMessage.warning(`一个选项集最多 ${MAX_OPTIONS} 个选项`)
      break
    }
    const [label, value] = line.split(/[,，\t]/).map((part) => part.trim())
    if (!label) continue
    rows.value = [
      ...rows.value,
      { key: nextRowKey(), label, value: value || label, status: 1 },
    ]
  }
  batchText.value = ''
  batchVisible.value = false
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return
  for (const row of rows.value) {
    if (!row.label.trim()) {
      ElMessage.warning('选项名称不能为空')
      return
    }
    if (!row.id && !row.value.trim()) {
      ElMessage.warning(`选项「${row.label.trim()}」的选项值不能为空`)
      return
    }
  }
  const values = rows.value.map((row) => (row.value.trim() || row.label.trim()))
  const duplicated = values.find((value, index) => values.indexOf(value) !== index)
  if (duplicated) {
    ElMessage.warning(`选项值重复：${duplicated}`)
    return
  }

  submitting.value = true
  try {
    // 按选项名称排序保存：先把行序按名称重排，sort 随行序写入
    if (sortByName.value) {
      rows.value = [...rows.value].sort((a, b) => a.label.trim().localeCompare(b.label.trim(), 'zh-CN'))
    }

    if (!dialogTarget.value) {
      const created = await createOptionSet({
        name: form.name,
        apiName: form.apiName,
        description: form.description || undefined,
        status: form.status,
      })
      let sort = 1
      for (const row of rows.value) {
        await createOption(created, {
          label: row.label.trim(),
          value: row.value.trim() || row.label.trim(),
          sort,
          status: row.status,
        })
        sort += 1
      }
      ElMessage.success('创建成功')
    } else {
      await updateOptionSet(dialogTarget.value.id, {
        name: form.name,
        description: form.description || undefined,
        status: form.status,
      })
      // 同步选项：先删移除的，再按行序更新变化的 / 新增的
      const keptIds = new Set(rows.value.filter((row) => row.id).map((row) => row.id as number))
      for (const original of snapshotOptions.value) {
        if (!keptIds.has(original.id)) {
          await deleteOption(dialogTarget.value.id, original.id)
        }
      }
      const originalById = new Map(snapshotOptions.value.map((option) => [option.id, option]))
      let sort = 1
      for (const row of rows.value) {
        const label = row.label.trim()
        const value = row.value.trim() || label
        if (!row.id) {
          await createOption(dialogTarget.value.id, { label, value, sort, status: row.status })
        } else {
          const original = originalById.get(row.id)
          if (original && (original.label !== label || original.sort !== sort || original.status !== row.status)) {
            await updateOption(dialogTarget.value.id, row.id, { label, sort, status: row.status })
          }
        }
        sort += 1
      }
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: OptionSet) {
  try {
    await ElMessageBox.confirm(`确定删除选项集「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  await deleteOptionSet(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="page">
    <div class="page-toolbar">
      <h2>通用选项集</h2>
      <el-button type="primary" @click="openCreate">新建选项集</el-button>
    </div>

    <div class="search-bar">
      <span class="search-label">搜索：</span>
      <el-input
        v-model="query.keyword"
        placeholder="选项集名称 / API 名称"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
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

    <el-table v-loading="loading" :data="records" border empty-text="暂无选项集">
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="apiName" label="API名称" min-width="160" />
      <el-table-column prop="optionsSummary" label="选项信息" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ row.optionsSummary || '-' }}</template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="140" show-overflow-tooltip />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column prop="updatedAt" label="修改时间" width="170" />
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTarget ? '编辑通用选项集' : '新建通用选项集'"
      width="860px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：业务状态" />
        </el-form-item>
        <el-form-item label="API名称" prop="apiName">
          <el-input v-if="!dialogTarget" v-model="form.apiName" placeholder="例如：business_status" />
          <el-input v-else :model-value="form.apiName" disabled />
          <div v-if="dialogTarget" class="form-hint">API 名称创建后不可修改</div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>

        <el-form-item label="选项信息" required>
          <div class="option-editor">
            <div class="grid-row grid-header">
              <span class="col-drag" />
              <span>选项名称</span>
              <span>选项值</span>
              <span class="col-action">操作</span>
            </div>
            <div
              v-for="(row, index) in rows"
              :key="row.key"
              class="grid-row"
              @dragover.prevent
              @drop="onDrop(index)"
            >
              <span
                class="col-drag drag-handle"
                draggable="true"
                title="拖拽调整排序"
                @dragstart="onDragStart(index, $event)"
                @dragend="dragIndex = null"
              >
                <el-icon><Rank /></el-icon>
              </span>
              <el-input v-model="row.label" placeholder="选项名称，如：待处理" />
              <el-input
                v-if="!row.id"
                v-model="row.value"
                placeholder="选项值，如：pending"
              />
              <span v-else class="value-text" title="选项值创建后不可修改">{{ row.value }}</span>
              <div class="col-action">
                <el-button link type="primary" @click="toggleStatus(row)">
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button link type="danger" @click="removeRow(row)">删除</el-button>
              </div>
            </div>
            <div class="editor-footer">
              <div class="footer-left">
                <el-button @click="addRow" :disabled="optionsFull">添加</el-button>
                <el-popover v-model:visible="batchVisible" width="320" trigger="click">
                  <template #reference>
                    <el-button :disabled="optionsFull">批量添加</el-button>
                  </template>
                  <el-input
                    v-model="batchText"
                    type="textarea"
                    :rows="6"
                    placeholder="每行一条，格式：名称,值&#10;省略值时与名称相同&#10;例如：&#10;待处理,pending&#10;进行中"
                  />
                  <div style="text-align: right; margin-top: 8px">
                    <el-button size="small" @click="batchVisible = false">取消</el-button>
                    <el-button size="small" type="primary" @click="applyBatch">确定</el-button>
                  </div>
                </el-popover>
                <span class="option-count">{{ rows.length }}/{{ MAX_OPTIONS }}</span>
              </div>
              <el-checkbox v-model="sortByName">按选项名称排序保存</el-checkbox>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保 存</el-button>
      </template>
    </el-dialog>
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

.page-pagination {
  display: flex;
  margin-top: 16px;
}

.form-hint {
  width: 100%;
  font-size: 12px;
  color: #909399;
}

.option-editor {
  width: 100%;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 8px 10px;
}

.grid-row {
  display: grid;
  grid-template-columns: 24px 1fr 1fr 120px;
  gap: 8px;
  align-items: center;
  padding: 5px 0;
}

.grid-header {
  font-size: 12px;
  color: #909399;
}

.drag-handle {
  color: #c0c4cc;
  cursor: grab;
  display: inline-flex;
}

.drag-handle:active {
  cursor: grabbing;
}

.value-text {
  color: #606266;
}

.col-action {
  display: flex;
  gap: 4px;
  justify-content: flex-start;
}

.editor-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-count {
  font-size: 12px;
  color: #909399;
}
</style>
