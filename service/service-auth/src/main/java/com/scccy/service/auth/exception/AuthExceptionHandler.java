//package com.scccy.service.auth.exception;
//
//import com.scccy.common.modules.dto.ResultData;
//import com.scccy.common.modules.entity.ErrorCode;
//import com.scccy.common.modules.exception.BusinessException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
///**
// * Auth 服务自定义异常处理器
// * 优先级高于公共处理器，用于覆盖与本服务相关的错误响应
// */
//@Slf4j
//@RestControllerAdvice
//@Order(Ordered.HIGHEST_PRECEDENCE)
//public class AuthExceptionHandler {
//
//    /**
//     * 处理业务异常
//     */
//    @ExceptionHandler(BusinessException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResultData<Object> handleBusinessException(BusinessException e) {
//        log.warn("[AUTH] 业务异常: {}", e.getMessage());
//        return ResultData.fail(e.getErrorCode(), e.getMessage());
//    }
//
//    /**
//     * 处理认证相关异常（示例，可按需扩展）
//     */
//    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResultData<Object> handleAuthArgs(Exception e) {
//        log.warn("[AUTH] 非法请求参数: {}", e.getMessage());
//        return ResultData.fail(ErrorCode.PARAM_ERROR, e.getMessage());
//    }
//}
//
//
