# Login 流程说明

## 1. 登录流程概览

```
用户访问 OAuth2 授权端点
    ↓
未认证，触发 LoginUrlAuthenticationEntryPoint
    ↓
重定向到 /login (GET)
    ↓
AuthorizationController.login() 返回 login.html 视图
    ↓
用户填写用户名密码，提交表单 (POST /login)
    ↓
Spring Security 表单认证过滤器处理
    ↓
AuthenticationManager.authenticate()
    ↓
认证成功 → 重定向到原始授权请求
    ↓
认证失败 → 重定向到 /login?error
```

---

## 2. 详细流程说明

### 2.1 第一步：访问登录页面 (GET /login)

**触发条件**：
- 用户访问受保护的 OAuth2 端点（如 `/oauth2/authorize`）但未认证
- `AuthorizationServerConfig` 中配置的 `LoginUrlAuthenticationEntryPoint` 捕获到未认证请求

**代码位置**：
```java
// AuthorizationServerConfig.java
httpSecurity.exceptionHandling((exceptions) -> exceptions.defaultAuthenticationEntryPointFor(
    new LoginUrlAuthenticationEntryPoint(CUSTOM_LOGIN_FORM_URL),  // "/login"
    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
));
```

**执行方法**：
```java
// AuthorizationController.java
@GetMapping("/login")
public String login() {
    return "login";  // 返回 login.html 视图
}
```

**返回结果**：
- 返回 `login.html` 页面（Thymeleaf 模板）
- 页面包含用户名和密码输入表单
- 表单提交到 `POST /login`

---

### 2.2 第二步：提交登录表单 (POST /login)

**HTML 表单**：
```html
<form class="form-signin w-100 m-auto" method="post" th:action="@{/login}">
    <input type="text" id="username" name="username" class="form-control" required>
    <input type="password" id="password" name="password" class="form-control" required>
    <button type="submit">Sign in</button>
</form>
```

**关键点**：
- 表单提交到 `POST /login`
- 字段名：`username` 和 `password`（Spring Security 默认）
- **注意**：`AuthorizationController` 中没有处理 `POST /login` 的方法

---

### 2.3 第三步：Spring Security 自动处理登录

**重要说明**：

`AuthorizationController.login()` **只处理 GET 请求**，返回登录页面视图。

**POST 请求由 Spring Security 的默认表单登录机制处理**。

#### 为什么 Spring Security 会自动处理 POST /login？

1. **Spring Security 默认行为**：
   - 当检测到 `spring-boot-starter-security` 依赖时，Spring Security 会自动配置
   - 如果没有显式禁用，Spring Security 会启用表单登录（formLogin）
   - 默认登录处理 URL 就是 `/login`（POST）

2. **AuthorizationServerConfig 的作用**：
   - 它配置的是 **OAuth2 授权服务器**的过滤器链（`@Order(Ordered.HIGHEST_PRECEDENCE)`）
   - 这个过滤器链主要处理 OAuth2 相关端点（如 `/oauth2/authorize`、`/oauth2/token` 等）
   - **不包含** `/login` 端点的处理

3. **存在第二个 SecurityFilterChain**：
   - Spring Boot 会自动创建一个默认的 `SecurityFilterChain`（优先级较低）
   - 这个默认的过滤器链会处理 `/login` 的 POST 请求
   - 如果没有显式配置，会使用 Spring Security 的默认表单登录

#### 执行流程（POST /login）：

```
1. UsernamePasswordAuthenticationFilter 拦截 POST /login
    ↓
2. 提取表单参数：username 和 password
    ↓
3. 创建 UsernamePasswordAuthenticationToken
    ↓
4. 调用 AuthenticationManager.authenticate()
    ↓
5. AuthenticationManager 委托给 AuthenticationProvider
    ↓
6. DaoAuthenticationProvider（默认）尝试认证
    ↓
7. 查询 UserDetailsService（如果配置了）
    ↓
8. 验证用户名和密码
    ↓
    ├─ 认证成功
    │   ↓
    │   创建 Authentication 对象
    │   ↓
    │   保存到 SecurityContext
    │   ↓
    │   触发 AuthenticationSuccessEvent 事件
    │   ↓
    │   AuthenticationEvents.onSuccess() 监听并日志记录
    │   ↓
    │   重定向到原始请求（如 /oauth2/authorize）
    │
    └─ 认证失败
        ↓
        触发 AbstractAuthenticationFailureEvent 事件
        ↓
        AuthenticationEvents.onFailure() 监听并日志记录
        ↓
        重定向到 /login?error
```

---

### 2.4 关键组件

#### 2.4.1 AuthenticationEvents

**位置**：`com.scccy.service.auth.exception.AuthenticationEvents`

**作用**：监听认证成功和失败事件

```java
@Component
public class AuthenticationEvents {
    
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent successEvent) {
        log.info("{} 认证成功", successEvent.getAuthentication().getName());
    }
    
    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failureDisabledEvent) {
        log.warn("{} 认证失败，失败原因：{}",
                failureDisabledEvent.getAuthentication().getName(),
                failureDisabledEvent.getException().getMessage());
    }
}
```

**事件触发时机**：
- 认证成功时：触发 `AuthenticationSuccessEvent`
- 认证失败时：触发 `AbstractAuthenticationFailureEvent`（如 `BadCredentialsException`）

---

## 3. 代码执行路径

### 3.1 GET /login 流程

```
HTTP GET /login
    ↓
Spring Security Filter Chain
    ↓
DispatcherServlet
    ↓
AuthorizationController.login()  (注解：@GetMapping("/login"))
    ↓
返回 "login" 视图名
    ↓
ThymeleafViewResolver 解析
    ↓
渲染 templates/login.html
    ↓
返回 HTML 页面给浏览器
```

### 3.2 POST /login 流程

```
HTTP POST /login
    ↓
Spring Security Filter Chain
    ↓
UsernamePasswordAuthenticationFilter (自动配置)
    ↓
提取 username 和 password 参数
    ↓
创建 UsernamePasswordAuthenticationToken
    ↓
AuthenticationManager.authenticate()
    ↓
DaoAuthenticationProvider.authenticate()
    ↓
    ├─ 查询 UserDetailsService（如果配置了自定义的）
    │   或者使用内存用户（如果没有配置）
    ↓
    ├─ 验证密码
    ↓
    ├─ 认证成功
    │   ↓
    │   SecurityContextHolder.getContext().setAuthentication(...)
    │   ↓
    │   AuthenticationSuccessEvent 发布
    │   ↓
    │   AuthenticationEvents.onSuccess() 执行
    │   ↓
    │   SavedRequestAwareAuthenticationSuccessHandler
    │   ↓
    │   重定向到原始请求（如 /oauth2/authorize?client_id=xxx&...）
    │
    └─ 认证失败
        ↓
        AuthenticationFailureEvent 发布
        ↓
        AuthenticationEvents.onFailure() 执行
        ↓
        SimpleUrlAuthenticationFailureHandler
        ↓
        重定向到 /login?error
```

---

## 4. 当前实现的特点

### 4.1 没有显式的 UserDetailsService 配置

**影响**：
- Spring Security 会使用内存中的默认用户
- 或者需要查看是否有其他地方配置了 `UserDetailsService`

**建议**：
- 如果使用自定义认证，应该实现 `UserDetailsService`
- 可能需要通过 Feign 调用 `service-system` 来验证用户

### 4.2 使用默认表单登录

**特点**：
- 表单字段名必须是 `username` 和 `password`
- 登录处理 URL 是 `/login`（POST）
- 成功重定向到原始请求
- 失败重定向到 `/login?error`

---

## 5. 认证后的流程

### 5.1 认证成功后的重定向

当用户成功登录后：

1. **保存原始请求**：
   - Spring Security 在重定向到 `/login` 之前，会保存原始请求（如 `/oauth2/authorize?client_id=xxx&...`）

2. **重定向到原始请求**：
   - 登录成功后，`SavedRequestAwareAuthenticationSuccessHandler` 会自动重定向到原始请求

3. **继续 OAuth2 授权流程**：
   - 用户现在已认证
   - 可以继续访问 `/oauth2/authorize` 端点
   - 如果设置了 `requireAuthorizationConsent=true`，会重定向到 `/oauth2/consent` 页面

### 5.2 授权确认流程

```
用户已认证
    ↓
访问 /oauth2/authorize
    ↓
检查授权同意记录 (oauth2_authorization_consent 表)
    ↓
如果设置了 requireAuthorizationConsent=true
    ↓
重定向到 /oauth2/consent
    ↓
AuthorizationController.consent()
    ↓
查询已授权范围和需要授权的范围
    ↓
显示授权确认页面
    ↓
用户确认授权
    ↓
POST /oauth2/authorize
    ↓
生成授权码
    ↓
重定向到客户端的 redirect_uri
```

---

## 6. 总结

### 登录流程要点：

1. **GET /login**：
   - 由 `AuthorizationController.login()` 处理
   - 返回 `login.html` 视图

2. **POST /login**：
   - **不由 `AuthorizationController` 处理**
   - 由 **Spring Security 的默认表单登录机制**处理
   - 使用 `UsernamePasswordAuthenticationFilter`
   - 通过 `AuthenticationManager` 进行认证
   - 认证结果由 `AuthenticationEvents` 监听记录

3. **关键组件**：
   - `LoginUrlAuthenticationEntryPoint`：未认证时重定向到 `/login`
   - `UsernamePasswordAuthenticationFilter`：处理表单登录（Spring Security 自动配置）
   - `AuthenticationEvents`：监听认证成功/失败事件

4. **缺少的配置**（可能需要）：
   - 自定义 `UserDetailsService` 实现
   - 自定义 `PasswordEncoder` 配置
   - 显式的表单登录配置（`formLogin()`）

### 建议：

如果需要自定义认证逻辑（例如从 `service-system` 验证用户），应该：

1. 实现 `UserDetailsService` 接口
2. 配置 `PasswordEncoder`
3. 可能需要在 `HttpSecurity` 中显式配置 `formLogin()`

