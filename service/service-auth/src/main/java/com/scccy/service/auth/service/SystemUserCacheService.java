package com.scccy.service.auth.service;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CacheType;
import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.common.redis.cache.DefaultCacheArea;
import com.scccy.service.auth.fegin.SystemUserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * System 服务用户数据缓存封装
 * <p>
 * 通过 JetCache 复用 service-system 返回的用户信息和权限，
 * 减少 Feign 调用次数，缓解登录链路的重复查询。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemUserCacheService {

    private static final String CACHE_PREFIX = "system:user:";
    private static final int DEFAULT_EXPIRE_SECONDS = 60;

    private final SystemUserClient systemUserClient;

    /**
     * 获取单个用户信息，并通过 JetCache 缓存 60 秒。
     *
     * @param userName 用户名
     * @return 用户信息，可能为空（空值不会被缓存）
     */
    @Cached(
            area = DefaultCacheArea.SHORT_TIME_AREA,
            name = CACHE_PREFIX + "info:",
            key = "#userName",
            cacheType = CacheType.BOTH,
            expire = DEFAULT_EXPIRE_SECONDS,
            cacheNullValue = false
    )
    public SysUserMp getUserByUserName(String userName) {
        ResultData<SysUserMp> result = systemUserClient.getByUserName(userName);
        if (result == null || result.getData() == null) {
            log.warn("service-system 未返回用户信息: userName={}", userName);
            return null;
        }
        return result.getData();
    }

    /**
     * 获取用户权限，默认缓存 60 秒。
     *
     * @param userName 用户名
     * @return 权限列表（若查询失败返回空列表）
     */
    @Cached(
            area = DefaultCacheArea.SHORT_TIME_AREA,
            name = CACHE_PREFIX + "authorities:",
            key = "#userName",
            cacheType = CacheType.BOTH,
            expire = DEFAULT_EXPIRE_SECONDS,
            cacheNullValue = false
    )
    public List<String> getUserAuthorities(String userName) {
        ResultData<List<String>> result = systemUserClient.getUserAuthorities(userName);
        if (result == null || result.getData() == null) {
            log.warn("service-system 未返回用户权限: userName={}", userName);
            return Collections.emptyList();
        }
        return result.getData();
    }
}

