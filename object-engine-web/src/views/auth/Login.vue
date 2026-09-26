<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Grid, MagicStick } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { setLoginUser, setToken } from '@/constants/auth'
import type { FormInstance, FormRules } from 'element-plus'
import BrandLogo from '@/components/common/BrandLogo.vue'

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

/** 左侧品牌面的特性说明，内容与产品主线保持一致 */
const FEATURES = [
  { icon: Grid, title: '元数据驱动', text: '配置对象与字段，即刻生成运行时表单与列表' },
  { icon: MagicStick, title: '动态渲染', text: '前端按 Metadata 渲染控件，无需为每个对象写页面' },
  { icon: DataAnalysis, title: 'Record CRUD', text: '记录增删改查、关联查询与选项集开箱即用' },
]

async function handleSubmit() {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const result = await login({ username: form.username, password: form.password })
    setToken(result.token)
    setLoginUser(result.user)
    ElMessage.success(`欢迎回来，${result.user.name}`)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    await router.push(redirect)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-row :gutter="0" class="login-row">
      <el-col :span="12" class="login-brand">
        <div class="login-brand__inner">
          <BrandLogo size="lg" variant="light" />
          <h1 class="login-brand__title">
            不写页面，<br />
            也能交付一套业务系统
          </h1>
          <p class="login-brand__desc">
            基于元数据驱动的动态业务对象引擎：配置对象 → 配置字段 → 获取 Metadata →
            动态表单 / 动态列表 → Record CRUD。
          </p>
          <ul class="login-features">
            <li v-for="feature in FEATURES" :key="feature.title" class="login-feature">
              <span class="login-feature__icon">
                <el-icon :size="16"><component :is="feature.icon" /></el-icon>
              </span>
              <div>
                <div class="login-feature__title">{{ feature.title }}</div>
                <div class="login-feature__text">{{ feature.text }}</div>
              </div>
            </li>
          </ul>
          <div class="login-brand__footer">© 2026 Object Engine</div>
        </div>
      </el-col>

      <el-col :span="12" class="login-form-col">
        <el-card class="login-card" shadow="never">
          <h2 class="login-card__title">欢迎回来</h2>
          <p class="login-card__subtitle">请使用管理员分配的账号登录</p>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="0"
            @keyup.enter="handleSubmit"
          >
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名" size="large" autofocus />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                size="large"
                placeholder="密码"
                show-password
              />
            </el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-card__submit"
              :loading="submitting"
              @click="handleSubmit"
            >
              登 录
            </el-button>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  overflow: hidden;
  background-color: var(--oe-bg);
}

.login-row {
  height: 100%;
}

/* —— 左：品牌面 —— */
.login-brand {
  position: relative;
  display: flex;
  align-items: center;
  height: 100%;
  padding: var(--oe-space-7);
  overflow: hidden;
  color: var(--oe-on-brand);
  background:
    radial-gradient(900px 480px at 12% 8%, rgb(255 255 255 / 14%), transparent 60%),
    radial-gradient(700px 420px at 88% 96%, rgb(47 107 255 / 45%), transparent 62%),
    var(--oe-gradient-panel);
}

/* 斜向细纹，给纯色背景一点质感 */
.login-brand::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: repeating-linear-gradient(
    115deg,
    var(--oe-on-brand-stripe) 0 1px,
    transparent 1px 14px
  );
}

.login-brand__inner {
  position: relative;
  z-index: 1;
  max-width: 440px;
}

.login-brand__title {
  margin-top: var(--oe-space-7);
  font-size: 30px;
  line-height: 1.45;
  font-weight: 600;
  color: var(--oe-on-brand);
  letter-spacing: 0;
}

.login-brand__desc {
  margin: var(--oe-space-4) 0 0;
  font-size: var(--el-font-size-base);
  line-height: 1.8;
  color: var(--oe-on-brand-soft);
}

.login-features {
  margin: var(--oe-space-7) 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--oe-space-4);
}

.login-feature {
  display: flex;
  align-items: flex-start;
  gap: var(--oe-space-3);
}

.login-feature__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: var(--oe-radius-md);
  color: var(--oe-on-brand);
  background-color: var(--oe-on-brand-glass);
}

.login-feature__title {
  font-size: var(--el-font-size-base);
  font-weight: 600;
  color: var(--oe-on-brand);
}

.login-feature__text {
  margin-top: 2px;
  font-size: var(--el-font-size-small);
  color: var(--oe-on-brand-muted);
}

.login-brand__footer {
  margin-top: var(--oe-space-7);
  font-size: var(--el-font-size-extra-small);
  color: var(--oe-on-brand-faint);
}

/* —— 右：表单 —— */
.login-form-col {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: var(--oe-space-6);
}

.login-card {
  width: 100%;
  max-width: 380px;
  border: 1px solid var(--oe-border);
  box-shadow: var(--oe-shadow-2);
}

.login-card :deep(.el-card__body) {
  padding: var(--oe-space-7);
}

.login-card__title {
  font-size: 24px;
  font-weight: 600;
  color: var(--oe-text-1);
}

.login-card__subtitle {
  margin: var(--oe-space-2) 0 var(--oe-space-6);
  font-size: var(--el-font-size-base);
  color: var(--oe-text-3);
}

.login-card :deep(.el-form-item) {
  margin-bottom: var(--oe-space-5);
}

.login-card__submit {
  width: 100%;
  margin-top: var(--oe-space-2);
  letter-spacing: 0.4em;
  text-indent: 0.4em;
}

/* —— 窄屏：只留表单，品牌面隐藏 —— */
@media (width <= 992px) {
  .login-brand {
    display: none;
  }

  .login-form-col {
    width: 100%;
  }
}
</style>
