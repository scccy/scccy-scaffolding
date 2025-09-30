#!/bin/bash

# SCCCY 微服务脚手架 - Git Submodule 初始化脚本
# 用于初始化所有子模块

set -e

echo "🚀 开始初始化 Git Submodules..."

# 检查是否在 Git 仓库中
if [ ! -d ".git" ]; then
    echo "❌ 错误: 当前目录不是 Git 仓库"
    exit 1
fi

# 检查 Git 版本
git_version=$(git --version | cut -d' ' -f3)
echo "📋 Git 版本: $git_version"

# 初始化 submodules
echo "📦 初始化 submodules..."
git submodule init

# 更新 submodules
echo "🔄 更新 submodules..."
git submodule update --recursive

# 检查 submodule 状态
echo "📊 检查 submodule 状态..."
git submodule status

echo "✅ Git Submodules 初始化完成!"
echo ""
echo "📝 下一步操作:"
echo "   1. 运行 './scripts/build-all.sh' 构建所有模块"
echo "   2. 或者运行 'mvn clean install -DskipTests' 使用 Maven 构建"
echo ""
echo "🔧 常用命令:"
echo "   - 查看 submodule 状态: git submodule status"
echo "   - 更新所有 submodule: git submodule update --remote --merge"
echo "   - 进入特定服务: cd service/service-auth"
