package com.scccy.common.log.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import brave.sampler.Sampler;

/**
 * Spring Cloud Sleuth 配置类
 * 配置链路追踪采样率等参数
 *
 * @author scccy
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class SleuthConfig {

    /**
     * 配置链路追踪采样器
     * 默认采样率为 10%，可通过配置调整
     *
     * @param sampleRate 采样率 (0.0 - 1.0)
     * @return Sampler
     */
    @Bean
    @ConditionalOnProperty(name = "spring.sleuth.enabled", havingValue = "true", matchIfMissing = true)
    public Sampler sleuthSampler(
            @org.springframework.beans.factory.annotation.Value("${spring.sleuth.sampler.probability:0.1}") float sampleRate) {
        
        log.info("初始化 Sleuth 采样器，采样率: {}%", sampleRate * 100);
        
        return Sampler.create(sampleRate);
    }
}
