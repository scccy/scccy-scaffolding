# OAuth2 Gateway 验证指南

## 📋 验证步骤

### 前置条件
- ✅ Gateway 服务已启动（端口：30000 或 8080）
- ✅ Authorization Server (service-auth) 已启动（端口：30003）
- ✅ 已注册 OAuth2 客户端（或使用测试客户端）

---

## 🔍 步骤 1: 验证 Authorization Server 端点

### 1.1 检查 OAuth2 授权服务器元数据端点

```bash
curl http://localhost:30003/.well-known/oauth-authorization-server
```

**预期响应**：
```json
{
  "issuer": "http://localhost:30003",
  "authorization_endpoint": "http://localhost:30003/oauth2/authorize",
  "token_endpoint": "http://localhost:30003/oauth2/token",
  "jwks_uri": "http://localhost:30003/oauth2/jwks",
  ...
}
```

### 1.2 检查 JWK Set 端点

```bash
curl http://localhost:30003/oauth2/jwks
```

**预期响应**：
```json
{
  "keys": [
    {
      "kty": "RSA",
      "e": "AQAB",
      "kid": "...",
      "n": "..."
    }
  ]
}
```

---

## 🔑 步骤 2: 注册 OAuth2 客户端（如果没有）

### 2.1 注册测试客户端

```bash
curl -X POST http://localhost:30003/client \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "test-gateway-client",
    "clientName": "Gateway 测试客户端",
    "clientSecret": "test-secret",
    "grantTypes": ["client_credentials"],
    "clientAuthenticationMethods": ["client_secret_basic"],
    "scopes": ["read", "write"]
  }'
```

**注意**：如果数据库中已有测试客户端，可以跳过此步骤。

### 2.2 查看已注册的客户端

```bash
# 查询所有客户端
curl http://localhost:30003/client/conditions \
  -H "Content-Type: application/json" \
  -d '{
    "pageNum": 1,
    "pageSize": 10
  }'

# 根据 clientId 查询
curl "http://localhost:30003/client?clientId=test_client2"
```

---

## 🎫 步骤 3: 获取 Access Token

### 3.1 使用客户端凭证模式获取 Token

```bash
# 使用 HTTP Basic 认证（推荐）
curl -X POST http://localhost:30003/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "test_client2:your-client-secret" \
  -d "grant_type=client_credentials"
```

**或者使用表单参数**：

```bash
curl -X POST http://localhost:30003/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=test_client2&client_secret=your-client-secret"
```

**预期响应**：
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 300
}
```

### 3.2 保存 Token

```bash
# 保存 Token 到环境变量
export ACCESS_TOKEN=$(curl -s -X POST http://localhost:30003/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "test_client2:your-client-secret" \
  -d "grant_type=client_credentials" | jq -r '.access_token')

echo "Access Token: $ACCESS_TOKEN"
```

---

## 🚪 步骤 4: 验证 Gateway 作为 Resource Server

### 4.1 测试不带 Token 访问 Gateway（应该返回 401）

```bash
curl -v http://localhost:8080/demo/test
```

**预期响应**：
- HTTP 状态码：`401 Unauthorized`
- 响应头可能包含：`WWW-Authenticate: Bearer`

### 4.2 测试带 Token 访问 Gateway（应该验证成功）

```bash
curl -v http://localhost:8080/demo/test \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**预期响应**：
- HTTP 状态码：`200 OK` 或 `404 Not Found`（取决于后端服务）
- Gateway 应该能够验证 Token 并转发请求

### 4.3 测试无效 Token（应该返回 401）

```bash
curl -v http://localhost:8080/demo/test \
  -H "Authorization: Bearer invalid-token"
```

**预期响应**：
- HTTP 状态码：`401 Unauthorized`

---

## 📊 步骤 5: 验证用户信息传递

### 5.1 检查 Gateway 日志

查看 Gateway 日志，应该能看到以下内容：

```
提取用户信息: userId=... username=... authorities=...
```

### 5.2 检查后端服务日志（如果已启动）

如果后端服务已启动，查看后端服务日志，应该能看到请求头：

```
X-User-Id: ...
X-Username: ...
X-Authorities: ...
```

### 5.3 解析 Token 内容（验证 Token 包含的信息）

```bash
# 使用 jwt.io 在线工具
echo "$ACCESS_TOKEN" | cut -d'.' -f2 | base64 -d | jq '.'
```

**或者访问 https://jwt.io 并粘贴 Token**

**预期 Token 内容**（示例）：
```json
{
  "sub": "test_client2",
  "scope": "read write",
  "iat": 1234567890,
  "exp": 1234568190
}
```

**注意**：客户端凭证模式的 Token 不包含用户信息（userId, username 等），因为它是服务间调用。如果需要用户信息，需要使用授权码模式。

---

## 🔄 步骤 6: 完整流程测试（授权码模式 - 可选）

如果需要测试包含用户信息的 Token，可以使用授权码模式：

### 6.1 引导用户访问授权端点

```
http://localhost:30003/oauth2/authorize?
  client_id=test_client2&
  response_type=code&
  redirect_uri=https://www.baidu.com&
  scope=read write&
  state=xyz123
```

### 6.2 用户登录并确认授权

### 6.3 获取授权码后，使用授权码换取 Token

```bash
curl -X POST http://localhost:30003/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "test_client2:your-client-secret" \
  -d "grant_type=authorization_code&code=xxx&redirect_uri=https://www.baidu.com"
```

**预期响应**（包含用户信息）：
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 300,
  "refresh_token": "...",
  "scope": "read write",
  "id_token": "..."
}
```

---

## 🛠️ 使用验证脚本

可以使用提供的验证脚本自动执行以上步骤：

```bash
./verify-oauth2-gateway.sh
```

脚本会：
1. 验证 Authorization Server 端点
2. 提示输入客户端凭证
3. 获取 Access Token
4. 测试 Gateway 验证 Token
5. 验证用户信息传递

---

## ❌ 常见问题排查

### 问题 1: Gateway 无法连接到 Authorization Server

**错误信息**：
```
Unable to resolve "service-auth:30003"
```

**解决方案**：
1. 检查 Gateway 和 Authorization Server 是否在同一网络
2. 如果不在同一网络，修改 `gateway.yaml` 中的 `issuer-uri` 为 `http://localhost:30003`
3. 检查 Nacos 服务发现是否正常

### 问题 2: Token 验证失败

**错误信息**：
```
401 Unauthorized
```

**排查步骤**：
1. 检查 Token 是否过期
2. 检查 Token 格式是否正确（应该是 JWT）
3. 检查 Gateway 日志中的错误信息
4. 验证 Gateway 能否访问 Authorization Server 的 JWK Set 端点

### 问题 3: 用户信息未传递到后端服务

**排查步骤**：
1. 检查 Gateway 日志中是否有 "提取用户信息" 的日志
2. 检查 `UserInfoGatewayFilter` 是否正确配置到路由中
3. 检查后端服务是否能够接收请求头

---

## ✅ 验证清单

- [ ] Authorization Server 元数据端点可访问
- [ ] JWK Set 端点返回正确的密钥
- [ ] 可以成功获取 Access Token
- [ ] Gateway 拒绝未认证请求（401）
- [ ] Gateway 接受有效 Token（200）
- [ ] Gateway 拒绝无效 Token（401）
- [ ] Gateway 日志显示用户信息提取
- [ ] 后端服务能够接收用户信息请求头

---

## 📝 注意事项

1. **客户端凭证模式 vs 授权码模式**：
   - 客户端凭证模式：服务间调用，Token 不包含用户信息
   - 授权码模式：用户授权，Token 包含用户信息（userId, username 等）

2. **Token 格式**：
   - Access Token 是 JWT 格式
   - 可以使用 https://jwt.io 解析 Token 内容

3. **服务发现**：
   - 如果 Gateway 和 Authorization Server 在同一网络，使用服务名
   - 如果不在同一网络，使用 localhost 或 IP 地址

4. **端口配置**：
   - Authorization Server: 30003
   - Gateway: 30000 或 8080（根据实际配置）

---

## 🎯 快速验证命令

```bash
# 1. 检查 Authorization Server
curl http://localhost:30003/.well-known/oauth-authorization-server | jq '.issuer'

# 2. 获取 Token
TOKEN=$(curl -s -X POST http://localhost:30003/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "test_client2:your-client-secret" \
  -d "grant_type=client_credentials" | jq -r '.access_token')

# 3. 测试 Gateway（不带 Token）
curl -w "\nHTTP_CODE:%{http_code}\n" http://localhost:8080/demo/test

# 4. 测试 Gateway（带 Token）
curl -w "\nHTTP_CODE:%{http_code}\n" http://localhost:8080/demo/test \
  -H "Authorization: Bearer $TOKEN"
```

