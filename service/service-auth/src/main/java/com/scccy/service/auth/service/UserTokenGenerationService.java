package com.scccy.service.auth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.auth.dto.LoginResponse;
import com.scccy.service.auth.fegin.SystemUserClient;
import com.scccy.service.auth.oauth2.JWKCacheManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 用户 Token 生成服务
 * <p>
 * 用于普通用户登录和注册时生成 JWT Token
 * <p>
 * 职责：
 * - 普通用户登录后生成 Token
 * - 普通用户注册后自动生成 Token
 * <p>
 * 注意：此服务仅用于普通用户场景
 * 第三方 client_id 的 Token 生成由 Spring Authorization Server 自动处理
 *
 * @author scccy
 */
@Slf4j
@Service
public class UserTokenGenerationService {

    @Autowired
    private SystemUserClient systemUserClient;

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Autowired
    private AuthorizationServerSettings authorizationServerSettings;

    @Autowired(required = false)
    private JwtEncoder jwtEncoder;

    @Resource
    private JWKCacheManager jwkCacheManager;

    /**
     * 默认客户端 ID，用于普通用户 Token 生成
     * <p>
     * 注意：Auth 服务本身就是 Authorization Server，不需要外部配置客户端 ID
     * 这里使用固定的默认值，或者从 RegisteredClientRepository 获取第一个可用客户端
     */
    private static final String DEFAULT_USER_CLIENT_ID = "default_user_client";

    /**
     * 生成用户 JWT Token
     * <p>
     * 用于普通用户登录和注册后自动登录
     * <p>
     * 流程：
     * 1. 获取用户信息
     * 2. 获取用户权限
     * 3. 生成 JWT Token
     * 4. 返回 Token 和用户信息
     *
     * @param username 用户名
     * @return Token 和用户信息
     */
    public LoginResponse generateUserToken(String username) {
        log.info("生成用户 JWT Token: username={}", username);

        try {
            // 1. 获取用户信息
            ResultData<SysUserMp> userResult = systemUserClient.getByUserName(username);
            if (userResult == null || userResult.getData() == null) {
                log.warn("用户不存在: username={}", username);
                throw new RuntimeException("用户不存在");
            }

            SysUserMp user = userResult.getData();

            // 2. 获取用户权限
            ResultData<List<String>> authoritiesResult = systemUserClient.getUserAuthorities(username);
            List<String> authorities = authoritiesResult != null && authoritiesResult.getData() != null
                    ? authoritiesResult.getData()
                    : Collections.emptyList();

            // 3. 生成 JWT Token
            String token = generateJwtToken(user, authorities);

            // 4. 计算过期时间（从客户端配置中获取）
            Instant expiresAt = Instant.now().plus(2, ChronoUnit.HOURS);
            RegisteredClient userClient = findUserClient();
            if (userClient != null) {
                TokenSettings tokenSettings = userClient.getTokenSettings();
                expiresAt = Instant.now().plus(tokenSettings.getAccessTokenTimeToLive());
            }

            // 5. 构建登录响应
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setUserId(user.getUserId());
            loginResponse.setUsername(user.getUserName());
            loginResponse.setNickName(user.getNickName());
            loginResponse.setExpireTime(expiresAt.toEpochMilli());

            log.info("用户 JWT Token 生成成功: username={}, userId={}, expiresAt={}", 
                username, user.getUserId(), expiresAt);
            return loginResponse;
        } catch (Exception e) {
            log.error("生成用户 JWT Token 失败: username={}, error={}", username, e.getMessage(), e);
            throw new RuntimeException("生成 Token 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成 JWT Token
     * <p>
     * 优先使用 JwtEncoder 生成 Token，如果不可用则使用 JWKCacheManager 的密钥对直接生成
     *
     * @param user        用户信息
     * @param authorities 权限列表
     * @return JWT Token 字符串
     */
    private String generateJwtToken(SysUserMp user, List<String> authorities) {
        if (jwtEncoder != null) {
            // 使用 JwtEncoder 生成 Token
            return generateTokenWithJwtEncoder(user, authorities);
        } else {
            // 使用 JWKCacheManager 的密钥对直接生成 Token
            return generateTokenWithJWK(user, authorities);
        }
    }

    /**
     * 使用 JwtEncoder 生成 Token
     *
     * @param user        用户信息
     * @param authorities 权限列表
     * @return JWT Token 字符串
     */
    private String generateTokenWithJwtEncoder(SysUserMp user, List<String> authorities) {
        try {
            // 获取用户客户端配置（用于普通用户的默认客户端）
            RegisteredClient registeredClient = findUserClient();
            TokenSettings tokenSettings;
            String clientId;
            
            if (registeredClient != null) {
                tokenSettings = registeredClient.getTokenSettings();
                clientId = registeredClient.getClientId();
                log.debug("使用用户客户端配置生成 Token: clientId={}", clientId);
            } else {
                log.debug("未找到用户客户端配置，使用默认配置");
                // 使用默认 Token 设置（2 小时）
                tokenSettings = TokenSettings.builder()
                        .accessTokenTimeToLive(java.time.Duration.ofHours(2))
                        .build();
                // 使用 issuer 作为 audience，因为这是 Authorization Server 自己生成的 Token
                clientId = authorizationServerSettings.getIssuer();
            }

            // 获取 Token 设置
            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plus(tokenSettings.getAccessTokenTimeToLive());

            // 构建 JWT Claims
            org.springframework.security.oauth2.jwt.JwtClaimsSet.Builder claimsBuilder = 
                    org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                    .issuer(authorizationServerSettings.getIssuer())
                    .subject(user.getUserName())
                    .audience(Collections.singletonList(clientId))
                    .issuedAt(issuedAt)
                    .expiresAt(expiresAt)
                    .id(UUID.randomUUID().toString())
                    .claim("userId", user.getUserId())
                    .claim("username", user.getUserName())
                    .claim("authorities", authorities);

            if (user.getNickName() != null) {
                claimsBuilder.claim("nickName", user.getNickName());
            }
            if (user.getStatus() != null) {
                claimsBuilder.claim("status", user.getStatus());
            }

            org.springframework.security.oauth2.jwt.JwtClaimsSet claims = claimsBuilder.build();

            // 生成 JWT Token
            org.springframework.security.oauth2.jwt.JwtEncoderParameters encoderParameters = 
                    org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(claims);
            org.springframework.security.oauth2.jwt.Jwt jwt = jwtEncoder.encode(encoderParameters);

            log.debug("使用 JwtEncoder 生成用户 Token 成功: username={}, jti={}", user.getUserName(), jwt.getId());
            return jwt.getTokenValue();
        } catch (Exception e) {
            log.error("使用 JwtEncoder 生成用户 Token 失败: username={}, error={}", user.getUserName(), e.getMessage(), e);
            throw new RuntimeException("生成 Token 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用 JWKCacheManager 的密钥对直接生成 Token
     * <p>
     * 当 JwtEncoder 不可用时，使用 JWKCacheManager 的密钥对直接生成 JWT Token
     *
     * @param user        用户信息
     * @param authorities 权限列表
     * @return JWT Token 字符串
     */
    private String generateTokenWithJWK(SysUserMp user, List<String> authorities) {
        try {
            // 获取 JWKSet
            JWKSet jwkSet = jwkCacheManager.getJWKSet();
            RSAKey rsaKey = (RSAKey) jwkSet.getKeys().get(0);
            
            // 获取私钥
            RSAPrivateKey privateKey = rsaKey.toRSAPrivateKey();
            
            // 获取用户客户端配置（用于普通用户的默认客户端）
            RegisteredClient registeredClient = findUserClient();
            TokenSettings tokenSettings;
            String clientId;
            
            if (registeredClient != null) {
                tokenSettings = registeredClient.getTokenSettings();
                clientId = registeredClient.getClientId();
                log.debug("使用用户客户端配置生成 Token: clientId={}", clientId);
            } else {
                log.debug("未找到用户客户端配置，使用默认配置");
                // 使用默认 Token 设置（2 小时）
                tokenSettings = TokenSettings.builder()
                        .accessTokenTimeToLive(java.time.Duration.ofHours(2))
                        .build();
                // 使用 issuer 作为 audience，因为这是 Authorization Server 自己生成的 Token
                clientId = authorizationServerSettings.getIssuer();
            }

            // 计算过期时间
            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plus(tokenSettings.getAccessTokenTimeToLive());
            
            // 构建 JWT Claims
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .issuer(authorizationServerSettings.getIssuer())
                    .subject(user.getUserName())
                    .audience(Collections.singletonList(clientId))
                    .issueTime(Date.from(issuedAt))
                    .expirationTime(Date.from(expiresAt))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("userId", user.getUserId())
                    .claim("username", user.getUserName())
                    .claim("authorities", authorities);

            if (user.getNickName() != null) {
                claimsBuilder.claim("nickName", user.getNickName());
            }
            if (user.getStatus() != null) {
                claimsBuilder.claim("status", user.getStatus());
            }

            JWTClaimsSet claims = claimsBuilder.build();

            // 创建 JWS Header
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaKey.getKeyID())
                    .build();

            // 创建 SignedJWT
            SignedJWT signedJWT = new SignedJWT(header, claims);

            // 使用私钥签名
            JWSSigner signer = new RSASSASigner(privateKey);
            signedJWT.sign(signer);

            // 序列化为字符串
            String token = signedJWT.serialize();

            log.debug("使用 JWK 生成用户 Token 成功: username={}, jti={}", user.getUserName(), claims.getJWTID());
            return token;
        } catch (JOSEException e) {
            log.error("使用 JWK 生成用户 Token 失败: username={}, error={}", user.getUserName(), e.getMessage(), e);
            throw new RuntimeException("生成 Token 失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("使用 JWK 生成用户 Token 失败: username={}, error={}", user.getUserName(), e.getMessage(), e);
            throw new RuntimeException("生成 Token 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查找用户客户端
     * <p>
     * 尝试查找用于普通用户的默认客户端，如果不存在则返回 null
     * <p>
     * 注意：Auth 服务本身就是 Authorization Server，不需要外部配置客户端
     * 这里尝试查找常见的默认客户端 ID
     *
     * @return 注册的客户端，如果不存在则返回 null
     */
    private RegisteredClient findUserClient() {
        // 尝试查找常见的默认客户端 ID（用于普通用户）
        String[] defaultClientIds = {DEFAULT_USER_CLIENT_ID, "default_user", "test_client1", "default"};
        
        for (String clientId : defaultClientIds) {
            RegisteredClient client = registeredClientRepository.findByClientId(clientId);
            if (client != null) {
                log.debug("找到用户客户端: clientId={}", clientId);
                return client;
            }
        }
        
        // 如果找不到默认客户端，返回 null，使用默认配置
        log.debug("未找到用户客户端，将使用默认配置");
        return null;
    }
}

