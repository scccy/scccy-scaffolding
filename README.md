# scccy-scaffolding

`scccy-scaffolding` 是一套基于 Spring Boot 3.5 和 Spring Cloud 2025 的微服务脚手架，内置认证中心、系统服务、API 网关以及多种公共能力模块，适合作为 SaaS 或企业内部平台的基础工程。

## 技术栈概览
- Spring Boot 3.5.5 / Spring Cloud 2025.0.0 / Spring Cloud Alibaba 2025.0.0.0
- Spring Authorization Server、Spring Security OAuth2 Resource Server、JWT
- MyBatis-Plus、MySQL 8、JetCache/Redis
- Spring Cloud Gateway、OpenFeign、Nacos
- Knife4j、Micrometer、SkyWalking
- Docker Compose 基础设施（Nacos、Redis、Kafka、Seata、Flink 等）

## 模块说明

| 模块 | 说明 |
| --- | --- |
| `gateway/` | API 网关，统一路由、灰度、鉴权与文档聚合 |
| `service/service-auth/` | 认证授权中心，提供用户注册/登录、JWT 签发与黑名单、OAuth2 授权流程 |
| `service/service-system/` | 用户与系统管理基础服务，向 Auth 提供内部注册、用户信息、权限查询 |
| `service/service-demo/` | 业务示例服务，演示权限、缓存、日志等集成方式 |
| `service/service-feishu`、`service/service-wechatwrok`、`service/service-jackyun` | 外部平台集成示例 |
| `common/` | 公共依赖：日志、缓存、Knife4j、实体/DTO、基础配置等 |
| `core/core-flink/` | Flink 作业示例（公共组件与用户画像任务） |
| `docker/` | 基础设施 docker-compose（Nacos、Redis、Kafka、SkyWalking、Seata、Flink 等） |
| `docs/` | 架构说明与流程文档（如 JWT 流程、网关合规说明） |

## 基础设施
- 配置与注册中心：Nacos（`docker/nacos/docker-compose.yaml`）
- 缓存：Redis（`docker/redis/docker-compose.yaml`）
- 消息队列：Kafka（可选）
- 监控：SkyWalking、Micrometer
- 分布式事务：Seata（可选，含初始化脚本）
- 其他：Flink、MinIO 等按需选择

## 快速开始

### 1. 环境要求
- JDK 21
- Maven 3.9+
- Docker（用于快速启动基础设施）

### 2. 启动基础设施（可选）
```bash
cd docker/nacos && docker compose up -d
cd ../redis && docker compose up -d
# 需要时再启动 Kafka / SkyWalking / Seata / Flink
```

### 3. 导入配置
- Nacos 配置位于 `nacos_config_export/DEFAULT_GROUP/*.yaml`
- 根据环境修改数据库、Redis 等连接信息

### 4. 构建与启动
```bash
mvn clean install -Dmaven.test.skip

# 启动网关
mvn spring-boot:run -pl gateway -am

# 启动认证中心
mvn spring-boot:run -pl service/service-auth -am

# 启动系统服务
mvn spring-boot:run -pl service/service-system -am
```

### 5. 常用访问入口
- Knife4j 聚合文档：`http://{gateway-host}:{gateway-port}/doc.html`
- 用户认证接口：`http://{auth-host}:{auth-port}/api/user/**`
- OAuth2 授权端点：`/oauth2/**`

> 默认端口以 Nacos 与服务 `application.yml` 为准，可按需调整。

## 认证与授权说明
- `service-auth` 同时承担 Authorization Server 与用户认证中心：
  - `/api/user/register`、`/api/user/login` 返回包含 JWT 的 `LoginResponse`
  - `/api/user/logout` 将 Token 标记入黑名单（Redis）
  - `/oauth2/**` 提供客户端凭证、授权码、设备码等 OAuth2 流程
- `service-system` 内部接口通过 `@Anonymous` 标注，供 Auth 服务通过 Feign 调用完成注册与权限查询。
- Gateway 校验 JWT、同步黑名单，统一路由 `/api/user/**` 到 Auth，其他前缀到对应服务。

## 开发调试提示
- Base 模块提供 `DevPermitAllSecurityConfig`，在单服务联调场景可通过 `scccy.security.permit-all=true` 启用全局放行；生产请关闭并通过网关或资源服务器校验。
- Auth 与 System 已关闭公开端点 CSRF，确保注册、登录在前后端分离环境下可直接调用。
- `UserTokenGenerationService` 会尝试从 `RegisteredClientRepository` 查找默认用户客户端（例如 `default_user_client`）。如不存在，将使用内建默认配置（2 小时有效期）。

## 常用命令
```bash
# 全量构建
mvn clean install

# 启动指定服务
mvn spring-boot:run -pl service/service-auth -am

# 查看网关路由配置
cat nacos_config_export/DEFAULT_GROUP/gateway.yaml

# 校验 Gateway JWT（auth 模块提供脚本）
./service/service-auth/verify-oauth2-gateway.sh
```

## 目录导航速览
- `gateway/src/main`：网关过滤器、路由配置
- `service/service-auth/src/main/java/com/scccy/service/auth/config`：授权服务器 & 资源服务器安全链
- `service/service-system/src/main/java/com/scccy/service/system/controller/SysUserController.java`：系统用户接口（含 @Anonymous）
- `common/common-modules/src/main/java/com/scccy/common/modules`：实体、DTO、常量、注解
- `docs/`：JWT 流程、网关与授权中心协同策略等文档

## 后续建议
- 根据业务需求完善 `Oauth2RegisteredClient` 配置，确保普通用户客户端可被检索。
- 整合 CI/CD（例如 GitHub Actions、Nexus）完成自动化构建与工件发布。
- 结合 Docker Compose 或 Kubernetes 部署，实现生产级监控、日志与链路追踪。

如需更多细节，可参考 `docs/` 下的详细设计文档及各模块中自带的 README。欢迎在此脚手架基础上扩展业务能力。 

