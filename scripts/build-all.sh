#!/bin/bash

# SCCCY 微服务脚手架 - 构建所有模块脚本
# 用于构建整个项目的所有模块

set -e

echo "🏗️  开始构建 SCCCY 微服务脚手架..."

# 检查 Maven 是否安装
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: Maven 未安装或不在 PATH 中"
    echo "请安装 Maven 并确保它在 PATH 中"
    exit 1
fi

# 显示 Maven 版本
mvn_version=$(mvn --version | head -n 1)
echo "📋 $mvn_version"

# 检查 Java 版本
java_version=$(java -version 2>&1 | head -n 1)
echo "📋 $java_version"

echo ""
echo "🧹 清理项目..."
mvn clean

echo ""
echo "🔨 构建项目 (跳过测试)..."
mvn install -DskipTests

echo ""
echo "🧪 运行测试..."
mvn test

echo ""
echo "📦 打包项目..."
mvn package -DskipTests

echo ""
echo "✅ 项目构建完成!"
echo ""
echo "📁 构建产物位置:"
echo "   - JAR 文件: target/*.jar"
echo "   - 各服务 JAR: service/*/target/*.jar"
echo ""
echo "🚀 启动服务:"
echo "   - 认证服务: java -jar service/service-auth/target/service-auth-*.jar"
echo "   - 网关服务: java -jar service/service-gateway/target/service-gateway-*.jar"
echo ""
echo "🔧 其他命令:"
echo "   - 只构建特定模块: cd service/service-auth && mvn clean install"
echo "   - 跳过测试构建: mvn clean install -DskipTests"
echo "   - 生成依赖树: mvn dependency:tree"
