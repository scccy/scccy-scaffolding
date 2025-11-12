package com.scccy.gateway.config;

import com.scccy.common.modules.constant.SecurityPathConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

/**
 * Gateway Resource Server 配置
 * <p>
 * Gateway 作为 Resource Server，统一验证 Token
 * 使用 WebFlux（响应式）安全配置
 *
 * @author scccy
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
public class ResourceServerConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("配置 Gateway Resource Server，issuer-uri: {}", issuerUri);

        http
            .authorizeExchange(exchanges -> exchanges
                // 公开端点：OAuth2 相关、登录、健康检查、API 文档等
                .pathMatchers(SecurityPathConstants.PUBLIC_ENDPOINTS).permitAll()
                // 注意：@Anonymous 注解的路径由业务服务动态收集并放行
                // Gateway 作为统一入口，只放行公开端点，业务服务会自行处理 @Anonymous 注解的路径
                // 其他路径需要认证
                .anyExchange().authenticated()
            )
            // 前后端分离架构：未通过身份验证时返回 401 JSON，而不是重定向到登录页面
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new JsonServerAuthenticationEntryPoint())
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    // 使用 issuer-uri 自动发现 JWK Set
                    // Spring Security 会自动从 {issuer-uri}/.well-known/oauth-authorization-server 获取配置
                    // 然后从 jwks_uri 获取 JWK Set
                    .jwkSetUri(issuerUri + "/oauth2/jwks")
                )
            )
            .csrf(ServerHttpSecurity.CsrfSpec::disable);  // Gateway 通常禁用 CSRF

        return http.build();
    }
}

