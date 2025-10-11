package com.scccy.service.gateway.exception;


import com.scccy.common.modules.entity.ErrorCode;
import com.scccy.common.modules.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Controller层异常环绕切面
 * 统一拦截Controller层异常，做日志记录和异常转换
 * 适配WebFlux响应式编程
 * 优先级低于ErrorWebExceptionHandler，作为兜底处理
 * 
 * @author origin
 * @since 2025-08-13
 */
@Slf4j
@Aspect
@Component
@Order(1) // 优先级低于ErrorWebExceptionHandler
public class ControllerExceptionAspect {

    /**
     * 环绕Controller层方法，统一异常处理
     * 拦截所有Controller包下的方法
     */
    @Around("execution(* com.origin.gateway..*.controller..*(..))")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (BusinessException be) {
            // 业务异常直接抛出，由ErrorWebExceptionHandler处理
            log.warn("Controller业务异常: {}.{} - {}", 
                joinPoint.getSignature().getDeclaringTypeName(), 
                joinPoint.getSignature().getName(), 
                be.getMessage());
            throw be;
        } catch (Exception e) {
            // 其他异常记录日志并转换为业务异常
            log.error("Controller异常: {}.{} - {}", 
                joinPoint.getSignature().getDeclaringTypeName(), 
                joinPoint.getSignature().getName(), 
                e.getMessage(), e);
            
            // 转换为业务异常，由ErrorWebExceptionHandler处理
            throw new BusinessException(ErrorCode.GATEWAY_ERROR, "网关服务异常，请稍后重试", e);
        }
    }

    /**
     * 环绕返回Mono的方法，统一异常处理
     * 拦截所有返回Mono的Controller方法
     */
    @Around("execution(reactor.core.publisher.Mono com.origin.gateway..*.controller..*(..))")
    public Object aroundMonoController(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            if (result instanceof Mono) {
                return ((Mono<?>) result).onErrorMap(throwable -> {
                    if (throwable instanceof BusinessException) {
                        log.warn("Controller业务异常: {}.{} - {}", 
                            joinPoint.getSignature().getDeclaringTypeName(), 
                            joinPoint.getSignature().getName(), 
                            throwable.getMessage());
                        return throwable;
                    } else {
                        log.error("Controller异常: {}.{} - {}", 
                            joinPoint.getSignature().getDeclaringTypeName(), 
                            joinPoint.getSignature().getName(), 
                            throwable.getMessage(), throwable);
                        return new BusinessException(ErrorCode.GATEWAY_ERROR, "网关服务异常，请稍后重试", throwable);
                    }
                });
            }
            return result;
        } catch (Exception e) {
            log.error("Controller执行异常: {}.{} - {}", 
                joinPoint.getSignature().getDeclaringTypeName(), 
                joinPoint.getSignature().getName(), 
                e.getMessage(), e);
            throw new BusinessException(ErrorCode.GATEWAY_ERROR, "网关服务异常，请稍后重试", e);
        }
    }
}
