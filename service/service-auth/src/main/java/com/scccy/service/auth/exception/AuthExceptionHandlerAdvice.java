package com.scccy.service.auth.exception;

import com.scccy.common.modules.dto.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 认证服务异常处理器
 * <p>
 * 专门处理认证和授权相关的异常，优先级高于全局异常处理器
 * <p>
 * 优先级说明：
 * - 此处理器使用 @Order(100)，优先级高于全局异常处理器（@Order(LOWEST_PRECEDENCE)）
 * - 只有当此处理器无法匹配异常类型时，才会由全局异常处理器处理
 * <p>
 * 注意：Spring 中 @RestControllerAdvice 类不能通过继承共享异常处理方法，
 * 因此各异常处理器需要独立定义，通过 @Order 控制处理优先级。
 *
 * @author scccy
 * @since 2025-11-05
 */
@Slf4j
@Order(100)
@RestControllerAdvice
public class AuthExceptionHandlerAdvice {

    @ExceptionHandler(value = {InternalAuthenticationServiceException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResultData<?> internalAuthenticationServiceException(InternalAuthenticationServiceException ex) {
        log.error("Authentication Exception:{}", ex.getMessage());
        return ResultData.fail(AuthErrorType.UNAUTHORIZED_CLIENT);
    }

    @ExceptionHandler(value = {OAuth2AuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResultData<?> internalAuthenticationServiceException(OAuth2AuthenticationException ex) {
        log.error("On Authentication Failure :{}", ex.getMessage());
        return ResultData.fail(AuthErrorType.INVALID_CLIENT);
    }
}