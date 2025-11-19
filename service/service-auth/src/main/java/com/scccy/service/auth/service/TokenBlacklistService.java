package com.scccy.service.auth.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务
 * <p>
 * 用于管理 JWT Token 的黑名单，支持将 Token 加入黑名单和检查 Token 是否在黑名单中
 * 使用 Redis 缓存存储黑名单 Token，Token 过期后自动从黑名单中移除
 * <p>
 * 黑名单 Key 格式：
 * - 优先使用 jti：`jwt:blacklist:{jti}`
 * - 如果没有 jti，使用 token 整串：`jwt:blacklist:{token}`
 * <p>
 * TTL：Token 剩余有效期（exp - now），单位为毫秒
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

    /**
     * 将 JWT Token 加入黑名单
     * <p>
     * 优先使用 jti 作为 Key，如果没有 jti，使用 token 整串
     * TTL 设置为 Token 的剩余有效期（exp - now）
     *
     * @param jwt JWT Token 对象
     */
    public void addToBlacklist(Jwt jwt) {
        if (jwt == null) {
            log.warn("JWT Token 为空，无法加入黑名单");
            return;
        }

        try {
            // 优先使用 jti
            String jti = jwt.getId();
            String key = buildBlacklistKey(jti != null && !jti.isBlank() ? jti : jwt.getTokenValue());
            
            // 计算剩余有效期
            Instant expiresAt = jwt.getExpiresAt();
            if (expiresAt == null) {
                log.warn("JWT Token 没有过期时间，无法加入黑名单: key={}", maskKey(key));
                return;
            }

            long currentTime = System.currentTimeMillis();
            long expireTime = expiresAt.toEpochMilli();
            long remainingTime = expireTime - currentTime;

            if (remainingTime <= 0) {
                log.debug("JWT Token 已过期，无需加入黑名单: key={}", maskKey(key));
                return;
            }

            // 写入黑名单
            tokenBlacklistCache.put(key, true, remainingTime, TimeUnit.MILLISECONDS);
            log.info("JWT Token 已加入黑名单: key={}, expireTime={}, remainingTime={}ms", 
                maskKey(key), expireTime, remainingTime);
        } catch (Exception e) {
            log.error("将 JWT Token 加入黑名单失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 将 JWT Token 字符串加入黑名单
     * <p>
     * 注意：此方法需要先解析 JWT Token 字符串，建议使用 {@link #addToBlacklist(Jwt)} 方法
     *
     * @param token JWT Token 字符串
     * @param expiresAt 过期时间（时间戳，毫秒）
     */
    public void addToBlacklist(String token, Long expiresAt) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token 为空，无法加入黑名单");
            return;
        }

        try {
            String key = buildBlacklistKey(token);
            long currentTime = System.currentTimeMillis();
            long remainingTime = expiresAt - currentTime;

            if (remainingTime <= 0) {
                log.debug("Token 已过期，无需加入黑名单: key={}", maskKey(key));
                return;
            }

            tokenBlacklistCache.put(key, true, remainingTime, TimeUnit.MILLISECONDS);
            log.info("Token 已加入黑名单: key={}, expireTime={}, remainingTime={}ms", 
                maskKey(key), expiresAt, remainingTime);
        } catch (Exception e) {
            log.error("将 Token 加入黑名单失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 检查 JWT Token 是否在黑名单中
     *
     * @param jwt JWT Token 对象
     * @return true 如果在黑名单中，false 如果不在
     */
    public boolean isBlacklisted(Jwt jwt) {
        if (jwt == null) {
            return false;
        }

        try {
            // 优先使用 jti
            String jti = jwt.getId();
            String key = buildBlacklistKey(jti != null && !jti.isBlank() ? jti : jwt.getTokenValue());
            Boolean blacklisted = tokenBlacklistCache.get(key);
            return blacklisted != null && blacklisted;
        } catch (Exception e) {
            log.error("检查 JWT Token 黑名单失败: error={}", e.getMessage(), e);
            // 发生异常时，为了安全起见，返回 true（拒绝访问）
            return true;
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param tokenOrJti Token 字符串或 jti
     * @return true 如果在黑名单中，false 如果不在
     */
    public boolean isBlacklisted(String tokenOrJti) {
        if (tokenOrJti == null || tokenOrJti.trim().isEmpty()) {
            return false;
        }

        try {
            String key = buildBlacklistKey(tokenOrJti);
            Boolean blacklisted = tokenBlacklistCache.get(key);
            return blacklisted != null && blacklisted;
        } catch (Exception e) {
            log.error("检查 Token 黑名单失败: error={}", e.getMessage(), e);
            // 发生异常时，为了安全起见，返回 true（拒绝访问）
            return true;
        }
    }

    /**
     * 构建黑名单 Key
     *
     * @param id jti 或 token 整串
     * @return 黑名单 Key
     */
    private String buildBlacklistKey(String id) {
        return TOKEN_BLACKLIST_PREFIX + id;
    }

    /**
     * 掩码 Key（用于日志输出，保护敏感信息）
     *
     * @param key 原始 Key
     * @return 掩码后的 Key
     */
    private String maskKey(String key) {
        if (key == null || key.length() <= 32) {
            return "***";
        }
        int prefixLength = 16;
        int suffixLength = 8;
        String prefix = key.substring(0, prefixLength);
        String suffix = key.substring(key.length() - suffixLength);
        return prefix + "..." + suffix;
    }
}

