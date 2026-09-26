<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getObject } from '@/api/object'
import type { CustomObject } from '@/types/object'
import PageHeader from '@/components/common/PageHeader.vue'
import ObjectFormDialog from './components/ObjectFormDialog.vue'

const route = useRoute()
const router = useRouter()
const apiName = route.params.apiName as string

const loading = ref(false)
const object = ref<CustomObject>()

async function load() {
  loading.value = true
  try {
    object.value = await getObject(apiName)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void load()
})

function goFields() {
  router.push(`/admin/objects/${apiName}/fields`)
}

function goLayout() {
  router.push(`/admin/objects/${apiName}/layout`)
}

const dialogVisible = ref(false)

function openEdit() {
  if (object.value) {
    dialogVisible.value = true
  }
}

function handleSaved() {
  void load()
}
</script>

<template>
  <div class="page" v-loading="loading">
    <PageHeader :title="object?.objectName || '对象详情'" :description="object?.description || ''">
      <template #actions>
        <el-button :icon="ArrowLeft" @click="router.back()">返回</el-button>
        <el-button type="primary" @click="goFields">字段配置</el-button>
      </template>
    </PageHeader>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card v-if="object" shadow="never">
          <template #header>
            <span class="section-title">基本信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="对象名称">{{ object.objectName }}</el-descriptions-item>
            <el-descriptions-item label="API Name">
              <span class="text-api">{{ object.apiName }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="描述">{{ object.description || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ object.remark || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="object.status === 1 ? 'success' : 'info'" size="small">
                {{ object.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="排序">{{ object.sort }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ object.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ object.updatedAt }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card v-if="object" shadow="never">
          <template #header>
            <span class="section-title">快捷操作</span>
          </template>
          <div class="detail-actions">
            <!-- 四个入口都是导航而非主操作，主操作（字段配置）放在页头，因此统一用默认按钮 -->
            <el-button class="detail-action" @click="goFields">
              <span class="detail-action__text">
                <span class="detail-action__title">字段配置</span>
                <span class="detail-action__desc">声明字段类型、校验与选项来源</span>
              </span>
            </el-button>
            <el-button class="detail-action" @click="goLayout">
              <span class="detail-action__text">
                <span class="detail-action__title">布局配置</span>
                <span class="detail-action__desc">拖拽分组，前台与后台布局独立</span>
              </span>
            </el-button>
            <el-button
              class="detail-action"
              :disabled="object.status !== 1"
              @click="router.push(`/custom/${object.apiName}`)"
            >
              <span class="detail-action__text">
                <span class="detail-action__title">打开动态页面</span>
                <span class="detail-action__desc">
                  {{ object.status === 1 ? '查看前台动态列表与表单' : '对象已停用，需先启用' }}
                </span>
              </span>
            </el-button>
            <el-button class="detail-action" @click="openEdit">
              <span class="detail-action__text">
                <span class="detail-action__title">编辑对象</span>
                <span class="detail-action__desc">修改名称、描述、排序与状态</span>
              </span>
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <ObjectFormDialog v-model="dialogVisible" :object="object ?? null" @saved="handleSaved" />
  </div>
</template>

<style scoped>
.detail-actions {
  display: flex;
  flex-direction: column;
  gap: var(--oe-space-2);
}

/* 整行可点的操作项：左对齐、文案两行、hover 换底色 */
.detail-action {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  height: auto;
  margin: 0;
  padding: var(--oe-space-3) var(--oe-space-4);
  border: 1px solid var(--oe-border-soft);
  border-radius: var(--oe-radius-md);
  text-align: left;
  font-weight: 400;
}

.detail-action :deep(span) {
  margin: 0;
}

.detail-action:hover {
  border-color: var(--oe-brand-300);
  background-color: var(--oe-brand-50);
}

.detail-action__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-action__title {
  font-size: var(--el-font-size-base);
  font-weight: 500;
  color: var(--oe-text-1);
}

.detail-action__desc {
  font-size: var(--el-font-size-extra-small);
  color: var(--oe-text-3);
}
</style>
