package com.scccy.service.jackyun;

import com.scccy.common.base.annotation.ScccyServiceApplication;

import org.springframework.boot.SpringApplication;


@ScccyServiceApplication
public class ServiceDemoApplication {


    public static void main(String[] args) {
        System.setProperty("spring.application.name", "service-demo");

        new SpringApplication(ServiceDemoApplication.class);


    }

}
