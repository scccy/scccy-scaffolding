#!/bin/bash

# SCCCY 微服务脚手架 - 添加新 Submodule 脚本
# 用法: ./scripts/add-submodule.sh <submodule-name> <repository-url> [branch]

set -e

# 检查参数
if [ $# -lt 2 ]; then
    echo "❌ 用法: $0 <submodule-name> <repository-url> [branch]"
    echo ""
    echo "📝 示例:"
    echo "   $0 service-user https://github.com/scccy/service-user.git main"
    echo "   $0 service-order https://github.com/scccy/service-order.git"
    echo ""
    echo "📋 参数说明:"
    echo "   submodule-name: 子模块名称 (如: service-user)"
    echo "   repository-url: 远程仓库 URL"
    echo "   branch: 分支名称 (可选，默认为 main)"
    exit 1
fi

SUBMODULE_NAME=$1
REPOSITORY_URL=$2
BRANCH=${3:-main}

echo "➕ 添加新的 Git Submodule..."
echo "📋 子模块名称: $SUBMODULE_NAME"
echo "📋 仓库 URL: $REPOSITORY_URL"
echo "📋 分支: $BRANCH"

# 检查是否在 Git 仓库中
if [ ! -d ".git" ]; then
    echo "❌ 错误: 当前目录不是 Git 仓库"
    exit 1
fi

# 检查子模块目录是否已存在
SUBMODULE_PATH="service/$SUBMODULE_NAME"
if [ -d "$SUBMODULE_PATH" ]; then
    echo "❌ 错误: 目录 $SUBMODULE_PATH 已存在"
    exit 1
fi

# 添加 submodule
echo "🔗 添加 submodule..."
git submodule add -b "$BRANCH" "$REPOSITORY_URL" "$SUBMODULE_PATH"

# 初始化并更新 submodule
echo "🔄 初始化 submodule..."
git submodule update --init --recursive "$SUBMODULE_PATH"

# 更新 service/pom.xml 添加新模块
echo "📝 更新 service/pom.xml..."
if [ -f "service/pom.xml" ]; then
    # 备份原文件
    cp service/pom.xml service/pom.xml.backup
    
    # 添加新模块到 modules 部分
    sed -i.tmp "/<\/modules>/i\\
        <module>$SUBMODULE_NAME</module>\\
    " service/pom.xml
    
    # 删除临时文件
    rm service/pom.xml.tmp
    
    echo "✅ 已更新 service/pom.xml"
else
    echo "⚠️  警告: service/pom.xml 不存在，请手动添加模块"
fi

# 显示状态
echo ""
echo "📊 当前 submodule 状态:"
git submodule status

echo ""
echo "✅ Submodule 添加完成!"
echo ""
echo "📝 下一步操作:"
echo "   1. 检查更改: git status"
echo "   2. 提交更改: git add . && git commit -m 'Add $SUBMODULE_NAME submodule'"
echo "   3. 推送到远程: git push origin main"
echo ""
echo "🔧 进入新模块: cd $SUBMODULE_PATH"
