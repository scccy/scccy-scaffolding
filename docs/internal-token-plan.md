## 内部令牌实现计划

- **目标**：实现服务间调用时的内部令牌获取、缓存、注入与校验。

| 步骤 | 目标/说明 | 关键内容 | 成果物 | 状态 |
| --- | --- | --- | --- | --- |
| 1 | 在认证服务中注册用于服务间调用的 OAuth2 客户端 | • 确定客户端类型（建议 client_credentials）<br>• 配置 scope 如 `internal-service`<br>• 设置访问权限（可访问用户查询等接口）<br>• 记录 clientId/clientSecret | Nacos 或数据库中的客户端配置；客户端凭证说明 | 已完成（2025-11-13：注册 `internal-service-client`，secret `InternalSecret123!`，grant `client_credentials`，scope `internal-service`） |
| 2 | 实现 TokenManager，负责获取与刷新内部 access_token | • 封装调用 `/oauth2/token`（client_credentials）的逻辑<br>• 使用 JetCache（依赖 `common-redis-cache`）缓存 token，过期时间略短于真实有效期<br>• 支持刷新、失败重试、熔断等策略 | `TokenManager` 类/组件，包含缓存配置、日志与错误处理 | 已完成（2025-11-13：实现 `InternalTokenManager` 和 `InternalTokenProperties`，支持缓存、自动刷新、异常处理） |
| 3 | 编写 Feign 请求拦截器，自动为内部调用添加 Authorization 头 | • 实现 `RequestInterceptor`，从 `TokenManager` 获取 token<br>• 将 `Authorization: Bearer <token>` 注入所有内部 Feign 请求<br>• 支持对部分客户端禁用该拦截器（如外部调用） | 可复用的 Feign 拦截器，配置开关说明 | 已完成（2025-11-14：新增 `InternalTokenFeignRequestInterceptor`，支持 `scccy.internal-token.feign.skip-clients` 和 `@SkipInternalToken` 关闭注入） |
| 4 | 调整下游服务的资源服务器配置，校验内部令牌的 scope/claims | • 在 `ResourceServerConfig` 中对内部接口设置 scope 验证（如 `hasAuthority("SCOPE_internal-service")`）<br>• 必要时与用户 token 区分处理（例如内部接口只允许内部 scope）<br>• 在 system 等服务的关键接口上加权限控制 | 更新后的安全配置代码、scope 说明 | 已完成（2025-11-14：新增 `@InternalOnly` 注解与 `InternalOnlyUrlProperties`，资源服务器自动对匹配路径要求 `SCOPE_<scope>`） |
| 5 | 补充运维文档，说明配置项、令牌管理与异常处理方式 | • 如何配置内部客户端、如何轮换 clientSecret<br>• Token 缓存命中率、日志监控指标、异常告警方案<br>• 灰度/回滚方案（如缓存失效的应急处理） | 运维手册/FAQ Checklist | 待办 |

- **依赖**：`common-redis-cache`（JetCache 多级缓存）、Spring Authorization Server、Feign。
- **测试建议**：
  1. 单元测试：`TokenManager` 缓存与刷新逻辑、Feign 拦截器是否正确注入头部。
  2. 集成测试：模拟 `auth → system` 调用流程，验证 scope 校验。
  3. 压测/容错：模拟拦截器获取 token 失败时的降级策略、缓存过期后自动刷新。
- **配置提示**：`scccy.internal-token.token-url` 推荐使用 `lb://service-auth/oauth2/token` 这类服务发现地址，搭配 LoadBalanced WebClient，可避免在不同环境手动维护 host:port。
- **稳定性**：`InternalTokenManager` 已加入互斥刷新与次数受限的重试机制；当 `/oauth2/token` 连续失败超过 3 次会直接抛出异常，日志中会包含完整响应与堆栈，便于排查。
