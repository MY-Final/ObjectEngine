<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createMenu, updateMenu } from '@/api/menu'
import { listObjects } from '@/api/object'
import type { CustomObject } from '@/types/object'
import type { MenuItem, MenuParentOption, MenuPayload, MenuType } from '@/types/menu'
import { resolveIcon } from '@/utils/icon'

const props = defineProps<{
  /** 上级菜单候选：顶级 + 目录节点 */
  parentOptions: MenuParentOption[]
  /** 编辑目标；null 表示新建 */
  menu?: MenuItem | null
  /** 新建时的默认排序值，一般为现有最大 sort + 1 */
  defaultSort?: number
}>()
const visible = defineModel<boolean>({ required: true })
const emit = defineEmits<{ saved: [] }>()

const isEdit = computed(() => !!props.menu)

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  parentId: 0,
  menuName: '',
  menuType: 'LINK' as MenuType,
  objectApiName: '',
  routePath: '',
  component: '',
  icon: '',
  sort: 0,
  status: 1,
  visible: 1,
  target: '_self',
  remark: '',
})

const isObject = computed(() => form.menuType === 'OBJECT')
const isLink = computed(() => form.menuType === 'LINK')

/** OBJECT 菜单的关联对象选项（懒加载一次；接口失败时仍可手动输入 API 名称） */
const objectOptions = ref<CustomObject[]>([])
const objectOptionsLoaded = ref(false)

async function loadObjectOptions() {
  if (objectOptionsLoaded.value) return
  objectOptionsLoaded.value = true
  try {
    const result = await listObjects({ page: 1, pageSize: 100 })
    objectOptions.value = result.records
  } catch {
    objectOptionsLoaded.value = false
  }
}

/** 编辑时上级菜单不能选自己，后代随之排除，避免成环 */
const selectableParentOptions = computed(() => stripNode(props.parentOptions, props.menu?.id))

function stripNode(options: MenuParentOption[], excludeId?: number): MenuParentOption[] {
  if (!excludeId) return options
  const result: MenuParentOption[] = []
  for (const option of options) {
    if (option.value === excludeId) continue
    result.push({ ...option, children: stripNode(option.children, excludeId) })
  }
  return result
}

/** 校验规则按当前菜单类型动态生成，隐藏字段不校验 */
const rules = computed<FormRules>(() => ({
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  objectApiName: isObject.value
    ? [{ required: true, message: '请选择关联对象', trigger: 'change' }]
    : [],
  routePath: isLink.value
    ? [{ required: true, message: '请输入前端路由地址', trigger: 'blur' }]
    : [],
}))

watch(visible, (open) => {
  if (!open) return
  const target = props.menu
  if (target) {
    form.parentId = target.parentId ?? 0
    form.menuName = target.menuName
    form.menuType = target.menuType
    form.objectApiName = target.objectApiName ?? ''
    form.routePath = target.routePath ?? ''
    form.component = target.component ?? ''
    form.icon = target.icon ?? ''
    form.sort = target.sort
    form.status = target.status
    form.visible = target.visible
    form.target = target.target ?? '_self'
    form.remark = target.remark ?? ''
  } else {
    form.parentId = 0
    form.menuName = ''
    form.menuType = 'LINK'
    form.objectApiName = ''
    form.routePath = ''
    form.component = ''
    form.icon = ''
    form.sort = props.defaultSort ?? 0
    form.status = 1
    form.visible = 1
    form.target = '_self'
    form.remark = ''
  }
  formRef.value?.clearValidate()
  void loadObjectOptions()
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  // 全量更新：非对象菜单不传 objectApiName；OBJECT 的 routePath 交由后端按 /custom/{apiName} 生成
  const payload: MenuPayload = {
    parentId: form.parentId,
    menuName: form.menuName.trim(),
    menuType: form.menuType,
    objectApiName: isObject.value ? form.objectApiName : null,
    routePath: isLink.value ? form.routePath.trim() : null,
    component: form.component.trim() || null,
    icon: form.icon.trim() || null,
    sort: form.sort,
    status: form.status,
    visible: form.visible,
    target: isLink.value ? form.target : null,
    remark: form.remark.trim() || null,
  }

  submitting.value = true
  try {
    if (isEdit.value && props.menu) {
      await updateMenu(props.menu.id, payload)
      ElMessage.success('保存成功')
    } else {
      await createMenu(payload)
      ElMessage.success('创建成功')
    }
    visible.value = false
    emit('saved')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑菜单' : '新建菜单'" width="560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="上级菜单">
        <el-tree-select
          v-model="form.parentId"
          :data="selectableParentOptions"
          :render-after-expand="false"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="菜单名称" prop="menuName">
        <el-input v-model="form.menuName" placeholder="例如：对象管理" />
      </el-form-item>
      <el-form-item label="菜单类型" prop="menuType">
        <el-radio-group v-model="form.menuType" :disabled="isEdit">
          <el-radio-button value="DIRECTORY">目录</el-radio-button>
          <el-radio-button value="OBJECT">对象</el-radio-button>
          <el-radio-button value="LINK">链接</el-radio-button>
        </el-radio-group>
        <div v-if="isEdit" class="form-hint">菜单类型创建后不可修改</div>
      </el-form-item>
      <el-form-item v-if="isObject" label="关联对象" prop="objectApiName">
        <el-select
          v-model="form.objectApiName"
          filterable
          allow-create
          default-first-option
          placeholder="选择或输入对象 API 名称"
          style="width: 100%"
        >
          <el-option
            v-for="object in objectOptions"
            :key="object.apiName"
            :label="`${object.objectName}（${object.apiName}）`"
            :value="object.apiName"
          />
        </el-select>
        <div class="form-hint">菜单点击后跳转到 /custom/{对象API名称} 动态页面</div>
      </el-form-item>
      <el-form-item v-if="isLink" label="路由地址" prop="routePath">
        <el-input v-model="form.routePath" placeholder="/admin/menus 或 https://..." />
        <div class="form-hint">
          后台菜单以 /admin 开头（仅后台侧边栏显示）；前台页面用 /dashboard、/custom/*；外链填完整
          http(s) 地址
        </div>
      </el-form-item>
      <el-form-item v-if="isLink" label="打开方式">
        <el-select v-model="form.target" style="width: 100%">
          <el-option label="当前窗口" value="_self" />
          <el-option label="新窗口" value="_blank" />
        </el-select>
        <div class="form-hint">仅外链生效</div>
      </el-form-item>
      <el-form-item label="图标">
        <el-input v-model="form.icon" placeholder="Element Plus 图标名，如 Setting">
          <template #append>
            <el-icon v-if="resolveIcon(form.icon)">
              <component :is="resolveIcon(form.icon)" />
            </el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
        <div class="form-hint">越小越靠前</div>
      </el-form-item>
      <el-form-item label="组件路径">
        <el-input v-model="form.component" placeholder="预留字段，当前导航未使用" />
      </el-form-item>
      <el-form-item v-if="isEdit" label="状态">
        <span v-if="form.menuType === 'OBJECT'" class="form-hint">状态由关联对象控制，请在对象管理中启用/停用</span>
        <el-switch
          v-else
          v-model="form.status"
          :active-value="1"
          :inactive-value="0"
          active-text="启用"
          inactive-text="停用"
        />
      </el-form-item>
      <el-form-item v-if="isEdit" label="是否显示">
        <el-switch
          v-model="form.visible"
          :active-value="1"
          :inactive-value="0"
          active-text="显示"
          inactive-text="隐藏"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.form-hint {
  width: 100%;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
</style>
