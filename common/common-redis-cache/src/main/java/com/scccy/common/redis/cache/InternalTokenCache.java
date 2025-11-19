package com.scccy.common.redis.cache;

import jakarta.annotation.Nullable;

/**
 * 内部 token 缓存抽象
 *
 * <p>
 * 所有内部服务间调用使用的 JWT 都通过该接口统一访问 Redis。
 * 典型 key 结构建议为：{@code internal:token:{clientId}:{audience}:{scope}}。
 * </p>
 */
public interface InternalTokenCache {

    /**
     * 从缓存中获取内部 token
     *
     * @param cacheKey 业务方已经拼装好的 key（含 clientId/audience/scope 等）
     * @return token 字符串；未命中返回 {@code null}
     */
    @Nullable
    String getToken(String cacheKey);

    /**
     * 写入/更新内部 token
     *
     * @param cacheKey 业务方已经拼装好的 key
     * @param token    token 字符串
     * @param ttlSec   过期时间（秒），应略短于真实 token TTL
     */
    void putToken(String cacheKey, String token, long ttlSec);

    /**
     * 主动剔除缓存，用于登出、密钥轮换等场景
     *
     * @param cacheKey 缓存 key
     */
    void evict(String cacheKey);

    /**
     * 获取 key 的剩余过期时间
     *
     * @param cacheKey 缓存 key
     * @return 剩余秒数；若 key 不存在返回 {@code null}，若无过期时间则返回 -1
     */
    @Nullable
    Long getExpireSeconds(String cacheKey);
}


