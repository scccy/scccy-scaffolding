package com.scccy.service.system;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.scccy.service","com.scccy.common"})
@Slf4j
@EnableDiscoveryClient
@MapperScan("com.scccy.service.**.mapper")
public class SystemApplication {


    public static void main(String[] args) {

        System.setProperty("spring.application.name", "service-system");

        SpringApplication.run(SystemApplication.class, args);
    }

}
