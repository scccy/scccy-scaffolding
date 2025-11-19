package com.scccy.common.base.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 公共 WebClient 配置，提供带负载均衡能力的 Builder
 */
@Configuration
public class WebClientConfig {

    /**
     * 提供一个 LoadBalanced WebClient.Builder，支持使用服务名（lb://service-id）访问
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}

