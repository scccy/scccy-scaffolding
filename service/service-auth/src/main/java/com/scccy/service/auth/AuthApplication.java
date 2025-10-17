package com.scccy.service.auth;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication(scanBasePackages = {"com.scccy.service", "com.scccy.common"})
@Slf4j
@EnableDiscoveryClient
@MapperScan
public class AuthApplication {

    @SneakyThrows
    public static void main(String[] args) {

        System.setProperty("spring.application.name", "service-auth");

        SpringApplication.run(AuthApplication.class, args);
    }

}
