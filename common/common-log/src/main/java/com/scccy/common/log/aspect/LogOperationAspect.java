package com.scccy.common.log.aspect;


import com.scccy.common.log.annotation.LogOperation;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;

/**
 * 日志操作切面
 * 拦截带有 @LogOperation 注解的方法，记录操作日志
 *
 * @author scccy
 * @since 2025-10-11
 */
@Slf4j
@Aspect
@Component
public class LogOperationAspect {

    /**
     * 环绕通知，记录方法执行日志
     */
    @Around("@annotation(com.scccy.common.log.annotation.LogOperation)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogOperation logOperation = method.getAnnotation(LogOperation.class);

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String operation = logOperation.value().isEmpty() ? methodName : logOperation.value();
        String type = logOperation.type();

        // 记录请求参数
        if (logOperation.recordRequest()) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                log.info("[{}] 开始执行 - 类: {}, 方法: {}, 参数: {}", 
                        type, className, methodName, args);
            } else {
                log.info("[{}] 开始执行 - 类: {}, 方法: {}", 
                        type, className, methodName);
            }
        }

        StopWatch stopWatch = null;
        if (logOperation.recordExecutionTime()) {
            stopWatch = new StopWatch();
            stopWatch.start();
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            log.error("[{}] 执行异常 - 类: {}, 方法: {}, 异常: {}", 
                    type, className, methodName, e.getMessage(), e);
            throw e;
        } finally {
            if (logOperation.recordExecutionTime() && stopWatch != null) {
                stopWatch.stop();
                long executionTime = stopWatch.getTotalTimeMillis();
                
                if (logOperation.recordResponse()) {
                    log.info("[{}] 执行完成 - 类: {}, 方法: {}, 耗时: {}ms, 结果: {}", 
                            type, className, methodName, executionTime, result);
                } else {
                    log.info("[{}] 执行完成 - 类: {}, 方法: {}, 耗时: {}ms", 
                            type, className, methodName, executionTime);
                }
            } else if (logOperation.recordResponse()) {
                log.info("[{}] 执行完成 - 类: {}, 方法: {}, 结果: {}", 
                        type, className, methodName, result);
            } else {
                log.info("[{}] 执行完成 - 类: {}, 方法: {}", 
                        type, className, methodName);
            }
        }
    }
}
