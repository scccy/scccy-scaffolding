# sys_user 与 OAuth2 系列表的关系说明

## 1. 表结构概览

### 1.1 sys_user 表（业务系统用户表）

**位置**：`service-system` 服务管理的数据库

**主要字段**：
- `user_id` (Long) - 用户ID，主键
- `user_name` (String) - 用户账号（用户名）
- `password` (String) - 密码（BCrypt 加密）
- `nick_name` (String) - 用户昵称
- `status` (Integer) - 账号状态（0正常 1停用）
- `del_flag` (Integer) - 删除标志（0存在 2删除）
- 其他业务字段...

**作用**：存储业务系统的用户基本信息

---

### 1.2 oauth2_registered_client 表（OAuth2 客户端注册表）

**主要字段**：
- `id` (String) - UUID，主键
- `client_id` (String) - 客户端ID，唯一索引
- `client_secret` (String) - 客户端密钥
- `client_name` (String) - 客户端名称
- `authorization_grant_types` (String) - 支持的授权类型
- `scopes` (String) - 支持的授权范围
- 其他配置字段...

**作用**：存储已注册的 OAuth2 客户端配置信息

**与 sys_user 的关系**：**无直接关系**（独立的客户端配置表）

---

### 1.3 oauth2_authorization 表（OAuth2 授权记录表）

**主要字段**：
- `id` (String) - UUID，主键
- `registered_client_id` (String) - 客户端ID
- **`principal_name` (String) - 主体名称（用户名）**
- `authorization_grant_type` (String) - 授权类型
- `access_token_value` (blob) - Access Token
- `refresh_token_value` (blob) - Refresh Token
- `oidc_id_token_value` (blob) - ID Token
- 其他 Token 相关字段...

**作用**：存储 OAuth2 授权过程中的所有 Token 和状态信息

**关键字段说明**：
- `principal_name`：**存储的是 `sys_user.user_name`（用户名）**
  - DDL 注释写的是"一般为 clientId"，但这是**注释错误**
  - 实际使用中，在授权码模式、设备码模式下，存储的是用户名
  - 只有在客户端凭证模式（client_credentials）下，可能是 clientId

---

### 1.4 oauth2_authorization_consent 表（授权同意记录表）

**主要字段**：
- `registered_client_id` (String) - 客户端ID（复合主键）
- **`principal_name` (String) - 主体名称（用户名）（复合主键）**
- `authorities` (String) - 已同意的授权范围（逗号分隔）

**作用**：存储用户的授权同意记录，用于判断用户是否已授权客户端访问特定资源

**关键字段说明**：
- `principal_name`：**存储的是 `sys_user.user_name`（用户名）**
  - DDL 注释写的是"一般为 clientId"，但这是**注释错误**
  - 实际使用中，始终存储的是用户名

---

## 2. 表关系图

```
┌─────────────────────┐
│    sys_user 表      │
│  (service-system)   │
├─────────────────────┤
│ user_id (PK)        │
│ user_name (UK) ◄────┼─────┐
│ password            │      │
│ nick_name           │      │
│ status              │      │
│ del_flag            │      │
└─────────────────────┘      │
                              │
                              │ 通过 userName 关联
                              │
        ┌─────────────────────┴─────────────────────┐
        │                                           │
        ▼                                           ▼
┌──────────────────────────┐      ┌──────────────────────────┐
│ oauth2_authorization 表   │      │oauth2_authorization_consent│
├──────────────────────────┤      ├──────────────────────────┤
│ id (PK)                  │      │ registered_client_id (PK) │
│ registered_client_id     │      │ principal_name (PK) ◄─────┼──┐
│ principal_name ◄─────────┼──────┘ authorities             │  │
│                          │                                │  │
│ (存储各种 Token)         │                                │  │
└──────────────────────────┘                                │  │
                                                             │  │
        ┌───────────────────────────────────────────────────┘  │
        │                                                       │
        │  关联关系：                                           │
        │  sys_user.user_name = oauth2_authorization.principal_name│
        │  sys_user.user_name = oauth2_authorization_consent.principal_name│
        │                                                       │
        ▼                                                       │
┌──────────────────────────┐                                 │
│ oauth2_registered_client │                                 │
├──────────────────────────┤                                 │
│ id (PK)                  │                                 │
│ client_id (UK)           │                                 │
│                          │                                 │
│ (与 sys_user 无直接关系)  │                                 │
└──────────────────────────┘                                 │
                                                              │
        ┌──────────────────────────────────────────────────────┘
        │
        │ 间接关系：
        │ oauth2_authorization.registered_client_id → oauth2_registered_client.client_id
        │ oauth2_authorization_consent.registered_client_id → oauth2_registered_client.client_id
```

---

## 3. 数据关联关系

### 3.1 核心关联关系

#### 关联字段映射

| OAuth2 表 | OAuth2 字段 | sys_user 字段 | 关联说明 |
|-----------|------------|---------------|----------|
| `oauth2_authorization` | `principal_name` | `user_name` | **字符串关联**，存储用户名 |
| `oauth2_authorization_consent` | `principal_name` | `user_name` | **字符串关联**，存储用户名 |
| `oauth2_registered_client` | 无 | 无 | **无直接关联**（独立的客户端配置） |

#### 重要说明

1. **没有外键约束**：
   - OAuth2 表与 `sys_user` 表之间**没有数据库层面的外键约束**
   - 关联是通过**字符串匹配**实现的（`principal_name = user_name`）
   - 这种设计的原因是：
     - `sys_user` 表在 `service-system` 服务中管理
     - OAuth2 表在 `service-auth` 服务中管理
     - 跨服务数据库，无法建立外键约束

2. **principal_name 的实际含义**：
   - DDL 注释说"一般为 clientId"，这是**不准确的注释**
   - **实际使用中，principal_name 存储的是用户名（user_name）**
   - 只有在 `client_credentials` 授权模式下，principal_name 可能是 clientId

---

## 4. 数据流转中的关系

### 4.1 登录认证流程

```
用户登录
    ↓
POST /login (AuthService.authenticate())
    ↓
调用 SystemUserClient.getByUserName(userName)
    ↓
查询 sys_user 表 (service-system)
    ↓
获取 SysUserMp (包含 user_name, password 等)
    ↓
验证密码，创建 Authentication
    ↓
Authentication.getName() 返回 user_name
    ↓
保存到 SecurityContext
```

### 4.2 OAuth2 授权流程

```
用户已认证（SecurityContext 中有 Authentication）
    ↓
访问 /oauth2/authorize
    ↓
principal.getName() 获取 user_name
    ↓
创建 OAuth2Authorization 对象
    ↓
principal_name = user_name (从 Authentication.getName() 获取)
    ↓
保存到 oauth2_authorization 表
    ↓
principal_name = user_name
```

### 4.3 授权同意流程

```
用户确认授权
    ↓
principal.getName() 获取 user_name
    ↓
创建 OAuth2AuthorizationConsent 对象
    ↓
principal_name = user_name
registered_client_id = client.id
    ↓
保存到 oauth2_authorization_consent 表
    ↓
复合主键：(registered_client_id, principal_name)
```

### 4.4 用户信息查询流程

```
访问 /oauth2/userinfo
    ↓
从 JWT Token 中提取 principal.getName()
    ↓
得到 user_name
    ↓
调用 SystemUserClient.getByUserName(user_name)
    ↓
查询 sys_user 表 (service-system)
    ↓
获取 SysUserMp
    ↓
映射为 OidcUserInfo 返回
```

---

## 5. 代码中的关联证据

### 5.1 登录时设置 principal

```java
// AuthService.java
Authentication authentication = new UsernamePasswordAuthenticationToken(
    userName,  // ← 这里设置为 userName（sys_user.user_name）
    null,
    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
);
```

### 5.2 OAuth2 授权时使用 principal

```java
// AuthorizationController.java
OAuth2AuthorizationConsent currentAuthorizationConsent = 
    this.authorizationConsentService.findById(
        registeredClient.getId(), 
        principal.getName()  // ← 这里获取的是 user_name
    );
```

### 5.3 用户信息映射时使用 principal

```java
// AuthorizationServerConfig.java
Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> {
    JwtAuthenticationToken principal = (JwtAuthenticationToken) authentication.getPrincipal();
    SysUserMp user = userService.getByUserName(principal.getName()).getData();  // ← 使用 userName 查询
    return OidcUserInfo.builder()
        .subject(user.getUserName())  // ← 使用 user_name
        .name(user.getNickName())
        .build();
};
```

---

## 6. 关系总结

### 6.1 直接关联关系

1. **sys_user.user_name → oauth2_authorization.principal_name**
   - 关系类型：**字符串关联**（无外键约束）
   - 存储内容：用户名（userName）
   - 使用场景：所有需要用户身份识别的 OAuth2 授权流程

2. **sys_user.user_name → oauth2_authorization_consent.principal_name**
   - 关系类型：**字符串关联**（无外键约束）
   - 存储内容：用户名（userName）
   - 使用场景：用户授权同意记录

### 6.2 间接关联关系

3. **oauth2_authorization.registered_client_id → oauth2_registered_client.client_id**
   - 关联到客户端配置

4. **oauth2_authorization_consent.registered_client_id → oauth2_registered_client.client_id**
   - 关联到客户端配置

### 6.3 无关联关系

5. **oauth2_registered_client 与 sys_user**
   - **无直接关联关系**
   - 客户端注册是独立的配置，与用户无关
   - 客户端是应用级别的配置，用户是业务级别的实体

---

## 7. 数据完整性说明

### 7.1 潜在问题

由于**没有外键约束**，可能出现以下问题：

1. **数据不一致**：
   - `oauth2_authorization.principal_name` 中存储的用户名可能在 `sys_user` 表中不存在
   - 例如：用户被删除后，OAuth2 授权记录仍存在

2. **数据清理困难**：
   - 删除用户时，需要手动清理相关的 OAuth2 授权记录
   - 没有级联删除机制

### 7.2 建议的处理方式

1. **应用层保证一致性**：
   - 在代码中确保 `principal_name` 始终存储有效的 `user_name`
   - 通过 Feign 调用验证用户是否存在

2. **定期清理**：
   - 可以创建定时任务，清理无效的授权记录
   - 查询 `oauth2_authorization.principal_name`，验证用户是否仍存在

3. **日志记录**：
   - 在授权流程中记录用户ID，方便后续追踪和清理

---

## 8. 特殊场景说明

### 8.1 客户端凭证模式（client_credentials）

在 `client_credentials` 授权模式下：

- **principal_name** 可能存储的是 `client_id`（而不是用户名）
- 因为这种模式不需要用户认证，只有客户端认证
- 但代码中主要使用授权码模式，所以通常存储的是用户名

### 8.2 设备码授权模式（device_code）

在 `device_code` 授权模式下：

- **principal_name** 存储的是用户名（user_name）
- 用户通过设备码登录后，会创建包含用户名的 Authentication 对象

---

## 9. 数据查询示例

### 9.1 查询用户的 OAuth2 授权记录

```sql
-- 查询指定用户的所有授权记录
SELECT * FROM oauth2_authorization 
WHERE principal_name = 'admin';

-- 关联查询（需要跨服务，实际需要通过 Feign 调用）
-- 1. 查询 sys_user 获取用户信息
-- 2. 查询 oauth2_authorization 获取授权记录
-- 3. 在应用层关联
```

### 9.2 查询用户的授权同意记录

```sql
-- 查询指定用户对指定客户端的授权同意
SELECT * FROM oauth2_authorization_consent 
WHERE principal_name = 'admin' 
  AND registered_client_id = 'xxx';
```

### 9.3 查询客户端的所有用户授权

```sql
-- 查询指定客户端的所有用户授权记录
SELECT * FROM oauth2_authorization 
WHERE registered_client_id = 'xxx';
```

---

## 10. 总结

### 核心关系

1. **sys_user 表**：
   - 存储业务用户信息
   - `user_name` 是唯一的用户标识

2. **oauth2_authorization 表**：
   - 存储 OAuth2 授权和 Token 信息
   - `principal_name` 字段存储用户名（user_name）
   - 通过 `user_name` 与 `sys_user` 关联（字符串匹配）

3. **oauth2_authorization_consent 表**：
   - 存储用户授权同意记录
   - `principal_name` 字段存储用户名（user_name）
   - 通过 `user_name` 与 `sys_user` 关联（字符串匹配）

4. **oauth2_registered_client 表**：
   - 存储 OAuth2 客户端配置
   - 与 `sys_user` **无直接关系**

### 设计特点

- **跨服务数据关联**：通过字符串（user_name）关联，而不是数据库外键
- **松耦合设计**：`service-auth` 不直接访问 `sys_user` 表，而是通过 Feign 调用 `service-system` 服务
- **数据一致性**：依赖应用层逻辑保证，而非数据库约束

