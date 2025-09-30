#!/bin/bash

# SCCCY 微服务脚手架 - Git Submodule 更新脚本
# 用于更新所有子模块到最新版本

set -e

echo "🔄 开始更新 Git Submodules..."

# 检查是否在 Git 仓库中
if [ ! -d ".git" ]; then
    echo "❌ 错误: 当前目录不是 Git 仓库"
    exit 1
fi

# 显示当前 submodule 状态
echo "📊 当前 submodule 状态:"
git submodule status

echo ""
echo "🔄 更新所有 submodules 到最新版本..."

# 更新所有 submodule 到远程最新版本
git submodule update --remote --merge

# 显示更新后的状态
echo ""
echo "📊 更新后的 submodule 状态:"
git submodule status

# 检查是否有未提交的更改
if [ -n "$(git status --porcelain)" ]; then
    echo ""
    echo "⚠️  检测到未提交的更改:"
    git status --short
    
    echo ""
    echo "💡 建议操作:"
    echo "   1. 检查更改: git diff"
    echo "   2. 提交更改: git add . && git commit -m 'Update submodules to latest versions'"
    echo "   3. 推送到远程: git push origin main"
else
    echo ""
    echo "✅ 所有 submodules 已是最新版本，无需提交更改"
fi

echo ""
echo "🎉 Submodule 更新完成!"
