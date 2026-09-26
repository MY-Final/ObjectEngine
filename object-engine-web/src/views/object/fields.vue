<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheckFilled, Plus } from '@element-plus/icons-vue'
import { deleteField, listFields } from '@/api/field'
import { getObject } from '@/api/object'
import type { CustomField, FieldType } from '@/types/field'
import type { CustomObject } from '@/types/object'
import { FIELD_TYPE_LABEL_MAP } from '@/constants/field'
import PageHeader from '@/components/common/PageHeader.vue'
import FieldFormDialog from './components/FieldFormDialog.vue'

const route = useRoute()
const router = useRouter()
const apiName = route.params.apiName as string

const loading = ref(false)
const objectInfo = ref<CustomObject>()
const fields = ref<CustomField[]>([])
const deletingApiName = ref('')

async function load() {
  loading.value = true
  try {
    objectInfo.value = await getObject(apiName)
    fields.value = await listFields(apiName)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void load()
})

function goBackToObject() {
  router.push(`/admin/objects/${apiName}`)
}

async function handleDelete(row: CustomField) {
  try {
    await ElMessageBox.confirm(
        h('div', null, [
          h('p', { style: 'margin: 0 0 8px' }, `确定要删除字段「${row.fieldName}」吗？`),
          h('p', { class: 'form-hint' }, '删除字段后，该字段将不再出现在当前对象的表单和列表中。'),
        ]),
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  deletingApiName.value = row.apiName
  try {
    await deleteField(apiName, row.apiName)
    ElMessage.success('删除成功')
    await load()
  } finally {
    deletingApiName.value = ''
  }
}

const dialogVisible = ref(false)
const dialogTarget = ref<CustomField | null>(null)

function openCreate() {
  dialogTarget.value = null
  dialogVisible.value = true
}

function openEdit(row: CustomField) {
  dialogTarget.value = row
  dialogVisible.value = true
}

function handleSaved() {
  void load()
}
</script>

<template>
  <div class="page" v-loading="loading">
    <PageHeader
      title="字段配置"
      :description="objectInfo
        ? `对象：${objectInfo.objectName} / API Name：${objectInfo.apiName}`
        : `API Name：${apiName}`"
    >
      <template #actions>
        <el-button @click="goBackToObject">返回对象</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新建字段</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="table-card is-flush">
      <template #header>
        <div class="section-title">字段列表<span class="text-muted">按排序升序</span></div>
      </template>

      <el-table :data="fields" empty-text="该对象还没有字段">
        <el-table-column prop="fieldName" label="字段名称" min-width="150">
          <template #default="{ row }">
            <div class="cell-main">{{ row.fieldName }}</div>
            <div class="text-api">{{ row.apiName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">
              {{ FIELD_TYPE_LABEL_MAP[row.fieldType as FieldType] ?? row.fieldType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="必填" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.requiredFlag === 1" color="var(--oe-danger)"><CircleCheckFilled /></el-icon>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="唯一" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.uniqueFlag === 1" color="var(--oe-danger)"><CircleCheckFilled /></el-icon>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="可搜索" width="90" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.searchableFlag === 1" color="var(--oe-brand-500)"><CircleCheckFilled /></el-icon>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="可排序" width="90" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.sortableFlag === 1" color="var(--oe-brand-500)"><CircleCheckFilled /></el-icon>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
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
    </el-card>

    <FieldFormDialog
      v-model="dialogVisible"
      :object-api-name="apiName"
      :field="dialogTarget"
      :default-sort="fields.length + 1"
      @saved="handleSaved"
    />
  </div>
</template>

<style scoped>
.cell-main {
  font-weight: 500;
  color: var(--oe-text-1);
}
</style>
