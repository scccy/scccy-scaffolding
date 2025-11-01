# Service-Auth 模块架构与数据流转说明

## 1. 模块概述

`service-auth` 是一个基于 **Spring Authorization Server** 的 OAuth2 授权服务模块，实现了完整的 OAuth2 和 OpenID Connect (OIDC) 协议支持。

### 核心功能
- ✅ **OAuth2 授权码模式** (Authorization Code Flow)
- ✅ **客户端凭证模式** (Client Credentials Flow)
- ✅ **设备码授权模式** (Device Code Flow)
- ✅ **OpenID Connect 1.0** 支持
- ✅ **JWT Token** 生成与验证
- ✅ **用户授权同意管理**
- ✅ **客户端注册与配置**

### 技术栈
- Spring Boot 3.x
- Spring Security 6.x
- Spring Authorization Server
- MyBatis-Plus (数据持久化)
- MySQL (数据存储)
- Thymeleaf (页面模板)

---

## 2. 模块结构

```
service-auth/
├── config/                          # 配置类
│   └── AuthorizationServerConfig    # OAuth2 授权服务器配置
├── controller/                      # 控制器层
│   ├── AuthorizationController       # 授权相关页面控制器
│   ├── RegisteredClientController   # 客户端管理控制器
│   └── TokenController             # Token 相关控制器
├── oauth2/                          # OAuth2 核心组件
│   ├── device/                      # 设备码授权相关
│   │   ├── DeviceClientAuthenticationConverter
│   │   ├── DeviceClientAuthenticationProvider
│   │   └── DeviceClientAuthenticationToken
│   ├── handler/                     # 处理器
│   │   ├── Oauth2AccessDeniedHandler
│   │   ├── Oauth2DeviceSuccessHandler
│   │   └── Oauth2FailureHandler
│   ├── Oauth2AuthorizationConsentService  # 授权同意服务
│   └── Oauth2RegisteredClientRepository  # 客户端仓库
├── dao/                             # 数据访问层
│   ├── mapper/                      # MyBatis Mapper
│   └── service/                     # Service 层
├── domain/                          # 领域模型
│   ├── mp/                          # MyBatis-Plus 实体
│   ├── form/                        # 表单对象
│   └── vo/                          # 视图对象
└── fegin/                           # Feign 客户端
    ├── SystemUserClient             # 系统用户服务客户端
    └── SystemUserClientFallback    # 降级服务
```

---

## 3. 数据表结构

### 3.1 oauth2_registered_client (客户端注册表)

存储已注册的 OAuth2 客户端信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(100) | UUID，主键 |
| client_id | varchar(100) | 客户端ID，唯一索引 |
| client_secret | varchar(200) | 客户端密钥（BCrypt加密） |
| client_name | varchar(200) | 客户端名称 |
| authorization_grant_types | varchar(1000) | 支持的授权类型（逗号分隔） |
| client_authentication_methods | varchar(1000) | 客户端认证方法（逗号分隔） |
| redirect_uris | varchar(1000) | 重定向URI（逗号分隔） |
| scopes | varchar(1000) | 支持的授权范围（逗号分隔） |
| client_settings | text | 客户端配置（JSON） |
| token_settings | text | Token 配置（JSON） |

### 3.2 oauth2_authorization (授权记录表)

存储 OAuth2 授权过程中的所有 Token 和状态信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | varchar(100) | UUID，主键 |
| registered_client_id | varchar(100) | 客户端ID |
| principal_name | varchar(200) | 主体名称（用户名） |
| authorization_grant_type | varchar(100) | 授权类型 |
| authorized_scopes | varchar(1000) | 已授权的范围 |
| state | varchar(500) | 状态信息（CSRF防护） |
| authorization_code_value | blob | 授权码（加密存储） |
| access_token_value | blob | Access Token（JWT，加密存储） |
| refresh_token_value | blob | Refresh Token（加密存储） |
| oidc_id_token_value | blob | ID Token（JWT，加密存储） |
| device_code_value | blob | 设备码（加密存储） |
| user_code_value | blob | 用户码（加密存储） |

**注意**：所有 Token 相关字段使用 `blob` 类型存储加密后的二进制数据。

### 3.3 oauth2_authorization_consent (授权同意记录表)

存储用户的授权同意记录，用于判断用户是否已授权客户端访问特定资源。

| 字段 | 类型 | 说明 |
|------|------|------|
| registered_client_id | varchar(100) | 客户端ID（复合主键） |
| principal_name | varchar(200) | 主体名称（复合主键） |
| authorities | text | 已同意的授权范围（逗号分隔） |

---

## 4. 权限控制机制

### 4.1 安全过滤器链配置

`AuthorizationServerConfig` 配置了 OAuth2 授权服务器的安全过滤器链：

```java
SecurityFilterChain authorizationServerSecurityFilterChain(
    HttpSecurity httpSecurity,
    RegisteredClientRepository registeredClientRepository,
    AuthorizationServerSettings authorizationServerSettings
)
```

**关键配置点**：
1. **优先级最高** (`@Order(Ordered.HIGHEST_PRECEDENCE)`)：确保 OAuth2 过滤器链优先执行
2. **端点配置**：
   - `/oauth2/authorize` - 授权端点
   - `/oauth2/token` - Token 端点
   - `/oauth2/device_authorization` - 设备授权端点
   - `/oauth2/userinfo` - 用户信息端点
3. **客户端认证**：支持多种认证方式（client_secret_basic、client_secret_post、none等）
4. **设备码支持**：自定义 Converter 和 Provider 支持设备码授权流程

### 4.2 核心组件

#### 4.2.1 RegisteredClientRepository

**实现类**：`Oauth2RegisteredClientRepository`

**职责**：
- 从数据库查询客户端配置信息
- 将 `Oauth2RegisteredClientMp` 转换为 Spring Security 的 `RegisteredClient`
- 验证客户端是否存在及其有效性

**数据流转**：
```
数据库 (oauth2_registered_client)
    ↓ MyBatis-Plus
Oauth2RegisteredClientMp (实体)
    ↓ RegisteredClientConvert
RegisteredClient (Spring Security)
    ↓ OAuth2 授权流程
```

#### 4.2.2 OAuth2AuthorizationConsentService

**实现类**：`Oauth2AuthorizationConsentService`

**职责**：
- 保存用户的授权同意记录
- 查询用户是否已同意授权
- 删除授权同意记录

**数据流转**：
```
用户授权同意操作
    ↓
OAuth2AuthorizationConsent (Spring Security)
    ↓ AuthorizationConsentConvert
Oauth2AuthorizationConsentMp (实体)
    ↓ MyBatis-Plus
数据库 (oauth2_authorization_consent)
```

#### 4.2.3 UserInfoMapper

**职责**：将 JWT Token 中的用户信息映射到 OIDC UserInfo

**数据流转**：
```
JWT Token (JwtAuthenticationToken)
    ↓ 提取用户名
SystemUserClient.getByUserName()
    ↓ Feign 调用 service-system
SysUserMp (用户信息)
    ↓ 映射
OidcUserInfo (OIDC 用户信息)
```

---

## 5. 数据流转详解

### 5.1 授权码模式 (Authorization Code Flow)

```
┌──────────┐                ┌──────────┐                ┌──────────┐
│   用户   │                │ 客户端   │                │授权服务器│
│ (浏览器) │                │ 应用     │                │service-auth│
└────┬─────┘                └────┬─────┘                └────┬─────┘
     │                            │                            │
     │  1. 访问受保护资源         │                            │
     │──────────────────────────>│                            │
     │                            │                            │
     │  2. 重定向到授权端点       │                            │
     │<──────────────────────────│                            │
     │   GET /oauth2/authorize?  │                            │
     │   client_id=xxx&scope=xxx │                            │
     │                            │                            │
     │  3. 用户登录              │                            │
     │───────────────────────────────────────────────────────>│
     │   POST /login              │                            │
     │                            │                            │
     │  4. 显示授权确认页面       │                            │
     │<───────────────────────────────────────────────────────│
     │   GET /oauth2/consent      │                            │
     │   (查询授权同意记录)        │                            │
     │   oauth2_authorization_consent表                        │
     │                            │                            │
     │  5. 用户确认授权           │                            │
     │───────────────────────────────────────────────────────>│
     │   POST /oauth2/authorize   │                            │
     │                            │                            │
     │  6. 生成授权码并重定向     │                            │
     │<───────────────────────────────────────────────────────│
     │   重定向: redirect_uri?    │                            │
     │   code=xxx&state=xxx        │                            │
     │                            │                            │
     │                            │  7. 使用授权码换取Token    │
     │                            │───────────────────────────>│
     │                            │   POST /oauth2/token       │
     │                            │   grant_type=authorization_code│
     │                            │   code=xxx                 │
     │                            │                            │
     │                            │  8. 验证授权码并生成Token  │
     │                            │   (查询 oauth2_authorization表)│
     │                            │   (生成 JWT Token)         │
     │                            │                            │
     │                            │  9. 返回 Access Token      │
     │                            │<───────────────────────────│
     │                            │   {                        │
     │                            │     "access_token": "xxx", │
     │                            │     "refresh_token": "xxx",│
     │                            │     "expires_in": 7200     │
     │                            │   }                        │
     │                            │                            │
     │  10. 使用 Token 访问资源   │                            │
     │────────────────────────────────────────────────────────>│
     │   GET /api/resource        │                            │
     │   Authorization: Bearer xxx│                            │
     │                            │                            │
```

**关键数据库操作**：

1. **授权请求阶段**：
   - 查询 `oauth2_registered_client` 表验证客户端
   - 查询 `oauth2_authorization_consent` 表检查已授权范围

2. **授权码生成阶段**：
   - 插入/更新 `oauth2_authorization` 表，存储授权码信息

3. **Token 交换阶段**：
   - 查询 `oauth2_authorization` 表验证授权码
   - 生成 JWT Token（包含用户信息、客户端ID、授权范围等）
   - 更新 `oauth2_authorization` 表，存储 Token 信息

4. **授权同意保存**：
   - 用户确认授权后，保存/更新 `oauth2_authorization_consent` 表

---

### 5.2 设备码授权模式 (Device Code Flow)

```
┌──────────┐                ┌──────────┐                ┌──────────┐
│ 设备应用 │                │ 授权服务器│                │   用户   │
│          │                │service-auth│                │ (浏览器) │
└────┬─────┘                └────┬─────┘                └────┬─────┘
     │                            │                            │
     │  1. 请求设备码             │                            │
     │──────────────────────────>│                            │
     │   POST /oauth2/device_authorization│                   │
     │   client_id=xxx            │                            │
     │                            │                            │
     │  2. 返回设备码和用户码      │                            │
     │<───────────────────────────│                            │
     │   {                        │                            │
     │     "device_code": "xxx",  │                            │
     │     "user_code": "xxx",    │                            │
     │     "verification_uri":    │                            │
     │       "/oauth2/activate",  │                            │
     │     "expires_in": 600      │                            │
     │   }                        │                            │
     │                            │                            │
     │                            │  3. 显示用户码给用户       │
     │                            │───────────────────────────>│
     │                            │   用户码: xxx              │
     │                            │   访问: /oauth2/activate   │
     │                            │                            │
     │                            │  4. 用户输入用户码验证      │
     │                            │<───────────────────────────│
     │                            │   GET /oauth2/activate     │
     │                            │   ?user_code=xxx           │
     │                            │                            │
     │                            │  5. 显示授权确认页面       │
     │                            │───────────────────────────>│
     │                            │   GET /oauth2/consent       │
     │                            │                            │
     │                            │  6. 用户确认授权           │
     │                            │<───────────────────────────│
     │                            │   POST /oauth2/device_verification│
     │                            │                            │
     │  7. 轮询请求 Token         │                            │
     │───────────────────────────>│                            │
     │   POST /oauth2/token       │                            │
     │   grant_type=device_code   │                            │
     │   device_code=xxx          │                            │
     │                            │                            │
     │   (可能需要多次轮询，直到   │                            │
     │    用户完成授权)           │                            │
     │                            │                            │
     │  8. 返回 Access Token      │                            │
     │<───────────────────────────│                            │
     │   {                        │                            │
     │     "access_token": "xxx", │                            │
     │     "refresh_token": "xxx" │                            │
     │   }                        │                            │
```

**关键数据库操作**：

1. **设备码生成阶段**：
   - 验证客户端（查询 `oauth2_registered_client` 表）
   - 插入 `oauth2_authorization` 表，存储 `device_code` 和 `user_code`

2. **用户验证阶段**：
   - 用户访问 `/oauth2/activate`，输入 `user_code`
   - 查询 `oauth2_authorization` 表验证 `user_code`
   - 显示授权确认页面

3. **授权完成阶段**：
   - 用户确认授权后，更新 `oauth2_authorization` 表
   - 保存授权同意记录到 `oauth2_authorization_consent` 表

4. **Token 获取阶段**：
   - 设备应用轮询 `/oauth2/token` 端点
   - 验证 `device_code`（查询 `oauth2_authorization` 表）
   - 生成 JWT Token 并返回

---

### 5.3 客户端凭证模式 (Client Credentials Flow)

```
┌──────────┐                ┌──────────┐
│ 客户端   │                │授权服务器│
│ 应用     │                │service-auth│
└────┬─────┘                └────┬─────┘
     │                            │
     │  1. 使用客户端凭证获取Token │
     │───────────────────────────>│
     │   POST /oauth2/token       │
     │   grant_type=client_credentials│
     │   client_id=xxx            │
     │   client_secret=xxx         │
     │                            │
     │  2. 验证客户端凭证         │
     │   (查询 oauth2_registered_client表)│
     │                            │
     │  3. 生成 Token             │
     │   (生成 JWT Token)         │
     │   (存储到 oauth2_authorization表)│
     │                            │
     │  4. 返回 Access Token      │
     │<───────────────────────────│
     │   {                        │
     │     "access_token": "xxx", │
     │     "token_type": "Bearer", │
     │     "expires_in": 7200     │
     │   }                        │
     │                            │
```

**关键数据库操作**：

1. **客户端认证**：
   - 查询 `oauth2_registered_client` 表验证 `client_id` 和 `client_secret`
   - 验证客户端支持的 `grant_type` 是否包含 `client_credentials`

2. **Token 生成**：
   - 生成 JWT Token（不包含用户信息，只包含客户端ID和授权范围）
   - 插入 `oauth2_authorization` 表存储 Token 信息

---

### 5.4 Token 刷新流程 (Refresh Token Flow)

```
┌──────────┐                ┌──────────┐
│ 客户端   │                │授权服务器│
│ 应用     │                │service-auth│
└────┬─────┘                └────┬─────┘
     │                            │
     │  1. 使用 Refresh Token     │
     │───────────────────────────>│
     │   POST /oauth2/token       │
     │   grant_type=refresh_token │
     │   refresh_token=xxx        │
     │                            │
     │  2. 验证 Refresh Token     │
     │   (查询 oauth2_authorization表)│
     │   (检查 Token 是否过期)     │
     │                            │
     │  3. 生成新的 Access Token  │
     │   (可选：生成新的 Refresh Token)│
     │   (更新 oauth2_authorization表)│
     │                            │
     │  4. 返回新的 Token         │
     │<───────────────────────────│
     │   {                        │
     │     "access_token": "new_xxx",│
     │     "refresh_token": "new_xxx",│
     │     "expires_in": 7200     │
     │   }                        │
     │                            │
```

**关键数据库操作**：

1. **Refresh Token 验证**：
   - 查询 `oauth2_authorization` 表验证 `refresh_token`
   - 检查 Token 是否过期

2. **新 Token 生成**：
   - 生成新的 JWT Token
   - 更新 `oauth2_authorization` 表，替换旧的 Token

---

### 5.5 用户信息获取流程 (OpenID Connect UserInfo)

```
┌──────────┐                ┌──────────┐                ┌──────────┐
│ 客户端   │                │授权服务器│                │ 用户服务 │
│ 应用     │                │service-auth│                │service-system│
└────┬─────┘                └────┬─────┘                └────┬─────┘
     │                            │                            │
     │  1. 使用 Access Token 获取用户信息│                     │
     │───────────────────────────>│                            │
     │   GET /oauth2/userinfo      │                            │
     │   Authorization: Bearer xxx│                            │
     │                            │                            │
     │  2. 验证 JWT Token          │                            │
     │   (解析 Token，提取用户信息)│                            │
     │                            │                            │
     │  3. 调用用户服务获取详细信息│                            │
     │                            │───────────────────────────>│
     │                            │   Feign: SystemUserClient  │
     │                            │   .getByUserName(username)│
     │                            │                            │
     │                            │  4. 返回用户详细信息       │
     │                            │<───────────────────────────│
     │                            │   SysUserMp                │
     │                            │                            │
     │  5. 映射为用户信息并返回    │                            │
     │<───────────────────────────│                            │
     │   {                        │                            │
     │     "sub": "username",     │                            │
     │     "name": "nickname"     │                            │
     │   }                        │                            │
     │                            │                            │
```

**关键数据流转**：

1. **Token 验证**：
   - 验证 JWT Token 签名和有效期
   - 从 Token 中提取用户名（principal name）

2. **用户信息查询**：
   - 通过 Feign 客户端调用 `service-system` 服务
   - 查询用户详细信息（`SysUserMp`）

3. **信息映射**：
   - 使用 `userInfoMapper` 将 `SysUserMp` 映射为 `OidcUserInfo`
   - 返回标准化的 OIDC 用户信息

---

## 6. 关键流程说明

### 6.1 客户端注册流程

```
管理员/系统
    ↓
POST /client (RegisteredClientController)
    ↓
RegisteredClientForm (表单验证)
    ↓
RegisteredClientConvert.convertToRegisteredClientPo()
    ↓
Oauth2RegisteredClientMp (实体)
    ↓
Oauth2RegisteredClientMpService.save()
    ↓
MyBatis-Plus 插入数据库
    ↓
oauth2_registered_client 表
```

### 6.2 用户授权同意流程

```
用户访问授权端点
    ↓
GET /oauth2/consent (AuthorizationController)
    ↓
查询 oauth2_registered_client 表（验证客户端）
    ↓
查询 oauth2_authorization_consent 表（检查已授权范围）
    ↓
显示授权确认页面
    ↓
用户确认授权
    ↓
POST /oauth2/authorize
    ↓
Oauth2AuthorizationConsentService.save()
    ↓
保存授权同意记录
    ↓
oauth2_authorization_consent 表
```

### 6.3 Token 生成与存储流程

```
授权成功/Token 交换
    ↓
Spring Authorization Server
    ↓
生成 JWT Token
    ↓
OAuth2AuthorizationService (Spring Security)
    ↓
保存授权记录
    ↓
Oauth2AuthorizationMpService.save()
    ↓
MyBatis-Plus 插入/更新数据库
    ↓
oauth2_authorization 表
    ↓
存储 Token 信息（加密存储为 blob）
```

---

## 7. 安全机制

### 7.1 客户端认证

支持多种客户端认证方法：
- **client_secret_basic**：HTTP Basic 认证
- **client_secret_post**：POST 参数认证
- **none**：无认证（如设备码流程）

### 7.2 Token 安全

- **JWT Token**：自包含，包含签名和过期时间
- **加密存储**：所有 Token 在数据库中加密存储（blob 类型）
- **签名算法**：RS256（非对称加密）
- **Token 格式**：`self-contained`（自包含格式）

### 7.3 授权同意

- **授权同意记录**：记录用户已同意的授权范围，避免重复询问
- **Scope 验证**：验证客户端请求的 scope 是否在已注册范围内
- **CSRF 防护**：使用 `state` 参数防止 CSRF 攻击

### 7.4 错误处理

- **统一错误处理**：`Oauth2FailureHandler` 统一处理 OAuth2 相关错误
- **访问拒绝处理**：`Oauth2AccessDeniedHandler` 处理无权限访问
- **降级服务**：`SystemUserClientFallback` 处理用户服务不可用的情况

---

## 8. 依赖关系

```
service-auth
    ↓
├── common-modules (通用模块)
├── common-redis-cache (Redis 缓存)
└── service-system (用户服务)
    └── Feign 调用获取用户信息
```

---

## 9. 配置要点

### 9.1 数据库配置

需要创建以下表：
- `oauth2_registered_client`：客户端注册表
- `oauth2_authorization`：授权记录表
- `oauth2_authorization_consent`：授权同意记录表

### 9.2 端点配置

默认端点（可通过 `AuthorizationServerSettings` 配置）：
- `/oauth2/authorize`：授权端点
- `/oauth2/token`：Token 端点
- `/oauth2/device_authorization`：设备授权端点
- `/oauth2/userinfo`：用户信息端点
- `/oauth2/jwks`：JWK Set 端点

### 9.3 自定义页面

- `/login`：登录页面
- `/oauth2/consent`：授权确认页面
- `/oauth2/activate`：设备验证页面
- `/oauth2/activated`：设备验证成功页面

---

## 10. 总结

`service-auth` 模块实现了完整的 OAuth2 和 OpenID Connect 协议支持，通过以下核心机制实现权限控制：

1. **客户端管理**：通过 `RegisteredClientRepository` 管理客户端配置
2. **授权流程**：支持授权码、设备码、客户端凭证等多种授权流程
3. **Token 管理**：JWT Token 的生成、存储和验证
4. **授权同意**：通过 `OAuth2AuthorizationConsentService` 管理用户的授权同意
5. **用户信息**：通过 Feign 客户端获取用户详细信息并映射为 OIDC 格式

所有数据流转都通过标准化的 OAuth2 流程进行，确保安全性和可扩展性。

