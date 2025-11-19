package com.scccy.common.base.config;

import com.scccy.common.base.config.properties.InternalTokenFeignProperties;
import com.scccy.common.base.config.properties.InternalTokenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({InternalTokenProperties.class, InternalTokenFeignProperties.class})
public class InternalTokenAutoConfiguration {
}


