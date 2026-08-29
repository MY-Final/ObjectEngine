# AGENTS.md — object-engine

基于 Spring Boot 的后端服务（当前处于初始化阶段）。团队开发规范统一放在根目录 `agents/` 文件夹，按编号排列；改动相关流程前先读对应文件。

## 项目概况

- 技术栈：Spring Boot 3.5.14（parent 统一管理版本），Java 17，Lombok。
- 包路径：`com.myfinal.objectengine`，主类 `ObjectEngineApplication`。
- 数据层：MyBatis-Plus 3.5.8（`mybatis-plus-spring-boot3-starter`）+ MySQL 8 + HikariCP。
- 工作流：Flowable 7.2.0（`flowable-spring-boot-starter`），`flowable.database-schema-update: false` 不自动建表；更换数据库时先手动执行 Flowable 建表脚本，或临时改 true 建完再改回 false。
- Flowable 必须配置 `flowable.database-catalog: object_engine`：共享 MySQL 实例上其他库也有 ACT_ 表，不限定 catalog 时 Flowable 判断表存在与否会跨库误判，导致空库上走错误的升级路径而启动失败。
- 其他依赖：springdoc-openapi 2.8.5（Swagger UI）、fastjson2、hutool-all、nacos-client。
- 远程仓库：`https://github.com/MY-Final/ObjectEngine.git`，主分支 `main`。

## 常用命令

| 用途         | 命令                       |
| ------------ | -------------------------- |
| 全量测试     | `./mvnw test`              |
| 本地起服务   | `./mvnw spring-boot:run`   |
| 只编译       | `./mvnw -q compile`        |

- 一律用仓库自带的 Maven wrapper（`mvnw`），不要用系统 `mvn`（避免版本不一致）。
- 环境：Windows + Git Bash。`mvnw test` 会启动完整 Spring 上下文并**真实连接数据库**，所以它同时是构建验证和数据库连通性验证。

## 数据库（重要）

- 连接信息在 `src/main/resources/application.yaml`：`jdbc:mysql://192.168.2.9:33306/object`，账号 root/root（内网开发库）。
- ORM 用 MyBatis-Plus；不要引入会自动改表结构的配置（如 JPA `ddl-auto: update`），也不要让 agent 直接执行 DDL。建表/改表写 SQL 脚本交给用户执行。
- Service 层用传统 API：接口继承 `com.baomidou.mybatisplus.extension.service.IService<T>`，实现继承 `com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<M, T>`。**项目固定用 MyBatis-Plus 3.5.8**（最后一个包含 IService/ServiceImpl 的版本，用户 2026-08-29 明确选择不用 3.5.9+ 的新 API），不要擅自升级 MP；升级到 3.5.9+ 会移除这两个类，届时需迁移到 `IRepository`/`CrudRepository` 并补回 `mybatis-plus-jsqlparser` 模块。
- URL 中的 `allowPublicKeyRetrieval=true` 和 `serverTimezone=Asia/Shanghai` 是 MySQL 8 认证与时区所需参数，不要删除。

## 团队规范（必读）

规范放在 `agents/` 下，新增规范继续追加编号文件：

1. [agents/01-git-commit.md](agents/01-git-commit.md) — Git 提交规范：`<type>: 中文描述` + 空行 + `- ` 开头的正文，type 用 feat/fix/refactor/docs/test/build/chore/perf/ci。
2. [agents/02-git-worktree.md](agents/02-git-worktree.md) — Git worktree 规范：worktree 建在仓库同级 `../object-engine.worktrees/` 目录，禁止嵌套在仓库内，合并后立即清理。

## 其他约定

- 提交信息、文档、注释用中文；代码标识符（类名、变量名等）用英文。
- 编译目标 Java 17，不要使用 17 之后的语法或 API。
- Windows 下 git 的 LF→CRLF 转换警告属正常现象，不要为此改动 `.gitattributes`。
