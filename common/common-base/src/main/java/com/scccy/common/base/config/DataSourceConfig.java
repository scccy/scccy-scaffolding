package com.scccy.common.base.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 数据源配置类
 * 作者: scccy
 * 创建时间: 2025-07-31
 */
@Configuration
@EnableTransactionManagement
@ConditionalOnClass(DataSource.class)
public class DataSourceConfig {

    /**
     * 配置数据源事务管理器
     * Spring Boot会自动配置DataSource，这里显式配置事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }
} 