package com.scccy.common.base.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 权限服务类
 * <p>
 * 用于 @PreAuthorize("@ss.hasPermi('system:activity:add')") 注解
 * Bean 名称必须是 "ss"，这样 @PreAuthorize("@ss.hasPermi(...)") 才能工作
 *
 * @author scccy
 */
@Component("ss")
public class PermissionService {
    
    private static final String HEADER_AUTHORITIES = "X-Authorities";
    
    /**
     * 检查当前用户是否有指定权限
     * <p>
     * 从请求头 X-Authorities 中获取权限列表，检查是否包含指定权限
     * 权限格式：ROLE_ADMIN,ROLE_USER,system:user:list,system:user:add
     *
     * @param permission 权限标识，如：system:activity:add
     * @return true 如果有权限，false 如果没有权限
     */
    public boolean hasPermi(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 从请求头获取权限列表
            List<String> authorities = getAuthorities();
            
            // 检查是否包含指定权限
            return authorities.contains(permission);
        } catch (Exception e) {
            // 如果无法获取权限，返回 false（安全优先）
            return false;
        }
    }
    
    /**
     * 检查当前用户是否有任意一个权限
     *
     * @param permissions 权限标识数组
     * @return true 如果有任意一个权限，false 如果都没有权限
     */
    public boolean hasAnyPermi(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        
        try {
            List<String> authorities = getAuthorities();
            return Arrays.stream(permissions)
                    .anyMatch(authorities::contains);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查当前用户是否有指定角色
     *
     * @param role 角色标识，如：ROLE_ADMIN
     * @return true 如果有角色，false 如果没有角色
     */
    public boolean hasRole(String role) {
        return hasPermi(role);  // 角色也是权限的一种
    }
    
    /**
     * 从请求头获取权限列表
     *
     * @return 权限列表
     */
    private List<String> getAuthorities() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return Collections.emptyList();
            }
            
            HttpServletRequest request = attributes.getRequest();
            String authoritiesStr = request.getHeader(HEADER_AUTHORITIES);
            
            if (authoritiesStr != null && !authoritiesStr.trim().isEmpty()) {
                return Arrays.asList(authoritiesStr.split(","));
            }
        } catch (Exception e) {
            // 忽略异常，返回空列表
        }
        return Collections.emptyList();
    }
}

