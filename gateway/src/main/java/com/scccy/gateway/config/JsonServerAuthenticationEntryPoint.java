package com.scccy.gateway.config;

import com.alibaba.fastjson2.JSON;
import com.scccy.common.modules.dto.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Gateway JSON 认证入口点
 * <p>
 * 用于在 Gateway（WebFlux）中返回统一的 JSON 格式错误响应
 * 使用 ResultData 格式，与业务服务保持一致
 *
 * @author scccy
 * @since 2025-01-XX
 */
@Slf4j
public class JsonServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        log.debug("未认证访问: {}", exchange.getRequest().getURI());
        
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("Charset", "UTF-8");
        
        // 使用 ResultData 统一错误响应格式
        ResultData<Object> result = ResultData.fail(HttpStatus.UNAUTHORIZED.value(), "需要有效的访问令牌");
        String jsonResponse = JSON.toJSONString(result);
        
        DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}

