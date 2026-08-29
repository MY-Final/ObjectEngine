<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listFields } from '@/api/field'
import { getObject } from '@/api/object'
import { getLayout, saveLayout } from '@/api/layout'
import type { CustomField } from '@/types/field'
import type { LayoutConfig, LayoutType } from '@/types/layout'
import {
  buildDefaultLayoutConfig,
  normalizeLayoutSections,
  prepareLayoutForSave,
  validateLayoutForSave,
} from '@/utils/layout'
import LayoutEditor from '@/components/layout/LayoutEditor.vue'
import LayoutPreview from '@/components/layout/LayoutPreview.vue'

const route = useRoute()
const router = useRouter()
const apiName = route.params.apiName as string

const loading = ref(true)
const saving = ref(false)
const objectName = ref('')
const fields = ref<CustomField[]>([])
const activeTab = ref<LayoutType>('FRONTEND')
const config = ref<LayoutConfig>({ sections: [] })
const layoutName = ref('')
const droppedApiNames = ref<string[]>([])

const enabledFields = computed(() => fields.value.filter((field) => field.status !== 0))

async function loadTab(type: LayoutType) {
  loading.value = true
  try {
    try {
      fields.value = await listFields(apiName)
      const stored = await getLayout(apiName, type)
      const source = stored?.configJson ?? buildDefaultLayoutConfig(fields.value)
      // 打开时自动过滤已删除/停用字段（规范第五十五节 Test 7）
      const normalized = normalizeLayoutSections(source, fields.value)
      droppedApiNames.value = normalized.droppedApiNames
      config.value = {
        sections: normalized.sections.map((section) => ({
          id: section.id,
          title: section.title,
          sort: 0,
          columns: 12,
          fields: section.fields.map((renderField, index) => ({
            fieldApiName: renderField.field.apiName,
            span: renderField.span,
            sort: index + 1,
          })),
        })),
      }
      layoutName.value = stored?.layoutName ?? (type === 'FRONTEND' ? '默认前台布局' : '默认后台布局')
      if (normalized.droppedApiNames.length > 0) {
        ElMessage.warning('布局中存在已删除或停用字段，已自动忽略')
      }
    } catch {
      // 布局接口不可用（如后端未升级）时回退默认布局，不让页面崩溃
      config.value = buildDefaultLayoutConfig(fields.value)
    }
  } finally {
    loading.value = false
  }
}

function handleTabChange(type: string | number) {
  void loadTab(type as LayoutType)
}

async function handleSave() {
  const issues = validateLayoutForSave(config.value)
  if (issues.length > 0) {
    ElMessage.error(issues[0])
    return
  }
  saving.value = true
  try {
    const cleaned = prepareLayoutForSave(config.value, fields.value)
    await saveLayout(apiName, activeTab.value, {
      layoutName: layoutName.value,
      layoutType: activeTab.value,
      status: 1,
      configJson: cleaned,
    })
    ElMessage.success('布局保存成功')
    await loadTab(activeTab.value)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    objectName.value = (await getObject(apiName)).objectName
  } catch {
    // 对象不存在时保留 apiName 展示，错误提示由 http 层统一处理
  }
  await loadTab(activeTab.value)
})
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-toolbar">
      <h2>
        布局配置
        <span class="layout-subtitle">
          对象：{{ objectName || apiName }} / API：{{ apiName }}
        </span>
      </h2>
      <el-button @click="router.push(`/admin/objects/${apiName}`)">返回对象</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="前台布局" name="FRONTEND" />
      <el-tab-pane label="后台布局" name="BACKEND" />
    </el-tabs>

    <el-row :gutter="16">
      <el-col :span="12">
        <LayoutEditor
          v-model="config"
          :fields="fields"
          :layout-type="activeTab"
          :saving="saving"
          @save="handleSave"
        />
      </el-col>
      <el-col :span="12">
        <LayoutPreview :fields="enabledFields" :layout="config" />
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.layout-subtitle {
  margin-left: 12px;
  font-size: 13px;
  font-weight: normal;
  color: #909399;
}
</style>
