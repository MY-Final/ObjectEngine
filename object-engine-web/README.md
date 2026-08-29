# object-engine-web

Object Engine 前端工程（Vue 3 + TypeScript + Vite + Element Plus）。

后端为基于 Metadata 驱动的动态业务对象引擎，前端负责：

```text
Object Metadata
        ↓
Dynamic Form（下一阶段）
        ↓
Dynamic Table（下一阶段）
        ↓
Dynamic CRUD（下一阶段）
```

当前阶段仅完成基础工程初始化，动态组件为占位实现。

## 技术栈

- Vue 3 + TypeScript + Vite
- Element Plus / Vue Router / Pinia / Axios
- ESLint + Prettier（包管理器：pnpm）

## 启动

```bash
pnpm install
pnpm dev        # 开发环境 http://localhost:5173
pnpm build      # 类型检查 + 生产构建
pnpm lint       # 代码检查
```

## 后端接口与代理

- API 基础路径：`/api/v1`（见 `.env` 的 `VITE_API_BASE_URL`）
- 开发环境通过 Vite proxy 把 `/api` 转发到 `http://localhost:8080`（见 `vite.config.ts`），
  本地需先启动后端服务（仓库根目录 `./mvnw spring-boot:run`）
- 生产环境由部署侧（如 Nginx）把 `/api` 反代到后端

## 目录结构

```text
src
├── api            # Axios 封装与 Object/Field/Record API
├── components
│   ├── dynamic    # DynamicForm / DynamicTable / FieldRenderer（占位）
│   └── common
├── layouts        # DefaultLayout（侧边菜单 + 主内容区）
├── router         # 路由
├── stores         # Pinia
├── types          # Object/Field/Record/Metadata 类型
└── views          # dashboard / object（对象与字段管理）/ custom（动态对象页）
```

## 当前进度

已完成：工程初始化、Axios 统一封装、API 层、类型定义、路由与布局、对象/字段管理页骨架、动态页面占位。

未实现（下一阶段）：FieldRenderer 字段渲染、DynamicForm、DynamicTable、动态 CRUD。
