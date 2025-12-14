package com.scccy.service.auth.config;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.auth.fegin.SystemUserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OAuth2 Token 自定义配置
 * <p>
 * 配置 Token 增强器，在 JWT Token 中携带用户信息和权限
 *
 * @author scccy
 */
@Slf4j
@Configuration
public class TokenCustomizerConfig {

    @Autowired
    private SystemUserClient systemUserClient;

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            Authentication principal = context.getPrincipal();
            String username = principal.getName();
            
            log.debug("Token 自定义器处理: username={}, grantType={}", 
                username, context.getAuthorizationGrantType());
            
            // 从系统服务获取用户信息
            SysUserMp user = getUserInfo(username);
            if (user == null) {
                log.warn("无法获取用户信息: username={}", username);
                // 如果无法获取用户信息，使用默认值
                context.getClaims().claim("username", username);
                context.getClaims().claim("authorities", Collections.emptyList());
                return;
            }
            
            // 获取用户权限（目前先返回空列表，后续需要扩展 SystemUserClient 获取权限）
            List<String> authorities = getUserAuthorities(username, user);
            
            // 添加自定义 claims
            context.getClaims().claim("username", user.getUserName());
            context.getClaims().claim("authorities", authorities);
            
            // 添加用户ID
            if (user.getUserId() != null) {
                context.getClaims().claim("userId", user.getUserId());
            }
            
            // 添加昵称（可选）
            if (user.getNickName() != null) {
                context.getClaims().claim("nickName", user.getNickName());
            }
            
            // 添加用户状态（可选）
            if (user.getStatus() != null) {
                context.getClaims().claim("status", user.getStatus());
            }
            
            log.debug("Token claims 已设置: userId={}, username={}, authorities={}", 
                user.getUserId(), user.getUserName(), authorities);
        };
    }
    
    /**
     * 从系统服务获取用户信息
     */
    private SysUserMp getUserInfo(String username) {
        try {
            ResultData<SysUserMp> result = systemUserClient.getByUserName(username);
            if (result != null && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("获取用户信息失败: username={}, error={}", username, e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 获取用户权限
     * <p>
     * TODO: 后续需要扩展 SystemUserClient 添加获取用户权限的接口
     * 目前先返回空列表，或者根据用户基本信息生成默认权限
     *
     * @param username 用户名
     * @param user 用户信息
     * @return 权限列表
     */
    private List<String> getUserAuthorities(String username, SysUserMp user) {
        // TODO: 调用 service-system 获取用户权限
        // 返回权限列表，如：["USER_READ", "USER_WRITE", "ROLE_ADMIN"]
        // 目前先返回默认权限
        List<String> authorities = new ArrayList<>();
        
        // 可以根据用户状态或角色添加默认权限
        if (user.getStatus() != null && user.getStatus() == 0) {
            // 用户状态正常，添加基本权限
            authorities.add("ROLE_USER");
        }
        
        // 后续扩展：从权限表或角色表查询用户权限
        // 例如：调用 systemUserClient.getUserAuthorities(username)
        
        return authorities;
    }
}

