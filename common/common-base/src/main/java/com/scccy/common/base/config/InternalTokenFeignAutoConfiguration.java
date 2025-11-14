package com.scccy.common.base.config;

import com.scccy.common.base.config.properties.InternalTokenFeignProperties;
import com.scccy.common.base.feign.InternalTokenFeignRequestInterceptor;
import com.scccy.common.base.manager.InternalTokenManager;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 内部令牌 Feign 自动配置
 */
@Configuration
@ConditionalOnClass(RequestInterceptor.class)
@EnableConfigurationProperties(InternalTokenFeignProperties.class)
@ConditionalOnProperty(prefix = "scccy.internal-token", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InternalTokenFeignAutoConfiguration {

    @Bean
    @ConditionalOnBean(InternalTokenManager.class)
    @ConditionalOnMissingBean(name = "internalTokenFeignRequestInterceptor")
    @ConditionalOnProperty(prefix = "scccy.internal-token.feign", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RequestInterceptor internalTokenFeignRequestInterceptor(InternalTokenManager internalTokenManager,
                                                                   InternalTokenFeignProperties feignProperties) {
        return new InternalTokenFeignRequestInterceptor(internalTokenManager, feignProperties);
    }
}

