# Common 通用模块集合

## 概述

Common 模块集合提供了项目中的通用功能模块，包含基础配置、工具类、日志、缓存、文档等通用组件，供各个微服务模块复用。

## 模块列表

### common-base
Spring 基础配置模块，提供 Spring Boot 应用的基础配置和依赖管理。

**主要功能：**
- 统一启动注解 `@ScccyServiceApplication`
- Spring Security OAuth2 资源服务器配置
- MyBatis Plus 数据源配置
- Redis 配置
- Web MVC 全局配置
- 全局异常处理
- HTTP 客户端（OkHttp3）
- OpenFeign 配置

### common-modules
通用工具模块，包含业务无关的通用工具类和组件。

**主要功能：**
- 通用工具方法
- 常量定义（如安全路径常量）
- JWT 工具类
- JSON 处理工具

### common-log
日志和链路追踪模块，提供统一的日志管理和分布式链路追踪功能。

**主要功能：**
- Log4j2 日志框架
- Micrometer Tracing 链路追踪
- Zipkin 集成
- 异步日志（Disruptor）

### common-excel
Excel 导入导出工具模块，基于 FastExcel 提供便捷的 Excel 操作功能。

**主要功能：**
- Excel 数据导出
- Excel 数据导入
- 支持从 `@Schema` 注解读取列名
- 批量数据处理

### common-knife4j
Knife4j API 文档自动配置模块，基于 SpringDoc OpenAPI3。

**主要功能：**
- OpenAPI3 自动配置
- Knife4j UI 集成
- 统一安全配置（Bearer Token）
- 网关集成支持

### common-redis-cache
基于 JetCache 的多级缓存模块，提供本地和远程缓存的统一管理。

**主要功能：**
- 多级缓存（本地 Caffeine + 远程 Redis）
- 多种缓存区域（default、longTime、shortTime）
- 自动配置支持
- 高性能缓存能力

### common-wechatwork
企业微信 SDK 封装模块，提供企业微信聊天内容存档功能。

**主要功能：**
- 聊天记录拉取
- 媒体消息拉取
- 数据解密
- SDK 管理

## 模块依赖关系

```
common-base
  ├── common-modules (基础工具)
  └── 其他模块可选择性依赖

common-log (独立模块)
common-excel (独立模块)
common-knife4j (独立模块)
common-redis-cache (独立模块)
common-wechatwork (独立模块)
```

## 使用方式

各微服务模块可以根据需要引入相应的 common 子模块：

- **基础服务模块**：通常需要引入 `common-base`
- **需要日志追踪**：引入 `common-log`
- **需要 Excel 功能**：引入 `common-excel`
- **需要 API 文档**：引入 `common-knife4j`
- **需要缓存功能**：引入 `common-redis-cache`
- **需要企业微信功能**：引入 `common-wechatwork`

## 版本要求

- Java 21+
- Spring Boot 3.5.5
- Maven 3.6+

## 构建说明

Common 模块集合使用 Maven 多模块结构，在根目录执行构建命令即可构建所有子模块。

## 模块文档

各子模块的详细文档请参考对应的 README 文件：
- [common-base/README.md](common-base/README.md)
- [common-modules/README.md](common-modules/README.md)
- [common-log/README.md](common-log/README.md)
- [common-excel/README.md](common-excel/README.md)
- [common-knife4j/README.md](common-knife4j/README.md)
- [common-redis-cache/README.md](common-redis-cache/README.md)
- [common-wechatwork/README.md](common-wechatwork/README.md)

