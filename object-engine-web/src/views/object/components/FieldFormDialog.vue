<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { createField, updateField } from '@/api/field'
import { listEnabledOptions, listOptionSets } from '@/api/optionSet'
import { listObjects } from '@/api/object'
import type {
  CreateFieldPayload,
  CustomField,
  FieldConfig,
  FieldOption,
  FieldType,
  UpdateFieldPayload,
} from '@/types/field'
import type { CustomObject } from '@/types/object'
import type { OptionSet, OptionSetItem } from '@/types/optionSet'
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

/** 随机生成字段 API 名称：field_ + 8 位小写字母数字，符合 API_NAME_PATTERN */
function generateApiName(): string {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789'
  let suffix = ''
  for (let i = 0; i < 8; i++) {
    suffix += chars[Math.floor(Math.random() * chars.length)]
  }
  return `field_${suffix}`
}
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

// —— 选项来源（仅选择类字段）：LOCAL 自定义选项 / GLOBAL 通用选项集 ——
const optionSource = ref<'LOCAL' | 'GLOBAL'>('LOCAL')
const optionSetId = ref<number>()
const enabledSets = ref<OptionSet[]>([])
const globalOptions = ref<OptionSetItem[]>([])
const optionSetError = ref('')
const optionSetsLoaded = ref(false)

async function loadOptionSets() {
  if (optionSetsLoaded.value) return
  optionSetsLoaded.value = true
  try {
    const result = await listOptionSets({ page: 1, pageSize: 100, status: 1 })
    enabledSets.value = result.records
  } catch {
    optionSetsLoaded.value = false
  }
}

async function loadPreview(optionSetId?: number) {
  globalOptions.value = []
  if (!optionSetId) return
  try {
    globalOptions.value = await listEnabledOptions(optionSetId)
  } catch {
    globalOptions.value = []
  }
}

function handleOptionSourceChange(source: 'LOCAL' | 'GLOBAL') {
  if (source === 'GLOBAL') {
    void loadOptionSets()
    void loadPreview(optionSetId.value)
  } else {
    optionSetError.value = ''
  }
}

watch(optionSetId, (id) => {
  if (optionSource.value === 'GLOBAL') {
    void loadPreview(id)
  }
})

// —— 关联关系（REFERENCE）：选择业务对象 ——
const referenceTarget = ref<string>()
const enabledObjects = ref<CustomObject[]>([])
const referenceError = ref('')
const objectsLoaded = ref(false)

async function loadObjects() {
  if (objectsLoaded.value) return
  objectsLoaded.value = true
  try {
    const result = await listObjects({ page: 1, pageSize: 100 })
    enabledObjects.value = result.records.filter((object) => object.status === 1)
  } catch {
    objectsLoaded.value = false
  }
}

watch(
  () => form.fieldType,
  (fieldType) => {
    if (fieldType === 'REFERENCE') {
      void loadObjects()
    }
  },
)

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
    referenceTarget.value =
      typeof target.configJson?.targetObjectApiName === 'string'
        ? target.configJson.targetObjectApiName
        : undefined
    referenceError.value = ''
    if (form.fieldType === 'REFERENCE') {
      void loadObjects()
    }
    optionSource.value = target.optionSource === 'GLOBAL' ? 'GLOBAL' : 'LOCAL'
    optionSetId.value = target.optionSetId ?? undefined
    optionSetError.value = ''
    if (isOptionType.value) {
      void loadOptionSets()
      void loadPreview(optionSetId.value)
    }
  } else {
    // 新建默认预填随机生成的 API 名称，想规范命名的直接改写
    form.apiName = generateApiName()
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
    optionSource.value = 'LOCAL'
    optionSetId.value = undefined
    globalOptions.value = []
    optionSetError.value = ''
    referenceTarget.value = undefined
    referenceError.value = ''
  }
  formRef.value?.clearValidate()
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().then(() => true).catch(() => false)
  if (!valid) return

  // 选择类：LOCAL 校验自有选项，GLOBAL 引用通用选项集（选项由后端在元数据中注入）；
  // 文本与数字类把长度 / 位数配置写入 configJson，其余一律存 null
  let configJson: FieldConfig | null = null
  let payloadSource: 'LOCAL' | 'GLOBAL' | undefined
  let payloadSetId: number | null | undefined
  if (isOptionType.value) {
    payloadSource = optionSource.value
    payloadSetId = optionSource.value === 'GLOBAL' ? (optionSetId.value ?? null) : null
    if (optionSource.value === 'GLOBAL') {
      if (!optionSetId.value) {
        optionSetError.value = '引用通用选项集时必须选择选项集'
        return
      }
      optionSetError.value = ''
      configJson = null
    } else {
      const trimmed = options.value.map((option) => ({
        label: option.label.trim(),
        value: option.value.trim(),
      }))
      if (trimmed.length === 0 || trimmed.some((option) => !option.label || !option.value)) {
        optionsError.value = '选择类字段必须至少配置一个选项，且选项名称 / 选项值均不能为空'
        return
      }
      configJson = { options: trimmed }
    }
  } else if (form.fieldType === 'REFERENCE') {
    if (!referenceTarget.value) {
      referenceError.value = '请选择要关联的业务对象'
      return
    }
    referenceError.value = ''
    configJson = { targetObjectApiName: referenceTarget.value }
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
        optionSource: payloadSource,
        optionSetId: payloadSetId ?? null,
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
        optionSource: payloadSource,
        optionSetId: payloadSetId ?? null,
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
        <el-input v-if="!isEdit" v-model="form.apiName" placeholder="可自行填写或随机生成，创建后不可修改">
          <template #append>
            <el-button :icon="Refresh" title="随机生成" @click="form.apiName = generateApiName()" />
          </template>
        </el-input>
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
      <template v-if="form.fieldType === 'REFERENCE'">
        <el-form-item label="业务对象" required>
          <el-select
            v-model="referenceTarget"
            filterable
            :disabled="isEdit"
            placeholder="选择要关联的业务对象"
            style="width: 100%"
          >
            <el-option
              v-for="object in enabledObjects"
              :key="object.apiName"
              :label="`${object.objectName}（${object.apiName}）`"
              :value="object.apiName"
            />
          </el-select>
          <div v-if="referenceError" class="options-error">{{ referenceError }}</div>
          <div v-else-if="isEdit" class="form-hint">业务对象创建后不可修改</div>
          <div v-else class="form-hint">表单中将可搜索并关联该对象的数据记录，列表展示记录名称</div>
        </el-form-item>
      </template>
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
          v-else-if="form.fieldType === 'NUMBER' || form.fieldType === 'MONEY' || form.fieldType === 'PERCENT'"
          v-model="numberDefaultValue"
          :controls="false"
          style="width: 100%"
        />
        <el-date-picker
          v-else-if="form.fieldType === 'DATE'"
          v-model="form.defaultValue"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="默认日期（可选）"
          style="width: 100%"
        />
        <el-time-picker
          v-else-if="form.fieldType === 'TIME'"
          v-model="form.defaultValue"
          value-format="HH:mm:ss"
          placeholder="默认时间（可选）"
          style="width: 100%"
        />
        <span v-else-if="form.fieldType === 'REFERENCE'" class="form-hint">关联字段不支持默认值</span>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <template v-if="isOptionType">
        <el-form-item label="选项来源">
          <el-radio-group v-model="optionSource" @change="handleOptionSourceChange">
            <el-radio-button value="LOCAL">自定义选项</el-radio-button>
            <el-radio-button value="GLOBAL">通用选项集</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <template v-if="optionSource === 'GLOBAL'">
          <el-form-item label="选项集" required>
            <el-select
              v-model="optionSetId"
              filterable
              placeholder="选择通用选项集"
              style="width: 100%"
            >
              <el-option
                v-for="set in enabledSets"
                :key="set.id"
                :label="`${set.name}（${set.apiName}）`"
                :value="set.id"
              />
            </el-select>
            <div v-if="optionSetError" class="options-error">{{ optionSetError }}</div>
            <div v-else-if="globalOptions.length" class="form-hint">
              当前选项：{{ globalOptions.map((option) => option.label).join('、') }}
            </div>
            <div v-else class="form-hint">该选项集暂无启用中的选项</div>
          </el-form-item>
        </template>
      </template>
      <el-form-item v-if="isOptionType && optionSource === 'LOCAL'" label="选项配置">
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
