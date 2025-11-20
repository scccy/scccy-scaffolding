package com.scccy.common.seata.config;


import org.apache.seata.spring.annotation.GlobalTransactionScanner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Seata 客户端自动装配。
 */
@AutoConfiguration
@ConditionalOnClass(GlobalTransactionScanner.class)
@EnableConfigurationProperties(SeataClientProperties.class)
@ConditionalOnProperty(prefix = "seata.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SeataAutoConfiguration {

    /**
     * 注册全局事务扫描器，自动接入 @GlobalTransactional 等能力。
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalTransactionScanner globalTransactionScanner(SeataClientProperties properties) {
        return new GlobalTransactionScanner(properties.getApplicationId(), properties.getTxServiceGroup());
    }
}
