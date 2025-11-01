package com.scccy.service.auth.service;

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

    /**
     * Token黑名单缓存前缀
     */
    private static final String TOKEN_BLACKLIST_PREFIX = "jwt:blacklist:";

    @Resource
    private CacheManager cacheManager;

    /**
     * Token黑名单缓存
     */
    private Cache<String, Boolean> tokenBlacklistCache;

    @PostConstruct
    public void init() {
        // 初始化Token黑名单缓存
        // 使用远程缓存（Redis），Token过期后自动从缓存中移除
        QuickConfig qc = QuickConfig.newBuilder("remote_")
                .cacheType(CacheType.REMOTE)  // 仅使用远程缓存（Redis），不使用本地缓存
                .syncLocal(false)  // 不需要同步本地缓存
                .build();
        tokenBlacklistCache = cacheManager.getOrCreateCache(qc);
    }

    /**
     * 将Token加入黑名单
     * <p>
     * Token会一直保留在黑名单中，直到其自然过期（通过Redis的TTL机制）
     * 这样即使Token过期后，也不会被重新使用
     *
     * @param token       JWT Token
     * @param expireTime  Token过期时间（时间戳，毫秒）
     */
    public void addToBlacklist(String token, Long expireTime) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token为空，无法加入黑名单");
            return;
        }

        try {
            String cacheKey = getCacheKey(token);
            long currentTime = System.currentTimeMillis();

            // 计算Token剩余有效时间（毫秒）
            long remainingTime = expireTime - currentTime;

            if (remainingTime <= 0) {
                // Token已经过期，无需加入黑名单
                log.debug("Token已过期，无需加入黑名单: token={}", maskToken(token));
                return;
            }

            // 将Token加入黑名单，TTL设置为Token剩余有效时间
            // 使用put方法的expire参数设置过期时间（毫秒）
            tokenBlacklistCache.put(cacheKey, true, remainingTime, TimeUnit.MILLISECONDS);

            log.info("Token已加入黑名单: token={}, expireTime={}", maskToken(token), expireTime);
        } catch (Exception e) {
            log.error("将Token加入黑名单失败: token={}, error={}", maskToken(token), e.getMessage(), e);
            // 不抛出异常，避免影响登出流程
        }
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param token JWT Token
     * @return true-在黑名单中，false-不在黑名单中
     */
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
            // 发生异常时，为了安全起见，返回true（认为Token在黑名单中）
            // 这样可以防止在Redis故障时，已登出的Token被继续使用
            return true;
        }
    }

    /**
     * 从黑名单中移除Token（可选功能）
     * <p>
     * 通常不需要手动移除，因为Token过期后会自动从缓存中移除
     * 但在某些特殊场景下（如管理员撤销黑名单），可能需要此功能
     *
     * @param token JWT Token
     */
    public void removeFromBlacklist(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        try {
            String cacheKey = getCacheKey(token);
            tokenBlacklistCache.remove(cacheKey);
            log.info("Token已从黑名单中移除: token={}", maskToken(token));
        } catch (Exception e) {
            log.error("从黑名单中移除Token失败: token={}, error={}", maskToken(token), e.getMessage(), e);
        }
    }

    /**
     * 获取缓存Key
     *
     * @param token JWT Token
     * @return 缓存Key
     */
    private String getCacheKey(String token) {
        // 为了安全，使用Token的哈希值作为缓存Key的一部分
        // 这样即使Token泄露，也不会在Redis中暴露完整的Token
        // 直接使用完整Token作为Key，因为我们需要精确匹配
        return TOKEN_BLACKLIST_PREFIX + token;
    }

    /**
     * 掩码Token（用于日志输出）
     * <p>
     * 只显示Token的前8位和后8位，中间用*代替，保护敏感信息
     *
     * @param token JWT Token
     * @return 掩码后的Token
     */
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

