package com.scccy.common.base.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 内部服务令牌配置属性
 * <p>
 * 用于配置服务间调用的内部 OAuth2 客户端凭证
 * <p>
 * 默认配置已通过 {@code InternalTokenEnvironmentPostProcessor} 自动加载，
 * 无需在 yml 中手动配置，除非需要覆盖默认值。
 * 
 * @author scccy
 */
@Data
@ConfigurationProperties(prefix = "scccy.internal-token")
public class InternalTokenProperties {
    
    /**
     * 是否启用内部令牌功能
     */
    private Boolean enabled = true;
    
    /**
     * OAuth2 客户端 ID
     */
    private String clientId;
    
    /**
     * OAuth2 客户端密钥
     */
    private String clientSecret;
    
    /**
     * OAuth2 Token 端点 URL
     * 默认: lb://service-auth/oauth2/token
     */
    private String tokenUrl = "lb://service-auth/oauth2/token";
    
    /**
     * OAuth2 Scope
     * 默认: internal-service
     */
    private String scope = "internal-service";

    /**
     * Token audience，针对特定目标服务可选配置
     */
    private String audience;

    /**
     * OAuth2 授权模式，默认为 client_credentials
     */
    private String grantType = "client_credentials";
    
    /**
     * Token 缓存过期时间（秒），应该略小于真实 token 有效期
     * 默认: 540（9分钟，token有效期10分钟时）
     */
    private Long cacheExpireSeconds = 540L;
    
    /**
     * Token 刷新提前时间（秒），在 token 过期前多少秒开始刷新
     * 默认: 60（1分钟）
     */
    private Long refreshAheadSeconds = 60L;
}
