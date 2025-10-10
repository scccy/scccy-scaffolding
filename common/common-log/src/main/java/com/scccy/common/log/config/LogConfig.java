package com.scccy.common.log.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

/**
 * 日志配置类
 * 配置链路追踪和日志相关功能
 *
 * @author scccy
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class LogConfig {

    /**
     * 配置 Zipkin 报告器
     * 用于将链路追踪数据发送到 Zipkin 服务器
     *
     * @param zipkinEndpoint Zipkin 服务器地址
     * @return AsyncReporter
     */
    @Bean
    @ConditionalOnProperty(name = "spring.zipkin.enabled", havingValue = "true", matchIfMissing = false)
    public AsyncReporter<zipkin2.Span> zipkinReporter(
            @org.springframework.beans.factory.annotation.Value("${spring.zipkin.base-url:http://localhost:9411}") String zipkinEndpoint) {
        
        log.info("初始化 Zipkin 报告器，目标地址: {}", zipkinEndpoint);
        
        OkHttpSender sender = OkHttpSender.create(zipkinEndpoint + "/api/v2/spans");
        return AsyncReporter.create(sender);
    }
}
