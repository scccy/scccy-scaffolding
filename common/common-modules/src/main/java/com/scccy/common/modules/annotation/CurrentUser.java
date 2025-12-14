package com.scccy.common.modules.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当前用户注解
 * <p>
 * 支持两种使用方式：
 * <p>
 * 方式一：方法注解（推荐，使用 AOP 环绕）
 * <pre>
 * &#064;CurrentUser
 * &#064;GetMapping("/
 * public ResultData<?> getById(@PathVariable Long id) {
 *     CurrentUserInfo user = UserContextUtils.getCurrentUser();
 *     Long userId = user.getUserId();
 *     // ...
 * }
 * </pre>
 * <p>
 * 方式二：参数注解（使用 HandlerMethodArgumentResolver）
 * <pre>
 * &#064;GetMapping("/
 * public ResultData<?> getById(@PathVariable Long id, @CurrentUser CurrentUserInfo user) {
 *     Long userId = user.getUserId();
 *     // ...
 * }
 * </pre>
 *
 * @author scccy
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
    /**
     * 是否必需（如果为 true，且请求头中没有用户信息，会抛出异常）
     *
     * @return true 必需，false 可选
     */
    boolean required() default true;
}

