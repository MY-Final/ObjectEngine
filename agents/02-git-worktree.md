# Git Worktree 规范

需要并行处理多个任务、或不想动当前工作区时，使用 `git worktree`，不要把仓库重新 clone 一份。

## 创建

```bash
# 1. 主仓库先同步主分支
git checkout main && git pull origin main

# 2. 在仓库外、与仓库同级的目录创建 worktree，目录名与分支名对应
git worktree add ../object-engine.worktrees/<分支名> -b <分支名>
```

约定：

- 分支命名：功能用 `feat/<主题>`，修复用 `fix/<主题>`。
- worktree 目录统一放在仓库**同级**目录 `../object-engine.worktrees/` 下，命名与分支同名（`/` 换成 `-`）。
- **禁止**在仓库工作区内嵌套创建 worktree（会产生嵌套仓库问题）；确有需要必须先征得用户确认并把目录加入 `.gitignore`。

## 在 worktree 中工作

- 每个 worktree 只挂一个分支；git 不允许两个 worktree 检出同一分支，冲突时先清理旧的。
- 提交信息遵循 [01-git-commit.md](./01-git-commit.md)。
- 合并前在 worktree 内跑 `./mvnw test`，通过后再合回 `main`。

## 清理

```bash
git worktree remove ../object-engine.worktrees/<名字>   # 分支合并后立即清理
git branch -d <分支名>                                   # 删除已合并的分支
git worktree prune                                       # 手动删过目录后清理残留记录
```

查看现有 worktree：`git worktree list`。
