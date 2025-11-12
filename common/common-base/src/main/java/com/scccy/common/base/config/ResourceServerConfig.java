package com.scccy.common.base.config;

import com.alibaba.fastjson2.JSON;
import com.scccy.common.modules.constant.SecurityPathConstants;
import com.scccy.common.modules.dto.ResultData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.server.PathContainer.parsePath;

/**
 * Resource Server 基础配置
 * <p>
 * 为所有使用 {@code @ScccyServiceApplication} 的服务提供基础的 Resource Server 配置。
 * <p>
 * 架构设计：
 * <ul>
 *     <li>所有服务默认需要 Token 验证（作为 Resource Server）</li>
 *     <li>不需要验证的接口通过 {@code @Anonymous} 注解标记，系统会自动扫描并放行</li>
 *     <li>这样架构更统一，也更安全（双重验证：Gateway + 业务服务）</li>
 * </ul>
 * <p>
 * 默认配置：
 * <ul>
 *     <li>启用 OAuth2 Resource Server（验证 Token）</li>
 *     <li>支持 {@code @Anonymous} 注解的路径自动放行（通过 {@code PermitAllUrlProperties} 动态收集）</li>
 *     <li>其他路径需要认证</li>
 *     <li>禁用 CSRF（因为使用 JWT Token）</li>
 *     <li>无状态会话管理（STATELESS）</li>
 * </ul>
 * <p>
 * 配置要求：
 * <ul>
 *     <li>必须在服务的 application.yml 中配置 {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}</li>
 *     <li>如果没有配置 issuer-uri，Resource Server 无法工作，服务启动会失败</li>
 * </ul>
 * <p>
 * 配置示例（在服务的 application.yml 或 Nacos 配置中）：
 * <pre>
 * spring:
 *   security:
 *     oauth2:
 *       resourceserver:
 *         jwt:
 *           issuer-uri: http://service-auth:30003  # Authorization Server 地址
 * </pre>
 * <p>
 * {@code @Anonymous} 注解使用示例：
 * <pre>
 * &#64;Anonymous
 * &#64;GetMapping(&quot;/public&quot;)
 * public ResultData&lt;?&gt; publicEndpoint() {
 *     // 公开接口，不需要 Token
 * }
 * </pre>
 * <p>
 * 注意：{@code @Anonymous} 注解的使用：
 * <ul>
 *     <li>支持方法级别和类级别的 {@code @Anonymous} 注解</li>
 *     <li>系统会在启动时自动扫描所有标记了 {@code @Anonymous} 的接口，无需手动配置路径</li>
 *     <li>路径变量（如 {@code {id}}）会自动替换为 {@code *}，以支持路径匹配</li>
 * </ul>
 *
 * @author scccy
 * @since 2025-01-XX
 */
@Slf4j
@Configuration
@EnableWebSecurity
@ConditionalOnClass(HttpSecurity.class)
@ConditionalOnProperty(
    name = "spring.security.oauth2.resourceserver.jwt.issuer-uri",
    matchIfMissing = false
)
public class ResourceServerConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    /**
     * 动态收集的匿名访问 URL（可选，如果存在则使用，否则使用路径约定）
     */
    @Autowired(required = false)
    private PermitAllUrlProperties permitAllUrlProperties;


    /**
     * Resource Server SecurityFilterChain 配置
     * <p>
     * 所有服务默认需要 Token 验证，通过 {@code @Anonymous} 注解标记的接口可以放行。
     * <p>
     * 路径匹配规则：
     * <ul>
     *     <li>公开端点：OAuth2、登录、健康检查、API 文档等</li>
     *     <li>{@code @Anonymous} 注解路径：动态收集（通过 {@code PermitAllUrlProperties}）</li>
     *     <li>其他路径：需要认证</li>
     * </ul>
     * <p>
     * 注意：不再使用路径约定（如 /public、/anonymous、/internal），
     * 所有匿名访问路径必须通过 {@code @Anonymous} 注解标记。
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("配置业务服务 Resource Server，issuer-uri: {}", issuerUri);
        log.info("公开端点路径: {}", java.util.Arrays.toString(SecurityPathConstants.PUBLIC_ENDPOINTS));
        
        // 获取匿名访问路径：仅使用动态收集的路径（通过 @Anonymous 注解）
        List<String> anonymousPaths = new ArrayList<>();
        if (permitAllUrlProperties != null && !permitAllUrlProperties.getUrls().isEmpty()) {
            anonymousPaths.addAll(permitAllUrlProperties.getUrls());
            log.info("使用动态收集的匿名访问路径，共 {} 个: {}", anonymousPaths.size(), anonymousPaths);
        } else {
            log.warn("未找到 @Anonymous 注解标记的路径，请确保使用 @Anonymous 注解标记需要匿名访问的接口");
        }

        // 优化性能：使用 securityMatcher 排除静态资源路径
        // 静态资源请求将完全跳过 Spring Security 过滤器链，直接由 Spring MVC 处理
        // 这样可以显著提升静态资源加载速度
        RequestMatcher staticResourceMatcher = createStaticResourceMatcher();
        RequestMatcher securityMatcher = new NegatedRequestMatcher(staticResourceMatcher);

        http
            // 使用 securityMatcher 精确匹配，排除静态资源路径
            // 静态资源请求将完全跳过此过滤器链，提升性能
            .securityMatcher(securityMatcher)
            // 禁用 CSRF（因为使用 JWT Token，不需要 CSRF 保护）
            .csrf(AbstractHttpConfigurer::disable)
            // 配置会话管理为无状态（因为使用 JWT）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置路径授权规则
            .authorizeHttpRequests(auth -> {
                // 公开端点：OAuth2 相关、登录、健康检查、API 文档等
                auth.requestMatchers(SecurityPathConstants.PUBLIC_ENDPOINTS).permitAll();
                
                // 支持 @Anonymous 注解的路径（动态收集或路径约定）
                if (!anonymousPaths.isEmpty()) {
                    auth.requestMatchers(anonymousPaths.toArray(new String[0])).permitAll();
                }
                
                // 其他路径需要认证
                auth.anyRequest().authenticated();
            })
            // 配置异常处理：对于未认证的请求，返回 401 而不是重定向到 /login
            // Resource Server 应该返回 401，而不是重定向到登录页面
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    String requestUri = request.getRequestURI();
                    log.warn("未认证访问被拦截: {} (方法: {})", requestUri, request.getMethod());
                    log.debug("请求路径详情: URI={}, QueryString={}, ContextPath={}", 
                        requestUri, request.getQueryString(), request.getContextPath());
                    
                    // 检查是否有 Authorization 头
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader == null || authHeader.isEmpty()) {
                        log.debug("请求缺少 Authorization 头");
                    } else {
                        log.debug("请求包含 Authorization 头: {}", authHeader.startsWith("Bearer ") ? "Bearer Token" : "其他格式");
                    }
                    
                    // 使用 ResultData 统一错误响应格式
                    ResultData<Object> result = ResultData.fail(HttpStatus.UNAUTHORIZED.value(), "需要有效的访问令牌");
                    String jsonResponse = JSON.toJSONString(result);
                    
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(jsonResponse);
                })
            )
            // 配置 OAuth2 Resource Server
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    // 使用 issuer-uri 自动发现 JWK Set
                    // Spring Security 会自动从 {issuer-uri}/.well-known/oauth-authorization-server 获取配置
                    // 然后从 jwks_uri 获取 JWK Set
                    .jwkSetUri(issuerUri + "/oauth2/jwks")
                )
            );

        return http.build();
    }

    /**
     * 创建静态资源匹配器
     * <p>
     * 用于识别静态资源请求，这些请求应该完全跳过 Spring Security 过滤器链
     * 以提升静态资源加载性能
     * <p>
     * 注意：只匹配明确的静态资源路径，避免使用可能导致解析错误的模式
     * <p>
     * 使用 Spring Security 6.x 推荐的 {@code PathPatternParser} 替代已弃用的 {@code AntPathRequestMatcher}
     *
     * @return RequestMatcher 静态资源匹配器
     */
    private RequestMatcher createStaticResourceMatcher() {
        List<RequestMatcher> matchers = new ArrayList<>();
        
        // 静态资源路径模式（只使用明确的路径，避免使用 /*.js 这样的模式）
        String[] staticResourcePatterns = {
            "/webjars/**",           // webjars 资源（最重要，包含所有 JS/CSS 等）
            "/swagger-ui/**",        // Swagger UI 资源
            "/swagger-resources/**", // Swagger 资源
            "/v3/api-docs/**",       // API 文档资源
            "/favicon.ico",          // 网站图标
            "/static/**",            // 静态资源目录
            "/public/**",            // 公共资源目录
            "/resources/**"          // 资源目录
            // 注意：不包含 /doc.html/**，因为 /doc.html 本身需要经过过滤器链
            // /doc.html 下的资源会通过 /webjars/** 匹配（因为 Knife4j 的资源都在 webjars 中）
        };
        
        // 创建 PathPatternParser（Spring Security 6.x 推荐的方式）
        PathPatternParser pathPatternParser = new PathPatternParser();
        
        // 为每个模式创建 PathPattern 并包装为 RequestMatcher（替代已弃用的 AntPathRequestMatcher）
        for (String pattern : staticResourcePatterns) {
            PathPattern pathPattern = pathPatternParser.parse(pattern);
            matchers.add((request) -> pathPattern.matches(parsePath(request.getRequestURI())));
        }
        
        // 返回 OR 匹配器：匹配任意一个模式即认为是静态资源
        return new OrRequestMatcher(matchers);
    }
}

