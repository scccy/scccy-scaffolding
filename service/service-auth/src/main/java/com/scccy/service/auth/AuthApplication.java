package com.scccy.service.auth;

import com.scccy.common.base.annotation.ScccyServiceApplication;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;

@ScccyServiceApplication
public class AuthApplication {

    @SneakyThrows
    public static void main(String[] args) {

        System.setProperty("spring.application.name", "service-auth");

        SpringApplication.run(AuthApplication.class, args);
    }

}
