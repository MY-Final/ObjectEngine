<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createObject, updateObject } from '@/api/object'
import type { CustomObject } from '@/types/object'
import { API_NAME_HINT, API_NAME_PATTERN } from '@/constants/field'

const props = defineProps<{ object?: CustomObject | null }>()
const visible = defineModel<boolean>({ required: true })
const emit = defineEmits<{ saved: [] }>()

const isEdit = computed(() => !!props.object)

const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({
  objectName: '',
  apiName: '',
  description: '',
  remark: '',
  icon: '',
  sort: 0,
  status: 1,
})

const rules: FormRules = {
  objectName: [{ required: true, message: '请输入对象名称', trigger: 'blur' }],
  apiName: [
    {
      required: true,
      validator: (_rule, value: string, callback) => {
        if (!value) {
          callback(new Error('请输入 API 名称'))
        } else if (!API_NAME_PATTERN.test(value)) {
          callback(new Error(API_NAME_HINT))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

watch(visible, (open) => {
  if (!open) return
  const target = props.object
  if (target) {
    form.objectName = target.objectName
    form.apiName = target.apiName
    form.description = target.description ?? ''
    form.remark = target.remark ?? ''
    form.icon = target.icon ?? ''
    form.sort = target.sort
    form.status = target.status
  } else {
    form.objectName = ''
    form.apiName = ''
    form.description = ''
    form.remark = ''
    form.icon = ''
    form.sort = 0
    form.status = 1
  }
  formRef.value?.clearValidate()
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && props.object) {
      // apiName 创建后不可修改，编辑时不提交该字段
      await updateObject(props.object.apiName, {
        objectName: form.objectName,
        description: form.description || undefined,
        remark: form.remark || undefined,
        icon: form.icon || undefined,
        sort: form.sort,
        status: form.status,
      })
      ElMessage.success('保存成功')
    } else {
      await createObject({
        apiName: form.apiName,
        objectName: form.objectName,
        description: form.description || undefined,
        remark: form.remark || undefined,
        icon: form.icon || undefined,
        sort: form.sort,
      })
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
  <el-dialog v-model="visible" :title="isEdit ? '编辑对象' : '新建对象'" width="520px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="API名称" prop="apiName">
        <el-input
          v-if="!isEdit"
          v-model="form.apiName"
          placeholder="例如 project__c，创建后不可修改"
        />
        <el-input v-else :model-value="form.apiName" disabled />
      </el-form-item>
      <el-form-item label="对象名称" prop="objectName">
        <el-input v-model="form.objectName" placeholder="例如：项目" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="图标">
        <el-input v-model="form.icon" placeholder="例如：project" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item v-if="isEdit" label="状态">
        <el-switch
          v-model="form.status"
          :active-value="1"
          :inactive-value="0"
          active-text="启用"
          inactive-text="停用"
        />
      </el-form-item>
      <el-form-item v-else label="状态">
        <span class="form-hint">新建对象默认启用</span>
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
  font-size: 12px;
  color: #909399;
}
</style>
