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

样式分四层，全部在 `src/styles/`，由 `main.ts` 引入 `index.css`（顺序固定：tokens → base → element → utilities）：

| 文件             | 放什么                                                                                             |
| ---------------- | -------------------------------------------------------------------------------------------------- |
| `tokens.css`     | 全站唯一的颜色 / 圆角 / 阴影 / 间距来源，先定义 `--oe-*` 语义变量，再映射给 Element Plus 的 `--el-*` |
| `base.css`       | reset、页面底色、滚动条                                                                              |
| `element.css`    | Element Plus 组件级微调，只写变量覆盖不了的部分（表格线取舍、卡片阴影层级、弹窗头尾分隔线）            |
| `utilities.css`  | 页面级原语：`.page` / `.search-bar` / `.form-hint` / `.page-pagination` / `.table-card` / `.w-*`      |

硬性约定：

- **业务样式里不允许出现裸 hex**（`#909399`、`#606266`、`#f5f7fa` 之类），一律引用 `--oe-*` 语义 token。唯一允许出现字面量色的地方是 `tokens.css` 本身。这样换品牌色和切深色模式都只改一个文件，页面里写死颜色会在深色下穿帮。
- **明暗主题只靠 `html.dark` 覆盖同一批 token**（见 `tokens.css`），不要为暗色单独写一套选择器。品牌面（首页横幅、登录页品牌面）刻意在两套主题下保持一致，相应 token 放在 `:root` 而不是 `html.dark`。
- 主题状态在 `stores/app.ts`（`theme` / `isDark` / `initTheme` / `toggleTheme`），`main.ts` 挂载前调用 `initTheme()` 避免闪白。
- `:deep(.el-xxx)` 覆写组件内部结构仅在确有必要时使用，并注明原因；页面级微调用 scoped style。当前仅侧边栏深色轨道用到（Element Plus 的 `.el-menu-item.is-active` 只设了 `color`，没有 `background`，不在 `--el-menu-*` 变量的可覆盖范围内）。

## 页面骨架约定

- 页面根容器用 `.page`，标题区用 `src/components/common/PageHeader.vue`（标题 + 描述 + `#actions` 插槽），不要各页手搓 `.page-toolbar`。
- 列表页统一形态：`PageHeader` → `el-card.table-card.is-flush`（`#header` 放 `SearchBar`、`#footer` 放 `el-pagination`）→ 表格。`is-flush` 让表格与卡片边缘齐平。
- 筛选条用 `SearchBar`（内部只组合 `el-*`），表单项下的说明文字用全局 `.form-hint` / `.form-error`。
- 表格不加 `border` 属性（全网格观感过重），行悬停与表头底色已在 `element.css` 统一。
- 表格里「主标题 + API 名称」这类两行信息，统一写成 `.cell-main` + `.text-api`。
- 筛选控件宽度用 `.w-92` / `.w-110` / `.w-130` / `.w-160` / `.w-200` / `.w-220` / `.w-240` / `.w-full`，不要写内联 `style="width: 200px"`。
