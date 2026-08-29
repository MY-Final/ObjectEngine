# Vue 前端项目初始化任务

## 一、项目目标

初始化一个用于 **Object Engine** 的前端项目。

后端是一个基于 Metadata 驱动的动态业务对象引擎，前端后续需要实现：

```text
Object Metadata
        ↓
Dynamic Form
        ↓
Dynamic Table
        ↓
Dynamic CRUD
```

当前任务只负责：

> **初始化前端项目基础工程，不实现具体业务功能。**

---

# 二、技术栈

必须使用：

* Vue 3
* TypeScript
* Vite
* Element Plus
* Vue Router
* Pinia
* Axios
* ESLint
* Prettier

包管理器：

```text
pnpm
```

不要使用：

* React
* Vue 2
* Element UI
* Ant Design Vue
* Tailwind CSS
* UnoCSS

---

# 三、项目初始化

项目名称：

```text
object-engine-web
```

建议：

```text
Vue 3
TypeScript
Vite
```

Node.js 使用当前 LTS 版本兼容的配置。

初始化后必须能够：

```bash
pnpm install
pnpm dev
pnpm build
```

全部正常执行。

---

# 四、目录结构

初始化完成后整理为：

```text
object-engine-web
│
├── public
│
├── src
│   │
│   ├── api
│   │   ├── http.ts
│   │   ├── object.ts
│   │   ├── field.ts
│   │   └── record.ts
│   │
│   ├── assets
│   │
│   ├── components
│   │   ├── dynamic
│   │   │   ├── DynamicForm.vue
│   │   │   ├── DynamicTable.vue
│   │   │   └── FieldRenderer.vue
│   │   │
│   │   └── common
│   │
│   ├── layouts
│   │   └── DefaultLayout.vue
│   │
│   ├── router
│   │   └── index.ts
│   │
│   ├── stores
│   │
│   ├── types
│   │   ├── object.ts
│   │   ├── field.ts
│   │   └── record.ts
│   │
│   ├── views
│   │   ├── dashboard
│   │   │   └── index.vue
│   │   │
│   │   ├── object
│   │   │   ├── index.vue
│   │   │   ├── detail.vue
│   │   │   └── fields.vue
│   │   │
│   │   └── custom
│   │       └── object.vue
│   │
│   ├── App.vue
│   ├── main.ts
│   └── style.css
│
├── .env
├── .env.development
├── .env.production
├── .gitignore
├── eslint.config.js
├── index.html
├── package.json
├── pnpm-lock.yaml
├── tsconfig.json
├── vite.config.ts
└── README.md
```

注意：

`components/dynamic` 下的组件当前可以只建立空组件或最基础的占位实现。

**不要现在开始实现 DynamicForm / DynamicTable。**

---

# 五、Axios 基础封装

创建：

```text
src/api/http.ts
```

统一封装 Axios。

后端 Base URL：

```text
/api/v1
```

开发环境通过 Vite proxy 转发。

例如：

```text
前端
http://localhost:5173

        ↓

/api/v1

        ↓

后端
http://localhost:8080
```

Axios 实例要求：

```text
baseURL = /api/v1
timeout = 合理默认值
```

统一处理：

```text
200
400
404
500
```

错误统一通过 Element Plus：

```text
ElMessage.error(...)
```

提示用户。

---

# 六、API 模块

提前建立 API 文件：

```text
src/api/object.ts
src/api/field.ts
src/api/record.ts
```

暂时只定义 API 方法，不需要完整实现页面。

### Object API

对应：

```text
POST   /custom-objects
GET    /custom-objects
GET    /custom-objects/{apiName}
PUT    /custom-objects/{apiName}
DELETE /custom-objects/{apiName}
```

### Field API

对应：

```text
POST   /custom-objects/{apiName}/fields
GET    /custom-objects/{apiName}/fields
PUT    /custom-objects/{apiName}/fields/{fieldApiName}
DELETE /custom-objects/{apiName}/fields/{fieldApiName}
```

### Metadata API

可以放在：

```text
src/api/object.ts
```

接口：

```text
GET /objects/{apiName}/metadata
```

### Record API

对应：

```text
POST   /objects/{apiName}/records
GET    /objects/{apiName}/records
GET    /objects/{apiName}/records/{id}
DELETE /objects/{apiName}/records/{id}
```

---

# 七、TypeScript 类型

建立：

```text
src/types/object.ts
src/types/field.ts
src/types/record.ts
```

---

## Object 类型

```ts
export interface CustomObject {
  id: number
  apiName: string
  objectName: string
  description?: string
  remark?: string
  icon?: string
  status: number
  sort: number
  createdAt: string
  updatedAt: string
}
```

---

## Field 类型

```ts
export type FieldType =
  | 'TEXT'
  | 'TEXTAREA'
  | 'NUMBER'
  | 'MONEY'
  | 'DATE'
  | 'SELECT'
```

```ts
export interface FieldOption {
  label: string
  value: string
}
```

```ts
export interface FieldConfig {
  options?: FieldOption[]
  [key: string]: unknown
}
```

```ts
export interface CustomField {
  id: number
  objectId: number
  apiName: string
  fieldName: string
  fieldType: FieldType
  description?: string
  remark?: string
  requiredFlag: number
  uniqueFlag: number
  searchableFlag: number
  sortableFlag: number
  sort: number
  defaultValue?: string | null
  configJson?: FieldConfig | null
  status: number
  createdAt: string
  updatedAt: string
}
```

---

## Record 类型

Record 的动态字段不能写死。

使用：

```ts
export interface CustomRecord {
  id: number
  objectId: number
  data: Record<string, unknown>
  remark?: string
  createdAt: string
  updatedAt: string
}
```

---

# 八、Metadata 类型

创建：

```text
src/types/object.ts
```

增加：

```ts
export interface ObjectMetadata {
  object: CustomObject
  fields: CustomField[]
}
```

这是整个前端后续动态渲染的核心类型。

---

# 九、Vue Router

建立基础路由：

```text
/
 /dashboard

/objects
/objects/:apiName
/objects/:apiName/fields

/custom/:apiName
```

对应：

```text
/dashboard
    ↓
系统首页

/objects
    ↓
对象管理

/objects/:apiName
    ↓
对象详情

/objects/:apiName/fields
    ↓
字段配置

/custom/:apiName
    ↓
动态对象页面
```

---

# 十、基础 Layout

建立：

```text
src/layouts/DefaultLayout.vue
```

使用 Element Plus：

```text
ElContainer
ElAside
ElHeader
ElMain
```

左侧菜单：

```text
首页

对象管理
```

对象管理下面暂时可以只有：

```text
对象列表
```

整体先保持简单。

---

# 十一、对象管理页面

创建：

```text
src/views/object/index.vue
```

当前只实现基础页面骨架：

```text
对象管理

[ 新建对象 ]

---------------------------------------

对象名称 | API名称 | 状态 | 创建时间 | 操作

项目       project__c    启用       ...
客户       customer__c  启用       ...

---------------------------------------
```

操作：

```text
查看
字段
删除
```

页面数据通过 API 获取。

---

# 十二、字段管理页面

创建：

```text
src/views/object/fields.vue
```

页面用于后续配置：

```text
对象：项目
API：project__c

[ 新建字段 ]

--------------------------------------------

字段名称 | API名称 | 类型 | 必填 | 排序 | 操作
项目名称 | name     | 文本 | 是   | 1   | 编辑 删除
项目金额 | amount   | 金额 | 否   | 2   | 编辑 删除
项目状态 | status   | 下拉 | 是   | 3   | 编辑 删除
```

当前可以先完成页面骨架。

字段编辑使用：

```text
ElDialog
ElForm
ElFormItem
```

---

# 十三、Dynamic 组件

提前建立：

```text
src/components/dynamic/DynamicForm.vue
src/components/dynamic/DynamicTable.vue
src/components/dynamic/FieldRenderer.vue
```

当前只需要：

```vue
<template>
  <div>
    Dynamic Form
  </div>
</template>
```

或者简单占位。

**不要现在实现完整动态表单。**

下一阶段才实现：

```text
Metadata
   ↓
FieldRenderer
   ↓
Element Plus Component
```

---

# 十四、样式要求

整体使用：

```text
Element Plus
```

不要引入其它 UI 框架。

页面风格：

* 简洁
* 企业级后台
* 留白合理
* 信息层级清晰
* 不要过度设计
* 不要渐变背景
* 不要 AI 风格视觉
* 不要大量圆角卡片
* 不要炫酷动画

重点是：

> 这是一个业务对象管理平台，不是营销网站。

---

# 十五、环境变量

创建：

```text
.env
.env.development
.env.production
```

例如：

```env
VITE_API_BASE_URL=/api/v1
```

开发环境 Vite Proxy：

```text
/api
    ↓
http://localhost:8080
```

不要把后端地址硬编码到业务 API 文件中。

---

# 十六、Vite Proxy

开发环境配置：

```text
/api
```

代理到：

```text
http://localhost:8080
```

最终：

```text
浏览器
    ↓
http://localhost:5173/api/v1/objects/project__c/metadata
    ↓
Vite Proxy
    ↓
http://localhost:8080/api/v1/objects/project__c/metadata
```

---

# 十七、代码规范

使用：

```text
TypeScript strict
```

禁止：

```ts
any
```

除非确实无法避免。

动态 Record 数据使用：

```ts
Record<string, unknown>
```

不要：

```ts
Record<string, any>
```

组件使用：

```vue
<script setup lang="ts">
```

---

# 十八、不要过度封装

当前阶段不要建立：

```text
复杂组件工厂
复杂状态管理
复杂权限系统
复杂表单引擎
复杂插件系统
复杂低代码引擎
```

只搭好基础工程。

未来真正核心的是：

```text
FieldRenderer
        ↓
FieldType → Element Plus
```

以及：

```text
ObjectMetadata
        ↓
DynamicForm
        ↓
DynamicTable
```

---

# 十九、初始化验收标准

完成后必须满足：

### 1. 项目可以启动

```bash
pnpm dev
```

### 2. 项目可以构建

```bash
pnpm build
```

### 3. 可以访问

```text
/dashboard
```

### 4. 可以访问

```text
/objects
```

### 5. 可以访问

```text
/custom/project__c
```

即使此时动态页面还是占位页面也可以。

### 6. Axios 已经可以访问

```text
/api/v1
```

### 7. TypeScript 类型已经建立

```text
Object
Field
Record
Metadata
```

### 8. API 层已经建立

```text
object.ts
field.ts
record.ts
```

### 9. Dynamic 组件目录已经建立

```text
components/dynamic
```

---

# 二十、非常重要

当前任务的目标是：

> **初始化一个干净、可持续开发的 Vue 3 + Element Plus 前端工程。**

不要在初始化阶段自行扩展需求。

不要实现：

```text
权限
登录
用户
角色
租户
数据权限
Workflow
审批
关联对象
Layout
高级筛选
导入导出
```

也不要自行修改后端 API 契约。

后续开发严格基于：

```text
/api/v1
```

的 Object Engine API。

初始化完成后，输出：

```text
1. 项目目录结构
2. package.json 核心依赖
3. 启动方式
4. 已完成内容
5. 尚未实现内容
6. pnpm build 结果
```

不要生成与当前需求无关的代码。
