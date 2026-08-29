<script setup lang="ts">
import { computed } from 'vue'
import type { CustomField } from '@/types/field'
import type { LayoutConfig, LayoutSection as LayoutSectionConfig, LayoutType } from '@/types/layout'
import LayoutSection from './LayoutSection.vue'
const props = defineProps<{
  fields: CustomField[]
  layoutType: LayoutType
  saving?: boolean
}>()

const config = defineModel<LayoutConfig>({ required: true })

const emit = defineEmits<{ save: [] }>()

const enabledFields = computed(() => props.fields.filter((field) => field.status !== 0))

// 未加入任何 Section 的字段（字段池）
const unusedFields = computed(() =>
  enabledFields.value.filter(
    (field) => !config.value.sections.some((section) =>
      section.fields.some((layoutField) => layoutField.fieldApiName === field.apiName),
    ),
  ),
)

function updateSection(index: number, section: LayoutSectionConfig) {
  config.value = {
    sections: config.value.sections.map((item, i) => (i === index ? section : item)),
  }
}

function moveSection(index: number, offset: -1 | 1) {
  const target = index + offset
  if (target < 0 || target >= config.value.sections.length) return
  const sections = [...config.value.sections]
  const moved = sections.splice(index, 1)[0]
  if (moved === undefined) return
  sections.splice(target, 0, moved)
  config.value = { sections }
}

function removeSection(index: number) {
  config.value = { sections: config.value.sections.filter((_, i) => i !== index) }
}

function addSection() {
  config.value = {
    sections: [
      ...config.value.sections,
      {
        id: `section_${Date.now()}`,
        title: '新建分组',
        sort: config.value.sections.length + 1,
        columns: 12,
        fields: [],
      },
    ],
  }
}
</script>


<template>
  <div class="layout-editor">
    <div v-for="(section, index) in config.sections" :key="section.id" class="editor-section">
      <LayoutSection
        :section="section"
        :fields="enabledFields"
        :unused-fields="unusedFields"
        :is-first="index === 0"
        :is-last="index === config.sections.length - 1"
        @update="updateSection(index, $event)"
        @remove="removeSection(index)"
        @up="moveSection(index, -1)"
        @down="moveSection(index, 1)"
      />
    </div>

    <el-button class="add-section" @click="addSection">+ 添加 Section</el-button>

    <div class="editor-footer">
      <span v-if="unusedFields.length > 0" class="unused-hint">
        还有 {{ unusedFields.length }} 个未使用字段（未加入布局的字段不会显示）
      </span>
      <el-button type="primary" :loading="saving" @click="emit('save')">保存布局</el-button>
    </div>
  </div>
</template>

<style scoped>
.editor-section {
  margin-bottom: 12px;
}

.add-section {
  width: 100%;
  margin-bottom: 12px;
}

.editor-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

.unused-hint {
  font-size: 12px;
  color: #e6a23c;
}
</style>
