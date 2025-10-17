package com.scccy.common.log.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;

/**
 * Micrometer Tracing 自动配置类
 * 替代 yml 配置文件，通过 Java 配置提供链路追踪功能
 * 
 * 使用方式：
 * 1. 直接引用 common-log 模块即可自动配置
 * 2. 可通过系统属性覆盖默认配置：
 *    -Dmanagement.tracing.enabled=true
 *    -Dmanagement.tracing.sampling.probability=0.1
 *    -Dmanagement.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
 *
 * @author scccy
 * @since 2025-10-11
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(TracingAutoConfiguration.TracingProperties.class)
public class TracingAutoConfiguration {

    /**
     * 链路追踪配置属性
     * 支持通过系统属性、环境变量或配置文件覆盖默认值
     */
    @Data
    @ConfigurationProperties(prefix = "management.tracing")
    public static class TracingProperties {
        private boolean enabled = true;
        private Sampling sampling = new Sampling();
        private Zipkin zipkin = new Zipkin();

        @Data
        public static class Sampling {
            private double probability = 0.1;
        }

        @Data
        public static class Zipkin {
            private Tracing tracing = new Tracing();

            @Data
            public static class Tracing {
                private String endpoint = "http://117.50.197.170:9411/api/v2/spans";
            }
        }
    }

    /**
     * 配置 Micrometer Tracing 属性
     * 通过系统属性设置 Micrometer Tracing 的配置
     */
    @Bean
    @ConditionalOnProperty(name = "management.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public TracingPropertiesConfigurer tracingPropertiesConfigurer(
            TracingProperties properties,
            @Value("${spring.application.name:unknown}") String applicationName) {
        
        // 设置系统属性，让 Spring Boot 的自动配置生效
        System.setProperty("management.tracing.sampling.probability", String.valueOf(properties.getSampling().getProbability()));
        System.setProperty("management.zipkin.tracing.endpoint", properties.getZipkin().getTracing().getEndpoint());
        System.setProperty("spring.application.name", applicationName);
        return new TracingPropertiesConfigurer(properties);
    }

    /**
     * 初始化追踪配置
     * 自动配置 Micrometer Tracing，无需手动配置
     */
    @Bean
    @ConditionalOnProperty(name = "management.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public TracingInitializer tracingInitializer(
            TracingProperties properties,
            @Value("${spring.application.name:unknown}") String applicationName) {

        // 同时使用日志输出
        log.info("=== Micrometer Tracing 自动配置 ===");
        log.info("应用名称: {}", applicationName);
        log.info("启用状态: {}", properties.isEnabled());
        log.info("采样率: {}%", properties.getSampling().getProbability() * 100);
        log.info("Zipkin 端点: {}", properties.getZipkin().getTracing().getEndpoint());
        log.info("=====================================");
        
        return new TracingInitializer(properties);
    }

    /**
     * 追踪初始化器
     * 负责初始化追踪相关的配置
     */
    public record TracingInitializer(TracingProperties properties) {
    }

    /**
     * 追踪属性配置器
     * 负责设置系统属性以启用 Micrometer Tracing
     */
    public record TracingPropertiesConfigurer(TracingProperties properties) {
    }
}
