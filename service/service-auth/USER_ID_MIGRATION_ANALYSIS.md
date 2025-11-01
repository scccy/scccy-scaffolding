# 将 principal_name 改为 user_id 的可行性分析

## 1. 当前实现

### 1.1 当前关联方式

**关联字段映射**：
- `sys_user.user_name` → `oauth2_authorization.principal_name` (varchar)
- `sys_user.user_name` → `oauth2_authorization_consent.principal_name` (varchar)

**当前实现**：
- `Authentication.getName()` 返回 `userName`（字符串）
- `principal_name` 存储 `userName`（字符串）

---

## 2. 改为 user_id 的可行性

### 2.1 技术可行性 ✅

**完全可以实现**，原因如下：

1. **字段类型支持**：
   - `oauth2_authorization.principal_name` 是 `varchar(200)`
   - `oauth2_authorization_consent.principal_name` 是 `varchar(200)`
   - 可以存储字符串格式的 `user_id`（如 "123"）

2. **Spring Security 支持**：
   - `Authentication.getName()` 返回 `Object`（通常是 `String`）
   - 可以设置为字符串格式的 `user_id`（如 `String.valueOf(userId)`）

3. **现有接口支持**：
   - `service-system` 已有 `/sysUser/id/{id}` 接口
   - 可以根据 `userId` 查询用户信息

---

## 3. 改为 user_id 的优势

### 3.1 数据稳定性 ⭐⭐⭐⭐⭐

- **用户名可能修改**：用户可能修改用户名，如果使用 `user_name`，授权记录会失效
- **ID 不会改变**：`user_id` 是主键，一旦创建就不会改变
- **授权记录持久性**：即使用户名修改，授权记录仍然有效

### 3.2 数据规范性 ⭐⭐⭐⭐

- **主键关联**：使用主键关联更符合数据库设计规范
- **数据完整性**：即使 `user_name` 字段被修改或删除，授权记录仍然可以追踪到用户

### 3.3 查询性能 ⭐⭐⭐

- **索引优势**：`user_id` 是主键，通常有索引，查询更快
- **唯一性保证**：`user_id` 是唯一的，查询更准确

---

## 4. 需要修改的地方

### 4.1 数据库表结构

**不需要修改表结构**：
- `principal_name` 字段类型 `varchar(200)` 已经可以存储字符串格式的 `user_id`
- **只需要修改字段注释**，说明存储的是 `user_id` 而不是 `user_name`

### 4.2 代码修改点

#### 4.2.1 AuthService.java

**修改位置**：`AuthService.authenticate()` 方法

**当前代码**：
```java
Authentication authentication = new UsernamePasswordAuthenticationToken(
    userName,  // ← 使用 userName
    null,
    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
);
```

**修改后**：
```java
// 获取 userId
Long userId = user.getUserId();

// 创建认证对象，使用 userId（转为字符串）作为 principal
Authentication authentication = new UsernamePasswordAuthenticationToken(
    String.valueOf(userId),  // ← 改为 userId（字符串格式）
    null,
    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
);
```

---

#### 4.2.2 SystemUserClient.java

**需要添加**：根据 `userId` 查询用户的方法

**当前代码**：
```java
@GetMapping("/userName")
ResultData<SysUserMp> getByUserName(@RequestParam String userName);
```

**需要添加**：
```java
@GetMapping("/id/{id}")
ResultData<SysUserMp> getById(@PathVariable Long id);
```

**注意**：`service-system` 已经提供了 `/sysUser/id/{id}` 接口，可以直接使用

---

#### 4.2.3 AuthorizationServerConfig.java

**修改位置**：`userInfoMapper` 函数

**当前代码**：
```java
Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> {
    JwtAuthenticationToken principal = (JwtAuthenticationToken) authentication.getPrincipal();
    SysUserMp user = userService.getByUserName(principal.getName()).getData();  // ← 通过 userName 查询
    return OidcUserInfo.builder()
        .subject(user.getUserName())  // ← 使用 userName
        .name(user.getNickName())
        .build();
};
```

**修改后**：
```java
Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> {
    JwtAuthenticationToken principal = (JwtAuthenticationToken) authentication.getPrincipal();
    // principal.getName() 返回的是 userId（字符串格式）
    Long userId = Long.parseLong(principal.getName());  // ← 解析为 Long
    SysUserMp user = userService.getById(userId).getData();  // ← 通过 userId 查询
    return OidcUserInfo.builder()
        .subject(user.getUserName())  // ← 仍然使用 userName 作为 subject（OIDC 标准）
        .name(user.getNickName())
        .build();
};
```

---

#### 4.2.4 AuthorizationController.java

**修改位置**：`consent()` 方法

**当前代码**：
```java
OAuth2AuthorizationConsent currentAuthorizationConsent = 
    this.authorizationConsentService.findById(
        registeredClient.getId(), 
        principal.getName()  // ← 返回 userName
    );
```

**修改后**：
```java
// principal.getName() 现在返回 userId（字符串格式）
OAuth2AuthorizationConsent currentAuthorizationConsent = 
    this.authorizationConsentService.findById(
        registeredClient.getId(), 
        principal.getName()  // ← 返回 userId（字符串格式）
    );
// 后续如果需要显示用户名，需要根据 userId 查询用户信息
```

---

#### 4.2.5 Oauth2AuthorizationConsentService.java

**不需要修改**：
- 只是存储和查询逻辑，不需要改变
- `principal_name` 字段存储的内容从 `user_name` 改为 `user_id`（字符串格式）

---

#### 4.2.6 AuthorizationController.login() 返回

**当前代码**：
```java
return ResultData.ok("登录成功", authentication.getName());  // ← 返回 userName
```

**修改后**：
```java
// 如果需要返回用户信息，可能需要返回 userId 或其他用户信息
// 或者保持返回 userId，前端可以通过其他接口获取用户详细信息
return ResultData.ok("登录成功", authentication.getName());  // ← 返回 userId（字符串格式）
```

---

## 5. 数据迁移方案

### 5.1 现有数据迁移

**问题**：如果数据库中已有数据，`principal_name` 字段存储的是 `user_name`

**迁移方案**：

#### 方案 A：通过 userName 查询 userId 并更新

```sql
-- 假设可以跨服务查询（实际可能需要应用层处理）
UPDATE oauth2_authorization oa
INNER JOIN sys_user su ON oa.principal_name = su.user_name
SET oa.principal_name = CAST(su.user_id AS CHAR);

-- 同上，更新 oauth2_authorization_consent
UPDATE oauth2_authorization_consent oac
INNER JOIN sys_user su ON oac.principal_name = su.user_name
SET oac.principal_name = CAST(su.user_id AS CHAR);
```

**注意**：由于跨服务，可能需要编写迁移脚本，通过 Feign 调用查询并更新

#### 方案 B：保持向后兼容（推荐）

1. **新数据**：使用 `user_id`
2. **旧数据**：迁移脚本统一转换
3. **查询逻辑**：先尝试解析为 `user_id`（数字），如果不是数字，则认为是旧数据（`user_name`），先查询 `user_id` 再使用

---

## 6. 潜在问题与解决方案

### 6.1 问题 1：JWT Token 中的 subject

**问题**：
- OIDC 标准中，`subject`（sub）应该是**稳定的、唯一的用户标识符**
- 如果使用 `user_id`，JWT Token 中的 `sub` 字段应该是 `user_id` 还是 `user_name`？

**分析**：
- OIDC 规范要求 `sub` 是**稳定的标识符**，不会随时间改变
- `user_id` 是主键，不会改变 ✅
- `user_name` 可能改变 ❌

**建议**：
- JWT Token 的 `sub` 字段使用 `user_id`（字符串格式）
- 但在用户信息（UserInfo）中，`subject` 仍然可以使用 `user_name`（符合 OIDC 标准）

---

### 6.2 问题 2：客户端凭证模式（client_credentials）

**问题**：
- 在 `client_credentials` 模式下，没有用户认证，只有客户端认证
- 此时 `principal_name` 应该存储什么？

**当前实现**：
- DDL 注释说"一般为 clientId"，这是正确的
- 代码中可能已经处理了这种情况

**解决方案**：
- 保持现状：客户端凭证模式下，`principal_name` 可以是 `client_id`
- 在查询逻辑中判断：如果是数字，则按 `user_id` 处理；如果是字符串且不是数字，则按 `client_id` 或 `user_name` 处理

---

### 6.3 问题 3：显示用户名

**问题**：
- 在授权确认页面或其他地方，需要显示用户名
- 如果 `principal_name` 存储的是 `user_id`，需要查询用户信息

**解决方案**：
- 在需要显示用户名的地方，先解析 `user_id`，然后调用 `SystemUserClient.getById()` 获取用户信息
- 或者缓存用户信息到 Session/Context 中

---

## 7. 推荐方案

### 7.1 推荐使用 user_id ✅

**理由**：
1. ✅ **数据稳定性**：用户名可能修改，ID 不会变
2. ✅ **数据规范性**：使用主键关联更规范
3. ✅ **技术可行**：完全支持，无需修改数据库结构
4. ✅ **向后兼容**：可以通过迁移脚本处理旧数据

### 7.2 实施步骤

#### 步骤 1：修改 SystemUserClient

添加根据 `userId` 查询的方法：
```java
@GetMapping("/id/{id}")
ResultData<SysUserMp> getById(@PathVariable Long id);
```

#### 步骤 2：修改 AuthService

将 `principal` 从 `userName` 改为 `userId`（字符串格式）

#### 步骤 3：修改 AuthorizationServerConfig

将用户信息查询从 `getByUserName()` 改为 `getById()`

#### 步骤 4：修改 AuthorizationController

如果需要在页面显示用户名，先查询用户信息

#### 步骤 5：数据迁移

编写迁移脚本，将现有数据的 `principal_name` 从 `user_name` 转换为 `user_id`

#### 步骤 6：更新 DDL 注释

更新数据库表注释，说明 `principal_name` 存储的是 `user_id`

---

## 8. 兼容性考虑

### 8.1 向后兼容策略

如果需要支持旧数据，可以这样处理：

```java
// 判断 principal_name 是 user_id 还是 user_name
public SysUserMp getUserByIdOrName(String principalName) {
    try {
        // 尝试解析为数字（user_id）
        Long userId = Long.parseLong(principalName);
        return systemUserClient.getById(userId).getData();
    } catch (NumberFormatException e) {
        // 如果不是数字，则认为是旧数据（user_name）
        return systemUserClient.getByUserName(principalName).getData();
    }
}
```

---

## 9. 总结

### 9.1 可行性结论

✅ **完全可行**，改为 `user_id` 有以下优势：

1. **数据稳定性**：即使用户名修改，授权记录仍然有效
2. **数据规范性**：使用主键关联更符合规范
3. **技术可行**：无需修改数据库结构，只需修改代码逻辑

### 9.2 需要修改的代码

1. `AuthService.authenticate()` - 使用 `userId` 作为 principal
2. `SystemUserClient` - 添加 `getById()` 方法
3. `AuthorizationServerConfig.userInfoMapper` - 改为通过 `userId` 查询
4. `AuthorizationController` - 如果需要显示用户名，先查询用户信息
5. 数据库 DDL 注释 - 更新说明

### 9.3 注意事项

1. **数据迁移**：需要处理现有数据
2. **显示用户名**：在需要显示用户名的地方，需要先查询用户信息
3. **客户端凭证模式**：保持 `client_id` 的处理逻辑不变

