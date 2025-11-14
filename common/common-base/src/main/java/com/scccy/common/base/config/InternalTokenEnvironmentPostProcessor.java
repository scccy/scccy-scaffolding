package com.scccy.common.base.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;
import java.util.Map;

/**
 * 内部令牌环境后处理器
 * <p>
 * 在环境准备阶段设置内部令牌的默认配置值，确保自动加载默认配置。
 * <p>
 * 通过 {@code META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor.imports} 文件注册。
 * <p>
 * 默认配置说明：
 * <ul>
 *     <li>clientId: internal-service-client（默认内部服务客户端）</li>
 *     <li>clientSecret: InternalSecret123!（默认密钥，建议生产环境修改）</li>
 *     <li>tokenUrl: http://service-auth:30002/oauth2/token（默认认证服务地址）</li>
 *     <li>scope: internal-service（默认作用域）</li>
 *     <li>enabled: true（默认启用）</li>
 * </ul>
 * <p>
 * 配置覆盖优先级（从高到低）：
 * <ol>
 *     <li>Nacos 配置中心的配置（最高优先级）</li>
 *     <li>应用本地 application.yml 配置</li>
 *     <li>环境变量配置（SCCCY_INTERNAL_TOKEN_CLIENT_ID 等）</li>
 *     <li>此默认配置（最低优先级）</li>
 * </ol>
 *
 * @author scccy
 */
public class InternalTokenEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 设置默认配置属性（仅在属性不存在时设置）
        Map<String, Object> defaultProperties = new HashMap<>();
        
        // 内部令牌基础配置
        setIfAbsent(environment, defaultProperties, "scccy.internal-token.enabled", "true");
        setIfAbsent(environment, defaultProperties, "scccy.internal-token.client-id", "internal-service-client");
        setIfAbsent(environment, defaultProperties, "scccy.internal-token.client-secret", "InternalSecret123!");
        setIfAbsent(environment, defaultProperties, "scccy.internal-token.token-url", "http://service-auth:30002/oauth2/token");
        setIfAbsent(environment, defaultProperties, "scccy.internal-token.scope", "internal-service");
        setIfAbsent(environment, defaultProperties, "scccy.internal-token.cache-expire-seconds", "540");
        setIfAbsent(environment, defaultProperties, "scccy.internal-token.refresh-ahead-seconds", "60");
        
        // 从环境变量覆盖（如果存在）
        overrideFromEnv(environment, defaultProperties, "SCCCY_INTERNAL_TOKEN_CLIENT_ID", "scccy.internal-token.client-id");
        overrideFromEnv(environment, defaultProperties, "SCCCY_INTERNAL_TOKEN_CLIENT_SECRET", "scccy.internal-token.client-secret");
        overrideFromEnv(environment, defaultProperties, "SCCCY_INTERNAL_TOKEN_TOKEN_URL", "scccy.internal-token.token-url");
        overrideFromEnv(environment, defaultProperties, "SCCCY_INTERNAL_TOKEN_SCOPE", "scccy.internal-token.scope");
        overrideFromEnv(environment, defaultProperties, "SCCCY_INTERNAL_TOKEN_ENABLED", "scccy.internal-token.enabled");
        
        // 将默认配置添加到环境变量中（最低优先级）
        if (!defaultProperties.isEmpty()) {
            MutablePropertySources propertySources = environment.getPropertySources();
            MapPropertySource defaultPropertySource = new MapPropertySource("internal-token-default-config", defaultProperties);
            // 添加到最后（最低优先级），这样用户配置可以覆盖默认值
            propertySources.addLast(defaultPropertySource);
        }
    }
    
    /**
     * 设置属性值（仅在属性不存在时设置）
     */
    private void setIfAbsent(ConfigurableEnvironment environment, Map<String, Object> defaultProperties, 
                            String key, String value) {
        if (!environment.containsProperty(key)) {
            defaultProperties.put(key, value);
        }
    }
    
    /**
     * 从环境变量覆盖默认值
     */
    private void overrideFromEnv(ConfigurableEnvironment environment, Map<String, Object> defaultProperties, 
                                String envKey, String propKey) {
        String envValue = environment.getProperty(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            // 如果用户已配置该属性，则不覆盖
            if (!environment.containsProperty(propKey)) {
                defaultProperties.put(propKey, envValue);
            }
        }
    }
}

