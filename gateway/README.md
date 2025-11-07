# Gateway 网关服务

## 概述

基于 Spring Cloud Gateway 的 API 网关服务，提供统一的路由、认证、限流等网关功能。作为系统的统一入口，负责请求路由、Token 验证、用户信息传递等核心功能。

## 主要功能

- **统一路由**: 基于 Spring Cloud Gateway 的响应式路由功能
- **OAuth2 认证**: 作为 OAuth2 Resource Server，统一验证 JWT Token
- **用户信息传递**: 从 Token 中提取用户信息，通过请求头传递给后端服务
- **服务发现**: 集成 Nacos 服务发现，自动路由到注册的微服务
- **负载均衡**: 使用 Spring Cloud LoadBalancer 进行服务负载均衡
- **API 文档聚合**: 集成 Knife4j Gateway，聚合各微服务的 API 文档
- **统一错误响应**: 提供统一的 JSON 格式错误响应

## 核心组件

### 配置类

- `ResourceServerConfig`: OAuth2 Resource Server 配置，使用 WebFlux 安全配置
- `JsonServerAuthenticationEntryPoint`: 统一 JSON 格式认证错误响应

### 过滤器

- `UserInfoGatewayFilter`: 用户信息网关过滤器，从 Token 提取用户信息并添加到请求头

### 启动类

- `GatewayApplication`: 网关服务启动类

## 技术架构

### 响应式编程

Gateway 基于 Spring WebFlux（响应式编程模型），使用 Reactor 框架，提供高性能的非阻塞 I/O 处理能力。

### 安全认证流程

1. 客户端请求携带 JWT Token（Authorization Header）
2. Gateway 验证 Token 的有效性（通过 JWK Set）
3. 从 Token 中提取用户信息（userId、username、authorities）
4. 将用户信息添加到请求头，传递给后端服务
5. 后端服务从请求头获取用户信息，无需再次解析 Token

### 路由配置

路由配置通过 Nacos 配置中心管理，支持动态更新路由规则，无需重启服务。

## 配置说明

### 基础配置

- **端口**: 默认 30000
- **服务名**: gateway
- **配置中心**: Nacos
- **服务注册**: Nacos Discovery

### OAuth2 配置

- **Issuer URI**: OAuth2 授权服务器的地址
- **JWK Set URI**: 用于验证 Token 的公钥端点

### 公开端点

以下端点无需认证即可访问：

- OAuth2 相关端点（`/oauth2/**`）
- 登录端点（`/login`）
- 健康检查（`/actuator/health`）
- API 文档（`/doc.html`、`/v3/api-docs/**`）
- 包含 `/public` 的路径

### 用户信息请求头

Gateway 会将以下用户信息添加到请求头，传递给后端服务：

- `X-User-Id`: 用户ID
- `X-Username`: 用户名
- `X-Authorities`: 用户权限（逗号分隔）

## 依赖说明

### 核心依赖

- Spring Cloud Gateway: 网关核心框架
- Spring Security OAuth2 Resource Server: OAuth2 认证支持
- Nacos Discovery: 服务发现
- Nacos Config: 配置中心
- Spring Cloud LoadBalancer: 负载均衡
- Knife4j Gateway: API 文档聚合

### Common 模块依赖

- `common-log`: 日志和链路追踪
- `common-modules`: 通用工具类（JwtUtils 等）

## 使用方式

### 启动服务

启动 Gateway 服务后，所有微服务的请求都通过 Gateway 进行路由。

### 路由规则

路由规则在 Nacos 配置中心的 `gateway.yaml` 中配置，支持：

- 路径匹配
- 服务发现路由
- 负载均衡
- 重试机制

### API 文档访问

启动 Gateway 后，访问 `/doc.html` 可以查看聚合的 API 文档。

## 注意事项

1. **响应式编程**: Gateway 使用 WebFlux，不支持传统的 Servlet API
2. **无数据库**: Gateway 不连接数据库，排除 MyBatis Plus 等数据库相关依赖
3. **Token 验证**: Gateway 统一验证 Token，后端服务无需再次验证
4. **用户信息传递**: 后端服务通过请求头获取用户信息，而不是从 Token 解析
5. **CSRF 禁用**: Gateway 通常禁用 CSRF 保护（前后端分离架构）

## 版本要求

- Java 21+
- Spring Boot 3.5.5
- Spring Cloud 2025.0.0.0
- Nacos 3.0

## 性能优化

- 使用响应式编程模型，提供高并发处理能力
- 支持懒加载初始化（开发环境）
- 集成链路追踪，便于性能监控和问题排查
