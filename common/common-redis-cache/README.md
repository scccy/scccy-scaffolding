# Common Redis Cache 模块

## 概述

基于 JetCache 的多级缓存模块，提供本地缓存（Caffeine）和远程缓存（Redis）的统一管理，支持多级缓存策略。**引入该模块后自动加载默认配置，无需在每个服务的 Nacos 配置中重复配置**。

## 主要功能

- **多级缓存**: 支持本地缓存（Caffeine）和远程缓存（Redis）两级缓存
- **缓存区域**: 提供默认缓存区域（default、longTime、shortTime）
- **自动配置**: 自动加载默认配置，支持通过 Nacos 配置中心覆盖配置
- **智能 Redis URI**: 自动从环境变量或 Spring Redis 配置获取 Redis 连接信息
- **高性能**: 本地缓存使用 Caffeine，提供高性能的本地缓存能力
- **分布式缓存**: 远程缓存使用 Redis，支持分布式环境下的缓存共享

## 核心组件

### 配置类

- `JetCacheEnvironmentPostProcessor`: 环境后处理器，在环境准备阶段设置默认配置

### 缓存区域

- `DefaultCacheArea`: 默认缓存区域常量
  - `LONG_TIME_AREA`: 长时间缓存区域
  - `SHORT_TIME_AREA`: 短时间缓存区域

## 缓存区域说明

### 本地缓存

- `default`: 默认本地缓存，过期时间 5 分钟
- `longTime`: 长时间本地缓存，过期时间 1 小时
- `shortTime`: 短时间本地缓存，过期时间 1 分钟

### 远程缓存（Redis）

- `default`: 默认远程缓存，过期时间 2 小时
- `longTime`: 长时间远程缓存，过期时间 12 小时
- `shortTime`: 短时间远程缓存，过期时间 5 分钟

## 默认配置机制

### 自动加载默认配置

引入 `common-redis-cache` 模块后，会自动加载默认配置，**无需在 Nacos 配置中心配置完整的 JetCache 配置**。

### 配置覆盖优先级

配置覆盖优先级（从高到低）：

1. **Nacos 配置中心的配置**（最高优先级）
2. **应用本地 application.yml 配置**
3. **环境变量配置**（REDIS_HOST、REDIS_PORT 等）
4. **Spring Redis 配置**（spring.redis.host、spring.redis.port 等）
5. **默认配置**（最低优先级）

### Redis URI 自动构建

如果未在 Nacos 中配置 `jetcache.remote.*.uri`，系统会自动从以下来源获取 Redis 连接信息：

1. **环境变量**：`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`、`REDIS_DATABASE`
2. **Spring Redis 配置**：`spring.redis.host`、`spring.redis.port`、`spring.redis.password`、`spring.redis.database`
3. **默认值**：`localhost:6379`，无密码，数据库 0

### 使用方式

#### 方式一：完全使用默认配置（推荐）

只需要在 Nacos 中配置 Redis 连接信息（如果使用 Spring Redis 配置，则无需额外配置）：

```yaml
spring:
  redis:
    host: ${REDIS_HOST:117.50.197.170}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
```

或者使用环境变量：
- `REDIS_HOST`: Redis 主机地址
- `REDIS_PORT`: Redis 端口
- `REDIS_PASSWORD`: Redis 密码（可选）
- `REDIS_DATABASE`: Redis 数据库索引

#### 方式二：在 Nacos 中覆盖部分配置

如果只需要覆盖部分配置（如过期时间），只需要配置需要覆盖的项：

```yaml
jetcache:
  remote:
    default:
      expireAfterWriteInMillis: 3600000  # 覆盖默认的 2 小时为 1 小时
```

#### 方式三：在 Nacos 中配置完整的 JetCache 配置

如果需要完全自定义配置，可以在 Nacos 中配置完整的 JetCache 配置，此时会完全覆盖默认配置。

## 使用方式

在服务类方法上使用 `@Cached` 注解即可启用缓存功能，支持指定缓存区域和过期时间。

## 依赖说明

- JetCache Starter Redis Lettuce
- Spring Boot Auto Configuration

## 版本要求

- Java 21+
- Spring Boot 3.5.5
- JetCache 2.7.7+

## 注意事项

1. **配置优先级**: Nacos 配置中心的配置优先级最高，可以覆盖所有默认配置
2. **Redis URI**: 如果 Nacos 中已配置完整的 `jetcache.remote.*.uri`，则不会自动构建 URI
3. **环境变量**: 支持通过环境变量配置 Redis 连接信息，适合容器化部署
4. **Spring Redis 配置**: 如果项目中已配置 Spring Redis，会自动使用 Spring Redis 的配置构建 JetCache Redis URI

