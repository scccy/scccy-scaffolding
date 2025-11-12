# Auth & Gateway 对“JWT 认证与登出黑名单流转”的符合性检查

本文对当前代码与《docs/jwt-auth-flow.md》中目标体系进行对照，列出已满足与待完善项，并给出落地建议。

---

## 一、总体目标回顾（摘要）
- 登录：客户端 → Gateway → Auth 签发 JWT（JWK 管理）。
- 访问业务：客户端携带 JWT → Gateway 验签+黑名单 → 业务微服务（注入用户上下文）。
- 登出：客户端 → Gateway → Auth 写入黑名单（Redis），后续由 Gateway 统一拦截。

---

## 二、Gateway 现状

### 已满足
1. 统一鉴权与验签
   - 位置：`gateway/config/ResourceServerConfig.java`
   - 能力：基于 `issuer-uri`/`jwks` 验证 JWT 签名与有效期（WebFlux Security）。
2. 统一用户上下文透传
   - 位置：`gateway/filter/UserInfoGatewayFilter.java`
   - 能力：从 JWT 解析 `userId/username/authorities`，写入内部请求头 `X-User-Id / X-Username / X-Authorities` 转发后端。
3. 统一 JSON 未认证响应
   - 位置：`gateway/config/JsonServerAuthenticationEntryPoint.java`
   - 能力：未认证返回 401 JSON（与业务服务风格一致）。

### 未满足（关键差距）
1. 黑名单统一拦截
   - 现状：未实现 Redis 黑名单查询与拦截逻辑。
   - 影响：登出后旧 Token 仍可能在有效期内被网关放行。

---

## 三、Auth 现状

### 已满足
1. OAuth2 授权服务器能力
   - 位置：`service-auth/config/AuthorizationServerConfig.java`
   - 能力：授权码/设备码等现代配置；发布 OIDC/JWK Set；使用 Spring Authorization Server。
2. JWT Claims 自定义
   - 位置：`service-auth/config/TokenCustomizerConfig.java`
   - 能力：可从 `service-system` 拉取用户信息，向 Token 注入 `userId/username/nickName/status/authorities` 等。
3. JWK 管理与缓存
   - 位置：`service-auth/oauth2/JWKCacheManager.java`
   - 能力：生成并缓存 JWKSet（已将极长 TTL 调整为合理值）。
4. 客户端注册能力
   - 位置：`RegisteredClientController` + `RegisteredClientConvert`
   - 能力：支持动态注册/更新客户端，并持久化 `token_settings`（已修复 TTL 字段使用表单值）。

### 未满足（关键差距）
1. 统一“登出/撤销”接口
   - 现状：未提供 /logout 或 /oauth2/revoke 的集中处理，未统一写入黑名单。
2. 黑名单集中写入
   - 现状：无统一黑名单服务（auth 内未实现）。
3. 直连“用户名密码登录”接口（非 OAuth2）
   - 现状：`USER_AUTH_PLAN.md` 为规划文档，未发现 `UserAuthController` 等落地接口；若需要“/api/user/login”直连模式，还需实现。

---

## 四、Service（业务服务）现状

### 已满足
1. 基于请求头的权限控制链路
   - 示例：`service-demo` 使用 `@PreAuthorize` + `PermissionService("ss")`；`PermissionService` 从 `X-Authorities` 判断权限/角色。

### 待确认/优化
1. 业务服务自身黑名单逻辑
   - 建议：不在业务内实现；由 Gateway 统一拦截即可，保持单一入口与一致性。

---

## 五、差距与改造建议（优先级排序）

### P0（必须）
1. Gateway 增加黑名单统一拦截
   - 方案：在 JWT 验签通过后、路由前，查询 Redis：`jwt:blacklist:{jti|token}`；命中则 401。
   - 依赖：Redis 连接（已具备 `common-redis-cache` 默认能力，可按需接入）。

2. Auth 提供集中登出/撤销端点
   - 方案：`POST /api/user/logout` 或遵循 OAuth2 `token revocation` 规范；写入 Redis 黑名单，TTL=Token 剩余有效期。
   - 约定：Key 前缀统一，如 `jwt:blacklist:`，Value= true。

### P1（推荐）
3. Auth 增加“用户名密码登录”直连接口（如需）
   - 方案：实现 `UserAuthController`（`/api/user/login`），生成 JWT（或走 OAuth2 Resource Owner Password 替代方案）。
   - 注意：现代 OAuth2 不再推荐密码模式，建议用授权码+PKCE；直连仅用于内部/特定场景。

4. 文档与观测
   - 网关黑名单命中日志、指标；Auth 登出写入黑名单日志；便于审计与排错。

---

## 六、与目标文档（jwt-auth-flow.md）对齐情况

| 能力点 | 目标文档要求 | 当前状态 | 说明 |
|---|---|---|---|
| 登录签发（Auth） | 统一签发 JWT，JWK 管理 | 已满足 | Spring Authorization Server + Token 自定义 + JWKSet |
| 网关验签 | issuer/jwks 验签 & 401 JSON | 已满足 | ResourceServerConfig + JsonServerAuthenticationEntryPoint |
| 用户上下文透传 | X-User-Id / X-Username / X-Authorities | 已满足 | UserInfoGatewayFilter |
| 统一黑名单拦截（Gateway） | 必须 | 未满足 | 需新增 Redis 查询与 401 拦截 |
| 登出/撤销（Auth） | 必须 | 未满足 | 需新增 /logout 或 /oauth2/revoke，写入黑名单 |
| 直连登录（/api/user/login） | 可选 | 未落地 | 目前为规划文档，未见控制器实现 |

---

## 七、下一步实施清单（精简）
- Gateway：新增黑名单过滤（Redis 查询命中→401），可选择在 Security 认证流程或自定义 GatewayFilter 中实现。
- Auth：新增登出端点，计算剩余 TTL 写入 `jwt:blacklist:{jti|token}`。
- 可选：实现直连登录接口或坚持 OAuth2 授权码/设备码为统一入口。

---

## 八、附录：黑名单键值约定（建议）
- Key：`jwt:blacklist:{jti}`（首选 jti；若无 jti，可退化为 token 整串）
- Value：`true`
- TTL：`exp - now`（毫秒）

> 注：使用 jti 更稳妥，避免存储整串 Token；签发时在 claims 中确保包含 jti。




