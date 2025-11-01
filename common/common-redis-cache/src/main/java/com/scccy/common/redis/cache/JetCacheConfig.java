package com.scccy.common.redis.cache;


import com.alicp.jetcache.autoconfigure.JetCacheAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * JetCache 缓存配置类
 * <p>
 * 注意：JetCache 配置已迁移到 Nacos 配置中心
 * 如果需要在本地保留默认配置，可以取消注释下面的 @PropertySource
 * </p>
 */
@AutoConfiguration(before = JetCacheAutoConfiguration.class)
public class JetCacheConfig {
}