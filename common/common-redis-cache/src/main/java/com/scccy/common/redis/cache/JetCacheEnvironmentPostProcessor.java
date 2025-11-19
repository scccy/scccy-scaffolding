package com.scccy.common.redis.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;
import java.util.Map;

/**
 * JetCache 环境后处理器
 * <p>
 * 在环境准备阶段设置 JetCache 的默认配置值，确保在 JetCacheAutoConfiguration 之前设置默认值。
 * <p>
 * 通过 {@code META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor.imports} 文件注册，
 * 无需使用传统的 {@code spring.factories} 文件。
 * <p>
 * 默认配置说明：
 * <ul>
 *     <li>本地缓存：使用 Caffeine，提供 default、longTime、shortTime 三个缓存区域</li>
 *     <li>远程缓存：使用 Redis Lettuce，提供 default、longTime、shortTime 三个缓存区域</li>
 *     <li>Redis URI：自动从环境变量或 Spring Redis 配置获取</li>
 * </ul>
 * <p>
 * 配置覆盖优先级（从高到低）：
 * <ol>
 *     <li>Nacos 配置中心的配置（最高优先级）</li>
 *     <li>应用本地 application.yml 配置</li>
 *     <li>环境变量配置（REDIS_HOST、REDIS_PORT 等）</li>
 *     <li>Spring Redis 配置（spring.redis.host、spring.redis.port 等）</li>
 *     <li>此默认配置（最低优先级）</li>
 * </ol>
 *
 * @author scccy
 */
public class JetCacheEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String FASTJSON2_ENCODER = "com.scccy.common.redis.cache.codec.Fastjson2ValueEncoder";
    private static final String FASTJSON2_DECODER = "com.scccy.common.redis.cache.codec.Fastjson2ValueDecoder";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 检查是否已经在 application.yml 或环境变量中配置了 Redis URI
        // 注意：Nacos 配置会在 EnvironmentPostProcessor 之后加载，所以这里检查的是本地配置
        // 如果 Nacos 中配置了 URI，会在之后加载并覆盖此默认值
        boolean hasRemoteUri = environment.containsProperty("jetcache.remote.default.uri") 
            || environment.containsProperty("jetcache.remote.longTime.uri")
            || environment.containsProperty("jetcache.remote.shortTime.uri");
        
        // 获取 Redis 连接信息（仅在未配置 URI 时使用）
        // 如果用户已在 application.yml 或环境变量中配置了 URI，则使用用户配置
        // 否则从 Spring Redis 配置或环境变量构建默认 URI
        String redisUri = null;
        if (!hasRemoteUri) {
            String redisHost = getProperty(environment, "REDIS_HOST", "spring.data.redis.host", "localhost");
            String redisPort = getProperty(environment, "REDIS_PORT", "spring.data.redis.port", "6379");
            String redisPassword = getProperty(environment, "REDIS_PASSWORD", "spring.data.redis.password", "");
            String redisDatabase = getProperty(environment, "REDIS_DATABASE", "spring.data.redis.database", "0");
            // 构建 Redis URI
            redisUri = buildRedisUri(redisHost, redisPort, redisPassword, redisDatabase);
        }
        
        // 设置默认配置属性（仅在属性不存在时设置）
        Map<String, Object> defaultProperties = new HashMap<>();
        
        // 全局配置
        setIfAbsent(environment, defaultProperties, "jetcache.statIntervalMinutes", "10");
        setIfAbsent(environment, defaultProperties, "jetcache.hidePackages", "io.github.opensabre");
        
        // 本地缓存配置 - default
        setIfAbsent(environment, defaultProperties, "jetcache.local.default.type", "caffeine");
        setIfAbsent(environment, defaultProperties, "jetcache.local.default.keyConvertor", "jackson");
        setIfAbsent(environment, defaultProperties, "jetcache.local.default.expireAfterWriteInMillis", "300000");
        setIfAbsent(environment, defaultProperties, "jetcache.local.default.expireAfterAccessInMillis", "180000");
        
        // 本地缓存配置 - longTime
        setIfAbsent(environment, defaultProperties, "jetcache.local.longTime.type", "caffeine");
        setIfAbsent(environment, defaultProperties, "jetcache.local.longTime.keyConvertor", "jackson");
        setIfAbsent(environment, defaultProperties, "jetcache.local.longTime.expireAfterWriteInMillis", "3600000");
        setIfAbsent(environment, defaultProperties, "jetcache.local.longTime.expireAfterAccessInMillis", "1800000");
        
        // 本地缓存配置 - shortTime
        setIfAbsent(environment, defaultProperties, "jetcache.local.shortTime.type", "caffeine");
        setIfAbsent(environment, defaultProperties, "jetcache.local.shortTime.keyConvertor", "jackson");
        setIfAbsent(environment, defaultProperties, "jetcache.local.shortTime.expireAfterWriteInMillis", "60000");
        setIfAbsent(environment, defaultProperties, "jetcache.local.shortTime.expireAfterAccessInMillis", "40000");
        
        // 远程缓存配置 - default（仅在未配置 URI 时设置默认 URI）
        setIfAbsent(environment, defaultProperties, "jetcache.remote.default.type", "redis.lettuce");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.default.expireAfterWriteInMillis", "7200000");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.default.keyConvertor", "jackson");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.default.valueEncoder", FASTJSON2_ENCODER);
        setIfAbsent(environment, defaultProperties, "jetcache.remote.default.valueDecoder", FASTJSON2_DECODER);
        setIfAbsent(environment, defaultProperties, "jetcache.remote.default.poolConfig.minIdle", "5");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.default.poolConfig.maxIdle", "20");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.default.poolConfig.maxTotal", "50");
        if (redisUri != null) {
            setIfAbsent(environment, defaultProperties, "jetcache.remote.default.uri", redisUri);
        }
        
        // 远程缓存配置 - longTime（仅在未配置 URI 时设置默认 URI）
        setIfAbsent(environment, defaultProperties, "jetcache.remote.longTime.type", "redis.lettuce");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.longTime.expireAfterWriteInMillis", "43200000");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.longTime.keyConvertor", "jackson");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.longTime.valueEncoder", FASTJSON2_ENCODER);
        setIfAbsent(environment, defaultProperties, "jetcache.remote.longTime.valueDecoder", FASTJSON2_DECODER);
        setIfAbsent(environment, defaultProperties, "jetcache.remote.longTime.poolConfig.minIdle", "5");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.longTime.poolConfig.maxIdle", "20");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.longTime.poolConfig.maxTotal", "50");
        if (redisUri != null) {
            setIfAbsent(environment, defaultProperties, "jetcache.remote.longTime.uri", redisUri);
        }
        
        // 远程缓存配置 - shortTime（仅在未配置 URI 时设置默认 URI）
        setIfAbsent(environment, defaultProperties, "jetcache.remote.shortTime.type", "redis.lettuce");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.shortTime.expireAfterWriteInMillis", "300000");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.shortTime.keyConvertor", "jackson");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.shortTime.valueEncoder", FASTJSON2_ENCODER);
        setIfAbsent(environment, defaultProperties, "jetcache.remote.shortTime.valueDecoder", FASTJSON2_DECODER);
        setIfAbsent(environment, defaultProperties, "jetcache.remote.shortTime.poolConfig.minIdle", "5");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.shortTime.poolConfig.maxIdle", "20");
        setIfAbsent(environment, defaultProperties, "jetcache.remote.shortTime.poolConfig.maxTotal", "50");
        if (redisUri != null) {
            setIfAbsent(environment, defaultProperties, "jetcache.remote.shortTime.uri", redisUri);
        }
        
        // 将默认配置添加到环境变量中（最低优先级）
        if (!defaultProperties.isEmpty()) {
            MutablePropertySources propertySources = environment.getPropertySources();
            MapPropertySource defaultPropertySource = new MapPropertySource("jetcache-default-config", defaultProperties);
            // 添加到最后（最低优先级），这样用户配置可以覆盖默认值
            propertySources.addLast(defaultPropertySource);
        }
    }
    
    /**
     * 获取属性值（按优先级：环境变量 > 系统属性 > 默认值）
     */
    private String getProperty(ConfigurableEnvironment environment, String envKey, String propKey, String defaultValue) {
        // 优先从环境变量获取
        String value = environment.getProperty(envKey);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        // 其次从系统属性获取（如 spring.redis.host）
        value = environment.getProperty(propKey);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        // 使用默认值
        return defaultValue;
    }
    
    /**
     * 设置属性值（仅在属性不存在时设置）
     */
    private void setIfAbsent(ConfigurableEnvironment environment, Map<String, Object> properties, String key, String value) {
        if (!environment.containsProperty(key)) {
            properties.put(key, value);
        }
    }
    
    /**
     * 构建 Redis URI
     */
    private String buildRedisUri(String host, String port, String password, String database) {
        if (password != null && !password.isEmpty()) {
            return String.format("redis://:%s@%s:%s/%s", password, host, port, database);
        } else {
            return String.format("redis://%s:%s/%s", host, port, database);
        }
    }
}
