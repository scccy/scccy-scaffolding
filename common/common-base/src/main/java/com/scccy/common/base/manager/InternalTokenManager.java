package com.scccy.common.base.manager;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.scccy.common.base.config.properties.InternalTokenProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 内部服务令牌管理器
 * <p>
 * 负责获取和缓存内部服务间调用的 OAuth2 access token
 * 
 * @author scccy
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "scccy.internal-token", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InternalTokenManager {
    
    private static final String TOKEN_CACHE_KEY = "internal_service_token";
    private static final String TOKEN_EXPIRE_TIME_KEY = "internal_service_token_expire_time";
    
    @Resource
    private InternalTokenProperties properties;
    
    @Resource
    @Qualifier("loadBalancedWebClientBuilder")
    private WebClient.Builder webClientBuilder;
    
    @Resource
    private CacheManager cacheManager;
    
    private Cache<String, String> tokenCache;
    private Cache<String, Long> expireTimeCache;
    private WebClient webClient;
    private final ExecutorService refreshExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "internal-token-refresh");
        t.setDaemon(true);
        return t;
    });
    private final ReentrantLock refreshLock = new ReentrantLock();
    private static final int MAX_REFRESH_RETRY = 3;
    private static final long RETRY_BACKOFF_MS = 300;
    
    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(properties.getClientId()) || !StringUtils.hasText(properties.getClientSecret())) {
            log.warn("内部令牌功能已启用，但未配置 clientId 或 clientSecret，将无法获取令牌");
            return;
        }
        
        // 初始化 token 缓存（使用两级缓存：本地 + Redis）
        QuickConfig tokenCacheConfig = QuickConfig.newBuilder("remote_internal_token:")
                .expire(Duration.ofSeconds(properties.getCacheExpireSeconds()))
                .cacheType(CacheType.BOTH)
                .syncLocal(true)
                .build();
        tokenCache = cacheManager.getOrCreateCache(tokenCacheConfig);
        System.out.println(tokenCacheConfig.toString());
        
        // 初始化过期时间缓存（仅本地缓存，用于判断是否需要提前刷新）
        QuickConfig expireTimeCacheConfig = QuickConfig.newBuilder("local_internal_token_expire:")
                .expire(Duration.ofSeconds(properties.getCacheExpireSeconds() + 60))
                .cacheType(CacheType.LOCAL)
                .build();
        expireTimeCache = cacheManager.getOrCreateCache(expireTimeCacheConfig);
        
        this.webClient = webClientBuilder.clone()
            .build();
        
        log.info("内部令牌管理器初始化完成，tokenUrl: {}, scope: {}", 
                properties.getTokenUrl(), properties.getScope());
    }
    
    /**
     * 获取内部服务令牌
     * <p>
     * 优先从缓存获取，如果缓存未命中或即将过期，则重新获取
     * 
     * @return access token
     * @throws RuntimeException 如果获取令牌失败
     */
    public String getToken() {
        if (!StringUtils.hasText(properties.getClientId()) || !StringUtils.hasText(properties.getClientSecret())) {
            throw new IllegalStateException("内部令牌配置不完整，无法获取令牌");
        }
        
        // 检查缓存中的 token
        String cachedToken = tokenCache.get(TOKEN_CACHE_KEY);
        if (cachedToken != null) {
            // 检查是否需要提前刷新
            Long expireTime = expireTimeCache.get(TOKEN_EXPIRE_TIME_KEY);
            if (expireTime != null) {
                long currentTime = System.currentTimeMillis() / 1000;
                long refreshTime = expireTime - properties.getRefreshAheadSeconds();
                
                if (currentTime < refreshTime) {
                    // 还未到刷新时间，直接返回缓存 token
                    log.debug("从缓存获取内部令牌");
                    return cachedToken;
                } else {
                    // 即将过期，异步刷新（当前请求仍返回旧 token）
                    log.info("内部令牌即将过期，触发异步刷新");
                    refreshTokenAsync();
                }
            }
            
            return cachedToken;
        }
        
        // 缓存未命中，同步获取新 token
        log.info("缓存未命中，获取新的内部令牌");
        return refreshToken();
    }
    
    /**
     * 同步刷新令牌
     * 
     * @return 新的 access token
     */
    private String refreshToken() {
        refreshLock.lock();
        try {
            String cached = tokenCache.get(TOKEN_CACHE_KEY);
            if (cached != null) {
                return cached;
            }
            RuntimeException last = null;
            for (int attempt = 1; attempt <= MAX_REFRESH_RETRY; attempt++) {
                try {
                    return fetchToken();
                } catch (RuntimeException e) {
                    last = e;
                    if (attempt < MAX_REFRESH_RETRY) {
                        log.warn("获取内部令牌失败，重试 {}/{}", attempt, MAX_REFRESH_RETRY, e);
                        try {
                            TimeUnit.MILLISECONDS.sleep(RETRY_BACKOFF_MS);
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            throw last != null ? last : new RuntimeException("获取内部令牌失败");
        } finally {
            refreshLock.unlock();
        }
    }

    private String fetchToken() {
        try {
            String credentials = properties.getClientId() + ":" + properties.getClientSecret();
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "client_credentials");
            formData.add("scope", properties.getScope());

            String responseText = webClient.post()
                .uri(properties.getTokenUrl())
                .header(HttpHeaders.AUTHORIZATION, basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();

            log.debug("内部令牌接口响应: {}", responseText);

            if (responseText == null) {
                throw new RuntimeException("获取内部令牌失败: 响应体为空");
            }

            JSONObject jsonResponse = JSON.parseObject(responseText);
            String accessToken = jsonResponse.getString("access_token");
            if (accessToken == null) {
                log.error("获取内部令牌失败，响应中缺少 access_token: {}", responseText);
                throw new RuntimeException("获取内部令牌失败: 响应中缺少 access_token");
            }

            Long expiresIn = jsonResponse.getLong("expires_in");
            if (expiresIn != null && expiresIn > 0) {
                long expireTime = System.currentTimeMillis() / 1000 + expiresIn;
                expireTimeCache.put(TOKEN_EXPIRE_TIME_KEY, expireTime);
            }

            tokenCache.put(TOKEN_CACHE_KEY, accessToken);
            log.info("成功获取内部令牌，过期时间: {} 秒", expiresIn);
            return accessToken;
        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("获取内部令牌失败，状态码: {}, 响应: {}", e.getRawStatusCode(), errorBody);
            throw new RuntimeException("获取内部令牌失败: " + e.getRawStatusCode() + " - " + errorBody, e);
        } catch (Exception e) {
            log.error("获取内部令牌时发生异常", e);
            throw new RuntimeException("获取内部令牌失败: " + e.getMessage(), e);
        }
    }

    /**
     * 异步刷新令牌（不阻塞当前请求）
     */
    private void refreshTokenAsync() {
        // 使用线程池异步刷新，避免阻塞当前请求
        refreshExecutor.submit(() -> {
            try {
                refreshToken();
            } catch (Exception e) {
                log.warn("异步刷新内部令牌失败", e);
            }
        });
    }
    
    /**
     * 清除缓存的令牌（用于强制刷新）
     */
    public void clearToken() {
        tokenCache.remove(TOKEN_CACHE_KEY);
        expireTimeCache.remove(TOKEN_EXPIRE_TIME_KEY);
        log.info("已清除内部令牌缓存");
    }

    @PreDestroy
    public void destroy() {
        refreshExecutor.shutdownNow();
    }
}
