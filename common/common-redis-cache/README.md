# Common Redis Cache 模块

## 概述

基于 JetCache 的多级缓存模块，提供本地缓存（Caffeine）和远程缓存（Redis）的统一管理，支持多级缓存策略。

## 主要功能

- **多级缓存**: 支持本地缓存（Caffeine）和远程缓存（Redis）两级缓存
- **缓存区域**: 提供默认缓存区域（default、longTime、shortTime）
- **自动配置**: 自动配置 JetCache，支持通过 Nacos 配置中心管理配置
- **高性能**: 本地缓存使用 Caffeine，提供高性能的本地缓存能力
- **分布式缓存**: 远程缓存使用 Redis，支持分布式环境下的缓存共享

## 核心组件

### 配置类

- `JetCacheConfig`: JetCache 自动配置类

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

## 配置说明

JetCache 配置已迁移到 Nacos 配置中心，通过 `jetcache` 配置前缀进行配置：

- `jetcache.local.*`: 本地缓存配置
- `jetcache.remote.*`: 远程缓存配置
- `jetcache.statIntervalMinutes`: 统计间隔（分钟）
- `jetcache.hidePackages`: 隐藏的包名

## 使用方式

在服务类方法上使用 `@Cached` 注解即可启用缓存功能，支持指定缓存区域和过期时间。

## 依赖说明

- JetCache Starter Redis Lettuce
- Spring Boot Auto Configuration

## 版本要求

- Java 21+
- Spring Boot 3.x
- JetCache 2.7.7+

