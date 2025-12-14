package com.scccy.service.demo.fegin;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * OAuth2 Token 获取 Feign 客户端
 * <p>
 * 用于调用 service-auth 的 OAuth2 Token 端点
 * <p>
 * 注意：OAuth2 Token 端点需要：
 * - Basic 认证（client_id:client_secret）
 * - form-urlencoded 格式的请求体
 *
 * @author scccy
 */
@FeignClient(name = "service-auth", path = "/oauth2", fallbackFactory = AuthTokenClientFallback.class)
public interface AuthTokenClient {

    /**
     * 客户端凭证模式获取 Token
     *
     * @param authorization Basic 认证头（格式：Basic base64(client_id:client_secret)）
     * @param grantType     授权类型（固定为 client_credentials）
     * @return Token 响应（Map 格式，包含 access_token、token_type、expires_in 等）
     */
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> getTokenByClientCredentials(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("grant_type") String grantType);

    /**
     * 密码模式获取 Token
     *
     * @param authorization Basic 认证头（格式：Basic base64(client_id:client_secret)）
     * @param grantType     授权类型（固定为 password）
     * @param username      用户名
     * @param password      密码（明文）
     * @return Token 响应（Map 格式，包含 access_token、token_type、expires_in、refresh_token 等）
     */
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> getTokenByPassword(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("grant_type") String grantType,
            @RequestParam("username") String username,
            @RequestParam("password") String password);

    /**
     * 刷新 Token
     *
     * @param authorization Basic 认证头（格式：Basic base64(client_id:client_secret)）
     * @param grantType     授权类型（固定为 refresh_token）
     * @param refreshToken  刷新 Token
     * @return Token 响应（Map 格式，包含 access_token、token_type、expires_in、refresh_token 等）
     */
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> refreshToken(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("grant_type") String grantType,
            @RequestParam("refresh_token") String refreshToken);
}

