package com.scccy.common.modules.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 当前用户信息
 * <p>
 * 用于 @CurrentUser 注解，从请求头中提取用户信息
 * <p>
 * 使用示例：
 * <pre>
 * @GetMapping("/{id}")
 * public ResultData<?> getById(@PathVariable Long id, @CurrentUser CurrentUserInfo user) {
 *     Long userId = user.getUserId();
 *     String username = user.getUsername();
 *     List<String> authorities = user.getAuthorities();
 *     // ...
 * }
 * </pre>
 *
 * @author scccy
 */
@Data
public class CurrentUserInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 权限列表
     * <p>
     * 格式：ROLE_ADMIN,ROLE_USER,system:user:list,system:user:add
     */
    private List<String> authorities;
    
    /**
     * 检查是否有指定权限
     *
     * @param permission 权限标识，如：system:user:list
     * @return true 如果有权限，false 如果没有权限
     */
    public boolean hasPermi(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return false;
        }
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        return authorities.contains(permission);
    }
    
    /**
     * 检查是否有任意一个权限
     *
     * @param permissions 权限标识数组
     * @return true 如果有任意一个权限，false 如果都没有权限
     */
    public boolean hasAnyPermi(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        for (String permission : permissions) {
            if (authorities.contains(permission)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否有指定角色
     *
     * @param role 角色标识，如：ROLE_ADMIN
     * @return true 如果有角色，false 如果没有角色
     */
    public boolean hasRole(String role) {
        return hasPermi(role);
    }
    
    /**
     * 检查是否有任意一个角色
     *
     * @param roles 角色标识数组
     * @return true 如果有任意一个角色，false 如果都没有角色
     */
    public boolean hasAnyRole(String... roles) {
        return hasAnyPermi(roles);
    }
    
    /**
     * 获取权限列表（如果为空则返回空列表）
     *
     * @return 权限列表
     */
    public List<String> getAuthorities() {
        return authorities != null ? authorities : Collections.emptyList();
    }
}

