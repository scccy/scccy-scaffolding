package com.scccy.service.demo;

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
public class ServiceDemoApplication {

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication app=new SpringApplication(ServiceDemoApplication.class);

        System.setProperty("spring.application.name", "service-demo");

        ConfigurableApplicationContext application=app.run(args);
        //ConfigurableApplicationContext application=SpringApplication.run(Knife4jSpringBootDemoApplication.class, args);
        Environment env = application.getEnvironment();
//        log.info("\n----------------------------------------------------------\n\t" +
//                        "Application '{}' is running! Access URLs:\n\t" +
//                        "Local: \t\thttp://localhost:{}\n\t" +
//                        "External: \thttp://{}:{}\n\t"+
//                        "Doc: \thttp://{}:{}/doc.html\n"+
//                        "----------------------------------------------------------",
//                env.getProperty("spring.application.name"),
//                env.getProperty("server.port"),
//                InetAddress.getLocalHost().getHostAddress(),
//                env.getProperty("server.port"),
//                InetAddress.getLocalHost().getHostAddress(),
//                env.getProperty("server.port"));
    }

}
