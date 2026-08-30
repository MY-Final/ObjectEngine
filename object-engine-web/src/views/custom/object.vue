<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMetadata } from '@/api/object'
import { createRecord } from '@/api/record'
import DynamicForm from '@/components/dynamic/DynamicForm.vue'
import DynamicObjectList from '@/components/dynamic/DynamicObjectList.vue'
import type { CustomField } from '@/types/field'
import type { ObjectMetadata } from '@/types/object'

/**
 * ObjectRuntime 页面：只负责路由参数、页头与新建弹窗；
 * 列表的查询 / 分页 / 删除全部由 DynamicObjectList 组件承担
 */
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

watch(
  apiName,
  () => {
    void loadMetadata()
  },
  { immediate: true },
)

// —— 新建记录 ——
const dialogVisible = ref(false)
const submitting = ref(false)
const formModel = ref<Record<string, unknown>>({})
const dynamicFormRef = ref<InstanceType<typeof DynamicForm>>()
const listRef = ref<InstanceType<typeof DynamicObjectList>>()

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
    // 列表组件重置筛选与页码后重新加载，新记录立即可见
    listRef.value?.refresh()
  } finally {
    submitting.value = false
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
        <el-button type="primary" @click="loadMetadata">重新加载</el-button>
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

      <DynamicObjectList ref="listRef" :object-api-name="apiName" />

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
</style>
