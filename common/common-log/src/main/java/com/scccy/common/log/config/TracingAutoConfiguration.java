package com.scccy.common.log.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 自动配置：注册 RequestContextListener 与 LogTraceFilter
 */
@Configuration
@Slf4j
public class TracingAutoConfiguration implements WebMvcConfigurer {

    @Bean
    @ConditionalOnMissingBean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 方式一：拦截器中添加traceId
        // registry.addInterceptor(new LogInterceptor())
        //         .addPathPatterns("/**")
        //         .excludePathPatterns("/static/**", "/templates/**");
    }

    @Bean
    public FilterRegistrationBean<LogTraceFilter> logTraceFilter() {
        // 方式二：过滤器中添加traceId
        final FilterRegistrationBean<LogTraceFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        final LogTraceFilter logTraceFilter = new LogTraceFilter();
        filterRegistrationBean.setFilter(logTraceFilter);
        filterRegistrationBean.setName("logTraceFilter");
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }




    @Bean
    @ConditionalOnProperty(name = "management.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public TracingInitializer tracingInitializer(
            TracingProperties properties,
            @Value("${spring.application.name:unknown}") String applicationName) {

        log.info("=== Micrometer Tracing 自动配置 ===");
        log.info("应用名称: {}", applicationName);
        log.info("启用状态: {}", properties.isEnabled());
        log.info("采样率: {}%", properties.getSampling().getProbability() * 100);
        log.info("Zipkin 端点: {}", properties.getZipkin().getTracing().getEndpoint());
        log.info("=====================================");

        return new TracingInitializer(properties);
    }

    public record TracingInitializer(TracingProperties properties) {
    }

}