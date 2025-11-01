package com.scccy.service.feishu;

import com.scccy.common.base.annotation.ScccyServiceApplication;
import org.springframework.boot.SpringApplication;

/**
 * 网关服务启动类
 * 
 * @author origin
 */
@ScccyServiceApplication
public class FeishuApplication {
    
    public static void main(String[] args) {

        System.setProperty("spring.application.name", "service-feishu");
        SpringApplication.run(FeishuApplication.class, args);
    }
} 