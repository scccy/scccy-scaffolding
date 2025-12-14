package com.scccy.common.modules.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.List;

/**
 * JWT 工具类
 * <p>
 * 提供从 Spring Security Jwt 对象提取信息的方法
 * 供 Authorization Server、Gateway 和 Resource Server 共同使用
 * <p>
 * 注意：
 * - 此工具类不负责生成或解析 JWT Token 字符串，而是从已经解析的 Jwt 对象中提取信息
 * - 使用静态方法，避免 MVC 和 WebFlux 环境下的 Bean 冲突
 * - 可以在 MVC（service-auth）和 WebFlux（gateway）中直接使用
 *
 * @author scccy
 */
@Slf4j
public class JwtUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private JwtUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 从 Jwt 对象提取用户ID
     *
     * @param jwt Jwt 对象
     * @return 用户ID，如果不存在返回 null
     */
    public static Long getUserId(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        try {
            Object userIdObj = jwt.getClaim("userId");
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            } else if (userIdObj instanceof String) {
                return Long.parseLong((String) userIdObj);
            }
            // 如果 userId 不存在，尝试从 subject 获取
            String subject = jwt.getSubject();
            if (subject != null) {
                try {
                    return Long.parseLong(subject);
                } catch (NumberFormatException e) {
                    log.warn("无法从 Jwt 的 subject 中解析用户ID: {}", subject);
                }
            }
        } catch (Exception e) {
            log.warn("从 Jwt 提取用户ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从 Jwt 对象提取用户名
     *
     * @param jwt Jwt 对象
     * @return 用户名，如果不存在返回 null
     */
    public static String getUsername(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        try {
            String username = jwt.getClaimAsString("username");
            if (username != null) {
                return username;
            }
            // 如果 username 不存在，返回 subject
            return jwt.getSubject();
        } catch (Exception e) {
            log.warn("从 Jwt 提取用户名失败: {}", e.getMessage());
            return jwt.getSubject();
        }
    }

    /**
     * 从 Jwt 对象提取权限列表
     *
     * @param jwt Jwt 对象
     * @return 权限列表，如果不存在返回空列表
     */
    public static List<String> getAuthorities(Jwt jwt) {
        if (jwt == null) {
            return Collections.emptyList();
        }
        try {
            List<String> authorities = jwt.getClaimAsStringList("authorities");
            if (authorities != null) {
                return authorities;
            }
            // 尝试从 Spring Security 的标准 claims 中获取
            Object authoritiesObj = jwt.getClaim("authorities");
            if (authoritiesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> authList = (List<String>) authoritiesObj;
                return authList;
            }
        } catch (Exception e) {
            log.warn("从 Jwt 提取权限列表失败: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * 从 Jwt 对象提取昵称
     *
     * @param jwt Jwt 对象
     * @return 昵称，如果不存在返回 null
     */
    public static String getNickName(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        try {
            return jwt.getClaimAsString("nickName");
        } catch (Exception e) {
            log.warn("从 Jwt 提取昵称失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Jwt 对象提取用户状态
     *
     * @param jwt Jwt 对象
     * @return 用户状态，如果不存在返回 null
     */
    public static Integer getStatus(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        try {
            Object statusObj = jwt.getClaim("status");
            if (statusObj instanceof Number) {
                return ((Number) statusObj).intValue();
            } else if (statusObj instanceof String) {
                return Integer.parseInt((String) statusObj);
            }
        } catch (Exception e) {
            log.warn("从 Jwt 提取用户状态失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从 Jwt 对象提取自定义 claim
     *
     * @param jwt       Jwt 对象
     * @param claimName claim 名称
     * @param clazz     返回类型
     * @param <T>       返回类型泛型
     * @return claim 值，如果不存在返回 null
     */
    public static <T> T getClaim(Jwt jwt, String claimName, Class<T> clazz) {
        if (jwt == null) {
            return null;
        }
        try {
            return jwt.getClaim(claimName);
        } catch (Exception e) {
            log.warn("从 Jwt 提取 claim {} 失败: {}", claimName, e.getMessage());
            return null;
        }
    }

    /**
     * 从 Jwt 对象提取字符串类型的 claim
     *
     * @param jwt       Jwt 对象
     * @param claimName claim 名称
     * @return claim 值，如果不存在返回 null
     */
    public static String getClaimAsString(Jwt jwt, String claimName) {
        if (jwt == null) {
            return null;
        }
        try {
            return jwt.getClaimAsString(claimName);
        } catch (Exception e) {
            log.warn("从 Jwt 提取 claim {} 失败: {}", claimName, e.getMessage());
            return null;
        }
    }

    /**
     * 从 Jwt 对象提取数字类型的 claim
     *
     * @param jwt       Jwt 对象
     * @param claimName claim 名称
     * @return claim 值，如果不存在返回 null
     */
    public static Long getClaimAsLong(Jwt jwt, String claimName) {
        if (jwt == null) {
            return null;
        }
        try {
            Object claimObj = jwt.getClaim(claimName);
            if (claimObj instanceof Number) {
                return ((Number) claimObj).longValue();
            } else if (claimObj instanceof String) {
                return Long.parseLong((String) claimObj);
            }
        } catch (Exception e) {
            log.warn("从 Jwt 提取 claim {} 失败: {}", claimName, e.getMessage());
        }
        return null;
    }
}

