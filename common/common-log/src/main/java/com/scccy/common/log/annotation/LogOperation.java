package com.scccy.common.log.annotation;

import java.lang.annotation.*;

/**
 * 日志操作注解
 * 用于标记需要记录操作日志的方法
 *
 * @author scccy
 * @since 2025-10-11
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /**
     * 操作描述
     */
    String value() default "";

    /**
     * 操作类型
     */
    String type() default "OPERATION";

    /**
     * 是否记录请求参数
     */
    boolean recordRequest() default true;

    /**
     * 是否记录响应结果
     */
    boolean recordResponse() default true;

    /**
     * 是否记录执行时间
     */
    boolean recordExecutionTime() default true;
}
