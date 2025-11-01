package com.scccy.service.auth.utils;

import com.scccy.service.auth.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * <p>
 * 提供JWT Token的生成、解析、验证等功能
 *
 * @author scccy
 */
@Slf4j
@Component
public class JwtUtils {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    public JwtUtils(JwtConfig jwtConfig, SecretKey secretKey) {
        this.jwtConfig = jwtConfig;
        this.secretKey = secretKey;
    }

    /**
     * 生成JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param claims   自定义Claims（可选）
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        // 构建Claims
        Map<String, Object> tokenClaims = new HashMap<>();
        if (claims != null) {
            tokenClaims.putAll(claims);
        }

        // 添加标准Claims
        tokenClaims.put("userId", userId);
        tokenClaims.put("username", username);

        return Jwts.builder()
                .claims(tokenClaims)
                .subject(String.valueOf(userId))  // 标准claim: sub
                .issuedAt(now)                    // 标准claim: iat
                .expiration(expiryDate)          // 标准claim: exp
                .signWith(secretKey)             // 签名算法: HS256
                .compact();
    }

    /**
     * 生成JWT Token（简化版，只包含用户ID和用户名）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, null);
    }

    /**
     * 解析JWT Token，获取Claims
     *
     * @param token JWT Token
     * @return Claims对象
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析JWT Token失败: {}", e.getMessage(), e);
            throw new IllegalArgumentException("无效的JWT Token: " + e.getMessage());
        }
    }

    /**
     * 验证JWT Token是否有效
     * <p>
     * 注意：此方法仅验证Token的签名和过期时间，不检查黑名单
     * 如需检查黑名单，请在使用此方法后调用TokenBlacklistService.isBlacklisted()
     *
     * @param token JWT Token
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            log.warn("JWT Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断Token是否过期
     *
     * @param claims Claims对象
     * @return true-已过期，false-未过期
     */
    public boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    /**
     * 判断Token是否过期
     *
     * @param token JWT Token
     * @return true-已过期，false-未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return isTokenExpired(claims);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从Token中获取Claims
     *
     * @param token JWT Token
     * @return Claims对象
     */
    public Claims getClaims(String token) {
        return parseToken(token);
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        } else if (userIdObj instanceof String) {
            return Long.parseLong((String) userIdObj);
        }
        // 如果userId不存在，尝试从subject获取
        String subject = claims.getSubject();
        if (subject != null) {
            try {
                return Long.parseLong(subject);
            } catch (NumberFormatException e) {
                log.warn("无法从Token的subject中解析用户ID: {}", subject);
            }
        }
        throw new IllegalArgumentException("Token中未找到有效的用户ID");
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        String username = (String) claims.get("username");
        if (username == null) {
            throw new IllegalArgumentException("Token中未找到用户名");
        }
        return username;
    }

    /**
     * 从Token中获取昵称
     *
     * @param token JWT Token
     * @return 昵称（如果不存在返回null）
     */
    public String getNickName(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("nickName");
    }

    /**
     * 从Token中获取用户状态
     *
     * @param token JWT Token
     * @return 用户状态（如果不存在返回null）
     */
    public Integer getStatus(String token) {
        Claims claims = parseToken(token);
        Object statusObj = claims.get("status");
        if (statusObj instanceof Number) {
            return ((Number) statusObj).intValue();
        } else if (statusObj instanceof String) {
            return Integer.parseInt((String) statusObj);
        }
        return null;
    }

    /**
     * 从请求头中提取Token
     *
     * @param authHeader 认证请求头（格式: "Bearer {token}"）
     * @return JWT Token（不含前缀）
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(jwtConfig.getTokenPrefix())) {
            return authHeader.substring(jwtConfig.getTokenPrefix().length());
        }
        return authHeader;  // 如果没有前缀，直接返回（兼容性处理）
    }

    /**
     * 刷新Token（生成新的Token，使用相同的Claims但更新过期时间）
     *
     * @param token 旧Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            Long userId = getUserId(token);
            String username = getUsername(token);

            // 保留所有自定义Claims
            Map<String, Object> customClaims = new HashMap<>();
            claims.forEach((key, value) -> {
                // 排除标准Claims
                if (!key.equals("sub") && !key.equals("iat") && !key.equals("exp")) {
                    customClaims.put(key, value);
                }
            });

            // 生成新Token
            return generateToken(userId, username, customClaims);
        } catch (Exception e) {
            log.error("刷新Token失败: {}", e.getMessage(), e);
            throw new IllegalArgumentException("无法刷新Token: " + e.getMessage());
        }
    }

    /**
     * 获取Token的过期时间
     *
     * @param token JWT Token
     * @return 过期时间（Date）
     */
    public Date getExpirationDate(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 获取Token的签发时间
     *
     * @param token JWT Token
     * @return 签发时间（Date）
     */
    public Date getIssuedAtDate(String token) {
        Claims claims = parseToken(token);
        return claims.getIssuedAt();
    }
}

