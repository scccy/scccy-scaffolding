package com.scccy.gateway.filter;

import com.scccy.common.modules.constant.UserHeaderConstants;
import com.scccy.common.modules.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 用户信息网关过滤器
 * <p>
 * 从 Token 中提取用户信息，添加到请求头传递给后端服务
 * <p>
 * 注意：使用 JwtUtils 静态方法，避免依赖注入
 *
 * @author scccy
 */
@Slf4j
@Component
public class UserInfoGatewayFilter extends AbstractGatewayFilterFactory<Object> {

    public UserInfoGatewayFilter() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(Object config) {
        return new OrderedGatewayFilter((exchange, chain) -> {
            return ReactiveSecurityContextHolder.getContext()
                .cast(org.springframework.security.core.context.SecurityContext.class)
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .flatMap(jwt -> {
                    // 使用 JwtUtils 静态方法提取用户信息
                    Long userId = JwtUtils.getUserId(jwt);
                    String username = JwtUtils.getUsername(jwt);
                    List<String> authorities = JwtUtils.getAuthorities(jwt);

                    log.debug("提取用户信息: userId={}, username={}, authorities={}",
                        userId, username, authorities);

                    // 添加用户信息到请求头（这些请求头不会暴露给前端，只在 Gateway 和后端服务之间传递）
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header(UserHeaderConstants.HEADER_USER_ID, userId != null ? String.valueOf(userId) : "")
                        .header(UserHeaderConstants.HEADER_USERNAME, username != null ? username : "")
                        .header(UserHeaderConstants.HEADER_AUTHORITIES, authorities != null && !authorities.isEmpty()
                            ? String.join(",", authorities) : "")
                        .build();

                    ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();

                    return chain.filter(modifiedExchange);
                })
                .switchIfEmpty(chain.filter(exchange));  // 如果没有认证信息，继续转发
        }, -100);  // 在路由之前执行
    }
}

