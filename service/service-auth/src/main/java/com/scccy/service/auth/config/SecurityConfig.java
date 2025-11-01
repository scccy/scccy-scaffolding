package com.scccy.service.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置
 * <p>
 * 配置登录相关的安全过滤器链，支持前后端分离的 JSON 登录
 *
 * @author scccy
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 登录接口安全配置
     * <p>
     * 优先级低于 OAuth2 授权服务器的过滤器链，但需要处理 /login 接口的访问控制
     * 允许 /login POST 接口公开访问，禁用表单登录
     *
     * @param httpSecurity Spring Security 过滤器链
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    @Order(1)
    public SecurityFilterChain loginSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("配置登录接口安全过滤器链");

        httpSecurity
                .securityMatcher("/login")
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated()
                )
                // 禁用表单登录（前后端分离，使用 JSON 登录）
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用 HTTP Basic 认证
                .httpBasic(AbstractHttpConfigurer::disable)
                // 禁用 CSRF（对于前后端分离的 API，通常使用 JWT Token，不需要 CSRF 保护）
                .csrf(AbstractHttpConfigurer::disable)
                // 无状态会话（前后端分离，不使用 Session）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return httpSecurity.build();
    }
}

