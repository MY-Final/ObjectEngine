<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getObject } from '@/api/object'
import type { CustomObject } from '@/types/object'
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
  router.push(`/objects/${apiName}/fields`)
}

function goLayout() {
  router.push(`/objects/${apiName}/layout`)
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
    <div class="page-toolbar">
      <h2>{{ object?.objectName || '对象详情' }}</h2>
      <div>
        <el-button @click="router.back()">返回</el-button>
        <el-button type="primary" @click="goFields">字段配置</el-button>
      </div>
    </div>

    <el-card v-if="object">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="对象名称">{{ object.objectName }}</el-descriptions-item>
        <el-descriptions-item label="API Name">{{ object.apiName }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ object.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ object.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="object.status === 1 ? 'success' : 'info'">
            {{ object.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="排序">{{ object.sort }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ object.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ object.updatedAt }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-actions">
        <el-button type="primary" @click="goFields">字段配置</el-button>
        <el-button type="primary" plain @click="goLayout">布局配置</el-button>
        <el-button @click="openEdit">编辑</el-button>
      </div>
    </el-card>

    <ObjectFormDialog v-model="dialogVisible" :object="object ?? null" @saved="handleSaved" />
  </div>
</template>

<style scoped>
.detail-actions {
  margin-top: 16px;
}
</style>
