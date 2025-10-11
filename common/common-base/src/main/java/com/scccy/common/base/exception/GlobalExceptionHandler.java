package com.scccy.common.base.exception;


import com.scccy.common.modules.dto.ResultData;
import com.scccy.common.modules.entity.ErrorCode;
import com.scccy.common.modules.exception.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 排除Gateway服务，因为Gateway使用WebFlux异常处理
 *
 * 职责范围：
 * 1. 处理通用基础异常（参数校验、运行时异常等）
 * 2. 为各微服务模块提供兜底异常处理
 * 3. 各微服务模块应该有自己的业务异常处理器
 *
 * @author origin
 * @since 2025-07-31
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnMissingClass("com.origin.gateway.GatewayApplication")
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 注意：此方法仅作为兜底处理，各微服务模块应该有自己的业务异常处理器
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<Object> handleBusinessException(BusinessException e) {
        log.warn("业务异常（base模块兜底处理）: {}", e.getMessage());
        return ResultData.fail(e.getErrorCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常: {}", message);
        return ResultData.fail(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<Object> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定异常: {}", message);
        return ResultData.fail(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<Object> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束违反异常: {}", message);
        return ResultData.fail(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultData<Object> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return ResultData.fail(ErrorCode.INTERNAL_ERROR, "系统内部错误");
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultData<Object> handleException(Exception e) {
        log.error("系统异常: ", e);
        return ResultData.fail(ErrorCode.INTERNAL_ERROR, "系统异常，请联系管理员");
    }

    // ========== 新增异常处理方法 ==========

    /**
     * 处理SQL语法错误异常
     */
    @ExceptionHandler(java.sql.SQLSyntaxErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultData<Object> handleSQLSyntaxErrorException(java.sql.SQLSyntaxErrorException e) {
        log.error("SQL语法错误: ", e);
        return ResultData.fail(ErrorCode.DATABASE_QUERY_ERROR, "数据库查询错误");
    }

    /**
     * 处理数据完整性违反异常
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<Object> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e) {
        log.error("数据完整性违反: ", e);
        return ResultData.fail(ErrorCode.DATABASE_CONSTRAINT_VIOLATION, "数据完整性错误");
    }

    /**
     * 处理重复键异常
     */
    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<Object> handleDuplicateKeyException(org.springframework.dao.DuplicateKeyException e) {
        log.error("重复键异常: ", e);
        return ResultData.fail(ErrorCode.DATABASE_CONSTRAINT_VIOLATION, "数据重复错误");
    }

    /**
     * 处理HTTP请求方法不支持异常
     */
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResultData<Object> handleHttpRequestMethodNotSupportedException(org.springframework.web.HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {} {}", e.getMethod(), e.getMessage());
        return ResultData.fail(ErrorCode.METHOD_NOT_ALLOWED, "请求方法不允许");
    }

    /**
     * 处理HTTP媒体类型不支持异常
     */
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<Object> handleHttpRequestMethodNotSupportedException(org.springframework.web.HttpMediaTypeNotSupportedException e) {
        log.warn("请求媒体类型不支持: {}", e.getMessage());
        return ResultData.fail(ErrorCode.PARAM_ERROR, "请求格式不支持");
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<Object> handleMissingServletRequestParameterException(org.springframework.web.bind.MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return ResultData.fail(ErrorCode.PARAM_ERROR, "缺少必要参数: " + e.getParameterName());
    }

    /**
     * 处理请求参数绑定异常
     */
    @ExceptionHandler(org.springframework.web.bind.ServletRequestBindingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<Object> handleServletRequestBindingException(org.springframework.web.bind.ServletRequestBindingException e) {
        log.warn("请求参数绑定异常: {}", e.getMessage());
        return ResultData.fail(ErrorCode.PARAM_ERROR, "参数绑定错误");
    }

    /**
     * 处理处理器未找到异常
     */
    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultData<Object> handleNoHandlerFoundException(org.springframework.web.servlet.NoHandlerFoundException e) {
        log.warn("处理器未找到: {} {}", e.getHttpMethod(), e.getRequestURL());
        return ResultData.fail(ErrorCode.NOT_FOUND, "接口不存在");
    }

    /**
     * 处理异步请求超时异常
     */
    @ExceptionHandler(org.springframework.web.context.request.async.AsyncRequestTimeoutException.class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public ResultData<Object> handleAsyncRequestTimeoutException(org.springframework.web.context.request.async.AsyncRequestTimeoutException e) {
        log.warn("异步请求超时: {}", e.getMessage());
        return ResultData.fail(ErrorCode.REQUEST_TIMEOUT, "请求超时");
    }

    /**
     * 处理HTTP消息不可读异常
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<Object> handleHttpMessageNotReadableException(org.springframework.http.converter.HttpMessageNotReadableException e) {
        log.warn("HTTP消息不可读: {}", e.getMessage());
        return ResultData.fail(ErrorCode.PARAM_ERROR, "请求消息格式错误");
    }

    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<Object> handleMethodArgumentTypeMismatchException(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e) {
        log.warn("方法参数类型不匹配: {} = {}", e.getName(), e.getValue());
        return ResultData.fail(ErrorCode.PARAM_ERROR, "参数类型错误: " + e.getName());
    }
}