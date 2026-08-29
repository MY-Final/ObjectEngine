# 前端 UI 组件规范（Element Plus）

前端 `object-engine-web/`（Vue 3 + TypeScript + Vite）统一使用 **Element Plus**（Element UI 的 Vue 3 版本，当前 2.14.x）。核心原则：**不重复造轮子**——能用组件库现成组件的一律直接用，不要手写 HTML/CSS/JS 重新实现一遍。

## 组件优先级

1. Element Plus 组件（`el-*`）优先：表单、表格、弹窗、消息提示、栅格布局、分页等一律用现成组件。
2. 图标统一用 `@element-plus/icons-vue`，不要引入第三方图标库或手写 SVG（特殊业务 logo 除外）。
3. 组件库确实覆盖不了的才自定义组件（见下方"动态渲染例外"），且自定义组件内部仍应组合 `el-*` 实现。

## 常见场景对照

| 场景        | 用什么                                                                |
| ----------- | --------------------------------------------------------------------- |
| 页面骨架    | el-container / el-header / el-aside / el-main                         |
| 栅格分列    | el-row + el-col（span），不要手写 grid/flex 布局体系                  |
| 表单        | el-form + el-form-item，校验走 rules，不要手写校验逻辑                |
| 列表        | el-table + el-table-column + el-pagination                            |
| 确认 / 提示 | ElMessageBox.confirm / ElMessage（显式 import）                       |
| 弹窗 / 抽屉 | el-dialog / el-drawer                                                 |
| 输入类控件  | el-input / el-select / el-date-picker / el-switch / el-input-number 等 |
| 加载态      | v-loading 指令                                                        |

## 引入方式

- `main.ts` 已 `app.use(ElementPlus)` 全量注册：模板里直接写 `el-*`，组件本身不需要逐个 import。
- 但以下内容必须显式 import：
  - 类型：`FormInstance`、`FormRules` 等从 `element-plus` 引入；
  - 函数式 API：`ElMessage`、`ElMessageBox`；
  - 图标：从 `@element-plus/icons-vue` 按需引入。

## 例外：动态渲染

自定义对象的字段在运行时按元数据渲染，组件无法在页面模板里写死，这类场景允许自定义组件，目前集中在：

- `src/components/dynamic/`：DynamicForm、DynamicTable、FieldRenderer、FieldValue；
- `src/components/layout/`：布局编辑器 / 预览组件。

约定：

- 动态组件内部仍然组合 Element Plus 实现。如 FieldRenderer 通过 `FIELD_COMPONENT_MAP`（`src/constants/field.ts`）把 fieldType 映射到 el-input / el-date-picker 等，再用 `<component :is>` 渲染；不要绕开 Element Plus 手写输入控件。
- 新增"按配置渲染"的需求时，优先扩展现有 dynamic 组件，而不是另起一套渲染体系。

## 样式

- 全局主题调整走 CSS 变量 / Element Plus 主题定制，不要为组件库已有的功能重写一套样式。
- `:deep(.el-xxx)` 覆写组件内部结构仅在确有必要时使用，并注明原因；页面级微调用 scoped style。
