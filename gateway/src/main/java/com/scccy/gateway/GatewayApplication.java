package com.scccy.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

        System.setProperty("spring.application.name", "gateway");
        
        SpringApplication.run(GatewayApplication.class, args);
    }
} 