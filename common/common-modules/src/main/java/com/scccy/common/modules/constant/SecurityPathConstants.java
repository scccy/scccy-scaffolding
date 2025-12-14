package com.scccy.common.modules.constant;

/**
 * Spring Security 路径常量
 * <p>
 * 统一管理 Gateway 和业务服务的公开访问路径，确保配置一致性。
 * <p>
 * 使用场景：
 * <ul>
 *     <li>Gateway Resource Server 配置（WebFlux）</li>
 *     <li>业务服务 Resource Server 配置（Servlet）</li>
 *     <li>Authorization Server 配置</li>
 * </ul>
 * <p>
 * 注意：
 * <ul>
 *     <li>这些路径在 Gateway 和业务服务中都应该配置为 permitAll()</li>
 *     <li>修改路径时，需要同步更新所有使用这些常量的配置类</li>
 * </ul>
 *
 * @author scccy
 * @since 2025-01-XX
 */
public class SecurityPathConstants {

    /**
     * 私有构造函数，防止实例化
     */
    private SecurityPathConstants() {
        throw new UnsupportedOperationException("常量类不能被实例化");
    }

    /**
     * 公开端点路径
     * <p>
     * 这些路径不需要 Token 验证，包括：
     * <ul>
     *     <li>OAuth2 相关端点</li>
     *     <li>登录端点</li>
     *     <li>健康检查端点</li>
     *     <li>API 文档相关端点</li>
     * </ul>
     * <p>
     * 注意：Spring Security 6.x 路径模式规则：
     * <ul>
     *     <li>{@code /**} 必须放在路径末尾</li>
     *     <li>路径模式必须符合 Spring Security 6.x 的解析规则</li>
     * </ul>
     */
    public static final String[] PUBLIC_ENDPOINTS = {
        "/oauth2/**",
        "/login",
        "/actuator/**",
        "/doc.html",           // Knife4j 文档页面
        "/swagger-ui/**",      // Swagger UI 资源（包含 /swagger-ui.html）
        "/v3/api-docs/**",     // API 文档资源
        "/swagger-resources/**", // Swagger 资源
        "/webjars/**"          // webjars 资源（包含 Knife4j 的所有静态资源）
        // 注意：不包含 /doc.html/**，因为：
        // 1. /doc.html 本身已经匹配文档页面
        // 2. /doc.html 下的资源（JS/CSS）会通过 /webjars/** 匹配（Knife4j 的资源都在 webjars 中）
        // 3. 避免潜在的路径模式解析问题
    };

    /**
     * {@code @Anonymous} 注解支持的路径约定
     * <p>
     * 使用 {@code @Anonymous} 注解标记的接口需要使用这些路径约定：
     * <ul>
     *     <li>路径包含 /public（如：/demo/permission/public）</li>
     *     <li>路径包含 /anonymous（如：/api/user/anonymous）</li>
     *     <li>路径包含 /internal（如：/demo/token/internal/user）</li>
     * </ul>
     * <p>
     * 示例：
     * <pre>
     * &#64;Anonymous
     * &#64;GetMapping("/public")
     * public ResultData&lt;?&gt; publicEndpoint() {
     *     // 公开接口，不需要 Token
     * }
     * </pre>
     * <p>
     * 注意：Spring Security 6.x 路径模式规则：
     * <ul>
     *     <li>{@code /**} 必须放在路径末尾</li>
     *     <li>使用 {@code /**\/public} 匹配包含 {@code /public} 的路径（如：/demo/permission/public）</li>
     *     <li>使用 {@code /**\/anonymous} 匹配包含 {@code /anonymous} 的路径</li>
     *     <li>使用 {@code /**\/internal} 匹配包含 {@code /internal} 的路径</li>
     * </ul>
     */
    public static final String[] ANONYMOUS_PATHS = {
        "/**/public",      // 匹配包含 /public 的路径（如：/demo/permission/public）
        "/**/anonymous",   // 匹配包含 /anonymous 的路径（如：/api/user/anonymous）
        "/**/internal"     // 匹配包含 /internal 的路径（如：/demo/token/internal/user）
    };

    /**
     * Authorization Server 专用公开端点
     * <p>
     * 这些路径仅在 Authorization Server（service-auth）中使用
     * <p>
     * 注意：Spring Security 6.x 路径模式规则：
     * <ul>
     *     <li>{@code /**} 必须放在路径末尾</li>
     *     <li>路径模式必须符合 Spring Security 6.x 的解析规则</li>
     * </ul>
     */
    public static final String[] AUTHORIZATION_SERVER_PUBLIC_ENDPOINTS = {
        "/login",
        "/oauth2/**",
        "/.well-known/**",
        "/actuator/**",
        "/doc.html",           // Knife4j 文档页面
        "/swagger-ui/**",      // Swagger UI 资源（包含 /swagger-ui.html）
        "/v3/api-docs/**",     // API 文档资源
        "/swagger-resources/**", // Swagger 资源
        "/webjars/**"          // webjars 资源（包含 Knife4j 的所有静态资源）
        // 注意：不包含 /doc.html/**，因为：
        // 1. /doc.html 本身已经匹配文档页面
        // 2. /doc.html 下的资源（JS/CSS）会通过 /webjars/** 匹配（Knife4j 的资源都在 webjars 中）
        // 3. 避免潜在的路径模式解析问题
    };
}

