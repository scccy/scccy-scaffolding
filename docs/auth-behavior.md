# 认证中心行为说明（第三方客户端 & 内部服务）

本文梳理当前 `service-auth` 的能力边界，并针对「第三方客户端」与「内部服务/普通用户」两类调用者给出流程说明、接口清单与约定。同时附上常用注解的作用，帮助业务团队在网关与微服务层面保持一致的安全策略。

---

## 1. 角色与通道

| 角色 | 典型调用方 | 使用入口 | Token 类型 | 是否走网关 |
| --- | --- | --- | --- | --- |
| 第三方客户端 | Web/APP、外部合作方、SaaS 集成 | `/api/user/**`、`/oauth2/**` | 用户 JWT（授权码/密码）或客户端凭证 | 必须 |
| 内部服务 / 普通用户 | 微服务之间的 Feign 调用、内部定时任务 | 原始业务接口（例如 service-system） | client_credentials 令牌（scope=`internal-service` 等） | 默认走网关；也可在集群内部直连，但仍需 JWT |

---

## 2. 第三方客户端行为

1. **登录/注册**  
   - 通过 Gateway 调用 `service-auth` 的 `/api/user/register`、`/api/user/login`。  
   - 返回内容包含 Access Token（JWT）与可选 Refresh Token。  
   - JWT 会在 Gateway 与业务服务双重校验，并被 `UserInfoGatewayFilter` 自动转换成 `X-User-*` 请求头。

2. **OAuth2 授权**  
   - 第三方集成可使用 `authorization_code`、`client_credentials` 等标准流程，端点位于 `/oauth2/*`。  
   - `RegisteredClient` 配置位于数据库/Nacos，可在 `service-auth` 中维护。

3. **登出与黑名单**  
   - 调用 `/api/user/logout` 或 `/oauth2/revoke`，`TokenBlacklistService` 会将 jti/token 写入 Redis `jwt:blacklist:*`。  
   - Gateway 的 `TokenBlacklistGlobalFilter` 会在后续请求中统一拦截，返回 401。

4. **权限控制**  
   - 业务服务通过 `@CurrentUser` 注入用户上下文，并结合 Spring Security 权限注解完成授权。  
   - 若接口需要对外开放，可添加 `@Anonymous`（见下文）做到精确放行。

---

## 3. 内部服务/普通用户行为

1. **内部令牌获取**  
   - `AuthTokenService`（`common-base`）在启动时根据 `scccy.internal-token.*` 配置自动启用，并通过 Redis 共享 token。  
   - Feign 默认注入 `FeignAuthRequestInterceptor`，对所有未跳过的 FeignClient 添加 `Authorization: Bearer <internal token>`。  
   - 令牌来自 `client_credentials` 模式，默认客户端 `internal-service-client`，scope `internal-service`。  
   - `scccy.internal-token.token-url` 建议使用 `lb://service-auth/oauth2/token`（或其他服务名）形式，依赖 LoadBalanced WebClient 自动发现实例，无需硬编码主机端口。

2. **内部接口访问**  
   - 在 Controller/方法上添加 `@InternalOnly`（可指定 scope）。  
   - `InternalOnlyUrlProperties` 会收集这些路径，`ResourceServerConfig` 自动要求 `hasAuthority("SCOPE_<scope>")`。  
   - 因此普通用户 JWT（不含内部 scope）无法调用这些接口，只有内部 Feign/任务可访问。

3. **绕过场景**  
   - 若某个 FeignClient 需要调用外部系统，可使用 `@SkipInternalToken` 或在配置 `scccy.internal-token.feign.skip-clients` 中列出名称，以避免误注入内部令牌。  
   - 单服务本地调试可通过 `scccy.security.permit-all=true` 暂时关闭资源服务器校验，但发布前必须关闭。

---

## 4. 常用注解与作用

| 注解 | 位置 | 作用 | 关联组件 |
| --- | --- | --- | --- |
| `@Anonymous` (`common-modules`) | Controller 或方法 | 将接口加入匿名放行列表，由 `PermitAllUrlProperties` 自动收集，Resource Server/Gateway 放行 | `PermitAllUrlProperties`, `ResourceServerConfig` |
| `@InternalOnly(scope = "internal-service")` (`common-modules`) | Controller 或方法 | 声明内部专用接口，要求请求携带 `SCOPE_internal-service` 等内部令牌 | `InternalOnlyUrlProperties`, `ResourceServerConfig` |
| `@SkipInternalToken` (`common-base`) | FeignClient 接口 | 禁止该 FeignClient 注入内部令牌，适用于对外调用 | `FeignAuthRequestInterceptor` |
| `@CurrentUser` (`common-modules`) | Controller 参数 | 自动从请求头注入用户信息（X-User-*），便于业务层消费 | `CurrentUserArgumentResolver` |

> 说明：`@InternalOnly` 与 `@Anonymous` 可叠加在不同方法上；方法级声明优先级高于类级。若同一个 Controller 既有对外接口又有内部接口，可在方法层面分别标注。

---

## 5. 推荐实践

1. **对外接口**
   - 入口统一走 Gateway；禁止服务直接对外暴露端口。
   - 用 `@Anonymous`、`@CurrentUser`、权限注解精确控制访问。
   - 调整 `SecurityPathConstants.PUBLIC_ENDPOINTS` 仅保留设施类接口。

2. **内部接口**
   - 在 service-system、service-auth 等内部对接 Controller 上标记 `@InternalOnly`。  
   - FeignClient 默认获取内部 token，无需业务代码处理；如需自定义 scope，可在 `@InternalOnly(scope = "xxx")` 与 Feign 客户端中同步配置。

3. **运维关注点**
   - 定期轮换内部客户端密钥（`internal-service-client`），确保 `AuthTokenService` 配置同步。  
   - 监控 Gateway 黑名单命中、内部 token 获取失败等日志，及时发现异常。

---

如需在某个业务场景中扩展授权模式，推荐先在 `docs/jwt-auth-flow.md` 与本说明中补充流程，再更新 `service-auth`/Gateway 的配置以保持一致性。欢迎补充更多注解或安全策略，保持脚手架在不同项目间的统一体验。
