package com.scccy.service.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动类
 * Gateway 是响应式服务（WebFlux），不需要数据库连接
 * 排除数据源相关的自动配置，避免因 common-modules 引入的 MyBatis 依赖导致启动失败
 * 
 * @author origin
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    
    public static void main(String[] args) {
        // 在 Spring 启动前设置服务名，确保 Log4j2 能够读取到正确的服务名
        // 这样日志文件会保存到 ./logs/service-gateway/ 目录，而不是 ./logs/unknown-service/
        System.setProperty("spring.application.name", "gateway");
        
        SpringApplication.run(GatewayApplication.class, args);
    }
} 