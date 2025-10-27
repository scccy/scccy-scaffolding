package com.scccy.service.wechatwork;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 企业微信
 * 
 * @author scccy
 */
//@SpringBootApplication
@SpringBootApplication(scanBasePackages = {"com.scccy.service", "com.scccy.common"})
@EnableDiscoveryClient
@EnableConfigurationProperties
@Slf4j
@EnableAsync
@MapperScan("com.scccy.service.**.dao.mapper")
@EnableJpaRepositories
public class WechatworkApplication {

    public static void main(String[] args) {

        System.setProperty("spring.application.name", "service-wechatwork");
        SpringApplication.run(WechatworkApplication.class, args);
    }
}