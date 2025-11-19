package com.scccy.common.modules.annotation;

import java.lang.annotation.*;

/**
 * 标记内部调用专用的接口
 * <p>
 * 被标记的 Controller 或方法会在资源服务器层面要求指定 scope（默认 internal-service）。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalOnly {

    /**
     * 访问此端点所需的 OAuth2 scope（不带 SCOPE_ 前缀）
     */
    String scope() default "internal-service";
}

