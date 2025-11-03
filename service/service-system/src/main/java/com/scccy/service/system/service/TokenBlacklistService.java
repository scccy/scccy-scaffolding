package com.scccy.service.system.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务
 * <p>
 * 用于管理JWT Token的黑名单，支持将Token加入黑名单和检查Token是否在黑名单中
 * 使用Redis缓存存储黑名单Token，Token过期后自动从黑名单中移除
 *
 * @author scccy
 */
@Slf4j
@Service
public class TokenBlacklistService {

    private static final String TOKEN_BLACKLIST_PREFIX = "jwt:blacklist:";

    @Resource
    private CacheManager cacheManager;

    private Cache<String, Boolean> tokenBlacklistCache;

    @PostConstruct
    public void init() {
        QuickConfig qc = QuickConfig.newBuilder("remote_")
                .cacheType(CacheType.REMOTE)
                .syncLocal(false)
                .build();
        tokenBlacklistCache = cacheManager.getOrCreateCache(qc);
    }

    public void addToBlacklist(String token, Long expireTime) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token为空，无法加入黑名单");
            return;
        }

        try {
            String cacheKey = getCacheKey(token);
            long currentTime = System.currentTimeMillis();
            long remainingTime = expireTime - currentTime;

            if (remainingTime <= 0) {
                log.debug("Token已过期，无需加入黑名单: token={}", maskToken(token));
                return;
            }

            tokenBlacklistCache.put(cacheKey, true, remainingTime, TimeUnit.MILLISECONDS);
            log.info("Token已加入黑名单: token={}, expireTime={}", maskToken(token), expireTime);
        } catch (Exception e) {
            log.error("将Token加入黑名单失败: token={}, error={}", maskToken(token), e.getMessage(), e);
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            String cacheKey = getCacheKey(token);
            Boolean blacklisted = tokenBlacklistCache.get(cacheKey);
            return blacklisted != null && blacklisted;
        } catch (Exception e) {
            log.error("检查Token黑名单失败: token={}, error={}", maskToken(token), e.getMessage(), e);
            return true;
        }
    }

    private String getCacheKey(String token) {
        return TOKEN_BLACKLIST_PREFIX + token;
    }

    private String maskToken(String token) {
        if (token == null || token.length() <= 16) {
            return "***";
        }
        int prefixLength = 8;
        int suffixLength = 8;
        String prefix = token.substring(0, prefixLength);
        String suffix = token.substring(token.length() - suffixLength);
        return prefix + "..." + suffix;
    }
}

