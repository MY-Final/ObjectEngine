<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createField, updateField } from '@/api/field'
import type {
  CreateFieldPayload,
  CustomField,
  FieldConfig,
  FieldOption,
  FieldType,
  UpdateFieldPayload,
} from '@/types/field'
import { API_NAME_HINT, API_NAME_PATTERN, FIELD_TYPE_OPTIONS } from '@/constants/field'
import SelectOptionEditor from './SelectOptionEditor.vue'

const props = defineProps<{
  objectApiName: string
  field?: CustomField | null
  /** 新建时的默认排序值，一般为当前字段数 + 1 */
  defaultSort?: number
}>()
const visible = defineModel<boolean>({ required: true })
const emit = defineEmits<{ saved: [] }>()

const isEdit = computed(() => !!props.field)
const isOptionType = computed(() => form.fieldType === 'SELECT' || form.fieldType === 'MULTI_SELECT')
const isTextLike = computed(() => form.fieldType === 'TEXT' || form.fieldType === 'TEXTAREA')
const isNumeric = computed(() => ['NUMBER', 'MONEY', 'PERCENT'].includes(form.fieldType))

const formRef = ref<FormInstance>()
const submitting = ref(false)
const optionsError = ref('')

const form = reactive({
  apiName: '',
  fieldName: '',
  fieldType: 'TEXT' as FieldType,
  description: '',
  remark: '',
  requiredFlag: false,
  uniqueFlag: false,
  searchableFlag: false,
  sortableFlag: false,
  sort: 1,
  defaultValue: null as string | null,
  status: 1,
})
const options = ref<FieldOption[]>([])

/** 类型相关配置：文本长度 / 数字位数，提交时按类型写入 configJson */
const typeConfig = reactive<{
  maxLength?: number
  minLength?: number
  integerLength?: number
  scale?: number
}>({})

function loadTypeConfig(config: FieldConfig | null | undefined) {
  typeConfig.maxLength = typeof config?.maxLength === 'number' ? config.maxLength : undefined
  typeConfig.minLength = typeof config?.minLength === 'number' ? config.minLength : undefined
  typeConfig.integerLength = typeof config?.integerLength === 'number' ? config.integerLength : undefined
  typeConfig.scale = typeof config?.scale === 'number' ? config.scale : undefined
}

// NUMBER / MONEY 的默认值用数字输入框编辑，后端 defaultValue 为字符串，这里做桥接
const numberDefaultValue = computed<number | undefined>({
  get: () =>
    form.defaultValue == null || form.defaultValue === '' ? undefined : Number(form.defaultValue),
  set: (value) => {
    form.defaultValue = value == null ? null : String(value)
  },
})

const rules: FormRules = {
  apiName: [
    {
      required: true,
      validator: (_rule, value: string, callback) => {
        if (!value) {
          callback(new Error('请输入字段 API 名称'))
        } else if (!API_NAME_PATTERN.test(value)) {
          callback(new Error(API_NAME_HINT))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  fieldName: [{ required: true, message: '请输入字段名称', trigger: 'blur' }],
  fieldType: [{ required: true, message: '请选择字段类型', trigger: 'change' }],
}

watch(visible, (open) => {
  if (!open) return
  optionsError.value = ''
  const target = props.field
  if (target) {
    form.apiName = target.apiName
    form.fieldName = target.fieldName
    form.fieldType = target.fieldType
    form.description = target.description ?? ''
    form.remark = target.remark ?? ''
    form.requiredFlag = target.requiredFlag === 1
    form.uniqueFlag = target.uniqueFlag === 1
    form.searchableFlag = target.searchableFlag === 1
    form.sortableFlag = target.sortableFlag === 1
    form.sort = target.sort
    form.defaultValue = target.defaultValue ?? null
    form.status = target.status
    options.value = (target.configJson?.options ?? []).map((option) => ({ ...option }))
    loadTypeConfig(target.configJson)
  } else {
    form.apiName = ''
    form.fieldName = ''
    form.fieldType = 'TEXT'
    form.description = ''
    form.remark = ''
    form.requiredFlag = false
    form.uniqueFlag = false
    form.searchableFlag = false
    form.sortableFlag = false
    form.sort = props.defaultSort ?? 1
    form.defaultValue = null
    form.status = 1
    options.value = []
    loadTypeConfig(null)
  }
  formRef.value?.clearValidate()
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  // 选择类必须配置 options；文本与数字类把长度 / 位数配置写入 configJson，其余一律存 null
  let configJson: FieldConfig | null = null
  if (isOptionType.value) {
    const trimmed = options.value.map((option) => ({
      label: option.label.trim(),
      value: option.value.trim(),
    }))
    if (trimmed.length === 0 || trimmed.some((option) => !option.label || !option.value)) {
      optionsError.value = '选择类字段必须至少配置一个选项，且选项名称 / 选项值均不能为空'
      return
    }
    configJson = { options: trimmed }
  } else if (isTextLike.value || isNumeric.value) {
    configJson = {}
    if (typeConfig.maxLength != null) configJson.maxLength = typeConfig.maxLength
    if (typeConfig.minLength != null) configJson.minLength = typeConfig.minLength
    if (typeConfig.integerLength != null) configJson.integerLength = typeConfig.integerLength
    if (typeConfig.scale != null) configJson.scale = typeConfig.scale
    if (Object.keys(configJson).length === 0) {
      configJson = null
    }
  }
  optionsError.value = ''

  const defaultValue =
    form.defaultValue != null && form.defaultValue !== '' ? form.defaultValue : null

  submitting.value = true
  try {
    if (isEdit.value && props.field) {
      // apiName / fieldType 创建后不可修改，编辑时不提交
      const payload: UpdateFieldPayload = {
        fieldName: form.fieldName,
        description: form.description || undefined,
        remark: form.remark || undefined,
        requiredFlag: form.requiredFlag ? 1 : 0,
        uniqueFlag: form.uniqueFlag ? 1 : 0,
        searchableFlag: form.searchableFlag ? 1 : 0,
        sortableFlag: form.sortableFlag ? 1 : 0,
        sort: form.sort,
        defaultValue,
        configJson,
        status: form.status,
      }
      await updateField(props.objectApiName, props.field.apiName, payload)
      ElMessage.success('保存成功')
    } else {
      const payload: CreateFieldPayload = {
        apiName: form.apiName,
        fieldName: form.fieldName,
        fieldType: form.fieldType,
        description: form.description || undefined,
        remark: form.remark || undefined,
        requiredFlag: form.requiredFlag ? 1 : 0,
        uniqueFlag: form.uniqueFlag ? 1 : 0,
        searchableFlag: form.searchableFlag ? 1 : 0,
        sortableFlag: form.sortableFlag ? 1 : 0,
        sort: form.sort,
        defaultValue,
        configJson,
      }
      await createField(props.objectApiName, payload)
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
  <el-dialog v-model="visible" :title="isEdit ? '编辑字段' : '新建字段'" width="560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="API名称" prop="apiName">
        <el-input
          v-if="!isEdit"
          v-model="form.apiName"
          placeholder="例如 name，创建后不可修改"
        />
        <el-input v-else :model-value="form.apiName" disabled />
      </el-form-item>
      <el-form-item label="字段名称" prop="fieldName">
        <el-input v-model="form.fieldName" placeholder="例如：项目名称" />
      </el-form-item>
      <el-form-item label="字段类型" prop="fieldType">
        <el-select v-model="form.fieldType" :disabled="isEdit" style="width: 100%">
          <el-option
            v-for="option in FIELD_TYPE_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <div v-if="isEdit" class="form-hint">字段类型创建后不可修改</div>
      </el-form-item>
      <template v-if="isTextLike">
        <el-form-item label="最大长度">
          <el-input-number
            v-model="typeConfig.maxLength"
            :min="1"
            :max="5000"
            controls-position="right"
            placeholder="不填则不限制"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最小长度">
          <el-input-number
            v-model="typeConfig.minLength"
            :min="0"
            controls-position="right"
            placeholder="不填则不限制"
            style="width: 100%"
          />
        </el-form-item>
      </template>
      <template v-else-if="isNumeric">
        <el-form-item label="整数长度">
          <el-input-number
            v-model="typeConfig.integerLength"
            :min="1"
            :max="15"
            controls-position="right"
            placeholder="不填则不限制"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="小数位数">
          <el-input-number
            v-model="typeConfig.scale"
            :min="0"
            :max="6"
            controls-position="right"
            :placeholder="form.fieldType === 'MONEY' ? '默认 2 位' : '不填则不限制'"
            style="width: 100%"
          />
        </el-form-item>
      </template>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="是否必填">
        <el-switch v-model="form.requiredFlag" />
      </el-form-item>
      <el-form-item label="是否唯一">
        <el-switch v-model="form.uniqueFlag" />
      </el-form-item>
      <el-form-item label="可搜索">
        <el-switch v-model="form.searchableFlag" />
      </el-form-item>
      <el-form-item label="可排序">
        <el-switch v-model="form.sortableFlag" />
      </el-form-item>
      <el-form-item label="默认值">
        <el-input
          v-if="form.fieldType === 'TEXT' || form.fieldType === 'TEXTAREA' || isOptionType"
          v-model="form.defaultValue"
          placeholder="默认值（可选）"
        />
        <el-input-number
          v-else-if="form.fieldType === 'NUMBER' || form.fieldType === 'MONEY'"
          v-model="numberDefaultValue"
          :controls="false"
          style="width: 100%"
        />
        <el-date-picker
          v-else
          v-model="form.defaultValue"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="默认日期（可选）"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item v-if="isOptionType" label="选项配置">
        <div class="options-editor">
          <SelectOptionEditor v-model="options" />
          <div v-if="optionsError" class="options-error">{{ optionsError }}</div>
        </div>
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
        <span class="form-hint">新建字段默认启用</span>
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

.options-editor {
  width: 100%;
}

.options-error {
  margin-top: 4px;
  font-size: 12px;
  color: #f56c6c;
}
</style>
