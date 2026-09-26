<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { CircleCheck, Grid, Menu, Right, User } from '@element-plus/icons-vue'
import { listObjects } from '@/api/object'
import { getMenuTree } from '@/api/menu'
import { listSysUsers } from '@/api/sysUser'
import type { CustomObject } from '@/types/object'
import type { MenuTreeItem } from '@/types/menu'
import { getLoginUser } from '@/constants/auth'
import PageHeader from '@/components/common/PageHeader.vue'
import StatCard from '@/components/common/StatCard.vue'

const router = useRouter()
const loginUser = getLoginUser()

/** 拉全量对象（上限 200，够算统计），对象数超了也只影响「启用数」的精度 */
const objects = ref<CustomObject[]>([])
const objectTotal = ref(0)
const objectEnabled = ref(0)
const menuCount = ref(0)
const userTotal = ref(0)
const statsLoading = ref(true)
const recentLoading = ref(true)

/** 统计卡片配色：图标色 + 底色 */
const TONES = {
  brand: { tone: 'var(--oe-brand-500)', toneBg: 'var(--oe-brand-50)' },
  success: { tone: 'var(--oe-success)', toneBg: 'var(--oe-success-soft)' },
  warning: { tone: 'var(--oe-warning)', toneBg: 'var(--oe-warning-soft)' },
  neutral: { tone: 'var(--oe-text-2)', toneBg: 'var(--oe-surface-3)' },
} as const

/** 最近更新的对象：按 updatedAt 倒序取 5 条 */
const recentObjects = computed(() =>
  [...objects.value]
    .sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
    .slice(0, 5),
)

/** 演示动态页时用第一个启用对象，没有可用对象时链接退回到对象列表 */
const sampleApiName = computed(
  () => objects.value.find((object) => object.status === 1)?.apiName ?? '',
)

/** 递归统计菜单树节点数（目录也要算进去） */
function countMenus(nodes: MenuTreeItem[]): number {
  return nodes.reduce((sum, node) => sum + 1 + countMenus(node.children ?? []), 0)
}

async function loadStats() {
  statsLoading.value = true
  // 三个接口互不依赖，各自兜底：任何一个失败都不该让整页空掉
  const [objectResult, menuResult, userResult] = await Promise.allSettled([
    listObjects({ page: 1, pageSize: 200 }),
    getMenuTree(),
    listSysUsers({ page: 1, pageSize: 1 }),
  ])
  if (objectResult.status === 'fulfilled') {
    objects.value = objectResult.value.records
    objectTotal.value = objectResult.value.total
    objectEnabled.value = objectResult.value.records.filter((item) => item.status === 1).length
  }
  if (menuResult.status === 'fulfilled') {
    menuCount.value = countMenus(menuResult.value)
  }
  if (userResult.status === 'fulfilled') {
    userTotal.value = userResult.value.total
  }
  statsLoading.value = false
  recentLoading.value = false
}

/** 快速上手：四步对应产品的实际主线，末步链接到真实可用的动态页 */
const STEPS = computed(() => [
  {
    title: '配置业务对象',
    text: '在对象管理里新建对象，定义对象名称与 API 名称，创建后自动注册到导航菜单。',
    action: '去配置对象',
    to: '/admin/objects',
  },
  {
    title: '配置字段',
    text: '声明字段类型、必填与唯一约束、默认值、选项来源（自定义选项或通用选项集）。',
    action: '去配置字段',
    to: sampleApiName.value ? `/admin/objects/${sampleApiName.value}/fields` : '/admin/objects',
  },
  {
    title: '获取 Metadata',
    text: '前端调用 /objects/{apiName}/metadata 拿到对象与字段的完整结构，作为动态渲染的唯一数据源。',
    action: '查看对象',
    to: sampleApiName.value ? `/admin/objects/${sampleApiName.value}` : '/admin/objects',
  },
  {
    title: '动态表单与 Record CRUD',
    text: '打开 /custom/{apiName} 即得到可筛选、可分页、可增删改查的动态列表。',
    action: '体验动态页面',
    to: sampleApiName.value ? `/custom/${sampleApiName.value}` : '/admin/objects',
  },
])

const SHORTCUTS = [
  { icon: Grid, title: '对象管理', desc: '新建业务对象、启停与删除', to: '/admin/objects' },
  { icon: Menu, title: '菜单管理', desc: '维护前台与后台导航结构', to: '/admin/menus' },
  { icon: Grid, title: '通用选项集', desc: '集中维护下拉选项', to: '/admin/option-sets' },
  { icon: User, title: '用户管理', desc: '账号、角色与密码维护', to: '/admin/users' },
]

onMounted(() => {
  void loadStats()
})
</script>

<template>
  <div class="page">
    <PageHeader title="首页" :description="`欢迎回来，${loginUser?.name || loginUser?.username || '管理员'}`" />

    <el-row :gutter="16">
      <el-col :span="24">
        <div class="hero">
          <div class="hero__text">
            <h3 class="hero__title">基于元数据驱动的动态业务对象引擎</h3>
            <p class="hero__desc">
              配置对象 → 配置字段 → 获取 Metadata → 动态表单 / 动态列表 → Record CRUD。
              不为每个业务对象单独写页面，运行时按元数据渲染界面。
            </p>
            <div class="hero__actions">
              <el-button type="primary" @click="router.push('/admin/objects')">进入对象管理</el-button>
              <el-button
                v-if="sampleApiName"
                text
                :icon="Right"
                @click="router.push(`/custom/${sampleApiName}`)"
              >
                体验动态页面
              </el-button>
            </div>
          </div>
          <div class="hero__aside">
            <el-statistic :value="objectEnabled" title="启用中的对象" />
            <el-divider direction="vertical" />
            <el-statistic :value="menuCount" title="导航菜单项" />
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="stat-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <StatCard
          label="业务对象"
          :value="objectTotal"
          :icon="Grid"
          :loading="statsLoading"
          hint="已创建的业务实体"
          v-bind="TONES.brand"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <StatCard
          label="启用中"
          :value="objectEnabled"
          :icon="CircleCheck"
          :loading="statsLoading"
          hint="停用对象不在前台显示"
          v-bind="TONES.success"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <StatCard
          label="导航菜单项"
          :value="menuCount"
          :icon="Menu"
          :loading="statsLoading"
          hint="含目录 / 对象 / 外链"
          v-bind="TONES.warning"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <StatCard
          label="系统用户"
          :value="userTotal"
          :icon="User"
          :loading="statsLoading"
          hint="可登录后台的账号"
          v-bind="TONES.neutral"
        />
      </el-col>
    </el-row>

    <el-row :gutter="16" class="block-row">
      <el-col :xs="24" :lg="15">
        <el-card shadow="never" class="block-card">
          <template #header>
            <div class="block-card__header">
              <span class="section-title">快速上手</span>
              <span class="text-muted">四步跑通一条完整链路</span>
            </div>
          </template>
          <el-steps :active="-1" direction="vertical" space="72px">
            <el-step
              v-for="(step, index) in STEPS"
              :key="step.title"
              :title="step.title"
              :description="step.text"
            >
              <template #icon>
                <span class="step-index">{{ index + 1 }}</span>
              </template>
              <template #extra>
                <el-button link type="primary" @click="router.push(step.to)">
                  {{ step.action }}
                  <el-icon class="el-icon--right"><Right /></el-icon>
                </el-button>
              </template>
            </el-step>
          </el-steps>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="block-card">
          <template #header>
            <div class="block-card__header">
              <span class="section-title">快捷入口</span>
            </div>
          </template>
          <div class="shortcuts">
            <button
              v-for="shortcut in SHORTCUTS"
              :key="shortcut.to"
              type="button"
              class="shortcut"
              @click="router.push(shortcut.to)"
            >
              <span class="shortcut__icon">
                <el-icon :size="16"><component :is="shortcut.icon" /></el-icon>
              </span>
              <span class="shortcut__text">
                <span class="shortcut__title">{{ shortcut.title }}</span>
                <span class="shortcut__desc">{{ shortcut.desc }}</span>
              </span>
              <el-icon class="shortcut__arrow" :size="12"><Right /></el-icon>
            </button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="block-card block-row">
      <template #header>
        <div class="block-card__header">
          <span class="section-title">最近更新的对象</span>
          <el-button link type="primary" @click="router.push('/admin/objects')">
            查看全部
            <el-icon class="el-icon--right"><Right /></el-icon>
          </el-button>
        </div>
      </template>
      <el-table
        v-loading="recentLoading"
        :data="recentObjects"
        empty-text="还没有创建任何业务对象"
      >
        <el-table-column prop="objectName" label="对象名称" min-width="140" />
        <el-table-column prop="apiName" label="API名称" min-width="160">
          <template #default="{ row }">
            <span class="text-api">{{ row.apiName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="row.status !== 1"
              @click="router.push(`/custom/${row.apiName}`)"
            >
              打开
            </el-button>
            <el-button link type="primary" @click="router.push(`/admin/objects/${row.apiName}`)">
              配置
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
/* —— 欢迎横幅：品牌渐变 + 右侧概要数字 —— */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--oe-space-7);
  padding: var(--oe-space-7);
  border-radius: var(--oe-radius-xl);
  color: var(--oe-on-brand);
  overflow: hidden;
  background:
    radial-gradient(760px 320px at 8% 0%, rgb(255 255 255 / 16%), transparent 60%),
    var(--oe-gradient-brand);
  box-shadow: var(--oe-shadow-brand);
}

.hero__title {
  font-size: 22px;
  font-weight: 600;
  color: var(--oe-on-brand);
}

.hero__desc {
  margin: var(--oe-space-3) 0 0;
  max-width: 620px;
  font-size: var(--el-font-size-base);
  line-height: 1.8;
  color: var(--oe-on-brand-soft);
}

.hero__actions {
  display: flex;
  align-items: center;
  gap: var(--oe-space-3);
  margin-top: var(--oe-space-5);
}

.hero__actions :deep(.el-button--primary) {
  --el-button-bg-color: var(--oe-on-brand);
  --el-button-border-color: var(--oe-on-brand);
  --el-button-text-color: var(--oe-brand-600);
  --el-button-hover-text-color: var(--oe-brand-700);
  --el-button-hover-bg-color: var(--oe-on-brand-soft);
  --el-button-hover-border-color: var(--oe-on-brand);
  box-shadow: none;
}

.hero__actions :deep(.el-button.is-text) {
  color: var(--oe-on-brand-soft);
}

.hero__actions :deep(.el-button.is-text:hover) {
  color: var(--oe-on-brand);
}

.hero__aside {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  padding: var(--oe-space-5) var(--oe-space-6);
  border-radius: var(--oe-radius-lg);
  background-color: var(--oe-on-brand-glass);
}

.hero__aside :deep(.el-divider--vertical) {
  height: 34px;
  border-color: var(--oe-on-brand-line);
}

.hero__aside :deep(.el-statistic__content) {
  color: var(--oe-on-brand);
  font-size: 30px;
  font-weight: 600;
}

.hero__aside :deep(.el-statistic__head) {
  color: var(--oe-on-brand-soft);
  font-size: var(--el-font-size-small);
}

@media (width <= 992px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }
}

/* —— 区块间距 —— */
.stat-row,
.block-row {
  margin-top: var(--oe-space-4);
}

.stat-row :deep(.el-col) {
  margin-bottom: var(--oe-space-4);
}

.block-card :deep(.el-card__header) {
  padding: var(--oe-space-4) var(--oe-space-5);
}

.block-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--oe-space-3);
}

/* —— 快速上手 —— */
.step-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  font-size: var(--el-font-size-extra-small);
  font-weight: 600;
  color: var(--oe-brand-600);
  background-color: var(--oe-brand-50);
}

.block-card :deep(.el-step) {
  padding-bottom: var(--oe-space-4);
}

.block-card :deep(.el-step__title) {
  font-size: var(--el-font-size-base);
  color: var(--oe-text-1);
}

.block-card :deep(.el-step__description) {
  font-size: var(--el-font-size-small);
  line-height: 1.7;
  color: var(--oe-text-3);
  max-width: 640px;
}

.block-card :deep(.el-step__extra) {
  padding-top: var(--oe-space-1);
}

/* —— 快捷入口 —— */
.shortcuts {
  display: flex;
  flex-direction: column;
  gap: var(--oe-space-2);
}

.shortcut {
  display: flex;
  align-items: center;
  gap: var(--oe-space-3);
  width: 100%;
  padding: var(--oe-space-3);
  border: 1px solid var(--oe-border-soft);
  border-radius: var(--oe-radius-md);
  background-color: var(--oe-surface);
  text-align: left;
  cursor: pointer;
  font: inherit;
  transition:
    border-color 0.15s ease,
    background-color 0.15s ease;
}

.shortcut:hover {
  border-color: var(--oe-brand-300);
  background-color: var(--oe-brand-50);
}

.shortcut__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  border-radius: var(--oe-radius-md);
  color: var(--oe-brand-600);
  background-color: var(--oe-brand-50);
}

.shortcut__text {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.shortcut__title {
  font-size: var(--el-font-size-base);
  font-weight: 500;
  color: var(--oe-text-1);
}

.shortcut__desc {
  font-size: var(--el-font-size-extra-small);
  color: var(--oe-text-3);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.shortcut__arrow {
  color: var(--oe-text-4);
}
</style>
