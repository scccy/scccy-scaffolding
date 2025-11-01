package com.scccy.service.wechatwork;

import com.scccy.common.base.annotation.ScccyServiceApplication;
import org.springframework.boot.SpringApplication;

/**
 * 企业微信
 * 
 * @author scccy
 */
@ScccyServiceApplication
public class WechatworkApplication {

    public static void main(String[] args) {

        System.setProperty("spring.application.name", "service-wechatwork");
        SpringApplication.run(WechatworkApplication.class, args);
    }
}