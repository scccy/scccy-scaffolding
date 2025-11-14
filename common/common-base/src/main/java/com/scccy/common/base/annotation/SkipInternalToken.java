package com.scccy.common.base.annotation;

import java.lang.annotation.*;

/**
 * 标记在 FeignClient 接口上，用于跳过内部令牌拦截器
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkipInternalToken {

    /**
     * 可选说明，记录为何要跳过
     */
    String reason() default "";
}

