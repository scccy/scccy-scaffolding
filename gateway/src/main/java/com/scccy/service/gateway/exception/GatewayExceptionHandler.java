package com.scccy.service.gateway.exception;


import com.alibaba.fastjson2.JSON;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.common.modules.entity.ErrorCode;
import com.scccy.common.modules.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Gateway WebFlux异常处理器
 * 处理Gateway服务中的异常，返回统一的错误响应
 * 
 * 职责范围：
 * 1. 处理网关层面的异常（路由异常、连接异常等）
 * 2. 处理业务异常（BusinessException）
 * 3. 处理系统异常（其他未捕获的异常）
 * 4. 确保所有异常都返回统一的ResultData格式
 * 
 * @author origin
 */
@Slf4j
@Component
@Order(-1)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus httpStatus;
        ResultData<Object> resultData;
        
        if (ex instanceof BusinessException) {
            BusinessException businessException = (BusinessException) ex;
            log.warn("Gateway业务异常: {}", businessException.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            resultData = ResultData.fail(businessException.getErrorCode(), businessException.getMessage());
        } else if (ex instanceof NotFoundException) {
            // 处理服务不可用异常
            log.error("Gateway服务不可用异常: {}", ex.getMessage());
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
            resultData = ResultData.fail(ErrorCode.SERVICE_UNAVAILABLE, "服务暂时不可用，请稍后重试");
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException statusException = (ResponseStatusException) ex;
            log.error("Gateway响应状态异常: {} - {}", statusException.getStatusCode(), statusException.getReason());
            httpStatus = HttpStatus.valueOf(statusException.getStatusCode().value());
            
            // 根据不同的状态码返回不同的错误信息
            if (statusException.getStatusCode().value() == 503) {
                resultData = ResultData.fail(ErrorCode.SERVICE_UNAVAILABLE, "服务暂时不可用，请稍后重试");
            } else if (statusException.getStatusCode().value() == 502) {
                resultData = ResultData.fail(ErrorCode.BAD_GATEWAY, "网关错误，请稍后重试");
            } else if (statusException.getStatusCode().value() == 504) {
                resultData = ResultData.fail(ErrorCode.GATEWAY_TIMEOUT, "网关超时，请稍后重试");
            } else {
                resultData = ResultData.fail(ErrorCode.INTERNAL_ERROR, "网关服务异常，请稍后重试");
            }
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            // 处理超时异常
            log.error("Gateway超时异常: {}", ex.getMessage());
            httpStatus = HttpStatus.REQUEST_TIMEOUT;
            resultData = ResultData.fail(ErrorCode.GATEWAY_TIMEOUT, "网关超时，请稍后重试");
        } else if (ex instanceof java.net.ConnectException) {
            // 处理连接异常
            log.error("Gateway连接异常: {}", ex.getMessage());
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
            resultData = ResultData.fail(ErrorCode.SERVICE_UNAVAILABLE, "服务连接失败，请稍后重试");
        } else if (ex instanceof java.net.SocketTimeoutException) {
            // 处理Socket超时异常
            log.error("Gateway Socket超时异常: {}", ex.getMessage());
            httpStatus = HttpStatus.REQUEST_TIMEOUT;
            resultData = ResultData.fail(ErrorCode.GATEWAY_TIMEOUT, "网关Socket超时，请稍后重试");
        } else if (ex instanceof org.springframework.web.server.ServerWebInputException) {
            // 处理输入异常
            log.error("Gateway输入异常: {}", ex.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            resultData = ResultData.fail(ErrorCode.PARAM_ERROR, "请求参数错误");
        } else if (ex instanceof org.springframework.web.server.ResponseStatusException) {
            // 处理响应状态异常
            org.springframework.web.server.ResponseStatusException statusEx = (org.springframework.web.server.ResponseStatusException) ex;
            log.error("Gateway响应状态异常: {} - {}", statusEx.getStatusCode(), statusEx.getReason());
            httpStatus = HttpStatus.valueOf(statusEx.getStatusCode().value());
            resultData = ResultData.fail(ErrorCode.GATEWAY_ERROR, statusEx.getReason());
        } else {
            log.error("Gateway系统异常: ", ex);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            resultData = ResultData.fail(ErrorCode.INTERNAL_ERROR, "网关服务异常，请稍后重试");
        }

        // 设置正确的HTTP状态码
        response.setStatusCode(httpStatus);

        String responseBody = JSON.toJSONString(resultData);
        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }
} 