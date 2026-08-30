<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMetadata } from '@/api/object'
import { deleteRecord, listRecords, updateRecord } from '@/api/record'
import type { CustomField } from '@/types/field'
import type { ObjectMetadata } from '@/types/object'
import type { CustomRecord, RecordFilterQuery } from '@/types/record'
import { buildFormModel } from '@/utils/record'
import DynamicForm from './DynamicForm.vue'
import DynamicFilter from './DynamicFilter.vue'
import DynamicTable from './DynamicTable.vue'

/**
 * 通用动态对象列表：
 * 按 objectApiName 拉取 Metadata 动态渲染字段列，内置关键字筛选 / 分页 / 删除；
 * 新建成功后由父组件调用 refresh() 重置筛选并重新加载
 */
const props = defineProps<{ objectApiName: string }>()

/** 启用字段按 sort 升序，作为列表列 */
const fields = ref<CustomField[]>([])
const records = ref<CustomRecord[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const query = ref<RecordFilterQuery>({ keyword: '' })
const listLoading = ref(false)
const deletingId = ref<number | null>(null)

async function loadMetadata() {
  const metadata: ObjectMetadata = await getMetadata(props.objectApiName)
  fields.value = (metadata.fields ?? [])
    .filter((field) => field.status === 1)
    .sort((a, b) => a.sort - b.sort)
}

async function loadRecords() {
  listLoading.value = true
  try {
    const result = await listRecords(props.objectApiName, {
      page: page.value,
      pageSize: pageSize.value,
      keyword: query.value.keyword.trim() || undefined,
    })
    records.value = result.records
    total.value = result.total
  } finally {
    listLoading.value = false
  }
}

/** 搜索 / 重置都从第 1 页开始查 */
function handleSearch() {
  page.value = 1
  void loadRecords()
}

function handleReset() {
  page.value = 1
  void loadRecords()
}

function handlePageChange() {
  void loadRecords()
}

function handleSizeChange() {
  page.value = 1
  void loadRecords()
}

async function handleDelete(row: CustomRecord) {
  try {
    await ElMessageBox.confirm('确定删除这条数据吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  deletingId.value = row.id
  try {
    await deleteRecord(props.objectApiName, row.id)
    ElMessage.success('删除成功')
    // 当前页仅剩这条且不在第一页时回退一页，避免停在空页
    if (records.value.length === 1 && page.value > 1) {
      page.value -= 1
    }
    await loadRecords()
  } finally {
    deletingId.value = null
  }
}

// —— 编辑记录 ——
const editVisible = ref(false)
const editingRecord = ref<CustomRecord | null>(null)
const editModel = ref<Record<string, unknown>>({})
const editFormRef = ref<InstanceType<typeof DynamicForm>>()
const editSubmitting = ref(false)

function openEdit(row: CustomRecord) {
  editingRecord.value = row
  editModel.value = buildFormModel(fields.value, row.data)
  editVisible.value = true
}

async function handleEditSubmit() {
  if (!editingRecord.value || !editFormRef.value) return
  const valid = await editFormRef.value.validate()
  if (!valid) return
  editSubmitting.value = true
  try {
    await updateRecord(props.objectApiName, editingRecord.value.id, editModel.value)
    ElMessage.success('保存成功')
    editVisible.value = false
    // 编辑不改变条数，保持当前筛选与页码刷新即可
    await loadRecords()
  } finally {
    editSubmitting.value = false
  }
}

/** 重置筛选与页码后重新加载，供外部（如新建成功后）调用 */
function refresh() {
  page.value = 1
  query.value = { keyword: '' }
  void loadRecords()
}

watch(
  () => props.objectApiName,
  async () => {
    page.value = 1
    query.value = { keyword: '' }
    fields.value = []
    records.value = []
    total.value = 0
    try {
      await loadMetadata()
    } catch {
      // 对象不存在 / 加载失败：拦截器已提示，列表保持空态
      return
    }
    await loadRecords()
  },
  { immediate: true },
)

defineExpose({ refresh })
</script>

<template>
  <div class="dynamic-object-list">
    <DynamicFilter v-model="query" :fields="fields" @search="handleSearch" @reset="handleReset" />
    <DynamicTable
      :fields="fields"
      :records="records"
      :loading="listLoading"
      :deleting-id="deletingId"
      @edit="openEdit"
      @delete="handleDelete"
    />
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      class="list-pagination"
      layout="total, sizes, prev, pager, next, jumper"
      :page-sizes="[10, 20, 50]"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />

    <el-dialog v-model="editVisible" title="编辑记录" width="560px">
      <DynamicForm
        ref="editFormRef"
        v-model="editModel"
        :fields="fields"
        :displays="editingRecord?.displays ?? null"
      />
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.list-pagination {
  display: flex;
  margin-top: 16px;
}
</style>
