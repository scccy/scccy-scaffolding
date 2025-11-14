package com.scccy.common.base.manager;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.scccy.common.base.config.properties.InternalTokenProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
    private OkHttpClient okHttpClient;
    
    @Resource
    private CacheManager cacheManager;
    
    private Cache<String, String> tokenCache;
    private Cache<String, Long> expireTimeCache;
    
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
        
        // 初始化过期时间缓存（仅本地缓存，用于判断是否需要提前刷新）
        QuickConfig expireTimeCacheConfig = QuickConfig.newBuilder("local_internal_token_expire:")
                .expire(Duration.ofSeconds(properties.getCacheExpireSeconds() + 60))
                .cacheType(CacheType.LOCAL)
                .build();
        expireTimeCache = cacheManager.getOrCreateCache(expireTimeCacheConfig);
        
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
        try {
            // 构建 Basic Auth header
            String credentials = properties.getClientId() + ":" + properties.getClientSecret();
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(
                    credentials.getBytes(StandardCharsets.UTF_8));
            
            // 构建请求体（使用 application/x-www-form-urlencoded）
            FormBody.Builder formBodyBuilder = new FormBody.Builder()
                    .add("grant_type", "client_credentials")
                    .add("scope", properties.getScope());
            
            RequestBody requestBody = formBodyBuilder.build();
            
            // 构建请求
            Request request = new Request.Builder()
                    .url(properties.getTokenUrl())
                    .header("Authorization", basicAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .post(requestBody)
                    .build();
            
            // 执行请求
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    log.error("获取内部令牌失败，状态码: {}, 响应: {}", response.code(), errorBody);
                    throw new RuntimeException("获取内部令牌失败: " + response.code() + " - " + errorBody);
                }
                
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new RuntimeException("获取内部令牌失败: 响应体为空");
                }
                
                String responseText = responseBody.string();
                JSONObject jsonResponse = JSON.parseObject(responseText);
                
                String accessToken = jsonResponse.getString("access_token");
                if (accessToken == null) {
                    log.error("获取内部令牌失败，响应中缺少 access_token: {}", responseText);
                    throw new RuntimeException("获取内部令牌失败: 响应中缺少 access_token");
                }
                
                // 计算过期时间（如果响应中包含 expires_in）
                Long expiresIn = jsonResponse.getLong("expires_in");
                if (expiresIn != null && expiresIn > 0) {
                    long expireTime = System.currentTimeMillis() / 1000 + expiresIn;
                    expireTimeCache.put(TOKEN_EXPIRE_TIME_KEY, expireTime);
                }
                
                // 缓存 token
                tokenCache.put(TOKEN_CACHE_KEY, accessToken);
                
                log.info("成功获取内部令牌，过期时间: {} 秒", expiresIn);
                return accessToken;
            }
        } catch (IOException e) {
            log.error("获取内部令牌时发生 IO 异常", e);
            throw new RuntimeException("获取内部令牌失败: " + e.getMessage(), e);
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
        new Thread(() -> {
            try {
                refreshToken();
            } catch (Exception e) {
                log.warn("异步刷新内部令牌失败", e);
            }
        }).start();
    }
    
    /**
     * 清除缓存的令牌（用于强制刷新）
     */
    public void clearToken() {
        tokenCache.remove(TOKEN_CACHE_KEY);
        expireTimeCache.remove(TOKEN_EXPIRE_TIME_KEY);
        log.info("已清除内部令牌缓存");
    }
}

