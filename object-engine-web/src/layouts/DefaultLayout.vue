<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const appStore = useAppStore()
</script>

<template>
  <el-container class="layout">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '200px'" class="layout-aside">
      <div class="layout-logo">Object Engine</div>
      <el-menu :default-active="route.path" router :collapse="appStore.sidebarCollapsed" class="layout-menu">
        <el-menu-item index="/dashboard">首页</el-menu-item>
        <el-menu-item index="/objects">对象管理</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <el-button text @click="appStore.toggleSidebar">
          {{ appStore.sidebarCollapsed ? '展开' : '收起' }}
        </el-button>
        <span class="layout-title">{{ route.meta.title }}</span>
      </el-header>
      <el-main class="layout-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}

.layout-aside {
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
  transition: width 0.2s;
}

.layout-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  border-bottom: 1px solid #e6e6e6;
  white-space: nowrap;
  overflow: hidden;
}

.layout-menu {
  border-right: none;
}

.layout-header {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}

.layout-title {
  font-size: 16px;
  font-weight: 600;
}

.layout-main {
  padding: 0;
}
</style>
