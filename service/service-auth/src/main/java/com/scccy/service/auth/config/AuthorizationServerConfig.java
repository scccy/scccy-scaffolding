package com.scccy.service.auth.config;

import com.alibaba.fastjson2.JSON;
import com.scccy.common.modules.constant.SecurityPathConstants;
import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.auth.fegin.SystemUserClient;
import com.scccy.service.auth.oauth2.device.DeviceClientAuthenticationConverter;
import com.scccy.service.auth.oauth2.device.DeviceClientAuthenticationProvider;
import com.scccy.service.auth.oauth2.handler.Oauth2AccessDeniedHandler;

import com.scccy.service.auth.oauth2.handler.Oauth2FailureHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpStatus;
import java.util.function.Function;

@Slf4j
@Configuration
public class AuthorizationServerConfig {

    // 注意：前后端分离架构，不再使用 HTML 页面
    // OAuth2 授权流程应由前端处理，后端只提供 API 接口
    // 如果需要授权确认页面，应该由前端实现，后端只提供授权数据 API

    @Resource
    private SystemUserClient userService;

    /**
     * 端点的 Spring Security 过滤器链
     *
     * @param httpSecurity Spring Security 过滤器链
     * @return SecurityFilterChain
     * @throws Exception 初使化异常
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity httpSecurity,
                                                                      RegisteredClientRepository registeredClientRepository,
                                                                      AuthorizationServerSettings authorizationServerSettings) throws Exception {
        log.info("Init HttpSecurity for Oauth2");
        // 现代方式：直接使用 OAuth2AuthorizationServerConfigurer 配置
        // 自定义用户映射器
        Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> {
            OidcUserInfoAuthenticationToken authentication = context.getAuthentication();
            JwtAuthenticationToken principal = (JwtAuthenticationToken) authentication.getPrincipal();
            SysUserMp user = userService.getByUserName(principal.getName()).getData();
            return OidcUserInfo.builder()
                    .subject(user.getUserName())
                    .name(user.getNickName()).build();
        };
        Oauth2FailureHandler errorResponseHandler = new Oauth2FailureHandler();
        // 新建设备码converter和provider
        DeviceClientAuthenticationConverter deviceClientAuthenticationConverter =
                new DeviceClientAuthenticationConverter(authorizationServerSettings.getDeviceAuthorizationEndpoint());
        DeviceClientAuthenticationProvider deviceClientAuthenticationProvider = new DeviceClientAuthenticationProvider(registeredClientRepository);

        // 现代方式：直接配置 OAuth2 授权服务器，而不是先调用 applyDefaultSecurity
        // 创建 OAuth2 授权服务器配置器
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();


        httpSecurity
                // 使用 securityMatcher 匹配所有需要处理的端点（统一使用 SecurityPathConstants 管理）
                // 包括 OAuth2 端点、登录端点、文档端点等，避免与其他过滤器链冲突
                .securityMatcher(SecurityPathConstants.AUTHORIZATION_SERVER_PUBLIC_ENDPOINTS)
                // 文档与公开端点不需要 CSRF 保护，避免 POST 公开接口被 403
                .csrf(AbstractHttpConfigurer::disable)
                .with(authorizationServerConfigurer, configurer -> configurer
                        // 设置客户端授权中失败的handler处理
                        .clientAuthentication((auth) -> auth.errorResponseHandler(errorResponseHandler))
                        // token 相关配置 如:/oauth2/token接口
                        .tokenEndpoint((token) -> token.errorResponseHandler(errorResponseHandler))
                        // Enable OpenID Connect 1.0
                        .oidc((oidc) -> {
                            // userinfo返回自定义用户信息
                            oidc.userInfoEndpoint((userInfo) -> {
                                        userInfo.userInfoMapper(userInfoMapper);
//                                userInfo.userInfoResponseHandler(new Oauth2SuccessHandler());
                                    }
                            );
                        })
                        // 前后端分离架构：不再设置 HTML 页面
                        // 授权确认和设备验证应由前端处理，后端只提供 API 接口
                        // 如果需要自定义授权流程，可以通过前端调用后端 API 实现
                        // 客户端认证添加设备码的converter和provider
                        .clientAuthentication(clientAuthentication ->
                                clientAuthentication
                                        .authenticationConverter(deviceClientAuthenticationConverter)
                                        .authenticationProvider(deviceClientAuthenticationProvider)
                        ));

        // 允许公开访问的端点
        httpSecurity.authorizeHttpRequests(authorize -> authorize
                // OAuth2 Token 撤销接口需要认证（排除在公开端点之外）
                .requestMatchers("/oauth2/revoke").authenticated()
                // 用户注册和登录接口公开访问（注意：securityMatcher 限制只处理 AUTHORIZATION_SERVER_PUBLIC_ENDPOINTS，这些路径由第二个过滤器链处理）
                // 其他公开端点（securityMatcher 已限制只处理这些路径）
                .requestMatchers(SecurityPathConstants.AUTHORIZATION_SERVER_PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
        );

        // 前后端分离架构：未通过身份验证时返回 401 JSON，而不是重定向到登录页面或返回 HTML
        httpSecurity.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    log.debug("未认证访问: {}", request.getRequestURI());
                    
                    // 使用 ResultData 统一错误响应格式
                    ResultData<Object> result = ResultData.fail(HttpStatus.UNAUTHORIZED.value(), "需要有效的访问令牌");
                    String jsonResponse = JSON.toJSONString(result);
                    
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(jsonResponse);
                })
        );
        // 处理使用access token访问用户信息端点和客户端注册端点
        httpSecurity.oauth2ResourceServer(resourceServer ->
                resourceServer
                        .jwt(Customizer.withDefaults())
                        .accessDeniedHandler(new Oauth2AccessDeniedHandler())
        );
        return httpSecurity.build();
    }

    /**
     * Resource Server 过滤器链配置
     * <p>
     * 处理需要 JWT Token 认证的接口（如 /api/user/logout）
     * 优先级低于 Authorization Server 过滤器链
     * <p>
     * 注意：此过滤器链只处理 `/api/**` 路径，`/oauth2/revoke` 由第一个过滤器链处理（但需要认证）
     *
     * @param httpSecurity Spring Security 过滤器链
     * @return SecurityFilterChain
     * @throws Exception 初始化异常
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("Init HttpSecurity for Resource Server (Auth Service)");

        httpSecurity
                // 匹配需要认证的接口（排除 Authorization Server 已处理的端点）
                .securityMatcher("/api/**")
                // 前后端分离 API 模式下关闭 CSRF，放行无需认证的 POST 注册/登录
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // 用户注册接口公开访问
                        .requestMatchers("/api/user/register").permitAll()
                        // 用户登录接口公开访问
                        .requestMatchers("/api/user/login").permitAll()
                        // 用户登出接口公开访问（登出不需要验证 token，即使 token 无效也应允许加入黑名单）
                        .requestMatchers("/api/user/logout").permitAll()
                        // 其他所有 /api/** 接口都需要内部服务 scope（仅服务间 Feign 调用）
                        .requestMatchers("/api/**").hasAuthority("SCOPE_internal-service")
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 配置 OAuth2 Resource Server
                .oauth2ResourceServer(resourceServer ->
                        resourceServer
                                .jwt(Customizer.withDefaults())
                                .accessDeniedHandler(new Oauth2AccessDeniedHandler())
                )
                // 前后端分离架构：未通过身份验证时返回 401 JSON
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.debug("未认证访问 Resource Server 接口: {}", request.getRequestURI());

                            // 使用 ResultData 统一错误响应格式
                            ResultData<Object> result = ResultData.fail(HttpStatus.UNAUTHORIZED.value(), "需要有效的访问令牌");
                            String jsonResponse = JSON.toJSONString(result);

                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(jsonResponse);
                        })
                );

        return httpSecurity.build();
    }
}
