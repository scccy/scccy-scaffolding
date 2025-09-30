# Git Submodule 管理项目

## 项目概述
SCCCY 微服务脚手架是一个基于 Spring Boot 的微服务架构项目，采用 Git Submodule 进行多仓库管理。

## 微服务架构

### 主仓库结构
```
scccy-scaffolding/                    # 主仓库
├── .gitmodules                      # Git submodule 配置文件
├── pom.xml                          # 主 Maven 配置文件
├── service/                         # 服务层目录
│   ├── pom.xml                      # 服务层 Maven 配置
│   ├── service-auth/                # 认证服务 (submodule)
│   ├── service-base/                # 基础服务 (submodule)
│   ├── service-common/              # 公共模块 (submodule)
│   └── service-gateway/             # 网关服务 (submodule)
├── core/                           # 核心模块目录
│   ├── core-flink/                 # Flink 相关模块
│   └── core-flink-api/             # Flink API 模块
└── scripts/                        # 管理脚本
    ├── setup-submodules.sh         # 初始化 submodule 脚本
    ├── update-submodules.sh        # 更新 submodule 脚本
    └── build-all.sh                # 构建所有模块脚本
```

## Git Submodule 仓库映射

| 本地路径 | 远程仓库 | 说明 |
|---------|---------|------|
| `service/service-auth` | `https://github.com/scccy/service-auth` | 认证服务 |
| `service/service-base` | `https://github.com/scccy/service-base` | 基础服务 |
| `service/service-common` | `https://github.com/scccy/service-common` | 公共模块 |
| `service/service-gateway` | `https://github.com/scccy/service-gateway` | 网关服务 |

## 快速开始

### 1. 克隆主仓库
```bash
git clone https://github.com/scccy/scccy-scaffolding.git
cd scccy-scaffolding
```

### 2. 初始化并更新所有 submodule
```bash
# 方法一：使用脚本（推荐）
chmod +x scripts/setup-submodules.sh
./scripts/setup-submodules.sh

# 方法二：手动执行
git submodule init
git submodule update --recursive
```

### 3. 构建项目
```bash
# 构建所有模块
chmod +x scripts/build-all.sh
./scripts/build-all.sh

# 或者使用 Maven
mvn clean install -DskipTests
```

## Git Submodule 常用命令

### 添加新的 submodule
```bash
git submodule add https://github.com/scccy/service-new.git service/service-new
git add .gitmodules service/service-new
git commit -m "Add service-new submodule"
```

### 更新所有 submodule 到最新版本
```bash
git submodule update --remote --merge
```

### 更新特定 submodule
```bash
cd service/service-auth
git pull origin main
cd ../..
git add service/service-auth
git commit -m "Update service-auth to latest version"
```

### 删除 submodule
```bash
# 1. 删除 .gitmodules 中的相关条目
git config -f .gitmodules --remove-section submodule.service/service-auth

# 2. 删除 .git/config 中的相关条目
git config -f .git/config --remove-section submodule.service/service-auth

# 3. 删除 submodule 目录
git rm --cached service/service-auth
rm -rf service/service-auth

# 4. 删除 .git/modules 中的相关目录
rm -rf .git/modules/service/service-auth

# 5. 提交更改
git commit -m "Remove service-auth submodule"
```

### 克隆包含 submodule 的仓库
```bash
# 方法一：克隆时同时初始化 submodule
git clone --recursive https://github.com/scccy/scccy-scaffolding.git

# 方法二：先克隆，再初始化 submodule
git clone https://github.com/scccy/scccy-scaffolding.git
cd scccy-scaffolding
git submodule init
git submodule update
```

## 开发工作流

### 1. 开发新功能
```bash
# 进入具体的服务目录
cd service/service-auth

# 创建新分支
git checkout -b feature/new-feature

# 开发完成后提交
git add .
git commit -m "Add new feature"
git push origin feature/new-feature

# 在主仓库中更新 submodule 引用
cd ../..
git add service/service-auth
git commit -m "Update service-auth to include new feature"
git push origin main
```

### 2. 同步团队成员的更改
```bash
# 拉取主仓库更新
git pull origin main

# 更新所有 submodule
git submodule update --remote --merge
```

## 注意事项

1. **版本管理**：每个 submodule 都有独立的版本控制，主仓库只记录 submodule 的特定提交
2. **分支管理**：建议为每个服务维护独立的分支策略
3. **构建顺序**：确保 `service-common` 在其他服务之前构建
4. **依赖管理**：使用 Maven 的 dependency management 统一管理版本

## 故障排除

### Submodule 状态异常
```bash
# 检查 submodule 状态
git submodule status

# 重置 submodule 到正确状态
git submodule deinit -f service/service-auth
git submodule update --init service/service-auth
```

### 构建失败
```bash
# 清理并重新构建
mvn clean
mvn install -DskipTests

# 如果某个模块构建失败，可以单独构建
cd service/service-auth
mvn clean install -DskipTests
```
