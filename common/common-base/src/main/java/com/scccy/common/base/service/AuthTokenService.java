package com.scccy.common.base.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.scccy.common.base.config.properties.InternalTokenProperties;
import com.scccy.common.redis.cache.InternalTokenCache;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 内部服务间调用的 JWT 管理服务。
 *
 * <p>职责：
 * <ul>
 *     <li>根据 {@code scccy.internal-token.*} 配置获取 access token</li>
 *     <li>使用 Redis 缓存（通过 {@link InternalTokenCache}）共享 token</li>
 *     <li>在 token 即将过期时自动刷新</li>
 * </ul>
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "scccy.internal-token", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthTokenService {

    private static final long MIN_TTL_SECONDS = 5L;

    private final InternalTokenProperties properties;
    private final InternalTokenCache tokenCache;
    private final WebClient webClient;

    private final ReentrantLock refreshLock = new ReentrantLock();
    private final ExecutorService asyncRefreshPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "internal-token-refresh");
        t.setDaemon(true);
        return t;
    });

    public AuthTokenService(InternalTokenProperties properties,
                            InternalTokenCache tokenCache,
                            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder) {
        this.properties = properties;
        this.tokenCache = tokenCache;
        this.webClient = webClientBuilder.clone().build();
    }

    @PostConstruct
    public void logInit() {
        log.info("AuthTokenService 初始化完成，tokenUrl={}, scope={}, audience={}",
            properties.getTokenUrl(), properties.getScope(), properties.getAudience());
    }

    /**
     * 使用默认配置获取内部 token。
     */
    public String getServiceToken() {
        return getServiceToken(properties.getAudience(), properties.getScope());
    }

    /**
     * 获取指定 audience/scope 的内部 token。
     *
     * @param audience 目标服务，可为空
     * @param scope    授权范围，若为空则使用默认 scope
     */
    public String getServiceToken(String audience, String scope) {
        validateClientConfig();
        String resolvedScope = StringUtils.hasText(scope) ? scope : properties.getScope();
        String resolvedAudience = StringUtils.hasText(audience) ? audience : properties.getAudience();
        String cacheKey = buildCacheKey(resolvedScope, resolvedAudience);

        String cached = tokenCache.getToken(cacheKey);
        if (cached != null && !shouldRefresh(cacheKey)) {
            return cached;
        }

        return refreshToken(cacheKey, resolvedScope, resolvedAudience);
    }

    /**
     * 主动清理默认 token 缓存。
     */
    public void evictDefaultToken() {
        String cacheKey = buildCacheKey(properties.getScope(), properties.getAudience());
        tokenCache.evict(cacheKey);
    }

    private void validateClientConfig() {
        if (!StringUtils.hasText(properties.getClientId()) || !StringUtils.hasText(properties.getClientSecret())) {
            throw new IllegalStateException("内部令牌 clientId/clientSecret 未配置，无法获取 token");
        }
    }

    private String buildCacheKey(String scope, String audience) {
        String clientId = properties.getClientId();
        String scopePart = StringUtils.hasText(scope) ? scope : "default";
        String audiencePart = StringUtils.hasText(audience) ? audience : "default";
        return "internal:token:" + clientId + ":" + scopePart + ":" + audiencePart;
    }

    private boolean shouldRefresh(String cacheKey) {
        Long ttl = tokenCache.getExpireSeconds(cacheKey);
        if (ttl == null) {
            return true;
        }
        long refreshAhead = Objects.requireNonNullElse(properties.getRefreshAheadSeconds(), 60L);
        if (ttl < 0) {
            return false;
        }
        return ttl <= refreshAhead;
    }

    private String refreshToken(String cacheKey, String scope, String audience) {
        refreshLock.lock();
        try {
            String cached = tokenCache.getToken(cacheKey);
            if (cached != null && !shouldRefresh(cacheKey)) {
                return cached;
            }
            return fetchToken(cacheKey, scope, audience);
        } finally {
            refreshLock.unlock();
        }
    }

    private String fetchToken(String cacheKey, String scope, String audience) {
        String credentials = properties.getClientId() + ":" + properties.getClientSecret();
        String basicAuth = "Basic " + Base64.getEncoder()
            .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", properties.getGrantType());
        if (StringUtils.hasText(scope)) {
            formData.add("scope", scope);
        }
        if (StringUtils.hasText(audience)) {
            formData.add("audience", audience);
        }

        try {
            String response = webClient.post()
                .uri(properties.getTokenUrl())
                .header(HttpHeaders.AUTHORIZATION, basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();

            if (response == null) {
                throw new IllegalStateException("获取内部令牌失败：响应为空");
            }

            JSONObject json = JSON.parseObject(response);
            String accessToken = json.getString("access_token");
            if (!StringUtils.hasText(accessToken)) {
                throw new IllegalStateException("获取内部令牌失败：响应缺少 access_token");
            }

            Long expiresIn = json.getLong("expires_in");
            long ttl = resolveTtl(expiresIn);

            tokenCache.putToken(cacheKey, accessToken, ttl);
            log.info("成功获取内部令牌（scope={} audience={}），ttl={}s", scope, audience, ttl);
            return accessToken;
        } catch (WebClientResponseException e) {
            String body = e.getResponseBodyAsString();
            log.error("获取内部令牌失败，状态码={}，响应={}", e.getRawStatusCode(), body);
            throw new IllegalStateException("获取内部令牌失败：" + e.getRawStatusCode() + " - " + body, e);
        } catch (Exception e) {
            log.error("获取内部令牌异常", e);
            throw new IllegalStateException("获取内部令牌异常：" + e.getMessage(), e);
        }
    }

    private long resolveTtl(Long expiresIn) {
        long defaultCache = Objects.requireNonNullElse(properties.getCacheExpireSeconds(), 540L);
        long refreshAhead = Objects.requireNonNullElse(properties.getRefreshAheadSeconds(), 60L);
        long ttl = defaultCache;
        if (expiresIn != null && expiresIn > 0) {
            long safeTtl = expiresIn - refreshAhead;
            if (safeTtl > 0) {
                ttl = Math.min(defaultCache, safeTtl);
            } else {
                ttl = Math.max(MIN_TTL_SECONDS, expiresIn / 2);
            }
        }
        return Math.max(MIN_TTL_SECONDS, ttl);
    }

    /**
     * 异步刷新，用于外部在检测到即将过期时调用。
     */
    public void refreshAsync() {
        asyncRefreshPool.submit(() -> {
            try {
                getServiceToken();
            } catch (Exception e) {
                log.warn("异步刷新内部令牌失败", e);
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        asyncRefreshPool.shutdown();
        try {
            if (!asyncRefreshPool.awaitTermination(2, TimeUnit.SECONDS)) {
                asyncRefreshPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


