package com.scccy.service.system.utils;

import com.scccy.service.system.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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

    public String generateToken(Long userId, String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        Map<String, Object> tokenClaims = new HashMap<>();
        if (claims != null) {
            tokenClaims.putAll(claims);
        }

        tokenClaims.put("userId", userId);
        tokenClaims.put("username", username);

        return Jwts.builder()
                .claims(tokenClaims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, null);
    }

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

    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            log.warn("JWT Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return isTokenExpired(claims);
        } catch (Exception e) {
            return true;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        } else if (userIdObj instanceof String) {
            return Long.parseLong((String) userIdObj);
        }
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

    public String getUsername(String token) {
        Claims claims = parseToken(token);
        String username = (String) claims.get("username");
        if (username == null) {
            throw new IllegalArgumentException("Token中未找到用户名");
        }
        return username;
    }

    public String getNickName(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("nickName");
    }

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

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(jwtConfig.getTokenPrefix())) {
            return authHeader.substring(jwtConfig.getTokenPrefix().length());
        }
        return authHeader;
    }

    public Date getExpirationDate(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }
}

