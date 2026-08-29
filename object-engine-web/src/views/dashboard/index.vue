<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="page">
    <el-card>
      <h2 style="margin-top: 0">欢迎使用 Object Engine</h2>
      <p>
        基于元数据驱动的动态业务对象引擎：
        配置对象 → 配置字段 → 获取 Metadata → 动态表单 / 动态列表 → Record CRUD。
      </p>
      <el-button type="primary" @click="router.push('/objects')">进入对象管理</el-button>
    </el-card>
  </div>
</template>
