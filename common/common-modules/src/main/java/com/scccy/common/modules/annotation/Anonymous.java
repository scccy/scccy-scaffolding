package com.scccy.common.modules.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 匿名访问注解
 * <p>
 * 用于标记 Controller 方法，表示该接口可以匿名访问（不需要 Token）
 * <p>
 * 使用方式：
 * <pre>
 * &#064;Anonymous
 * &#064;GetMapping("/public")
 * public ResultData<?> publicEndpoint() {
 *     // 公开接口，不需要 Token
 * }
 * </pre>
 * <p>
 * 注意：
 * <ul>
 *     <li>此注解主要用于文档和标记，实际放行需要在 Gateway 中配置路径模式</li>
 *     <li>Gateway 会根据路径模式自动放行，例如：包含 /public 或 /anonymous 的路径</li>
 *     <li>建议使用统一的路径约定，如：/**\/anonymous 等</li>
 * </ul>
 *
 * @author scccy
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Anonymous {
    /**
     * 是否启用（默认启用）
     *
     * @return true 启用，false 禁用
     */
    boolean value() default true;
}

