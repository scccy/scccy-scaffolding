package com.scccy.service.demo;

import com.scccy.common.base.annotation.ScccyServiceApplication;
import org.springframework.boot.SpringApplication;

/**
 * Demo 服务启动类
 * 
 * @author scccy
 */
@ScccyServiceApplication
public class ServiceDemoApplication {

    public static void main(String[] args) {
        System.setProperty("spring.application.name", "service-demo");
        SpringApplication.run(ServiceDemoApplication.class, args);
    }
}
