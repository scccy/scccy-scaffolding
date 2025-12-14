package com.scccy.common.base.utils;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * JWT 工具类
 * <p>
 * 提供从 Spring Security Jwt 对象提取信息的方法
 * 供 Authorization Server、Gateway 和 Resource Server 共同使用
 *
 * @author scccy
 */
@Component
public class JwtUtils {
    
    /**
     * 从 Jwt 对象提取用户ID
     *
     * @param jwt JWT 对象
     * @return 用户ID，如果不存在返回 null
     */
    public Long getUserId(Jwt jwt) {
        return jwt.getClaimAsLong("userId");
    }
    
    /**
     * 从 Jwt 对象提取用户名
     *
     * @param jwt JWT 对象
     * @return 用户名，如果不存在返回 null
     */
    public String getUsername(Jwt jwt) {
        return jwt.getClaimAsString("username");
    }
    
    /**
     * 从 Jwt 对象提取权限列表
     *
     * @param jwt JWT 对象
     * @return 权限列表，如果不存在返回空列表
     */
    public List<String> getAuthorities(Jwt jwt) {
        List<String> authorities = jwt.getClaimAsStringList("authorities");
        return authorities != null ? authorities : Collections.emptyList();
    }
    
    /**
     * 从 Jwt 对象提取昵称
     *
     * @param jwt JWT 对象
     * @return 昵称，如果不存在返回 null
     */
    public String getNickName(Jwt jwt) {
        return jwt.getClaimAsString("nickName");
    }
    
    /**
     * 从 Jwt 对象提取用户状态
     *
     * @param jwt JWT 对象
     * @return 用户状态，如果不存在返回 null
     */
    public Integer getStatus(Jwt jwt) {
        return jwt.getClaimAsInteger("status");
    }
    
    /**
     * 从 Jwt 对象提取自定义 claim
     *
     * @param jwt JWT 对象
     * @param claimName claim 名称
     * @param clazz 返回类型
     * @param <T> 泛型类型
     * @return claim 值，如果不存在返回 null
     */
    public <T> T getClaim(Jwt jwt, String claimName, Class<T> clazz) {
        return jwt.getClaim(claimName);
    }
    
    /**
     * 检查用户是否有指定权限
     *
     * @param jwt JWT 对象
     * @param authority 权限名称
     * @return 如果有权限返回 true，否则返回 false
     */
    public boolean hasAuthority(Jwt jwt, String authority) {
        List<String> authorities = getAuthorities(jwt);
        return authorities.contains(authority);
    }
}

