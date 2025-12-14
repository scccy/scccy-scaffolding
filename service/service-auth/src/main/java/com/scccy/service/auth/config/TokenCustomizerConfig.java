package com.scccy.service.auth.config;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.service.auth.fegin.SystemUserClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Collections;
import java.util.List;

/**
 * OAuth2 Token 自定义器配置
 * <p>
 * 用于在 JWT Token 中携带用户信息和权限
 *
 * @author scccy
 */
@Slf4j
@Configuration
public class TokenCustomizerConfig {

    @Resource
    private SystemUserClient systemUserClient;

    /**
     * JWT Token 自定义器
     * <p>
     * 在 Token 中添加用户 ID、用户名、权限等信息
     *
     * @return OAuth2TokenCustomizer
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            Authentication principal = context.getPrincipal();
            String username = principal.getName();

            log.debug("开始自定义 JWT Token，用户名: {}", username);

            // 从系统服务获取用户信息
            SysUserMp user = null;
            try {
                var result = systemUserClient.getByUserName(username);
                if (result != null && result.getData() != null) {
                    user = result.getData();
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败: {}", e.getMessage());
            }

            // 添加自定义 claims
            if (user != null) {
                // 添加用户ID
                if (user.getUserId() != null) {
                    context.getClaims().claim("userId", user.getUserId());
                    log.debug("添加 userId: {}", user.getUserId());
                }

                // 添加用户名
                context.getClaims().claim("username", user.getUserName());

                // 添加昵称
                if (user.getNickName() != null) {
                    context.getClaims().claim("nickName", user.getNickName());
                }

                // 添加用户状态
                if (user.getStatus() != null) {
                    context.getClaims().claim("status", user.getStatus());
                }
            } else {
                // 如果无法获取用户信息，至少添加用户名
                context.getClaims().claim("username", username);
            }

            // 获取用户权限（暂时返回空列表，后续可以扩展）
            List<String> authorities = getUserAuthorities(username, user);
            context.getClaims().claim("authorities", authorities);
            log.debug("添加 authorities: {}", authorities);

            // 对于客户端凭证模式，可能需要特殊处理
            if (context.getAuthorizationGrantType().equals(AuthorizationGrantType.CLIENT_CREDENTIALS)) {
                log.debug("客户端凭证模式，不添加用户信息");
                // 客户端凭证模式通常不包含用户信息
            }
        };
    }

    /**
     * 获取用户权限列表
     * <p>
     * 通过 Feign 调用 service-system 获取用户权限
     * 查询用户 → 角色 → 菜单权限的完整链路
     * 返回权限列表，包含：
     * - 角色标识：ROLE_ADMIN, ROLE_USER（Spring Security 标准格式）
     * - 菜单权限：system:user:list, system:user:add（菜单 perms 字段）
     *
     * @param username 用户名
     * @param user     用户信息（如果已获取，暂时未使用）
     * @return 权限列表
     */
    private List<String> getUserAuthorities(String username, SysUserMp user) {
        try {
            // 调用 service-system 获取用户权限
            var result = systemUserClient.getUserAuthorities(username);
            if (result != null && result.getData() != null) {
                log.debug("获取用户权限成功: username={}, authorities={}", username, result.getData());
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("获取用户权限失败: username={}, error={}", username, e.getMessage());
        }
        return Collections.emptyList();
    }
}

