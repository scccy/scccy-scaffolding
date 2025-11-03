package com.scccy.service.system.config;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT 配置类
 * <p>
 * 配置JWT密钥、过期时间等参数
 * 支持从配置文件读取或使用默认值
 *
 * @author scccy
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT密钥
     * 生产环境必须使用强随机密钥，建议通过环境变量设置
     */
    private String secret = "scccy-jwt-secret-key-change-this-in-production-environment-use-strong-random-key";

    /**
     * Token过期时间（毫秒）
     * 默认2小时
     */
    private Long expiration = 7200000L;

    /**
     * 刷新Token过期时间（毫秒）
     * 默认7天
     */
    private Long refreshExpiration = 604800000L;

    /**
     * Token请求头名称
     * 默认：Authorization
     */
    private String header = "Authorization";

    /**
     * Token前缀
     * 默认：Bearer
     */
    private String tokenPrefix = "Bearer ";

    /**
     * 获取SecretKey
     * 使用HMAC-SHA算法
     *
     * @return SecretKey
     */
    @Bean
    public SecretKey secretKey() {
        // 确保密钥长度至少为256位（32字节）以支持HS256算法
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // 如果密钥长度不足，使用密钥派生函数（KDF）扩展密钥
            throw new IllegalArgumentException("JWT密钥长度至少需要32字节（256位），当前长度: " + keyBytes.length);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

