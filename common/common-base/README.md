# Common Base 模块

## 概述

Spring 基础配置模块，提供 Spring Boot 应用的基础配置和依赖管理，包含安全配置、数据源配置、Web MVC 配置等核心功能。

## 主要功能

- **统一启动注解**: 提供 `@ScccyServiceApplication` 注解，简化微服务启动类配置
- **安全配置**: 集成 Spring Security OAuth2 Resource Server，提供资源服务器配置
- **数据源配置**: 集成 MyBatis Plus，提供数据库访问能力
- **Redis 配置**: 提供 Redis 连接和配置
- **Web MVC 配置**: 统一配置跨域、参数解析器等
- **全局异常处理**: 提供统一的异常处理机制
- **HTTP 客户端**: 集成 OkHttp3，提供 HTTP 请求能力
- **OpenFeign 配置**: 提供微服务间调用的 Feign 客户端配置
- **权限服务**: 提供统一的权限验证接口

## 核心组件

### 配置类

- `ResourceServerConfig`: OAuth2 资源服务器配置
- `WebMvcConfig`: Web MVC 全局配置
- `MyBatisPlusConfig`: MyBatis Plus 配置
- `RedisConfig`: Redis 连接配置
- `OkHttpConfig`: OkHttp3 客户端配置
- `OpenFeignConfig`: OpenFeign 客户端配置
- `PasswordEncoderConfig`: 密码编码器配置
- `DataSourceConfig`: 数据源配置
- `TransactionConfig`: 事务配置

### 安全配置开关

- `spring.security.oauth2.resourceserver.jwt.issuer-uri`：配置后启用标准资源服务器过滤链。
- `scccy.security.permit-all`：显式设为 `true` 时启用开发期全放行链，适合无鉴权的临时联调；默认 `false`。
- 两者互斥：当 `permit-all=true` 时不会装配资源服务器链；当配置了 `issuer-uri` 且 `permit-all!=true` 时只启用资源服务器链，以避免重复匹配 `any request` 导致的 `UnreachableFilterChainException`。

### 注解

- `@ScccyServiceApplication`: 微服务启动类统一注解，自动配置扫描路径和组件

### 服务接口

- `PermissionService`: 权限验证服务接口

### 工具类

- `OkHttpManager`: OkHttp3 HTTP 客户端管理器
- `CurrentUserArgumentResolver`: 当前用户参数解析器
- `CurrentUserAspect`: 当前用户切面

### 异常处理

- `GlobalExceptionHandler`: 全局异常处理器

## 使用方式

在微服务启动类上使用 `@ScccyServiceApplication` 注解即可自动配置所有必要的组件和扫描路径。

## 依赖说明

- Spring Boot Web Starter
- Spring Security OAuth2 Resource Server
- MyBatis Plus
- Spring Data Redis
- OpenFeign
- OkHttp3
- FastJSON2

## 版本要求

- Java 21+
- Spring Boot 3.5.5
