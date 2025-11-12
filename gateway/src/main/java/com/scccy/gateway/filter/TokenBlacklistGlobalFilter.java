package com.scccy.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 基于 Redis 的 JWT 黑名单统一拦截（Gateway 全局过滤器）
 * - 仅使用响应式 Redis（ReactiveStringRedisTemplate），不引入 MVC 的 Redis 依赖
 * - 优先读取已认证主体中的 jti；若不可用则回退到 Bearer token 整串
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBlacklistGlobalFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    @Value("${security.jwt.blacklist.prefix:jwt:blacklist:}")
    private String blacklistKeyPrefix;

    @Value("${security.jwt.blacklist.skip-client-credentials:true}")
    private boolean skipClientCredentials;

    /**
     * 以逗号分隔的路径前缀，匹配则跳过黑名单校验，如 /internal/,/actuator/
     */
    @Value("${security.jwt.blacklist.skip-path-prefixes:}")
    private String skipPathPrefixesCsv;
    private volatile Set<String> skipPathPrefixes;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 无 Authorization 头时直接放行（匿名或公开接口）
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return chain.filter(exchange);
        }

        String bearerToken = authHeader.substring(7).trim();
        if (bearerToken.isEmpty()) {
            return chain.filter(exchange);
        }

        // 路径级跳过
        if (shouldSkipByPath(exchange)) {
            return chain.filter(exchange);
        }

        // 优先从认证主体里获取 jti
        return exchange.getPrincipal()
            .cast(AbstractAuthenticationToken.class)
            .onErrorResume(e -> {
                // 取主体失败不影响后续流程，记录调试日志
                log.debug("获取认证主体失败，回退到 token 整串进行黑名单查询: {}", e.toString());
                return Mono.empty();
            })
            .flatMap(auth -> {
                if (auth instanceof JwtAuthenticationToken jwtAuth) {
                    Jwt jwt = jwtAuth.getToken();

                    // 机器到机器的凭证（client_credentials）可按需跳过
                    if (shouldSkipByClientCredentials(jwt)) {
                        return chain.filter(exchange);
                    }

                    String jti = null;
                    try {
                        jti = jwt.getClaimAsString("jti");
                    } catch (Exception ignored) {
                    }
                    String key = buildBlacklistKey(jti != null && !jti.isBlank() ? jti : bearerToken);
                    return checkAndBlockIfBlacklisted(exchange, chain, key);
                }
                // 不是 JWT 认证主体，回退使用 token 整串
                String key = buildBlacklistKey(bearerToken);
                return checkAndBlockIfBlacklisted(exchange, chain, key);
            })
            // 如果没有主体（未通过 Security 认证链，但带了 Bearer），依旧用整串 token 检查
            .switchIfEmpty(checkAndBlockIfBlacklisted(exchange, chain, buildBlacklistKey(bearerToken)));
    }

    private boolean shouldSkipByPath(ServerWebExchange exchange) {
        Set<String> prefixes = getOrInitSkipPrefixes(skipPathPrefixesCsv);
        if (prefixes.isEmpty()) {
            return false;
        }
        String path = exchange.getRequest().getURI().getPath();
        for (String prefix : prefixes) {
            if (!prefix.isEmpty() && path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldSkipByClientCredentials(Jwt jwt) {
        if (!skipClientCredentials) {
            return false;
        }
        // 常见判定：client_credentials 场景 sub == client_id（无用户主体）
        String clientId = safeClaim(jwt, "client_id");
        String sub = safeClaim(jwt, "sub");
        if (clientId != null && clientId.equals(sub)) {
            return true;
        }
        // 兼容部分颁发方在 claims 中放置 grant_type 或 token_use
        String grantType = safeClaim(jwt, "grant_type");
        return "client_credentials".equalsIgnoreCase(grantType);
    }

    private Set<String> getOrInitSkipPrefixes(String csv) {
        if (skipPathPrefixes != null) {
            return skipPathPrefixes;
        }
        synchronized (this) {
            if (skipPathPrefixes != null) return skipPathPrefixes;
            Set<String> set = new HashSet<>();
            if (csv != null && !csv.isBlank()) {
                Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(set::add);
            }
            skipPathPrefixes = set;
            return skipPathPrefixes;
        }
    }

    private String safeClaim(Jwt jwt, String name) {
        try {
            String v = jwt.getClaimAsString(name);
            return (v == null || v.isBlank()) ? null : v;
        } catch (Exception e) {
            return null;
        }
    }

    private String buildBlacklistKey(String id) {
        return blacklistKeyPrefix + Objects.toString(id, "");
    }

    private Mono<Void> checkAndBlockIfBlacklisted(ServerWebExchange exchange,
                                                  GatewayFilterChain chain,
                                                  String redisKey) {
        return reactiveStringRedisTemplate.opsForValue()
            .get(redisKey)
            .flatMap(value -> {
                // 任意存在即视为命中（值通常为 "true"）
                log.info("JWT 命中黑名单，拒绝访问。key={}, clientIp={}",
                    redisKey, exchange.getRequest().getRemoteAddress());
                return writeUnauthorized(exchange);
            })
            .switchIfEmpty(chain.filter(exchange));
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"Token 已被撤销或失效(黑名单)\",\"success\":false}";
        DataBufferFactory bufferFactory = response.bufferFactory();
        return response.writeWith(Mono.just(bufferFactory.wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * 安排在 Security 认证之后、路由之前。值越小优先级越高。
     * 这里取一个相对靠前的顺序，确保尽早拦截。
     */
    @Override
    public int getOrder() {
        return -50;
    }
}


