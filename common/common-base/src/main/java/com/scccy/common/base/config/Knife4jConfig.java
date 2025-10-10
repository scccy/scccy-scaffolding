package com.scccy.common.base.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j配置类
 * 用于配置API文档信息
 * 适用于Spring Boot 3.x + Jakarta EE
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SCCCY Scaffolding API文档")
                        .description("SCCCY脚手架项目微服务架构API接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SCCCY Team")
                                .email("support@scccy.com")
                                .url("https://github.com/scccy/scaffolding"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
} 