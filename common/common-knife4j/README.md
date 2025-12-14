# Common Knife4j 模块

## 概述

Knife4j API 文档自动配置模块，基于 SpringDoc OpenAPI3 和 Knife4j，提供 API 文档的自动配置功能。

## 主要功能

- **自动配置 OpenAPI3**: 自动配置 SpringDoc OpenAPI3 文档
- **Knife4j UI 集成**: 集成 Knife4j 文档界面
- **统一安全配置**: 自动配置 API Key 认证方式（Bearer Token）
- **网关集成**: 支持配置网关地址，统一文档访问入口
- **文档信息配置**: 支持配置文档标题、描述、版本、联系信息等

## 核心组件

### 配置类

- `SpringDocAutoConfiguration`: SpringDoc OpenAPI3 自动配置类

### 配置属性

- `SpringDocProperties`: SpringDoc 配置属性类，支持通过配置文件自定义文档信息

## 配置说明

通过 `springdoc` 配置前缀可以自定义文档信息：

- `springdoc.gateway-url`: 网关地址
- `springdoc.info.title`: 文档标题
- `springdoc.info.description`: 文档描述
- `springdoc.info.version`: API 版本
- `springdoc.info.contact`: 联系信息
- `springdoc.info.license`: 许可证信息

## 使用方式

引入该模块后，访问 `/doc.html` 即可查看 API 文档界面。

## 依赖说明

- Knife4j OpenAPI3 Jakarta Spring Boot Starter
- Spring Boot Auto Configuration

## 版本要求

- Java 21+
- Spring Boot 3.x

