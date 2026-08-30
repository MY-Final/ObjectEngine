<div align="center">

# Object Engine

**基于元数据驱动的动态业务对象引擎**

配置对象 → 配置字段 → 获取 Metadata → 动态表单 / 动态列表 → Record CRUD

![Java](https://img.shields.io/badge/Java-17-44739e?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs&logoColor=white)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.14-409EFF)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5-CC3E2D)

</div>

---

## 📖 项目简介

Object Engine 是一个低代码业务对象引擎：管理员在后台**配置对象与字段（元数据）**，系统即可自动生成动态表单、动态列表与完整的 Record CRUD 接口——**不为任何具体业务对象单独写页面或接口**，所有页面与数据结构均由元数据在运行时驱动。

| 分层 | 说明 |
| --- | --- |
| 后端 | Spring Boot 3.5 · Java 17 · MyBatis-Plus 3.5.8 · MySQL 8，包路径 `com.myfinal.objectengine` |
| 前端 | Vue 3.5 · TypeScript · Vite · Pinia · Element Plus（`object-engine-web/`） |
| 核心思路 | 字段值统一存 `custom_record.data_json`，**加字段类型零 DDL**；运行时组件只认 Metadata，不认业务对象 |

## ✨ 功能特性

- 🧱 **自定义对象**：对象 / 字段元数据配置，API 名称创建后不可修改（支持随机生成）
- 🧩 **17 种字段类型**：文本、数字、日期时间、选择、自动编号、关联、引用等（详见下方字段类型）
- 🪄 **动态表单 / 动态列表**：由 Metadata 驱动渲染，支持布局配置（分 Section / 分列）
- 🔗 **关联与引用**：LOOKUP 关联其他对象记录，REFERENCE 跨对象引用字段值（运行时计算，不落库）
- 🗂️ **通用选项集**：选项集可被多个对象的单选 / 多选字段复用，一处修改全局生效
- 🧭 **导航菜单**：菜单树可配置，前台 / 后台双区路由，对象创建后自动注册菜单
- 🔍 **数据能力**：分页、关键字搜索（按可搜索字段后端匹配）、字段唯一性校验
- 📡 **开放 API**：对象、字段、记录的完整 REST 接口，可直接供第三方系统对接

## 🧩 字段类型

> 全部字段类型均可配置：是否必填、是否唯一、可搜索、可排序、排序值、默认值。

### 基础字段

| 类型 | 说明 | 录入控件 | 校验与展示 |
| --- | --- | --- | --- |
| **文本** `TEXT` | 单行文本 | 输入框（带字数统计） | 可配置最大 / 最小长度 |
| **多行文本** `TEXTAREA` | 长文本 | 多行输入框（带字数统计） | 可配置最大 / 最小长度 |
| **数字** `NUMBER` | 数值 | 数字输入框 | 可配置整数长度（1~15 位）与小数位数（0~6 位），千分位展示 |
| **金额** `MONEY` | 金额数值 | 数字输入框 | 默认保留 2 位小数（可配位数），千分位展示 |
| **百分比** `PERCENT` | 百分数 | 数字输入框 | 展示自动追加 `%`，可配置小数位数 |
| **日期** `DATE` | 日期 | 日期选择器 | `yyyy-MM-dd` 格式校验 |
| **日期时间** `DATETIME` | 日期 + 时分秒 | 日期时间选择器 | `yyyy-MM-dd HH:mm:ss` 格式校验 |
| **自动编号** `AUTO_NUMBER` | 单据编号（如 `PO20260830-0001`） | 只读，保存后由系统生成 | 格式串自定义：`{0}` 序号补位、`{YYYY}/{YY}/{MM}/{DD}` 日期变量；起始编号可配，序号原子递增不重号 |
| **时间** `TIME` | 时间 | 时间选择器 | `HH:mm:ss` 格式校验 |
| **布尔** `BOOLEAN` | 开关 | Switch 开关 | 展示为「是 / 否」 |
| **电话** `PHONE` | 联系电话 | 输入框 | 手机号（11 位）或座机号格式校验 |
| **邮箱** `EMAIL` | 电子邮箱 | 输入框 | 邮箱格式校验 |
| **网址** `URL` | 链接地址 | 输入框 | 必须以 `http(s)://` 开头，列表渲染为可点击链接 |

### 选择类字段（支持通用选项集）

| 类型 | 说明 | 录入控件 | 校验与展示 |
| --- | --- | --- | --- |
| **下拉选择** `SELECT` | 单选 | 下拉框（可搜索、可清空） | 选项来源二选一：**自定义选项** 或 **引用通用选项集**；存 `value` 展示 `label` |
| **多选** `MULTI_SELECT` | 多选 | 多选下拉框 | 选项同上；值存数组，列表以「、」连接展示 |

> 💡 **通用选项集**：管理员统一维护一组选项（如「业务状态」），多个对象的字段可复用同一选项集，修改一处、全局生效；被字段引用的选项集不可删除。

### 关联字段

| 类型 | 说明 | 录入控件 | 校验与展示 |
| --- | --- | --- | --- |
| **关联关系** `LOOKUP` | 将当前记录关联到**另一个对象的一条记录** | 远程搜索下拉框（按关键字查目标对象记录） | 存储目标记录 **ID**；列表展示记录名称（`name` 字段 → 第一个文本字段 → `#ID`） |
| **引用字段** `REFERENCE` | 通过已有关联关系，**引用目标对象的某个字段值** | 只读（自动计算，不可编辑） | **运行时实时计算，不占用存储**；修改关联记录后自动刷新 |

> 💡 **联动规则**：引用字段必须建立在关联关系之上（当前对象 → LOOKUP 字段 → 目标对象 → 目标字段），且仅支持一跳；被引用的关联关系、目标字段、目标对象均受删除保护。

## 🚀 快速开始

### 环境要求

| 依赖 | 版本 |
| --- | --- |
| JDK | 17+ |
| Node.js | 22+ |
| MySQL | 8.0+ |

### 1️⃣ 初始化数据库

按顺序执行 `docs/sql/` 下的脚本（连接信息见 `src/main/resources/application.yaml`）：

```bash
docs/sql/
├── init.sql                  # 核心表：对象 / 字段 / 记录 等
├── custom_object_layout.sql  # 对象布局表
├── option_set.sql            # 通用选项集 + 菜单注册
└── lookup_reference.sql      # 关联 / 引用字段支持
```

### 2️⃣ 启动后端（端口 8080）

```bash
./mvnw spring-boot:run
```

### 3️⃣ 启动前端（端口 5173，`/api` 自动代理到 8080）

```bash
cd object-engine-web
npm install
npm run dev
```

访问 <http://localhost:5173> 🎉

## 🔌 API 一览

统一响应结构 `{ code, message, data }`，`code=200` 为成功。

| 资源 | 接口 | 说明 |
| --- | --- | --- |
| **对象** | `GET/POST /api/v1/custom-objects` | 对象分页（keyword）/ 新建 |
| | `GET/PUT/DELETE /api/v1/custom-objects/{apiName}` | 详情 / 修改 / 删除 |
| **字段** | `GET/POST /api/v1/custom-objects/{apiName}/fields` | 字段列表 / 新建 |
| | `PUT/DELETE .../fields/{fieldApiName}` | 修改 / 删除（含引用保护） |
| **记录** | `GET /api/v1/objects/{apiName}/records` | 分页 + `keyword` 关键字搜索 |
| | `GET/PUT/DELETE /api/v1/objects/{apiName}/records/{id}` | 详情 / 修改 / 删除 |
| | `POST /api/v1/objects/{apiName}/records` | 新建（元数据校验 + 唯一性查重） |
| **元数据** | `GET /api/v1/objects/{apiName}/metadata` | 对象 + 字段 + 布局，动态渲染唯一依赖 |
| **选项集** | `/api/v1/option-sets` | 选项集与选项的完整 CRUD |
| **菜单** | `/api/v1/menus` | 菜单 CRUD 与导航菜单树 |

## 📁 目录结构

```
object-engine
├── docs/sql/                    # 建表与升级脚本（按文件名顺序执行）
├── src/main/java/.../objectengine
│   ├── controller/              # REST 接口层
│   ├── service/                 # 业务逻辑层（接口 + 实现）
│   ├── domain/                  # 数据库实体
│   ├── engine/                  # 运行时引擎：字段类型校验注册表、Record 校验器
│   ├── dto / vo                 # 请求 / 响应结构
│   └── mapper                   # MyBatis-Plus Mapper
└── object-engine-web/src
    ├── api/                     # 后端接口封装（按资源划分）
    ├── components/dynamic/      # 运行时组件：DynamicForm / DynamicTable / FieldRenderer ...
    ├── views/                   # 页面（后台管理 + 前台动态页）
    └── utils/                   # 工具（记录展示文本、表单桥接等）
```

## 📄 更多文档

- **元数据驱动架构**：[docs/metadata-driven-architecture.md](docs/metadata-driven-architecture.md) —— 数据模型、运行时链路与字段类型扩展指南
- 开发规范与团队约定见 [AGENTS.md](AGENTS.md)
- 接口设计文档见 [docs/controller/v2.md](docs/controller/v2.md)

---

<div align="center">

**Object Engine** · 让业务对象配置即所得

</div>
