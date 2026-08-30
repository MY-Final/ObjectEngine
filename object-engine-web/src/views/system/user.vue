<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createSysUser,
  deleteSysUser,
  listSysUsers,
  resetSysUserPassword,
  updateSysUser,
  updateSysUserStatus,
} from '@/api/sysUser'
import type { SysUserItem } from '@/types/sysUser'
import { getLoginUser } from '@/constants/auth'

const currentUserId = computed(() => getLoginUser()?.id ?? null)

const loading = ref(false)
const records = ref<SysUserItem[]>([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 20, keyword: '', status: undefined as number | undefined })

async function load() {
  loading.value = true
  try {
    const result = await listSysUsers({
      page: query.page,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status,
    })
    records.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  void load()
}

function handleReset() {
  query.keyword = ''
  query.status = undefined
  query.page = 1
  void load()
}

function handlePageChange() {
  void load()
}

function handleSizeChange() {
  query.page = 1
  void load()
}

async function handleStatusChange(row: SysUserItem) {
  try {
    await updateSysUserStatus(row.id, row.status)
    ElMessage.success(row.status === 1 ? '已启用' : '已停用')
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

async function handleDelete(row: SysUserItem) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.name}（${row.username}）」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  await deleteSysUser(row.id)
  ElMessage.success('删除成功')
  await load()
}

// —— 新建 / 编辑 ——
const dialogVisible = ref(false)
const dialogTarget = ref<SysUserItem | null>(null)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  username: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  remark: '',
  status: 1,
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在 6 ~ 32 位之间', trigger: 'blur' },
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}

function openCreate() {
  dialogTarget.value = null
  form.username = ''
  form.password = ''
  form.name = ''
  form.email = ''
  form.phone = ''
  form.remark = ''
  form.status = 1
  dialogVisible.value = true
}

function openEdit(row: SysUserItem) {
  dialogTarget.value = row
  form.username = row.username
  form.password = ''
  form.name = row.name
  form.email = row.email ?? ''
  form.phone = row.phone ?? ''
  form.remark = row.remark ?? ''
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (dialogTarget.value) {
      await updateSysUser(dialogTarget.value.id, {
        name: form.name,
        email: form.email || undefined,
        phone: form.phone || undefined,
        remark: form.remark || undefined,
      })
      ElMessage.success('保存成功')
    } else {
      await createSysUser({
        username: form.username,
        password: form.password,
        name: form.name,
        email: form.email || undefined,
        phone: form.phone || undefined,
        remark: form.remark || undefined,
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

// —— 重置密码 ——
async function handleResetPassword(row: SysUserItem) {
  let password = ''
  try {
    const result = await ElMessageBox.prompt('请输入新密码（6 ~ 32 位）', `重置「${row.name}」的密码`, {
      type: 'warning',
      confirmButtonText: '重置',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPattern: /^.{6,32}$/,
      inputErrorMessage: '密码长度需在 6 ~ 32 位之间',
    })
    password = result.value
  } catch {
    return
  }
  await resetSysUserPassword(row.id, password)
  ElMessage.success('密码已重置')
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="page">
    <div class="page-toolbar">
      <h2>用户管理</h2>
      <el-button type="primary" @click="openCreate">新建用户</el-button>
    </div>

    <div class="search-bar">
      <span class="search-label">搜索：</span>
      <el-input
        v-model="query.keyword"
        placeholder="用户名 / 姓名"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-select
        v-model="query.status"
        placeholder="状态"
        clearable
        style="width: 110px"
        @change="handleSearch"
      >
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="records" border empty-text="暂无用户">
      <el-table-column prop="name" label="姓名" min-width="120" />
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.email || '-' }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" min-width="140">
        <template #default="{ row }">{{ row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            :disabled="row.id === currentUserId"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column prop="updatedAt" label="修改时间" width="170" />
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleResetPassword(row)">重置密码</el-button>
          <el-button
            link
            type="danger"
            :disabled="row.id === currentUserId"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.pageSize"
      class="page-pagination"
      layout="total, sizes, prev, pager, next, jumper"
      :page-sizes="[10, 20, 50]"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTarget ? '编辑用户' : '新建用户'"
      width="520px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-if="!dialogTarget" v-model="form.username" placeholder="登录账号，创建后不可修改" />
          <el-input v-else :model-value="form.username" disabled />
          <div v-if="dialogTarget" class="form-hint">用户名创建后不可修改</div>
        </el-form-item>
        <el-form-item v-if="!dialogTarget" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="6 ~ 32 位" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="展示用的姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item v-if="dialogTarget" label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
            :disabled="dialogTarget.id === currentUserId"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.search-label {
  font-size: 14px;
  color: #606266;
}

.page-pagination {
  display: flex;
  margin-top: 16px;
}

.form-hint {
  width: 100%;
  font-size: 12px;
  color: #909399;
}
</style>
