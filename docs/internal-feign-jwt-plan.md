# 内部 Feign JWT 自动生成方案

## 背景与目标
- 微服务之间通过 Feign 调用时需要自动附带 JWT，避免手工维护凭证。
- 统一由 `service-auth` 颁发内部调用令牌，确保链路级别的身份与权限校验。
- 在 `common` 模块沉淀公共的 Token 生成、验证与缓存能力，方便各服务复用。

## 核心步骤
1. **定义内部客户端标识**：为每个需要发起调用的服务在 Nacos 中配置 `clientId`/`clientSecret` 与授权范围（scope），并同步到 `service-auth`。
2. **扩展 `service-auth` 发行能力**：新增内部 JWT 模板（issuer=`internal`，audience=目标服务），支持短期令牌和刷新接口，公钥下发到 `common`。
3. **实现 `AuthTokenService`**：在 `common` 或各服务的共享模块内封装 JWT 申请、签发与缓存逻辑，提供 `getServiceToken(audience, scope)` 方法。
4. **注册 Feign `RequestInterceptor`**：所有需要向外呼叫的服务加载拦截器，自动从 `AuthTokenService` 获取令牌并在请求头写入 `Authorization: Bearer <token>`。
5. **接入端校验**：网关或目标服务的安全过滤链复用 `common` 的 JWT 解析工具，验证签名/过期/作用域，并将调用方服务信息注入 `SecurityContext`。
6. **观测与风控**：在 Token 申请失败、验证失败、scope 不匹配等场景打日志与指标，结合链路追踪头部一起上报。

## 执行计划
| 阶段 | 时间 | 负责人 | 输出 | 状态 |
| --- | --- | --- | --- | --- |
| 需求澄清 | T0 + 2 天 | 架构/安全 | Scope 和服务清单、Nacos 配置模板 | 待启动 |
| 能力建设 | T0 + 7 天 | service-auth 团队 | 内部 JWT API、密钥分发、单测 | 待启动 |
| 客户端改造 | T0 + 14 天 | 各业务服务负责人 | `AuthTokenService`、Feign 拦截器、配置落地 | 待启动 |
| 校验与灰度 | T0 + 18 天 | 测试/运维 | 联调报告、Nacos 配置发布、监控规则 | 待启动 |
| 全量上线 | T0 + 21 天 | 运维/研发 | 变更记录、回滚方案、上线回执 | 待启动 |

### 执行目标与完成情况
| 目标 | 衡量指标 | 当前进度 |
| --- | --- | --- |
| 内部 JWT 统一签发 | `service-auth` 支持内部 client、生成短期 token | 进行中（client_credentials claims 已自定义） |
| Feign 自动注入 | 所有 Feign Client 具备 RequestInterceptor 自动加 token | 已完成（AuthTokenService + Feign 拦截器落地） |
| Redis 统一缓存 | `common-redis-cache` 完成 token 缓存实现，命中率 ≥90% | 已完成（RedisInternalTokenCache 提供统一缓存） |
| 安全校验闭环 | `ResourceServerConfig` 能基于 scope 区分内部接口，并有监控告警 | 待启动 |

## 配置与交付物
- `docs/internal-feign-jwt-plan.md`（当前文档）持续更新状态。
- `docs/internal-call-jwt.md`：规范内部 Token 字段、示例请求/响应。
- `nacos_config_export/DEFAULT_GROUP/service-*.yaml`：新增 `internal-jwt` 配置区，管理 client 证书及开关。
- `common` 模块中的 `AuthTokenService`、`FeignAuthRequestInterceptor` 及对应单测。
    - 若业务仍处于开发联调阶段，可根据 `DevPermitAllSecurityConfig`（`scccy.security.permit-all=true`）控制全局放行；上线前需关闭。
    - `ResourceServerConfig` 将复用新的内部 scope 规则，`internal-only` 接口列表由 `InternalOnlyUrlProperties` 维护，确保内部 JWT 校验落地。
    - `common-redis-cache` 模块负责 Token 缓存实现（全部走 Redis，必要时由 Redis Cluster/哨兵保证 HA；本地缓存作为可选优化需另行评审）。
    - `AuthorizationServerConfig` 保持现有过滤链，后续由 token 发行流程新增 `OAuth2TokenCustomizer` 或 `RegisteredClient` 配置支撑内部场景。

### 推荐 Nacos 配置示例
```yaml
scccy:
  internal-token:
    enabled: true
    client-id: internal-service-client
    client-secret: InternalSecret123!
    token-url: lb://service-auth/oauth2/token   # 依赖 LoadBalanced WebClient，可直接写服务名
    scope: internal-service
    cache-expire-seconds: 540                   # 建议略短于真实 token TTL
    refresh-ahead-seconds: 60                   # 提前 60 秒刷新，避免抖动
    audience: service-system                    # 目标服务标识，可按需分环境覆盖
    grant-type: client_credentials              # 内部服务默认使用 client_credentials
```
- `cache-expire-seconds`、`refresh-ahead-seconds` 控制本地缓存策略，可在高并发场景按需缩短。
- `audience`、`scope` 建议与 `service-auth` 注册信息一致，便于权限校验。
- 若需要多目标服务，可通过 `internal-token.clients[*]` 的方式扩展多套凭证。

## 风险与缓解
- **令牌泄露**：开启服务间 mTLS、缩短过期时间、定期轮换密钥。
- **性能影响**：通过 Redis 统一缓存与提前刷新策略，避免每次调用都走网络申请；观测命中率并设置过期预警。
- **兼容旧链路**：提供特性开关 `scccy.security.internal-jwt`，按服务或路由渐进开启，保留回退策略。

## 任务拆解（待确认）
| 模块 | 文件 | 方法/组件 | 实现功能 | 状态 |
| --- | --- | --- | --- | --- |
| common/common-redis-cache | `InternalTokenCache`（新建组件） | `getToken`, `putToken`, `evict` | 承载内部 token 的 Redis 缓存读写，支持 TTL、刷新前置逻辑、降级策略 | 已完成（接口+Redis 实现） |
| service/service-auth | `AuthorizationServerConfig` + 新增 `InternalClientConfig` | `OAuth2TokenCustomizer`、内部 `RegisteredClient` 配置 | 注册内部 client，生成包含 audience/scope 的 JWT，暴露 JWKS | 部分完成（client_credentials 模式 claims 已区分内部服务） |
| gateway 或公共 starter | `AuthTokenService`（新建） | `getServiceToken`, `refreshIfNeeded` | 统一封装 token 申请、缓存与刷新逻辑 | 已完成（Redis 缓存 + 自动刷新） |
| gateway 或公共 starter | `FeignAuthRequestInterceptor` | `apply` | 在 Feign 请求前自动写入 `Authorization: Bearer <token>` | 已完成（默认拦截器注入 Authorization） |
| common/common-base | `ResourceServerConfig` | `resourceServerSecurityFilterChain` | 基于 `InternalOnlyUrlProperties` 判定内部接口 scope，并记录审计日志 | 待确认 |
| docs / nacos_config_export | `internal-feign-jwt-plan.md`, `service-*.yaml` | 配置模板与监控脚本 | 提供 Nacos 配置模板、Grafana/Prometheus 监控指标与告警示例 | 待确认 |
| service-system ↔ service-auth | 联调脚本/测试用例 | `@SpringBootTest`/链路联调 | 验证 token 申请、缓存命中、scope 校验的端到端闭环 | 待确认 |

