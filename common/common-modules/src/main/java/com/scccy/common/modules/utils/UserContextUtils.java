package com.scccy.common.modules.utils;

import com.scccy.common.modules.dto.CurrentUserInfo;

/**
 * 用户上下文工具类
 * <p>
 * 用于在方法内部获取当前用户信息
 * <p>
 * 使用 ThreadLocal 存储用户信息，通过 AOP 切面自动注入
 * <p>
 * 使用示例：
 * <pre>
 * @CurrentUser
 * @GetMapping("/{id}")
 * public ResultData<?> getById(@PathVariable Long id) {
 *     CurrentUserInfo user = UserContextUtils.getCurrentUser();
 *     Long userId = user.getUserId();
 *     String username = user.getUsername();
 *     // ...
 * }
 * </pre>
 *
 * @author scccy
 */
public class UserContextUtils {
    
    /**
     * ThreadLocal 存储当前用户信息
     */
    private static final ThreadLocal<CurrentUserInfo> USER_CONTEXT = new ThreadLocal<>();
    
    /**
     * 设置当前用户信息
     * <p>
     * 由 AOP 切面调用，开发者不应直接调用此方法
     *
     * @param userInfo 用户信息
     */
    public static void setCurrentUser(CurrentUserInfo userInfo) {
        USER_CONTEXT.set(userInfo);
    }
    
    /**
     * 获取当前用户信息
     * <p>
     * 如果方法上标注了 @CurrentUser 注解，AOP 切面会自动注入用户信息
     * <p>
     * 如果未找到用户信息，返回 null（不会抛出异常）
     *
     * @return 当前用户信息，如果未找到则返回 null
     */
    public static CurrentUserInfo getCurrentUser() {
        return USER_CONTEXT.get();
    }
    
    /**
     * 获取当前用户ID
     *
     * @return 用户ID，如果未找到则返回 null
     */
    public static Long getCurrentUserId() {
        CurrentUserInfo user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     *
     * @return 用户名，如果未找到则返回 null
     */
    public static String getCurrentUsername() {
        CurrentUserInfo user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
    
    /**
     * 获取当前用户权限列表
     *
     * @return 权限列表，如果未找到则返回空列表
     */
    public static java.util.List<String> getCurrentAuthorities() {
        CurrentUserInfo user = getCurrentUser();
        return user != null ? user.getAuthorities() : java.util.Collections.emptyList();
    }
    
    /**
     * 检查当前用户是否有指定权限
     *
     * @param permission 权限标识
     * @return true 如果有权限，false 如果没有权限或未找到用户
     */
    public static boolean hasPermi(String permission) {
        CurrentUserInfo user = getCurrentUser();
        return user != null && user.hasPermi(permission);
    }
    
    /**
     * 检查当前用户是否有指定角色
     *
     * @param role 角色标识
     * @return true 如果有角色，false 如果没有角色或未找到用户
     */
    public static boolean hasRole(String role) {
        CurrentUserInfo user = getCurrentUser();
        return user != null && user.hasRole(role);
    }
    
    /**
     * 清除当前用户信息
     * <p>
     * 由 AOP 切面在方法执行完成后调用，防止 ThreadLocal 内存泄漏
     * <p>
     * 开发者不应直接调用此方法
     */
    public static void clear() {
        USER_CONTEXT.remove();
    }
}

