package com.scccy.common.base.annotation;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.annotation.*;

/**
 * 微服务启动类统一注解
 * <p>
 * 使用此注解可以替代以下所有注解：
 * <ul>
 *     <li>@SpringBootApplication</li>
 *     <li>@EnableDiscoveryClient</li>
 *     <li>@EnableConfigurationProperties</li>
 *     <li>@EnableAsync</li>
 *     <li>@MapperScan</li>
 *     <li>@EnableJpaRepositories</li>
 *     <li>@EntityScan</li>
 *     <li>@EnableFeignClients</li>
 * </ul>
 * <p>
 * 使用方式：
 * <pre>
 * &#64;MicroServiceApplication
 * public class ServiceDemoApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(ServiceDemoApplication.class, args);
 *     }
 * }
 * </pre>
 * <p>
 * 注解会自动根据启动类所在包路径推断服务名，并动态配置扫描路径：
 * <ul>
 *     <li>Repository 扫描路径：{@code com.scccy.service.{serviceName}.dao.repository}</li>
 *     <li>Entity 扫描路径：{@code com.scccy.service.{serviceName}.domain.jpa}</li>
 * </ul>
 * <p>
 * 例如：如果启动类在 {@code com.scccy.service.demo} 包下，
 * 会自动配置：
 * <ul>
 *     <li>Repository: {@code com.scccy.service.demo.dao.repository}</li>
 *     <li>Entity: {@code com.scccy.service.demo.domain.jpa}</li>
 * </ul>
 * <p>
 * 注意：Spring Boot 3.x 不支持在 basePackages 中使用 {@code **} 通配符，
 * 因此我们通过编程式方式动态注册扫描路径，无需手动配置。
 *
 * @author scccy
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootApplication(scanBasePackages = {"com.scccy.service", "com.scccy.common"})
@EnableDiscoveryClient
@EnableConfigurationProperties
@EnableAsync
@MapperScan("com.scccy.service.**.dao.mapper")
@EnableFeignClients(basePackages = "com.scccy.service")
@Import(ScccyServiceApplicationRegistrar.class)
public @interface ScccyServiceApplication {
}

