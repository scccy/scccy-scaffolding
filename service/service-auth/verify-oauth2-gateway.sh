#!/bin/bash

# OAuth2 Gateway 验证脚本
# 用于验证 Gateway 和 Authorization Server 是否正常工作

echo "=========================================="
echo "OAuth2 Gateway 验证脚本"
echo "=========================================="
echo ""

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置
# 支持通过环境变量配置端口和路径，默认使用配置文件中的值
# 使用方法：export AUTH_SERVER_PORT=30003 && export GATEWAY_PORT=30000 && export TEST_PATH=/demo/get && ./verify-oauth2-gateway.sh
AUTH_SERVER_PORT="${AUTH_SERVER_PORT:-30003}"
GATEWAY_PORT="${GATEWAY_PORT:-30000}"
TEST_PATH="${TEST_PATH:-/demo/get}"  # 测试路径，默认使用 service-demo 的 /demo/get 端点
AUTH_SERVER_URL="http://localhost:${AUTH_SERVER_PORT}"
GATEWAY_URL="http://localhost:${GATEWAY_PORT}"

echo -e "${YELLOW}配置信息:${NC}"
echo "  Authorization Server: ${AUTH_SERVER_URL}"
echo "  Gateway: ${GATEWAY_URL}"
echo "  测试路径: ${TEST_PATH}"
echo "  (可通过环境变量 AUTH_SERVER_PORT、GATEWAY_PORT 和 TEST_PATH 修改)"
echo ""

echo "=========================================="
echo "步骤 1: 验证 Authorization Server 端点"
echo "=========================================="

echo -e "${YELLOW}1.1 检查 OAuth2 授权服务器元数据端点...${NC}"
METADATA_RESPONSE=$(curl -s "${AUTH_SERVER_URL}/.well-known/oauth-authorization-server")
if [ $? -eq 0 ] && echo "$METADATA_RESPONSE" | grep -q "issuer"; then
    echo -e "${GREEN}✓ OAuth2 授权服务器元数据端点正常${NC}"
    echo "$METADATA_RESPONSE" | jq '.' 2>/dev/null || echo "$METADATA_RESPONSE"
else
    echo -e "${RED}✗ OAuth2 授权服务器元数据端点访问失败${NC}"
    echo "响应: $METADATA_RESPONSE"
    exit 1
fi
echo ""

echo -e "${YELLOW}1.2 检查 JWK Set 端点...${NC}"
JWKS_RESPONSE=$(curl -s "${AUTH_SERVER_URL}/oauth2/jwks")
if [ $? -eq 0 ] && echo "$JWKS_RESPONSE" | grep -q "keys"; then
    echo -e "${GREEN}✓ JWK Set 端点正常${NC}"
    echo "$JWKS_RESPONSE" | jq '.' 2>/dev/null || echo "$JWKS_RESPONSE"
else
    echo -e "${RED}✗ JWK Set 端点访问失败${NC}"
    echo "响应: $JWKS_RESPONSE"
    exit 1
fi
echo ""

echo "=========================================="
echo "步骤 2: 获取 Access Token（客户端凭证模式）"
echo "=========================================="

echo -e "${YELLOW}提示: 需要先注册一个 OAuth2 客户端${NC}"
echo "请先访问 Authorization Server 的客户端注册接口注册一个客户端"
echo "或者使用已有的客户端凭证"
echo ""
read -p "请输入 client_id: " CLIENT_ID
read -p "请输入 client_secret: " CLIENT_SECRET

if [ -z "$CLIENT_ID" ] || [ -z "$CLIENT_SECRET" ]; then
    echo -e "${RED}✗ client_id 和 client_secret 不能为空${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}2.1 使用客户端凭证模式获取 Token...${NC}"
# 使用 curl 获取响应和 HTTP 状态码
TOKEN_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "${AUTH_SERVER_URL}/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "${CLIENT_ID}:${CLIENT_SECRET}" \
  -d "grant_type=client_credentials")

# 分离 HTTP 状态码和响应体
HTTP_CODE=$(echo "$TOKEN_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2 | tr -d ' ')
BODY=$(echo "$TOKEN_RESPONSE" | sed '/HTTP_CODE/d')

# 检查 HTTP 状态码
if [ "$HTTP_CODE" != "200" ]; then
    echo -e "${RED}✗ 获取 Token 失败（HTTP $HTTP_CODE）${NC}"
    echo "响应: $BODY"
    exit 1
fi

# 尝试使用 jq 提取 access_token
if command -v jq &> /dev/null; then
    ACCESS_TOKEN=$(echo "$BODY" | jq -r '.access_token' 2>/dev/null)
else
    # 如果没有 jq，使用 grep 和 sed 提取
    ACCESS_TOKEN=$(echo "$BODY" | grep -o '"access_token":"[^"]*"' | sed 's/"access_token":"\([^"]*\)"/\1/' | head -1)
fi

# 检查 access_token 是否提取成功
if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" = "null" ] || [ "$ACCESS_TOKEN" = "" ]; then
    # 检查响应中是否包含错误信息
    if echo "$BODY" | grep -qi "error"; then
        echo -e "${RED}✗ 获取 Token 失败（包含错误信息）${NC}"
        echo "响应: $BODY"
        if command -v jq &> /dev/null; then
            echo "$BODY" | jq '.' 2>/dev/null
        else
            echo "$BODY"
        fi
        exit 1
    else
        # 如果响应看起来正常但没有提取到 token，尝试其他方法提取
        echo -e "${YELLOW}⚠ 无法使用 jq 提取 access_token，尝试其他方法...${NC}"
        # 使用 sed 提取（更兼容）
        ACCESS_TOKEN=$(echo "$BODY" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p' | head -1)
        if [ -z "$ACCESS_TOKEN" ]; then
            echo -e "${RED}✗ 无法从响应中提取 access_token${NC}"
            echo "响应: $BODY"
            exit 1
        fi
    fi
fi

echo -e "${GREEN}✓ 成功获取 Access Token${NC}"
echo "Token 响应:"
if command -v jq &> /dev/null; then
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
else
    echo "$BODY"
fi
echo ""
if [ -n "$ACCESS_TOKEN" ]; then
    TOKEN_LENGTH=${#ACCESS_TOKEN}
    if [ $TOKEN_LENGTH -gt 50 ]; then
        echo "Access Token (前50个字符): ${ACCESS_TOKEN:0:50}..."
    else
        echo "Access Token: ${ACCESS_TOKEN}"
    fi
fi
echo ""

echo "=========================================="
echo "步骤 3: 验证 Gateway 作为 Resource Server"
echo "=========================================="

echo -e "${YELLOW}3.1 测试不带 Token 访问 Gateway（应该返回 401）...${NC}"
NO_TOKEN_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" "${GATEWAY_URL}${TEST_PATH}")
HTTP_CODE=$(echo "$NO_TOKEN_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
if [ "$HTTP_CODE" = "401" ]; then
    echo -e "${GREEN}✓ Gateway 正确拒绝了未认证请求（401）${NC}"
else
    echo -e "${YELLOW}⚠ 预期 401，实际返回: $HTTP_CODE${NC}"
    echo "响应: $NO_TOKEN_RESPONSE"
fi
echo ""

echo -e "${YELLOW}3.2 测试带 Token 访问 Gateway（应该验证成功）...${NC}"
WITH_TOKEN_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  "${GATEWAY_URL}${TEST_PATH}")
HTTP_CODE=$(echo "$WITH_TOKEN_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2 | tr -d ' ')
BODY=$(echo "$WITH_TOKEN_RESPONSE" | sed '/HTTP_CODE/d')

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "404" ]; then
    echo -e "${GREEN}✓ Gateway 验证 Token 成功（HTTP $HTTP_CODE）${NC}"
    echo "响应体: $BODY"
else
    echo -e "${RED}✗ Gateway 验证 Token 失败（HTTP $HTTP_CODE）${NC}"
    echo "响应: $WITH_TOKEN_RESPONSE"
fi
echo ""

echo "=========================================="
echo "步骤 4: 验证用户信息传递到后端服务"
echo "=========================================="

echo -e "${YELLOW}4.1 检查 Gateway 日志，确认用户信息已添加到请求头...${NC}"
echo "请在 Gateway 日志中查找以下内容："
echo "  - '提取用户信息: userId=... username=... authorities=...'"
echo "  - 'X-User-Id', 'X-Username', 'X-Authorities' 请求头"
echo ""
echo "如果后端服务已启动，也可以在后端服务的日志中查看请求头"
echo ""

echo "=========================================="
echo "步骤 5: 验证 Token 内容"
echo "=========================================="

echo -e "${YELLOW}5.1 解析 Token 内容（使用 jwt.io 或在线工具）...${NC}"
echo "Access Token: ${ACCESS_TOKEN}"
echo ""
echo "您可以使用以下方式解析 Token:"
echo "1. 访问 https://jwt.io 并粘贴 Token"
echo "2. 使用命令行工具: echo '$ACCESS_TOKEN' | cut -d'.' -f2 | base64 -d | jq"
echo ""

# 尝试解析 Token（如果安装了 base64 和 jq）
if command -v base64 &> /dev/null && command -v jq &> /dev/null; then
    echo "Token 内容预览:"
    PAYLOAD=$(echo "$ACCESS_TOKEN" | cut -d'.' -f2)
    # 添加填充
    PADDING=$((4 - ${#PAYLOAD} % 4))
    if [ $PADDING -ne 4 ]; then
        PAYLOAD="${PAYLOAD}$(printf '%*s' $PADDING | tr ' ' '=')"
    fi
    echo "$PAYLOAD" | base64 -d 2>/dev/null | jq '.' 2>/dev/null || echo "无法解析 Token"
fi

echo ""
echo "=========================================="
echo "验证完成！"
echo "=========================================="

