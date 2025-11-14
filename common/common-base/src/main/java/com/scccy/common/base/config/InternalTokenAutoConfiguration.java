package com.scccy.common.base.config;

import com.scccy.common.base.config.properties.InternalTokenProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 内部令牌自动配置类
 * <p>
 * 自动启用内部令牌配置属性，默认配置已通过 {@code InternalTokenEnvironmentPostProcessor} 自动加载
 * 
 * @author scccy
 */
@Configuration
@EnableConfigurationProperties(InternalTokenProperties.class)
@ConditionalOnProperty(prefix = "scccy.internal-token", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InternalTokenAutoConfiguration {
}

