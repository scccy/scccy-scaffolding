package com.scccy.common.log.aspect;


import com.scccy.common.log.annotation.Log;
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
    @Around("@annotation(com.scccy.common.log.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log log = method.getAnnotation(Log.class);

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String operation = log.value().isEmpty() ? methodName : log.value();
        String type = log.type();

        // 记录请求参数
        if (log.recordRequest()) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                LogOperationAspect.log.info("[{}] 开始执行 - 类: {}, 方法: {}, 参数: {}",
                        type, className, methodName, args);
            } else {
                LogOperationAspect.log.info("[{}] 开始执行 - 类: {}, 方法: {}",
                        type, className, methodName);
            }
        }

        StopWatch stopWatch = null;
        if (log.recordExecutionTime()) {
            stopWatch = new StopWatch();
            stopWatch.start();
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            LogOperationAspect.log.error("[{}] 执行异常 - 类: {}, 方法: {}, 异常: {}",
                    type, className, methodName, e.getMessage(), e);
            throw e;
        } finally {
            if (log.recordExecutionTime() && stopWatch != null) {
                stopWatch.stop();
                long executionTime = stopWatch.getTotalTimeMillis();
                
                if (log.recordResponse()) {
                    LogOperationAspect.log.info("[{}] 执行完成 - 类: {}, 方法: {}, 耗时: {}ms, 结果: {}",
                            type, className, methodName, executionTime, result);
                } else {
                    LogOperationAspect.log.info("[{}] 执行完成 - 类: {}, 方法: {}, 耗时: {}ms",
                            type, className, methodName, executionTime);
                }
            } else if (log.recordResponse()) {
                LogOperationAspect.log.info("[{}] 执行完成 - 类: {}, 方法: {}, 结果: {}",
                        type, className, methodName, result);
            } else {
                LogOperationAspect.log.info("[{}] 执行完成 - 类: {}, 方法: {}",
                        type, className, methodName);
            }
        }
    }
}
