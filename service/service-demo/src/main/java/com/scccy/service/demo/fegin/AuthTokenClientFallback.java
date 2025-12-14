package com.scccy.service.demo.fegin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 Token 获取 Feign 客户端降级服务工厂
 * <p>
 * 当 service-auth 服务不可用时，返回明确的错误信息
 *
 * @author scccy
 */
@Slf4j
@Component
public class AuthTokenClientFallback implements FallbackFactory<AuthTokenClient> {

    @Override
    public AuthTokenClient create(Throwable cause) {
        return new AuthTokenClient() {
            @Override
            public Map<String, Object> getTokenByClientCredentials(String authorization, String grantType) {
                log.error("service-auth 服务不可用，无法获取 Token（客户端凭证模式），错误原因: {}",
                        cause != null ? cause.getMessage() : "未知错误", cause);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "service_unavailable");
                error.put("error_description", "service-auth 服务不可用: " +
                        (cause != null ? cause.getMessage() : "服务调用失败"));
                return error;
            }

            @Override
            public Map<String, Object> getTokenByPassword(String authorization, String grantType, String username, String password) {
                log.error("service-auth 服务不可用，无法获取 Token（密码模式），username={}, 错误原因: {}",
                        username, cause != null ? cause.getMessage() : "未知错误", cause);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "service_unavailable");
                error.put("error_description", "service-auth 服务不可用: " +
                        (cause != null ? cause.getMessage() : "服务调用失败"));
                return error;
            }

            @Override
            public Map<String, Object> refreshToken(String authorization, String grantType, String refreshToken) {
                log.error("service-auth 服务不可用，无法刷新 Token，错误原因: {}",
                        cause != null ? cause.getMessage() : "未知错误", cause);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "service_unavailable");
                error.put("error_description", "service-auth 服务不可用: " +
                        (cause != null ? cause.getMessage() : "服务调用失败"));
                return error;
            }
        };
    }
}

