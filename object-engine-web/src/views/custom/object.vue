<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMetadata } from '@/api/object'
import { createRecord, deleteRecord, listRecords } from '@/api/record'
import DynamicForm from '@/components/dynamic/DynamicForm.vue'
import DynamicTable from '@/components/dynamic/DynamicTable.vue'
import type { CustomField } from '@/types/field'
import type { ObjectMetadata } from '@/types/object'
import type { CustomRecord } from '@/types/record'

const route = useRoute()
const router = useRouter()

const apiName = computed(() => route.params.apiName as string)

const metadataLoading = ref(true)
const loadFailed = ref(false)
const notFound = ref(false)
const metadata = ref<ObjectMetadata>()

/** 启用字段，按 sort ASC 排序，不依赖后端返回顺序 */
const activeFields = computed<CustomField[]>(() =>
  (metadata.value?.fields ?? [])
    .filter((field) => field.status === 1)
    .sort((a, b) => a.sort - b.sort),
)

const recordsLoading = ref(false)
const records = ref<CustomRecord[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 20

async function loadMetadata() {
  metadataLoading.value = true
  loadFailed.value = false
  notFound.value = false
  try {
    metadata.value = await getMetadata(apiName.value)
  } catch (error) {
    const status = (error as { response?: { status?: number } }).response?.status
    if (status === 404) {
      notFound.value = true
    } else {
      loadFailed.value = true
    }
  } finally {
    metadataLoading.value = false
  }
}

async function loadRecords() {
  recordsLoading.value = true
  try {
    const result = await listRecords(apiName.value, { page: page.value, pageSize })
    records.value = result.records
    total.value = result.total
  } finally {
    recordsLoading.value = false
  }
}

function init() {
  page.value = 1
  void loadMetadata().then(() => {
    if (!notFound.value && !loadFailed.value) {
      void loadRecords()
    }
  })
}

watch(apiName, init, { immediate: true })

function handlePageChange(nextPage: number) {
  page.value = nextPage
  void loadRecords()
}

// —— 新建记录 ——
const dialogVisible = ref(false)
const submitting = ref(false)
const formModel = ref<Record<string, unknown>>({})
const dynamicFormRef = ref<InstanceType<typeof DynamicForm>>()

function openCreate() {
  if (!metadata.value) return
  // 按 fields 初始化表单数据，存在 defaultValue 的字段使用默认值
  const model: Record<string, unknown> = {}
  for (const field of activeFields.value) {
    const defaultValue = field.defaultValue
    if (defaultValue == null || defaultValue === '') {
      model[field.apiName] = null
    } else if (field.fieldType === 'NUMBER' || field.fieldType === 'MONEY') {
      model[field.apiName] = Number(defaultValue)
    } else {
      model[field.apiName] = defaultValue
    }
  }
  formModel.value = model
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!dynamicFormRef.value) return
  const valid = await dynamicFormRef.value.validate()
  if (!valid) return
  submitting.value = true
  try {
    await createRecord(apiName.value, formModel.value)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    await loadRecords()
  } finally {
    submitting.value = false
  }
}

// —— 删除记录 ——
const deletingId = ref<number | null>(null)

async function handleDelete(record: CustomRecord) {
  try {
    await ElMessageBox.confirm('确定删除这条数据吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  deletingId.value = record.id
  try {
    await deleteRecord(apiName.value, record.id)
    ElMessage.success('删除成功')
    await loadRecords()
  } finally {
    deletingId.value = null
  }
}
</script>

<template>
  <div class="page">
    <div v-if="metadataLoading" v-loading="true" class="page-loading" />

    <el-result v-else-if="notFound" icon="warning" title="对象不存在" sub-title="该对象可能已被删除">
      <template #extra>
        <el-button type="primary" @click="router.push('/admin/objects')">返回对象管理</el-button>
      </template>
    </el-result>

    <el-result v-else-if="loadFailed" icon="error" title="无法加载对象信息">
      <template #extra>
        <el-button type="primary" @click="init">重新加载</el-button>
      </template>
    </el-result>

    <template v-else-if="metadata">
      <div class="page-toolbar">
        <div>
          <h2 style="margin: 0; font-size: 18px">{{ metadata.object.objectName }}</h2>
          <div v-if="metadata.object.description" class="object-desc">
            {{ metadata.object.description }}
          </div>
        </div>
        <el-button type="primary" @click="openCreate">新建</el-button>
      </div>

      <DynamicTable
        :fields="activeFields"
        :records="records"
        :loading="recordsLoading"
        :deleting-id="deletingId"
        @delete="handleDelete"
      />

      <el-pagination
        class="page-pagination"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="handlePageChange"
      />

      <el-dialog v-model="dialogVisible" :title="`新建${metadata.object.objectName}`" width="560px">
        <DynamicForm ref="dynamicFormRef" v-model="formModel" :fields="activeFields" />
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<style scoped>
.page-loading {
  height: 240px;
}

.object-desc {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.page-pagination {
  display: flex;
  margin-top: 16px;
}
</style>
