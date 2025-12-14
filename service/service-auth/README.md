# Service-Auth 认证授权服务

## 概述

`service-auth` 是系统的认证授权中心，提供两种服务模式：**第三方客户端 OAuth2 授权服务**和**自有微服务用户登录服务**。作为系统的统一认证入口，负责用户认证、Token 生成、权限管理等核心功能。

## 核心功能

### 第三方客户端 OAuth2 授权服务
- OAuth2 授权码模式（Authorization Code Flow）
- 客户端凭证模式（Client Credentials Flow）
- 设备码授权模式（Device Code Flow）
- OpenID Connect 1.0 支持
- 客户端注册与管理
- 用户授权同意管理

### 自有微服务用户登录服务
- 用户注册与登录
- JWT Token 生成与验证
- Token 黑名单管理
- 用户信息获取

## 两种服务模式

### 模式一：第三方客户端（OAuth2 授权服务）

**适用场景**：第三方应用需要接入系统，需要用户授权同意的场景

**使用流程**：

1. **客户端注册**
   - 第三方应用通过管理接口注册客户端
   - 获取 `client_id` 和 `client_secret`
   - 配置授权类型、回调地址、授权范围等

2. **获取 Token**
   - 使用 OAuth2 标准流程获取 Token
   - 支持授权码模式、客户端凭证模式等
   - Token 由 Spring Authorization Server 生成

3. **Token 使用**
   - Token 包含客户端信息、授权范围、用户信息
   - Token 过期时间由客户端注册时配置
   - 支持 Token 刷新机制

**数据存储**：
- 客户端信息存储在 `oauth2_registered_client` 表
- 授权记录和 Token 存储在 `oauth2_authorization` 表
- 用户授权同意记录存储在 `oauth2_authorization_consent` 表

### 模式二：自有微服务（用户登录服务）

**适用场景**：自有微服务、前端应用直接登录，无需 OAuth2 复杂流程

**使用流程**：

1. **用户注册**
   - 用户通过注册接口创建账号
   - 系统调用 `service-system` 保存用户信息

2. **用户登录**
   - 用户提交用户名和密码
   - 系统验证用户信息（调用 `service-system`）
   - 生成 JWT Token 返回给用户

3. **Token 使用**
   - Token 包含用户完整信息（userId、username、nickName 等）
   - Token 过期时间由全局配置决定
   - 支持 Token 黑名单机制（登出功能）

**数据存储**：
- 用户信息存储在 `service-system` 服务的用户表
- Token 黑名单存储在 Redis（通过 JetCache）

## 与 Service-System 的联动

`service-auth` 通过 Feign 客户端与 `service-system` 服务进行数据交互，实现用户信息的查询和管理。

### 数据交互接口

1. **用户信息查询**
   - 根据用户名获取用户详细信息
   - 用于登录验证和 Token 生成

2. **用户注册**
   - 创建新用户账号
   - 密码加密后存储到 `service-system`

3. **用户权限查询**
   - 获取用户的角色和菜单权限
   - 用于 Token 中权限信息的填充

### 数据流转

#### OAuth2 模式下的数据流

```
用户访问授权端点
    ↓
service-auth 验证客户端信息
    ↓ (查询 oauth2_registered_client 表)
用户登录/授权
    ↓
service-auth 生成 Token
    ↓ (调用 SystemUserClient.getByUserName)
service-system 返回用户信息
    ↓
service-auth 将用户信息添加到 Token Claims
    ↓ (调用 SystemUserClient.getUserAuthorities)
service-system 返回用户权限列表
    ↓
service-auth 将权限添加到 Token Claims
    ↓
生成完整的 JWT Token
    ↓
返回给客户端
```

#### 用户登录模式下的数据流

```
用户提交登录请求
    ↓
service-auth 接收登录信息
    ↓ (调用 SystemUserClient.getByUserName)
service-system 返回用户信息
    ↓
service-auth 验证密码和用户状态
    ↓
验证通过后生成 JWT Token
    ↓ (调用 SystemUserClient.getUserAuthorities)
service-system 返回用户权限列表
    ↓
service-auth 将用户信息和权限添加到 Token
    ↓
生成完整的 JWT Token
    ↓
返回给用户
```

#### 用户注册模式下的数据流

```
用户提交注册请求
    ↓
service-auth 接收注册信息
    ↓ (调用 SystemUserClient.register)
service-system 创建用户记录
    ↓
service-system 返回新创建的用户信息
    ↓
service-auth 生成初始 JWT Token
    ↓
返回 Token 和用户信息
```

## 数据流详解

### 1. OAuth2 Token 生成流程

**触发时机**：客户端通过 OAuth2 流程获取 Token 时

**数据流**：
1. Spring Authorization Server 开始生成 Token
2. Token 自定义器被调用
3. 从认证上下文中提取用户名
4. 通过 Feign 调用 `service-system` 获取用户详细信息
5. 通过 Feign 调用 `service-system` 获取用户权限列表
6. 将用户信息和权限添加到 Token Claims
7. Spring Authorization Server 使用 JWK 密钥对签名 Token
8. 将 Token 存储到 `oauth2_authorization` 表
9. 返回 Token 给客户端

**关键数据**：
- 用户信息：userId、username、nickName、status
- 权限信息：角色标识（ROLE_*）、菜单权限（system:*:*）
- 客户端信息：client_id、授权范围（scopes）

### 2. 用户登录 Token 生成流程

**触发时机**：用户通过 `/api/user/login` 接口登录时

**数据流**：
1. 接收用户名和密码
2. 通过 Feign 调用 `service-system` 获取用户信息
3. 验证用户状态（是否启用、是否删除）
4. 验证密码（BCrypt 加密验证）
5. 通过 Feign 调用 `service-system` 获取用户权限
6. 使用 JwtUtils 生成 JWT Token
7. 将用户信息和权限添加到 Token Claims
8. Token 签名（使用 JwtConfig 配置的密钥）
9. 返回 Token 给用户

**关键数据**：
- 用户信息：userId、username、nickName、status、email、phone
- 权限信息：角色和菜单权限列表
- Token 元数据：过期时间、签发时间等

### 3. 用户信息获取流程（OAuth2 UserInfo 端点）

**触发时机**：客户端使用 Access Token 获取用户信息时

**数据流**：
1. 客户端携带 Access Token 请求 `/oauth2/userinfo`
2. Gateway 验证 Token 有效性
3. service-auth 解析 Token，提取用户名
4. 通过 Feign 调用 `service-system` 获取用户详细信息
5. 将用户信息映射为 OIDC UserInfo 格式
6. 返回标准化的用户信息

**关键数据**：
- 用户标识：sub（用户名）
- 用户属性：name（昵称）等

### 4. Token 验证流程（Gateway）

**触发时机**：客户端携带 Token 访问受保护资源时

**数据流**：
1. Gateway 接收请求，提取 Token
2. Gateway 验证 Token 签名（通过 JWK Set）
3. Gateway 验证 Token 过期时间
4. Gateway 从 Token 中提取用户信息
5. Gateway 将用户信息添加到请求头
6. 转发请求到后端服务
7. 后端服务从请求头获取用户信息

**关键数据**：
- 请求头：X-User-Id、X-Username、X-Authorities

## 数据存储

### service-auth 本地存储

- **oauth2_registered_client**：客户端注册信息
- **oauth2_authorization**：授权记录和 Token（加密存储）
- **oauth2_authorization_consent**：用户授权同意记录

### service-system 存储

- **用户表**：用户基本信息、密码（BCrypt 加密）
- **角色表**：角色定义
- **菜单表**：菜单和权限定义
- **用户角色关联表**：用户与角色的关系
- **角色菜单关联表**：角色与菜单权限的关系

### Redis 存储（通过 JetCache）

- **Token 黑名单**：用户登出后的 Token 黑名单
- **JWK Set 缓存**：用于 Token 签名验证的密钥对

## 安全机制

### Token 安全

- **JWT 签名**：使用 RSA 密钥对签名，防止 Token 被篡改
- **Token 过期**：支持 Token 过期时间配置
- **Token 黑名单**：支持主动登出，将 Token 加入黑名单
- **密钥管理**：JWK Set 自动生成和管理

### 用户认证安全

- **密码加密**：使用 BCrypt 加密存储（强度 12）
- **用户状态验证**：验证用户是否启用、是否删除
- **权限验证**：Token 中包含用户权限，后端服务可进行权限控制

### 客户端认证安全

- **客户端密钥**：使用 BCrypt 加密存储
- **客户端认证方法**：支持多种认证方式（client_secret_basic 等）
- **授权范围控制**：限制客户端可访问的资源范围

## 使用方式

### 第三方客户端接入

1. 通过管理接口注册客户端，获取 `client_id` 和 `client_secret`
2. 根据业务需求选择合适的授权类型（授权码、客户端凭证等）
3. 实现 OAuth2 标准流程获取 Token
4. 使用 Token 访问受保护的资源

### 自有微服务接入

1. 用户通过注册接口创建账号
2. 用户通过登录接口获取 JWT Token
3. 在请求头中携带 Token 访问受保护的资源
4. 后端服务从请求头获取用户信息进行业务处理

## 版本要求

- Java 21+
- Spring Boot 3.5.5
- Spring Cloud 2025.0.0
- Spring Authorization Server
- Nacos 3.0

## 依赖服务

- **service-system**：用户信息管理、权限管理
- **Redis**：Token 黑名单、JWK Set 缓存
- **MySQL**：OAuth2 相关数据存储
- **Gateway**：统一 Token 验证和路由
