# 普通用户登录注册功能开发计划

## 📋 项目概述

实现普通用户的登录、注册、登出功能，与OAuth2第三方授权功能分离。使用JWT Token进行身份认证，将用户基础信息存储在JWT中。

---

## 🎯 功能目标

1. **用户注册**：新用户可以通过用户名、密码等信息注册账号
2. **用户登录**：已注册用户可以使用用户名密码登录，获取JWT Token
3. **用户登出**：用户可以主动登出，使JWT Token失效
4. **JWT工具类**：提供JWT Token的生成、解析、验证等基础功能
5. **Token存储用户信息**：在JWT中存储用户基础信息（userId、userName、nickName等）

---

## 📦 技术栈

- **JWT库**：`io.jsonwebtoken:jjwt` (0.12.5)
- **密码加密**：`BCryptPasswordEncoder` (已存在)
- **远程调用**：`Spring Cloud OpenFeign` (已存在)
- **数据存储**：通过`SystemUserClient`调用`service-system`服务

---

## 🔧 实施步骤

### 阶段一：JWT工具类开发

#### 1.1 创建JWT配置类
- **文件**：`com.scccy.service.auth.config.JwtConfig.java`
- **功能**：
  - 配置JWT密钥（从配置文件读取或自动生成）
  - 配置JWT过期时间
  - 提供JWT相关的Bean（SecretKey、Claims等）

#### 1.2 创建JWT工具类
- **文件**：`com.scccy.service.auth.utils.JwtUtils.java`
- **功能**：
  - `generateToken(userId, username, claims)` - 生成JWT Token
  - `parseToken(token)` - 解析JWT Token
  - `validateToken(token)` - 验证JWT Token有效性
  - `getClaims(token)` - 获取Token中的Claims
  - `getUserId(token)` - 获取用户ID
  - `getUsername(token)` - 获取用户名
  - `getNickName(token)` - 获取昵称
  - `isTokenExpired(token)` - 判断Token是否过期
  - `refreshToken(token)` - 刷新Token（可选）

#### 1.3 JWT中存储的用户信息
```json
{
  "sub": "userId",           // 用户ID（标准claim）
  "username": "admin",        // 用户名（自定义claim）
  "nickName": "管理员",        // 昵称（自定义claim）
  "userId": 1,               // 用户ID（自定义claim，便于查询）
  "status": 0,               // 用户状态（自定义claim）
  "iat": 1234567890,         // 签发时间（标准claim）
  "exp": 1234571490          // 过期时间（标准claim）
}
```

---

### 阶段二：用户服务扩展

#### 2.1 扩展SystemUserClient接口
- **文件**：`com.scccy.service.auth.fegin.SystemUserClient.java`
- **新增方法**：
  - `register(@RequestBody SysUserMp user)` - 用户注册
  - `updateUser(@RequestBody SysUserMp user)` - 更新用户信息（如果需要）

#### 2.2 创建用户注册服务
- **文件**：`com.scccy.service.auth.service.UserService.java`
- **功能**：
  - `register(RegisterBody registerBody)` - 用户注册
    - 验证用户名是否已存在
    - 加密密码
    - 设置默认状态和删除标记
    - 调用`service-system`创建用户
    - 返回注册结果
  - `login(String username, String password)` - 用户登录（复用AuthService逻辑或独立实现）
    - 验证用户信息
    - 生成JWT Token
    - 将用户信息写入JWT
    - 返回Token和用户信息

#### 2.3 创建用户登出服务（可选）
- **文件**：`com.scccy.service.auth.service.UserService.java`（扩展）
- **功能**：
  - `logout(String token)` - 用户登出
    - 将Token加入黑名单（使用Redis缓存）
    - 可选：记录登出日志

---

### 阶段三：Controller开发

#### 3.1 创建用户注册DTO
- **文件**：`com.scccy.service.auth.dto.RegisterBody.java`
- **字段**：
  - `username` (String, required) - 用户名
  - `password` (String, required) - 密码
  - `nickName` (String, optional) - 昵称
  - `email` (String, optional) - 邮箱
  - `phone` (String, optional) - 手机号

#### 3.2 创建登录响应DTO
- **文件**：`com.scccy.service.auth.dto.LoginResponse.java`
- **字段**：
  - `token` (String) - JWT Token
  - `userId` (Long) - 用户ID
  - `username` (String) - 用户名
  - `nickName` (String) - 昵称
  - `expireTime` (Long) - Token过期时间（时间戳）

#### 3.3 创建用户认证Controller
- **文件**：`com.scccy.service.auth.controller.UserAuthController.java`
- **路径前缀**：`/api/user`
- **功能**：
  - `POST /api/user/register` - 用户注册
    - 接收`RegisterBody`
    - 调用`UserService.register()`
    - 返回注册结果
  - `POST /api/user/login` - 用户登录
    - 接收`LoginBody`
    - 调用`UserService.login()`
    - 返回`LoginResponse`（包含JWT Token）
  - `POST /api/user/logout` - 用户登出
    - 从请求头获取Token（`Authorization: Bearer {token}`）
    - 调用`UserService.logout()`
    - 返回登出结果
  - `GET /api/user/info` - 获取当前用户信息（可选）
    - 从Token中解析用户信息
    - 返回用户详细信息

#### 3.4 添加OpenAPI3注解
- 为所有接口添加`@Tag`、`@Operation`、`@ApiResponses`等注解

---

### 阶段四：安全配置更新

#### 4.1 更新SecurityConfig
- **文件**：`com.scccy.service.auth.config.SecurityConfig.java`
- **修改**：
  - 允许`/api/user/**`路径公开访问（注册、登录）
  - 保护`/api/user/logout`和`/api/user/info`需要JWT认证
  - 添加JWT过滤器（可选，用于自动验证Token）

#### 4.2 创建JWT认证过滤器（可选）
- **文件**：`com.scccy.service.auth.filter.JwtAuthenticationFilter.java`
- **功能**：
  - 从请求头提取JWT Token
  - 验证Token有效性
  - 解析Token中的用户信息
  - 将用户信息设置到`SecurityContext`中

---

### 阶段五：Redis黑名单支持（可选）

#### 5.1 创建Token黑名单服务
- **文件**：`com.scccy.service.auth.service.TokenBlacklistService.java`
- **功能**：
  - `addToBlacklist(String token, Long expireTime)` - 将Token加入黑名单
  - `isBlacklisted(String token)` - 检查Token是否在黑名单中
  - 使用Redis存储，key格式：`jwt:blacklist:{token}`

---

## 📁 文件清单

### 新建文件

1. **配置类**
   - `com.scccy.service.auth.config.JwtConfig.java`

2. **工具类**
   - `com.scccy.service.auth.utils.JwtUtils.java`

3. **服务类**
   - `com.scccy.service.auth.service.UserService.java`
   - `com.scccy.service.auth.service.TokenBlacklistService.java`（可选）

4. **Controller**
   - `com.scccy.service.auth.controller.UserAuthController.java`

5. **DTO**
   - `com.scccy.service.auth.dto.RegisterBody.java`
   - `com.scccy.service.auth.dto.LoginResponse.java`

6. **过滤器**（可选）
   - `com.scccy.service.auth.filter.JwtAuthenticationFilter.java`

### 修改文件

1. **Feign Client**
   - `com.scccy.service.auth.fegin.SystemUserClient.java` - 添加注册接口

2. **安全配置**
   - `com.scccy.service.auth.config.SecurityConfig.java` - 更新访问控制规则

3. **配置文件**
   - `application.yml` - 添加JWT配置项（密钥、过期时间等）

---

## ⚙️ 配置文件

### application.yml 新增配置

```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key-change-this-in-production}  # JWT密钥，生产环境建议使用环境变量
  expiration: 7200000  # Token过期时间（毫秒），默认2小时
  refresh-expiration: 604800000  # 刷新Token过期时间（毫秒），默认7天
  header: Authorization  # Token请求头名称
  token-prefix: Bearer   # Token前缀
```

---

## 🔒 安全考虑

1. **密码加密**：使用`BCryptPasswordEncoder`（强度12）
2. **JWT密钥**：生产环境必须使用强随机密钥，建议从环境变量读取
3. **Token过期**：设置合理的过期时间（建议2小时）
4. **HTTPS**：生产环境必须使用HTTPS传输
5. **黑名单**：实现Token黑名单机制，支持主动登出
6. **防止暴力破解**：可以考虑添加登录失败次数限制

---

## 🧪 测试计划

### 单元测试

1. **JwtUtils测试**
   - Token生成和解析
   - Token过期验证
   - Claims提取

2. **UserService测试**
   - 用户注册（成功、失败场景）
   - 用户登录（成功、失败场景）
   - 用户登出

### 接口测试

1. **注册接口**
   - 正常注册
   - 用户名重复
   - 参数验证

2. **登录接口**
   - 正常登录
   - 用户名不存在
   - 密码错误
   - 用户状态异常

3. **登出接口**
   - 正常登出
   - Token无效
   - 未提供Token

---

## 📝 开发注意事项

1. **与OAuth2分离**：新的用户认证Controller独立于OAuth2授权流程
2. **错误处理**：统一使用`ResultData`返回结果
3. **日志记录**：记录关键操作（登录、注册、登出）
4. **参数验证**：使用`@Valid`和`@NotNull`等注解验证输入
5. **异常处理**：使用全局异常处理器统一处理异常
6. **代码复用**：复用现有的`AuthService`和`SystemUserClient`

---

## ✅ 验收标准

1. ✅ 用户可以成功注册账号
2. ✅ 用户可以成功登录并获取JWT Token
3. ✅ JWT Token中包含用户基础信息（userId、username、nickName等）
4. ✅ 用户可以使用Token访问受保护的资源
5. ✅ 用户可以成功登出，Token被加入黑名单
6. ✅ JWT工具类功能完整（生成、解析、验证）
7. ✅ 所有接口都有完整的OpenAPI3文档
8. ✅ 安全配置正确，接口访问权限合理

---

## 📅 时间估算

- **阶段一**（JWT工具类）：2小时
- **阶段二**（用户服务）：2小时
- **阶段三**（Controller）：2小时
- **阶段四**（安全配置）：1小时
- **阶段五**（Redis黑名单）：1小时（可选）
- **测试与调试**：2小时

**总计**：约8-10小时（不含可选功能）

---

## 🚀 下一步行动

1. ✅ 所有核心功能已完成
2. ✅ 进行集成测试
3. ✅ 验证Token黑名单功能
4. ✅ 验证登出后Token无法继续使用

---

## ✅ 实施进度

### 已完成 ✅

1. ✅ **阶段一：JWT工具类开发**
   - ✅ 创建JWT配置类（`JwtConfig.java`）
   - ✅ 创建JWT工具类（`JwtUtils.java`）
   - ✅ 实现JWT生成、解析、验证功能
   - ✅ 支持将用户基础信息写入JWT（userId、username、nickName、status、email、phonenumber）

2. ✅ **阶段二：用户服务扩展**
   - ✅ 扩展`SystemUserClient`接口，添加注册接口
   - ✅ 更新`SystemUserClientFallback`，添加注册方法的降级处理
   - ✅ 创建`UserService`服务类
   - ✅ 实现用户注册功能（包括用户名验证、密码加密）
   - ✅ 实现用户登录功能（验证用户状态、密码，生成JWT Token）

3. ✅ **阶段三：Controller开发**
   - ✅ 创建`RegisterBody` DTO（注册请求体）
   - ✅ 创建`LoginResponse` DTO（登录响应体）
   - ✅ 创建`UserAuthController`控制器
   - ✅ 实现`POST /api/user/register`接口（用户注册）
   - ✅ 实现`POST /api/user/login`接口（用户登录）
   - ✅ 实现`POST /api/user/logout`接口（用户登出）
   - ✅ 实现`GET /api/user/info`接口（获取当前用户信息）
   - ✅ 添加完整的OpenAPI3注解

4. ✅ **阶段四：安全配置更新**
   - ✅ 更新`SecurityConfig`，添加`userAuthSecurityFilterChain`
   - ✅ 配置`/api/user/**`路径的访问控制
   - ✅ 允许`/api/user/register`和`/api/user/login`公开访问
   - ✅ 其他接口需要认证

### 已完成 ✅

5. ✅ **阶段五：Redis黑名单支持**
   - ✅ 创建`TokenBlacklistService`服务类
   - ✅ 实现Token黑名单机制（使用JetCache + Redis）
   - ✅ 集成到登出功能（`/api/user/logout`）
   - ✅ 集成到获取用户信息功能（`/api/user/info`），防止黑名单Token获取用户信息
   - ✅ 支持Token过期后自动从黑名单移除（通过Redis TTL机制）

### 待完成 ⏳

- 无

### 文件清单

#### 新建文件 ✅

1. **配置类**
   - ✅ `com.scccy.service.auth.config.JwtConfig.java`

2. **工具类**
   - ✅ `com.scccy.service.auth.utils.JwtUtils.java`

3. **服务类**
   - ✅ `com.scccy.service.auth.service.UserService.java`
   - ✅ `com.scccy.service.auth.service.TokenBlacklistService.java`

4. **Controller**
   - ✅ `com.scccy.service.auth.controller.UserAuthController.java`

5. **DTO**
   - ✅ `com.scccy.service.auth.dto.RegisterBody.java`
   - ✅ `com.scccy.service.auth.dto.LoginResponse.java`

6. **文档**
   - ✅ `USER_AUTH_PLAN.md`（本文件）

#### 修改文件 ✅

1. **Feign Client**
   - ✅ `com.scccy.service.auth.fegin.SystemUserClient.java` - 添加注册接口

2. **Fallback**
   - ✅ `com.scccy.service.auth.fegin.SystemUserClientFallback.java` - 添加注册方法的降级处理

3. **安全配置**
   - ✅ `com.scccy.service.auth.config.SecurityConfig.java` - 添加用户认证接口安全配置

4. **工具类**
   - ✅ `com.scccy.service.auth.utils.JwtUtils.java` - 更新validateToken方法的注释

### 待配置项

#### 配置文件更新

需要在`application.yml`或Nacos配置中心添加JWT配置：

```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key-change-this-in-production}  # JWT密钥，生产环境建议使用环境变量
  expiration: 7200000  # Token过期时间（毫秒），默认2小时
  refresh-expiration: 604800000  # 刷新Token过期时间（毫秒），默认7天
  header: Authorization  # Token请求头名称
  token-prefix: Bearer   # Token前缀
```

---

## 📝 使用说明

### 1. 用户注册

**接口**：`POST /api/user/register`

**请求体**：
```json
{
  "username": "testuser",
  "password": "123456",
  "nickName": "测试用户",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

**响应**：
```json
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "testuser",
    "nickName": "测试用户",
    "expireTime": 1699123456789
  }
}
```

### 2. 用户登录

**接口**：`POST /api/user/login`

**请求体**：
```json
{
  "username": "testuser",
  "password": "123456"
}
```

**响应**：
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "testuser",
    "nickName": "测试用户",
    "expireTime": 1699123456789
  }
}
```

### 3. 用户登出

**接口**：`POST /api/user/logout`

**请求头**：
```
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "msg": "登出成功",
  "data": "登出成功"
}
```

### 4. 获取当前用户信息

**接口**：`GET /api/user/info`

**请求头**：
```
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "testuser",
    "nickName": "测试用户",
    "expireTime": 1699123456789
  }
}
```

---

**创建时间**：2025-11-01  
**最后更新**：2025-11-01

