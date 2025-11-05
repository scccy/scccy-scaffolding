# OAuth2 架构改造执行文档

## 📋 文档说明

本文档用于指导将现有权限控制系统改造为标准的 **OAuth2 + Gateway + Resource Server + Authorization Server** 架构模式。

**核心架构**：
- **Authorization Server (service-auth)**：统一签发 Token
- **Gateway (gateway)**：作为 Resource Server，统一验证 Token
- **Resource Server (service-system, service-wechatwork)**：后端业务服务，从网关获取用户信息

- **执行方式**：按顺序逐步执行，每完成一步标记 ✅
- **回滚方案**：每个阶段完成后可单独回滚
- **验证方式**：每步完成后进行测试验证

---

## 🎯 改造目标

### 当前架构问题
- ❌ service-system 自己生成 JWT Token，与 Authorization Server 分离
- ❌ 存在两套 Token 体系，不统一
- ❌ 各服务需要独立验证 Token，无法统一管理
- ❌ 网关只做路由转发，没有统一认证

### 目标架构
- ✅ **Authorization Server (service-auth)**：作为唯一的授权服务器，统一签发 Token
- ✅ **Gateway (gateway)**：作为 Resource Server，统一验证 Token，集中管理认证逻辑
- ✅ **Resource Server (service-system, service-wechatwork)**：后端业务服务，从网关传递的用户信息中获取用户身份
- ✅ 统一的权限控制体系，支持 Token 中携带权限信息
- ✅ 支持多种 OAuth2 授权模式（授权码、客户端凭证、密码模式等）

### 架构图
```
┌─────────────┐
│   客户端     │
└──────┬──────┘
       │ Access Token
       ▼
┌─────────────────────────────────────────────┐
│  Gateway (Resource Server)                  │
│  - 统一验证 Token                             │
│  - 提取用户信息                               │
│  - 添加用户信息到请求头                        │
│  - 路由转发到后端服务                          │
└──────┬──────────────────────────────────────┘
       │ 已验证的请求 + 用户信息
       ▼
┌─────────────────────────────────────────────┐
│  Resource Server (后端服务)                  │
│  - service-system                           │
│  - service-wechatwork                       │
│  - 从请求头获取用户信息                        │
│  - 执行业务逻辑                               │
└─────────────────────────────────────────────┘
       │
       │ Token 验证（需要时）
       ▼
┌─────────────────────────────────────────────┐
│  Authorization Server (service-auth)        │
│  - 签发 Token                                │
│  - 提供 JWK Set (/oauth2/jwks)              │
└─────────────────────────────────────────────┘
```

---

## 📊 改造阶段概览

| 阶段 | 名称 | 预计工作量 | 状态 |
|------|------|-----------|------|
| 阶段一 | 准备阶段 - Authorization Server 配置优化 | 2-3小时 | ⏳ 待开始 |
| 阶段二 | 核心改造 - Gateway 配置为 Resource Server | 3-4小时 | ⏳ 待开始 |
| 阶段三 | 业务改造 - 后端服务简化（移除独立验证） | 2-3小时 | ⏳ 待开始 |
| 阶段四 | 集成测试与验证 | 2-3小时 | ⏳ 待开始 |
| 阶段五 | 清理与优化 | 1-2小时 | ⏳ 待开始 |

---

## 🔧 阶段一：准备阶段 - Authorization Server 配置优化

### 目标
确保 Authorization Server (service-auth) 配置完整，支持 Resource Server 验证 Token。

### 步骤 1.1：配置 Authorization Server 的 Issuer URI

**目标**：配置授权服务器的发布者 URI，用于 Resource Server 自动发现 JWK Set

**执行内容**：
- [ ] 修改 `service-auth/src/main/resources/dev/application.yml`
- [ ] 添加 `spring.security.oauth2.authorization-server.issuer-uri` 配置

**配置示例**：
```yaml
spring:
  security:
    oauth2:
      authorization-server:
        issuer-uri: http://localhost:8080  # 开发环境
        # 生产环境使用实际域名
```

**预期结果**：
- Authorization Server 启动后，可以通过 `{issuer-uri}/.well-known/oauth-authorization-server` 访问元数据
- 可以通过 `{issuer-uri}/oauth2/jwks` 访问 JWK Set

**验证方式**：
```bash
# 访问元数据端点
curl http://localhost:8080/.well-known/oauth-authorization-server

# 访问 JWK Set 端点
curl http://localhost:8080/oauth2/jwks
```

**状态**：⏳ 待完成

---

### 步骤 1.2：配置 Token 增强器，在 Token 中携带用户信息和权限

**目标**：配置 OAuth2 Token 自定义器，在 JWT Token 中携带用户 ID、用户名、权限等信息

**执行内容**：
- [ ] 创建或修改 `service-auth/src/main/java/com/scccy/service/auth/config/TokenCustomizerConfig.java`
- [ ] 创建 `OAuth2TokenCustomizer<JwtEncodingContext>` Bean
- [ ] 从系统服务获取用户权限信息并添加到 Token claims

**代码示例**：
```java
@Configuration
public class TokenCustomizerConfig {

    @Autowired
    private SystemUserClient systemUserClient;

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            Authentication principal = context.getPrincipal();
            String username = principal.getName();
            
            // 从系统服务获取用户权限
            // 这里需要根据实际情况调用 service-system 获取权限
            List<String> authorities = getUserAuthorities(username);
            
            // 添加自定义 claims
            context.getClaims().claim("authorities", authorities);
            context.getClaims().claim("username", username);
            
            // 如果需要用户ID，可以从 Authentication 中获取或从数据库查询
            Long userId = getUserId(username);
            if (userId != null) {
                context.getClaims().claim("userId", userId);
            }
        };
    }
    
    private List<String> getUserAuthorities(String username) {
        // TODO: 调用 service-system 获取用户权限
        // 返回权限列表，如：["USER_READ", "USER_WRITE", "ROLE_ADMIN"]
        return Collections.emptyList();
    }
    
    private Long getUserId(String username) {
        // TODO: 从 Authentication 或数据库获取用户ID
        return null;
    }
}
```

**预期结果**：
- 生成的 JWT Token 中包含 `authorities`、`username`、`userId` 等自定义 claims
- Resource Server 可以从 Token 中提取这些信息

**验证方式**：
1. 获取 Access Token
2. 解析 Token（使用 jwt.io 或代码解析）
3. 确认 Token 中包含自定义 claims

**状态**：⏳ 待完成

---

### 步骤 1.3：确保 JWK Set 端点可访问

**目标**：验证 JWK Set 端点正常工作，Resource Server 可以获取公钥

**执行内容**：
- [ ] 检查 `AuthorizationServerConfig` 中是否配置了 Resource Server
- [ ] 验证 `/oauth2/jwks` 端点可访问

**预期结果**：
- 访问 `http://localhost:8080/oauth2/jwks` 返回 JSON 格式的密钥集合
- 返回格式符合 JWK Set 标准

**验证方式**：
```bash
curl http://localhost:8080/oauth2/jwks
```

**预期响应示例**：
```json
{
  "keys": [
    {
      "kty": "RSA",
      "e": "AQAB",
      "n": "...",
      "kid": "..."
    }
  ]
}
```

**状态**：⏳ 待完成

---

### 步骤 1.4：迁移 JwtUtils 工具类到 common-base

**目标**：将 JWT 工具类迁移到 common-base 模块，供 Authorization Server、Gateway 和后端服务共同使用

**执行内容**：
- [ ] 检查现有的 `service-system/src/main/java/com/scccy/service/system/utils/JwtUtils.java`
- [ ] 创建 `common/common-base/src/main/java/com/scccy/common/base/utils/JwtUtils.java`
- [ ] 迁移 JWT 工具类到 common-base，移除 Token 生成方法，保留/添加从 `Jwt` 对象提取信息的方法
- [ ] 在 service-auth、gateway 的 pom.xml 中确保已依赖 common-base
- [ ] 更新 service-system 中所有使用 JwtUtils 的地方，改为使用 common-base 中的版本（暂时保留，后续步骤会移除独立验证）

**代码示例**（`common/common-base/src/main/java/com/scccy/common/base/utils/JwtUtils.java`）：
```java
package com.scccy.common.base.utils;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JWT 工具类
 * <p>
 * 提供从 Spring Security Jwt 对象提取信息的方法
 * 供 Authorization Server、Gateway 和 Resource Server 共同使用
 *
 * @author scccy
 */
@Component
public class JwtUtils {
    
    /**
     * 从 Jwt 对象提取用户ID
     */
    public Long getUserId(Jwt jwt) {
        return jwt.getClaimAsLong("userId");
    }
    
    /**
     * 从 Jwt 对象提取用户名
     */
    public String getUsername(Jwt jwt) {
        return jwt.getClaimAsString("username");
    }
    
    /**
     * 从 Jwt 对象提取权限列表
     */
    public List<String> getAuthorities(Jwt jwt) {
        return jwt.getClaimAsStringList("authorities");
    }
    
    /**
     * 从 Jwt 对象提取昵称
     */
    public String getNickName(Jwt jwt) {
        return jwt.getClaimAsString("nickName");
    }
    
    /**
     * 从 Jwt 对象提取用户状态
     */
    public Integer getStatus(Jwt jwt) {
        return jwt.getClaimAsInteger("status");
    }
    
    /**
     * 从 Jwt 对象提取自定义 claim
     */
    public <T> T getClaim(Jwt jwt, String claimName, Class<T> clazz) {
        return jwt.getClaim(claimName);
    }
}
```

**预期结果**：
- JwtUtils 工具类位于 common-base 模块中
- service-auth、gateway 都可以使用同一个 JwtUtils
- 不再有 Token 生成逻辑（统一由 Authorization Server 生成）

**状态**：⏳ 待完成

---

### 步骤 1.5：测试 Authorization Server 的 Token 签发功能

**目标**：验证 Authorization Server 可以正常签发 Token

**执行内容**：
- [ ] 使用客户端凭证模式测试 Token 签发
- [ ] 使用授权码模式测试 Token 签发（可选）
- [ ] 验证生成的 Token 格式正确

**测试用例**：
```bash
# 客户端凭证模式
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=xxx&client_secret=xxx"
```

**预期结果**：
- 成功返回 Access Token
- Token 格式为 JWT
- Token 中包含必要的 claims

**状态**：⏳ 待完成

---

## 🔧 阶段二：核心改造 - Gateway 配置为 Resource Server

### 目标
将 Gateway 配置为 Resource Server，统一验证来自 Authorization Server 的 Token，并将用户信息传递给后端服务。

### 步骤 2.1：添加 Resource Server 依赖到 Gateway

**目标**：在 Gateway 中添加 Spring Security OAuth2 Resource Server 依赖

**执行内容**：
- [ ] 检查 `gateway/pom.xml` 是否已有 `spring-boot-starter-oauth2-resource-server` 依赖
- [ ] 如果没有，添加依赖

**依赖示例**：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

**注意**：Gateway 使用 WebFlux（响应式），需要确保依赖与 WebFlux 兼容

**预期结果**：
- Gateway 项目依赖中包含 Resource Server 相关类库
- 可以正常编译

**状态**：⏳ 待完成

---

### 步骤 2.2：配置 Gateway 作为 Resource Server

**目标**：配置 Gateway 作为 Resource Server，验证来自 Authorization Server 的 Token

**执行内容**：
- [ ] 创建 `gateway/src/main/java/com/scccy/gateway/config/ResourceServerConfig.java`
- [ ] 配置 OAuth2 Resource Server（WebFlux 版本）
- [ ] 配置 JWT 解码器，从 Authorization Server 获取 JWK Set

**代码示例**：
```java
package com.scccy.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Gateway Resource Server 配置
 * <p>
 * Gateway 作为 Resource Server，统一验证 Token
 * 使用 WebFlux（响应式）安全配置
 *
 * @author scccy
 */
@Configuration
@EnableWebFluxSecurity
public class ResourceServerConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchanges -> exchanges
                // 公开端点：OAuth2 相关、登录、健康检查等
                .pathMatchers("/oauth2/**", "/login", "/actuator/**", "/doc.html", "/swagger-ui/**").permitAll()
                // 其他路径需要认证
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwkSetUri(issuerUri + "/oauth2/jwks")  // 或使用 issuer-uri 自动发现
                    // .issuerLocation(issuerUri)  // 自动发现配置
                )
            )
            .csrf(csrf -> csrf.disable());  // Gateway 通常禁用 CSRF
        
        return http.build();
    }
}
```

**配置示例**（在 `gateway/src/main/resources/dev/application.yml` 中配置）：
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080  # Authorization Server 地址
          # 或显式配置
          # jwk-set-uri: http://localhost:8080/oauth2/jwks
```

**预期结果**：
- Gateway 启动后，除公开端点外，所有路径需要携带有效的 Access Token
- Token 验证通过后，可以从 `ReactiveSecurityContext` 中获取用户信息

**验证方式**：
1. 启动 Gateway
2. 访问业务接口，不携带 Token，应返回 401
3. 携带有效的 Access Token，应返回正常响应

**状态**：⏳ 待完成

---

### 步骤 2.3：创建自定义 Gateway Filter，传递用户信息到后端服务

**目标**：创建自定义 Gateway Filter，从 Token 中提取用户信息并添加到请求头，传递给后端服务

**执行内容**：
- [ ] 创建 `gateway/src/main/java/com/scccy/gateway/filter/UserInfoGatewayFilter.java`
- [ ] 从 ReactiveSecurityContext 中获取 Jwt 对象
- [ ] 使用 common-base 中的 `JwtUtils` 工具类提取用户信息（userId, username, authorities 等）
- [ ] 将用户信息添加到请求头
- [ ] 在 Gateway 路由配置中应用该 Filter

**注意**：此步骤需要使用阶段一迁移的 `JwtUtils` 工具类

**代码示例**：
```java
package com.scccy.gateway.filter;

import com.scccy.common.base.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 用户信息网关过滤器
 * <p>
 * 从 Token 中提取用户信息，添加到请求头传递给后端服务
 *
 * @author scccy
 */
@Slf4j
@Component
public class UserInfoGatewayFilter extends AbstractGatewayFilterFactory<Object> {

    private final JwtUtils jwtUtils;

    public UserInfoGatewayFilter(JwtUtils jwtUtils) {
        super(Object.class);
        this.jwtUtils = jwtUtils;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return new OrderedGatewayFilter((exchange, chain) -> {
            return ReactiveSecurityContextHolder.getContext()
                .cast(org.springframework.security.core.context.SecurityContext.class)
                .map(securityContext -> securityContext.getAuthentication())
                .cast(org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken.class)
                .map(jwtAuth -> jwtAuth.getToken())
                .flatMap(jwt -> {
                    // 使用 JwtUtils 工具类提取用户信息
                    Long userId = jwtUtils.getUserId(jwt);
                    String username = jwtUtils.getUsername(jwt);
                    List<String> authorities = jwtUtils.getAuthorities(jwt);
                    
                    log.debug("提取用户信息: userId={}, username={}, authorities={}", 
                        userId, username, authorities);
                    
                    // 添加用户信息到请求头
                    ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(request -> request.mutate()
                            .header("X-User-Id", userId != null ? String.valueOf(userId) : "")
                            .header("X-Username", username != null ? username : "")
                            .header("X-Authorities", authorities != null ? String.join(",", authorities) : "")
                            .build())
                        .build();
                    
                    return chain.filter(modifiedExchange);
                })
                .switchIfEmpty(chain.filter(exchange));  // 如果没有认证信息，继续转发
        }, -100);  // 在路由之前执行
    }
}
```

**预期结果**：
- Gateway 验证 Token 后，自动提取用户信息
- 用户信息添加到请求头，传递给后端服务

**状态**：⏳ 待完成

---

### 步骤 2.4：配置 Gateway 路由，应用用户信息 Filter

**目标**：在 Gateway 路由配置中应用用户信息 Filter

**执行内容**：
- [ ] 修改 Nacos 配置中心的 `gateway.yaml` 或 `gateway/src/main/resources/dev/application.yml`
- [ ] 在路由配置中添加 `UserInfoGatewayFilter`
- [ ] 配置各个服务的路由规则

**配置示例**（在 Nacos 配置中心的 `gateway.yaml` 中）：
```yaml
spring:
  cloud:
    gateway:
      routes:
        # 系统服务路由
        - id: system-service
          uri: lb://service-system
          predicates:
            - Path=/api/system/**
          filters:
            - StripPrefix=1
            - name: UserInfo  # 应用用户信息过滤器
        
        # 企业微信服务路由
        - id: wechatwork-service
          uri: lb://service-wechatwork
          predicates:
            - Path=/wechatwork/**
          filters:
            - StripPrefix=0  # 保留 /wechatwork 前缀
            - name: UserInfo  # 应用用户信息过滤器
```

**或者通过代码配置**（如果使用 Java 配置）：
```java
@Configuration
public class GatewayRouteConfig {
    
    @Autowired
    private UserInfoGatewayFilter userInfoGatewayFilter;
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("system-service", r -> r
                .path("/api/system/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter(userInfoGatewayFilter.apply(null))
                )
                .uri("lb://service-system")
            )
            .route("wechatwork-service", r -> r
                .path("/wechatwork/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .filter(userInfoGatewayFilter.apply(null))
                )
                .uri("lb://service-wechatwork")
            )
            .build();
    }
}
```

**预期结果**：
- Gateway 路由配置中包含用户信息 Filter
- 所有经过网关的请求都会自动添加用户信息到请求头

**状态**：⏳ 待完成

---

### 步骤 2.5：移除 service-system 中的 Token 生成逻辑

**目标**：移除 `UserService.login()` 中的 Token 生成逻辑，改为调用 Authorization Server

**执行内容**：
- [ ] 修改 `service-system/src/main/java/com/scccy/service/system/service/UserService.java`
- [ ] 移除 `generateUserToken()` 方法或标记为废弃
- [ ] 修改 `login()` 方法，改为调用 Authorization Server 的 Token 端点

**改造方案 A：直接调用 Authorization Server**
```java
@Service
public class UserService {
    
    @Autowired
    private AuthServerClient authServerClient;  // 需要创建 Feign Client
    
    public LoginResponse login(String username, String password) {
        // 1. 验证用户名密码（本地验证或调用 Authorization Server）
        // ... 验证逻辑 ...
        
        // 2. 调用 Authorization Server 获取 Token
        TokenResponse tokenResponse = authServerClient.getToken(
            "password",  // grant_type
            username,
            password,
            "system-client",  // client_id
            "client-secret"   // client_secret
        );
        
        // 3. 构建登录响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(tokenResponse.getAccessToken());
        loginResponse.setRefreshToken(tokenResponse.getRefreshToken());
        // ... 其他字段 ...
        
        return loginResponse;
    }
}
```

**改造方案 B：返回授权 URL（适用于授权码模式）**
```java
public LoginResponse login(String username, String password) {
    // 1. 验证用户名密码
    // ... 验证逻辑 ...
    
    // 2. 返回授权 URL，前端重定向到 Authorization Server
    String authUrl = "http://localhost:8080/oauth2/authorize?" +
        "client_id=system-client&" +
        "response_type=code&" +
        "redirect_uri=http://frontend/redirect&" +
        "scope=openid profile";
    
    LoginResponse response = new LoginResponse();
    response.setAuthUrl(authUrl);
    return response;
}
```

**预期结果**：
- service-system 不再自己生成 JWT Token
- 登录接口返回的 Token 来自 Authorization Server

**状态**：⏳ 待完成

---

### 步骤 2.6：测试 Gateway 作为 Resource Server

**目标**：验证 Gateway 可以正确验证来自 Authorization Server 的 Token，并将用户信息传递给后端服务

**执行内容**：
- [ ] 启动 Authorization Server (service-auth)
- [ ] 启动 Gateway
- [ ] 启动后端服务（service-system 或 service-wechatwork）
- [ ] 获取 Access Token（从 Authorization Server）
- [ ] 通过 Gateway 访问后端服务接口
- [ ] 验证后端服务可以接收到用户信息

**测试用例**：
```bash
# 1. 获取 Token（需要根据实际情况调整）
TOKEN=$(curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=xxx&client_secret=xxx" \
  | jq -r '.access_token')

# 2. 通过 Gateway 访问后端服务接口
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/wechatwork/xxx

# 3. 验证后端服务日志中可以看到用户信息请求头
# X-User-Id: 123
# X-Username: testuser
# X-Authorities: USER_READ,USER_WRITE
```

**预期结果**：
- Gateway 可以正确验证 Token
- 携带有效 Token 可以正常访问后端服务接口
- 不携带 Token 或 Token 无效返回 401
- 后端服务可以从请求头获取用户信息

**状态**：⏳ 待完成

---

## 🔧 阶段三：业务改造 - 后端服务简化（移除独立验证）

### 目标
简化后端服务（service-system、service-wechatwork），从网关传递的请求头中获取用户信息，移除独立的 Token 验证逻辑。

### 步骤 3.1：更新后端服务，从请求头获取用户信息

**目标**：修改后端服务（service-system、service-wechatwork）的 Controller，从网关传递的请求头中获取用户信息

**执行内容**：
- [ ] 检查 `service-system` 和 `service-wechatwork` 的 Controller
- [ ] 修改需要用户信息的接口，从请求头获取用户信息
- [ ] 使用 `@RequestHeader` 注解获取 `X-User-Id`、`X-Username`、`X-Authorities` 等

**代码示例**（service-wechatwork）：
```java
@RestController
@RequestMapping("/wechatwork")
public class WechatWorkController {
    
    @GetMapping("/users/{id}")
    public Result getUser(@PathVariable Long id,
                         @RequestHeader("X-User-Id") Long userId,
                         @RequestHeader("X-Username") String username) {
        // 从请求头获取用户信息
        log.info("用户 {} (ID: {}) 访问用户信息接口", username, userId);
        
        // 执行业务逻辑
        // ...
    }
    
    // 企业微信回调接口（不需要用户信息，网关会放行）
    @GetMapping("/callBack")
    public String callBack(@RequestParam String msg_signature,
                           @RequestParam String nonce,
                           @RequestParam String timestamp,
                           @RequestParam String echostr) {
        // 回调接口逻辑
        // ...
    }
}
```

**代码示例**（service-system）：
```java
@RestController
@RequestMapping("/api/system")
public class SystemController {
    
    @GetMapping("/users/{id}")
    public Result getUser(@PathVariable Long id,
                         @RequestHeader("X-User-Id") Long userId,
                         @RequestHeader("X-Username") String username,
                         @RequestHeader(value = "X-Authorities", required = false) String authorities) {
        // 从请求头获取用户信息
        log.info("用户 {} (ID: {}) 访问用户信息接口", username, userId);
        
        // 权限检查（如果需要）
        if (authorities != null && !authorities.contains("USER_READ")) {
            throw new AccessDeniedException("无权限访问");
        }
        
        // 执行业务逻辑
        // ...
    }
}
```

**预期结果**：
- 后端服务可以从请求头获取用户信息
- 不需要独立的 Token 验证逻辑

**状态**：⏳ 待完成

---

### 步骤 3.2：创建用户信息工具类（可选）

**目标**：创建工具类，方便从请求头提取用户信息

**执行内容**：
- [ ] 创建 `common/common-base/src/main/java/com/scccy/common/base/utils/UserInfoUtils.java`
- [ ] 提供从 `HttpServletRequest` 提取用户信息的方法

**代码示例**：
```java
package com.scccy.common.base.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 用户信息工具类
 * <p>
 * 从请求头中提取网关传递的用户信息
 *
 * @author scccy
 */
@Component
public class UserInfoUtils {
    
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_AUTHORITIES = "X-Authorities";
    
    /**
     * 从请求头获取用户ID
     */
    public Long getUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader(HEADER_USER_ID);
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 从请求头获取用户名
     */
    public String getUsername(HttpServletRequest request) {
        return request.getHeader(HEADER_USERNAME);
    }
    
    /**
     * 从请求头获取权限列表
     */
    public List<String> getAuthorities(HttpServletRequest request) {
        String authoritiesStr = request.getHeader(HEADER_AUTHORITIES);
        if (authoritiesStr != null && !authoritiesStr.isEmpty()) {
            return Arrays.asList(authoritiesStr.split(","));
        }
        return Collections.emptyList();
    }
    
    /**
     * 检查用户是否有指定权限
     */
    public boolean hasAuthority(HttpServletRequest request, String authority) {
        List<String> authorities = getAuthorities(request);
        return authorities.contains(authority);
    }
}
```

**预期结果**：
- 提供统一的工具类，方便从请求头提取用户信息
- 后端服务可以使用该工具类

**状态**：⏳ 待完成

---

### 步骤 3.3：简化后端服务安全配置（可选）

**目标**：如果后端服务不需要直接访问（都通过网关），可以简化安全配置

**执行内容**：
- [ ] 检查后端服务的安全配置
- [ ] 如果所有请求都通过网关，可以移除 Resource Server 配置
- [ ] 保留必要的安全配置（如回调接口公开访问）

**代码示例**（service-wechatwork - 简化版）：
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/wechatwork/callBack").permitAll()  // 企业微信回调接口公开
                .anyRequest().authenticated()  // 其他接口需要网关传递的用户信息
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf -> csrf.disable());
        
        // 注意：这里不配置 Resource Server，因为 Token 验证在网关层完成
        // 如果后端服务可能被直接访问（绕过网关），可以配置 Resource Server 作为备用验证
        
        return http.build();
    }
}
```

**预期结果**：
- 后端服务安全配置简化
- 如果所有请求都通过网关，不需要配置 Resource Server

**状态**：⏳ 待完成

---

### 步骤 3.4：测试后端服务从请求头获取用户信息

**目标**：验证后端服务可以正确从请求头获取用户信息

**执行内容**：
- [ ] 启动所有服务（Authorization Server、Gateway、后端服务）
- [ ] 获取 Access Token
- [ ] 通过 Gateway 访问后端服务接口
- [ ] 验证后端服务可以正确获取用户信息

**测试用例**：
```bash
# 1. 获取 Token
TOKEN=$(curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=xxx&client_secret=xxx" \
  | jq -r '.access_token')

# 2. 通过 Gateway 访问后端服务接口
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/wechatwork/xxx

# 3. 验证后端服务日志中可以看到用户信息请求头
# X-User-Id: 123
# X-Username: testuser
# X-Authorities: USER_READ,USER_WRITE
```

**预期结果**：
- 后端服务可以正确从请求头获取用户信息
- 业务逻辑可以正常执行

**状态**：⏳ 待完成

---

## 🔧 阶段四：集成测试与验证

### 目标
进行端到端测试，验证整个 OAuth2 流程正常工作。

### 步骤 4.1：测试授权码模式（如适用）

**目标**：验证授权码模式完整流程

**执行内容**：
- [ ] 前端访问 `/oauth2/authorize`
- [ ] 用户登录认证
- [ ] 用户授权同意
- [ ] 获取授权码
- [ ] 使用授权码换取 Token
- [ ] 使用 Token 访问 Resource Server

**预期结果**：
- 授权码模式流程完整，可以正常获取 Token 并访问资源

**状态**：⏳ 待完成

---

### 步骤 4.2：测试客户端凭证模式

**目标**：验证服务间调用使用客户端凭证模式

**执行内容**：
- [ ] 使用客户端凭证获取 Token
- [ ] 使用 Token 访问 Resource Server

**测试用例**：
```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=xxx&client_secret=xxx"
```

**预期结果**：
- 可以成功获取 Token
- Token 可以用于访问 Resource Server

**状态**：⏳ 待完成

---

### 步骤 4.3：测试 Token 刷新

**目标**：验证 Refresh Token 可以正常刷新 Access Token

**执行内容**：
- [ ] 获取 Access Token 和 Refresh Token
- [ ] 使用 Refresh Token 刷新 Access Token

**测试用例**：
```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token&refresh_token=xxx&client_id=xxx&client_secret=xxx"
```

**预期结果**：
- 可以使用 Refresh Token 获取新的 Access Token

**状态**：⏳ 待完成

---

### 步骤 4.4：测试权限控制

**目标**：验证 Token 中的权限信息可以用于权限控制

**执行内容**：
- [ ] 获取包含权限的 Token
- [ ] 访问需要特定权限的接口
- [ ] 验证权限不足时返回 403

**预期结果**：
- 权限控制正常工作
- 有权限可以访问，无权限返回 403

**状态**：⏳ 待完成

---

### 步骤 4.5：测试跨服务访问

**目标**：验证使用同一个 Token 可以通过 Gateway 访问不同的后端服务

**执行内容**：
- [ ] 获取 Access Token
- [ ] 使用同一个 Token 通过 Gateway 访问 service-system 和 service-wechatwork
- [ ] 验证 Gateway 统一验证 Token，并将用户信息传递给不同的后端服务

**测试用例**：
```bash
# 1. 获取 Token
TOKEN=$(curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=xxx&client_secret=xxx" \
  | jq -r '.access_token')

# 2. 使用同一个 Token 访问不同的后端服务
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/system/users/1
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/wechatwork/xxx
```

**预期结果**：
- 同一个 Token 可以通过 Gateway 访问不同的后端服务
- Gateway 统一验证 Token，后端服务从请求头获取用户信息

**状态**：⏳ 待完成

---

### 步骤 4.6：测试 Token 黑名单（如需要）

**目标**：验证 Token 加入黑名单后无法使用

**执行内容**：
- [ ] 获取 Access Token
- [ ] 将 Token 加入黑名单
- [ ] 尝试使用 Token 访问 Resource Server
- [ ] 验证返回 401

**预期结果**：
- 黑名单中的 Token 无法使用

**状态**：⏳ 待完成

---

## 🔧 阶段五：清理与优化

### 目标
清理冗余代码，优化配置，完善文档。

### 步骤 5.1：移除冗余代码

**目标**：移除不再使用的代码

**执行内容**：
- [ ] 移除 service-system 中不再使用的 JWT 生成相关代码（`JwtUtils.generateToken()` 等）
- [ ] 移除 service-system 中旧的 `JwtUtils` 类（已迁移到 common-base）
- [ ] 确保所有服务都使用 common-base 中的 `JwtUtils`
- [ ] 清理未使用的依赖（如不需要）

**预期结果**：
- 代码库中不再有冗余的 Token 生成逻辑
- 所有服务统一使用 common-base 中的 JWT 工具类

**状态**：⏳ 待完成

---

### 步骤 5.2：统一配置管理

**目标**：统一各服务的配置，使用配置中心或环境变量

**执行内容**：
- [ ] 统一 Authorization Server 的 issuer-uri 配置
- [ ] 统一 Resource Server 的 issuer-uri 配置
- [ ] 使用配置中心管理配置（如 Nacos）

**预期结果**：
- 配置统一管理，易于维护

**状态**：⏳ 待完成

---

### 步骤 5.3：优化性能

**目标**：优化 Token 验证性能

**执行内容**：
- [ ] 配置 JWK Set 缓存（Spring Security 默认会缓存）
- [ ] 检查 Token 验证性能
- [ ] 优化权限查询性能（如需要）

**预期结果**：
- Token 验证性能满足要求

**状态**：⏳ 待完成

---

### 步骤 5.4：更新文档

**目标**：更新相关文档，说明新的架构和使用方式

**执行内容**：
- [ ] 更新 README.md，说明 OAuth2 架构
- [ ] 更新 API 文档，说明如何获取和使用 Token
- [ ] 添加使用示例和最佳实践

**预期结果**：
- 文档完整，开发者可以快速上手

**状态**：⏳ 待完成

---

## 📝 注意事项

### 1. 向后兼容性
- 如果现有客户端已经使用旧的 Token，需要过渡期支持两套 Token
- 可以配置两个 SecurityFilterChain，分别处理旧 Token 和新 Token

### 2. 密钥管理
- 生产环境必须使用强随机密钥
- 建议使用 RSA 密钥对，而不是 HMAC
- 密钥需要妥善保管，不要提交到代码仓库

### 3. Token 过期时间
- Access Token 建议设置较短的过期时间（如 1-2 小时）
- Refresh Token 可以设置较长的过期时间（如 7-30 天）

### 4. 安全建议
- 使用 HTTPS 传输 Token
- 实施 Token 黑名单机制
- 定期轮换密钥
- 监控异常 Token 使用情况

### 5. 错误处理
- 统一处理 401（未授权）和 403（权限不足）错误
- 提供清晰的错误信息

---

## 🎯 完成检查清单

### 阶段一：准备阶段
- [ ] 步骤 1.1：配置 Issuer URI
- [ ] 步骤 1.2：配置 Token 增强器
- [ ] 步骤 1.3：验证 JWK Set 端点
- [ ] 步骤 1.4：迁移 JwtUtils 工具类到 common-base
- [ ] 步骤 1.5：测试 Token 签发

### 阶段二：核心改造 - Gateway 配置为 Resource Server
- [ ] 步骤 2.1：添加 Resource Server 依赖到 Gateway
- [ ] 步骤 2.2：配置 Gateway 作为 Resource Server
- [ ] 步骤 2.3：创建自定义 Gateway Filter，传递用户信息到后端服务
- [ ] 步骤 2.4：配置 Gateway 路由，应用用户信息 Filter
- [ ] 步骤 2.5：移除 service-system 中的 Token 生成逻辑
- [ ] 步骤 2.6：测试 Gateway 作为 Resource Server

### 阶段三：业务改造 - 后端服务简化
- [ ] 步骤 3.1：更新后端服务，从请求头获取用户信息
- [ ] 步骤 3.2：创建用户信息工具类（可选）
- [ ] 步骤 3.3：简化后端服务安全配置（可选）
- [ ] 步骤 3.4：测试后端服务从请求头获取用户信息

### 阶段四：集成测试
- [ ] 步骤 4.1：测试授权码模式
- [ ] 步骤 4.2：测试客户端凭证模式
- [ ] 步骤 4.3：测试 Token 刷新
- [ ] 步骤 4.4：测试权限控制
- [ ] 步骤 4.5：测试跨服务访问
- [ ] 步骤 4.6：测试 Token 黑名单

### 阶段五：清理优化
- [ ] 步骤 5.1：移除冗余代码
- [ ] 步骤 5.2：统一配置管理
- [ ] 步骤 5.3：优化性能
- [ ] 步骤 5.4：更新文档

---

## 📞 问题与支持

如果在改造过程中遇到问题，可以：

1. 查看 Spring Security OAuth2 Resource Server 官方文档
2. 检查日志，定位问题
3. 参考 Spring Authorization Server 示例代码
4. 查看本文档的相关步骤说明

---

**文档版本**：v1.0  
**创建日期**：2025-01-XX  
**最后更新**：2025-01-XX
