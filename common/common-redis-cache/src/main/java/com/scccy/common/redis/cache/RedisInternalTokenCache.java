package com.scccy.common.redis.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 基于 JetCache Redis 的内部 token 缓存实现
 *
 * <p>
 * 说明：
 * <ul>
 *     <li>所有内部调用 token 均存储在 Redis 中，键由上层统一生成</li>
 *     <li>TTL 必须略短于真实 token 的过期时间，避免使用已过期 token</li>
 *     <li>不做本地缓存，统一依赖 Redis 提供跨实例可见性</li>
 *     <li>使用 JetCache 的远程缓存进行读写，可在统计中查看命中率等指标</li>
 *     <li>使用 StringRedisTemplate 获取 TTL（JetCache Cache 接口不提供 getTtl 方法）</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class RedisInternalTokenCache implements InternalTokenCache {

    private final CacheManager cacheManager;
    private final StringRedisTemplate stringRedisTemplate;
    private Cache<String, String> cache;

    public RedisInternalTokenCache(CacheManager cacheManager, StringRedisTemplate stringRedisTemplate) {
        this.cacheManager = cacheManager;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostConstruct
    public void init() {
        // 创建远程缓存区域（仅使用 Redis，不使用本地缓存）
        QuickConfig qc = QuickConfig.newBuilder("remote_internal_token:")
            .cacheType(CacheType.REMOTE)
            .expire(Duration.ofSeconds(600)) // 默认 10 分钟，实际 TTL 由 putToken 方法控制
            .build();
        this.cache = cacheManager.getOrCreateCache(qc);
        log.info("RedisInternalTokenCache 初始化完成，使用 JetCache 远程缓存区域: remote_internal_token");
    }

    @Override
    public String getToken(String cacheKey) {
        return cache.get(cacheKey);
    }

    @Override
    public void putToken(String cacheKey, String token, long ttlSec) {
        if (ttlSec <= 0) {
            cache.put(cacheKey, token);
        } else {
            cache.put(cacheKey, token, ttlSec, TimeUnit.SECONDS);
        }
    }

    @Override
    public void evict(String cacheKey) {
        cache.remove(cacheKey);
    }

    @Override
    public Long getExpireSeconds(String cacheKey) {
        // JetCache Cache 接口不提供 getTtl 方法，直接使用 StringRedisTemplate 获取 TTL
        // 注意：需要确保 cacheKey 与实际 Redis 中的 key 格式一致（JetCache 会自动添加前缀）
        // JetCache 的 key 格式通常是：{area}:{key}，这里是 remote_internal_token:{cacheKey}
        String redisKey = "remote_internal_token:" + cacheKey;
        Long ttl = stringRedisTemplate.getExpire(redisKey);
        // Redis TTL: null 表示 key 不存在，-1 表示永不过期，>=0 表示剩余秒数
        return ttl;
    }
}


