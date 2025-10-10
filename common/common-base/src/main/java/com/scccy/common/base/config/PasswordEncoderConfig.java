package com.scccy.common.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密器配置类
 * 提供统一的PasswordEncoder Bean，供所有模块使用
 * 
 * @author origin
 * @since 2025-08-15
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 密码加密器
     * 使用BCrypt算法，强度为12，确保安全性
     * 
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
