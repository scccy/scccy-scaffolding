# JWT 认证与登出黑名单流转说明

## 参与角色
- 客户端（Web/APP）
- Gateway（统一入口与鉴权）
- Auth（认证授权中心，统一签发/撤销）
- 业务微服务（示例：Service-A）
- Redis（黑名单存储）
- JWK Set（Auth 公钥发布，用于网关验签）

---

## 一、普通用户登录（JWT 签发）
1. 客户端 → Gateway
   - 请求：POST /api/user/login（或触发 OAuth2 授权码流程）
   - 载荷：用户名/密码（HTTPS）
2. Gateway → Auth
   - 路由转发到 Auth 登录/授权端点
3. Auth（集中签发）
   - 校验凭证（可经由 service-system 拉取用户/权限）
   - 生成 JWT（包含 userId、username、authorities 等 claims）
   - 使用私钥签名（JWK 管理）
4. Auth → Gateway → 客户端
   - 返回 Access Token（JWT）及可选 Refresh Token
5. Auth → JWK Set
   - 对外发布 JWK Set（/oauth2/jwks），供 Gateway 拉取公钥验签

---

## 二、携带 JWT 访问业务接口（以 A 表查询为例）
1. 客户端 → Gateway
   - 请求：GET /api/a/query?id=...
   - Header：Authorization: Bearer <access_token>
2. Gateway（统一鉴权）
   - 基于 issuer/jwks 验签与有效期校验
   - 统一黑名单校验（见下文「黑名单统一拦截」）
   - 解析 JWT，写入内部请求头：X-User-Id、X-Username、X-Authorities
   - 路由转发至 Service-A
3. Service-A（业务处理）
   - 信任来自 Gateway 的用户上下文请求头
   - 基于注解等方式进行权限控制
4. Service-A → Gateway → 客户端
   - 返回查询结果

---

## 三、登出（Logout）与 JWT 黑名单（集中于 Auth + Gateway 拦截）
目标：让已签发的自包含 JWT 在未过期前也能立即失效。

1. 客户端 → Gateway
   - 请求：POST /api/user/logout（或 /oauth2/revoke）
   - Header：Authorization: Bearer <access_token>
2. Gateway → Auth
   - 路由转发登出/撤销请求
3. Auth（集中管理）
   - 解析 Token（取 jti 或 Token 整串作为 Key）
   - 计算剩余有效期（exp - now）
   - 写入 Redis 黑名单：jwt:blacklist:{jti 或 token} = true，TTL = 剩余有效期
   - 可选：删除或标记 oauth2_authorization 记录为撤销（内省式校验时使用）
4. Auth → Gateway → 客户端
   - 返回登出成功
5. 后续任意请求（统一拦截）
   - 客户端再携带相同旧 Token 请求
   - Gateway 验签后执行黑名单查询：命中 → 返回 401；未命中 → 正常转发

---

## 四、黑名单统一拦截（Gateway 侧）
- 拦截点：JWT 验签之后、路由之前（或在自定义 ReactiveAuthenticationManager/JWT 转换器阶段）
- 数据源：Redis
  - Key 约定：jwt:blacklist:{jti 或 token}
  - Value：true，TTL 设为 Token 剩余有效期
- 行为：
  - 命中 → 返回 401（统一 JSON 响应）
  - 未命中 → 继续转发并注入用户上下文请求头

---

## 五、配置与职责
- Auth
  - 统一签发 JWT（JWK 管理）
  - 提供登出/撤销端点，写入黑名单（Redis）
  - 可与 service-system 协作拉取用户信息与权限
- Gateway
  - issuer-uri/jwks 验证签名与有效期
  - 统一黑名单拦截（Redis 查询）
  - 生成用户上下文请求头转发后端
- 业务微服务
  - 不处理黑名单逻辑
  - 基于请求头与注解执行权限控制

---

## 六、端到端时序（简化）
- 登录：Client → Gateway → Auth → JWT（经 Gateway 返回 Client）
- 调用业务：Client(带 JWT) → Gateway(验签+黑名单) → Service-A
- 登出：Client(带 JWT) → Gateway → Auth(写黑名单) → OK
- 登出后再次访问：Client(旧 JWT) → Gateway(验签+黑名单命中) → 401

---

## 七、FAQ
- 为什么黑名单在 Gateway 拦截？
  - 单一入口、避免重复实现、保证一致性与性能。
- 权限变更如何生效？
  - 可在权限变更时批量将相关 Token 加入黑名单，或缩短 Token TTL 配合刷新。
- 业务服务是否需要黑名单？
  - 不需要，统一由 Gateway 拦截；业务只消费用户上下文请求头。

---

## 八、内部接口 Scope 控制
- 对纯内部的 Feign/RPC 调用接口，在 Controller 或方法上添加 `@InternalOnly(scope = "internal-service")`。
- 启动时 `InternalOnlyUrlProperties` 会收集这些路径，并由资源服务器自动要求 `SCOPE_<scope>`。
- 默认 scope 为 `internal-service`，与内部 client_credentials 令牌保持一致，可按需覆盖，例如 `@InternalOnly(scope = "reporting")`。
- 普通用户 JWT 不会携带该 scope，避免前端绕过；内部 Feign 可依靠自动注入的内部令牌通过校验。
