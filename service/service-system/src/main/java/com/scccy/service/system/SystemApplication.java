package com.scccy.service.system;

import com.scccy.common.base.annotation.ScccyServiceApplication;
import org.springframework.boot.SpringApplication;

/**
 * 飞书相关
 *
 * @author scccy
 */

@ScccyServiceApplication
public class SystemApplication {

    public static void main(String[] args) {
        System.setProperty("spring.application.name", "service-system");

        SpringApplication.run(SystemApplication.class, args);


    }
}