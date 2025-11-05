package com.scccy.service.auth.domain;


import com.alibaba.nacos.shaded.com.google.common.collect.Sets;
import com.scccy.service.auth.domain.form.RegisteredClientForm;
import com.scccy.service.auth.domain.mp.Oauth2RegisteredClientMp;
import com.scccy.service.auth.domain.vo.RegisteredClientVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RegisteredClientConvert {

    /**
     * RegisteredClientPo转换为RegisteredClient
     *
     * @param registeredClientPo PO对象
     * @return RegisteredClient
     */
    public RegisteredClient convertToRegisteredClient(Oauth2RegisteredClientMp registeredClientPo) {
        // 构建scope，处理 null 情况
        String scopesStr = registeredClientPo.getScopes();
        Set<String> scopes = (scopesStr != null && !scopesStr.trim().isEmpty())
                ? Arrays.stream(StringUtils.split(scopesStr, ",")).collect(Collectors.toSet())
                : Set.of("read");  // 默认值
        
        // 构建grantType，处理 null 情况（需要先构建 grantTypes，用于判断是否需要 redirectUris）
        String grantTypesStr = registeredClientPo.getAuthorizationGrantTypes();
        Set<AuthorizationGrantType> grantTypes = (grantTypesStr != null && !grantTypesStr.trim().isEmpty())
                ? Arrays.stream(StringUtils.split(grantTypesStr, ","))
                        .map(AuthorizationGrantType::new)
                        .collect(Collectors.toSet())
                : Set.of(new AuthorizationGrantType("client_credentials"));  // 默认值
        
        // 构建redirectUris，处理 null 情况
        // 注意：Spring Security OAuth2 要求 redirectUris 不能为空
        // 对于 client_credentials 授权类型，虽然不需要 redirectUris，但仍需要提供至少一个值
        String redirectUrisStr = registeredClientPo.getRedirectUris();
        Set<String> redirectUrisTemp;
        if (redirectUrisStr != null && !redirectUrisStr.trim().isEmpty()) {
            redirectUrisTemp = Arrays.stream(StringUtils.split(redirectUrisStr, ","))
                    .filter(uri -> uri != null && !uri.trim().isEmpty())
                    .collect(Collectors.toSet());
        } else {
            redirectUrisTemp = Set.of();  // 先设为空集合
        }
        
        // 如果 redirectUris 为空，且授权类型包含需要 redirect URI 的类型，则使用默认值
        // 如果只有 client_credentials，也需要提供一个占位符（虽然不会使用）
        final Set<String> redirectUris;
        if (redirectUrisTemp.isEmpty()) {
            boolean needsRedirectUri = grantTypes.stream()
                    .anyMatch(gt -> gt.getValue().equals("authorization_code") 
                            || gt.getValue().equals("implicit")
                            || gt.getValue().equals("device_code"));
            if (needsRedirectUri) {
                // 如果包含需要 redirect URI 的授权类型，使用默认值
                redirectUris = Set.of("http://localhost:30000/callback");
            } else {
                // 如果只有 client_credentials 等不需要 redirect URI 的类型，使用占位符
                redirectUris = Set.of("http://localhost:30000");
            }
        } else {
            redirectUris = redirectUrisTemp;
        }
        
        // 构建method，处理 null 情况
        String methodsStr = registeredClientPo.getClientAuthenticationMethods();
        Set<ClientAuthenticationMethod> methods = (methodsStr != null && !methodsStr.trim().isEmpty())
                ? Arrays.stream(StringUtils.split(methodsStr, ","))
                        .map(ClientAuthenticationMethod::new)
                        .collect(Collectors.toSet())
                : Set.of(new ClientAuthenticationMethod("client_secret_basic"));  // 默认值
        // 构建 RegisteredClient对象
        RegisteredClient.Builder registeredClientBuilder = RegisteredClient.withId(registeredClientPo.getId())
                .clientId(registeredClientPo.getClientId())
                .clientSecret(registeredClientPo.getClientSecret())
                .clientName(registeredClientPo.getClientName() != null ? registeredClientPo.getClientName() : registeredClientPo.getClientId())
                .clientSecretExpiresAt(registeredClientPo.getClientSecretExpiresAt() != null 
                        ? registeredClientPo.getClientSecretExpiresAt().toInstant() 
                        : Instant.now().plusSeconds(365L * 24 * 60 * 60))  // 默认1年后过期
                .redirectUris(uris -> uris.addAll(redirectUris))
                .clientAuthenticationMethods(methodSet -> methodSet.addAll(methods))
                .scopes(scopeSet -> scopeSet.addAll(scopes))
                .authorizationGrantTypes(grantType -> grantType.addAll(grantTypes))
                .clientSettings(buildClientSettings(registeredClientPo.getClientSettings()))
                .tokenSettings(buildTokenSettings(registeredClientPo.getTokenSettings()));
        return registeredClientBuilder.build();
    }

    /**
     * 构建 ClientSettings，处理 null 或空 Map 的情况
     *
     * @param clientSettingsMap 客户端设置 Map
     * @return ClientSettings
     */
    private ClientSettings buildClientSettings(Map<String, Object> clientSettingsMap) {
        if (clientSettingsMap != null && !clientSettingsMap.isEmpty()) {
            return ClientSettings.withSettings(clientSettingsMap).build();
        }
        // 使用默认的 ClientSettings
        return ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .build();
    }

    /**
     * 构建 TokenSettings，处理 null 或空 Map 的情况
     *
     * @param tokenSettingsMap Token 设置 Map
     * @return TokenSettings
     */
    private TokenSettings buildTokenSettings(Map<String, Object> tokenSettingsMap) {
        if (tokenSettingsMap != null && !tokenSettingsMap.isEmpty()) {
            try {
                // 尝试直接使用 withSettings，但需要处理 Duration 转换
                // 从 Map 中恢复 Duration 对象（如果存储的是秒数）
                Map<String, Object> processedSettings = new HashMap<>(tokenSettingsMap);
                
                // 处理 access-token-time-to-live（秒数 -> Duration）
                Object accessTokenTtl = processedSettings.get("settings.token.access-token-time-to-live");
                if (accessTokenTtl instanceof Number && !(accessTokenTtl instanceof Duration)) {
                    processedSettings.put("settings.token.access-token-time-to-live", 
                            Duration.ofSeconds(((Number) accessTokenTtl).longValue()));
                }
                
                // 处理 refresh-token-time-to-live（秒数 -> Duration）
                Object refreshTokenTtl = processedSettings.get("settings.token.refresh-token-time-to-live");
                if (refreshTokenTtl instanceof Number && !(refreshTokenTtl instanceof Duration)) {
                    processedSettings.put("settings.token.refresh-token-time-to-live", 
                            Duration.ofSeconds(((Number) refreshTokenTtl).longValue()));
                }
                
                // 处理 access-token-format（如果存储的是 Map 格式）
                Object accessTokenFormat = processedSettings.get("settings.token.access-token-format");
                if (accessTokenFormat instanceof Map) {
                    Map<?, ?> formatMap = (Map<?, ?>) accessTokenFormat;
                    Object value = formatMap.get("value");
                    if (value != null) {
                        processedSettings.put("settings.token.access-token-format", value);
                    }
                }
                
                return TokenSettings.withSettings(processedSettings).build();
            } catch (Exception e) {
                // 如果解析失败，使用默认值
                return buildDefaultTokenSettings();
            }
        }
        // 使用默认的 TokenSettings
        return buildDefaultTokenSettings();
    }
    
    /**
     * 构建默认的 TokenSettings
     *
     * @return 默认的 TokenSettings
     */
    private TokenSettings buildDefaultTokenSettings() {
        return TokenSettings.builder()
                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                .reuseRefreshTokens(true)
                .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
    }

    /**
     * 将RegisteredClientForm转为RegisteredClientPo，方便Dao存储
     *
     * @param registeredClientForm RegisteredClient对象实例
     * @return RegisteredClientPo实例
     */
    public Oauth2RegisteredClientMp convertToRegisteredClientPo(RegisteredClientForm registeredClientForm) {
        Oauth2RegisteredClientMp registeredClientPo = new Oauth2RegisteredClientMp();
        registeredClientPo.setId(registeredClientForm.getId());
        registeredClientPo.setClientId(registeredClientForm.getClientId());
        // 处理 clientName 为 null 的情况，默认使用 clientId
        String clientName = registeredClientForm.getClientName();
        if (clientName == null || clientName.trim().isEmpty()) {
            clientName = registeredClientForm.getClientId();
        }
        registeredClientPo.setClientName(clientName);
        registeredClientPo.setClientSecret(registeredClientForm.getClientSecret());
        registeredClientPo.setRedirectUris(registeredClientForm.getRedirectUri());
        // 处理 clientSecretExpires 为 null 的情况，默认设置为1年后（365天）
        Long clientSecretExpires = registeredClientForm.getClientSecretExpires();
        if (clientSecretExpires == null) {
            clientSecretExpires = 365L * 24 * 60 * 60; // 1年（秒）
        }
        registeredClientPo.setClientSecretExpiresAt(Date.from(Instant.now().plusSeconds(clientSecretExpires)));
        // 处理 grantTypes 为 null 的情况，默认使用 client_credentials 和 authorization_code
        Set<String> grantTypes = registeredClientForm.getGrantTypes();
        if (grantTypes == null || grantTypes.isEmpty()) {
            grantTypes = Set.of("client_credentials", "authorization_code");
        }
        registeredClientPo.setAuthorizationGrantTypes(String.join(",", grantTypes));
        // 处理 clientAuthenticationMethods 为 null 的情况，默认使用 client_secret_basic
        Set<String> clientAuthenticationMethods = registeredClientForm.getClientAuthenticationMethods();
        if (clientAuthenticationMethods == null || clientAuthenticationMethods.isEmpty()) {
            clientAuthenticationMethods = Set.of("client_secret_basic");
        }
        registeredClientPo.setClientAuthenticationMethods(StringUtils.join(clientAuthenticationMethods, ","));
        // 处理 scopes 为 null 的情况，默认使用 read
        Set<String> scopes = registeredClientForm.getScopes();
        if (scopes == null || scopes.isEmpty()) {
            scopes = Set.of("read");
        }
        registeredClientPo.setScopes(String.join(",", scopes));
        registeredClientPo.setClientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build().getSettings());
        // 手动构建 tokenSettings Map，将 Duration 转换为秒数（double），避免 Jackson 序列化问题
        Map<String, Object> tokenSettingsMap = new HashMap<>();
        TokenSettings defaultTokenSettings = TokenSettings.builder()
                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                .reuseRefreshTokens(true)
                .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        // 获取默认的 Duration 值并转换为秒数
        Map<String, Object> defaultSettings = defaultTokenSettings.getSettings();
        for (Map.Entry<String, Object> entry : defaultSettings.entrySet()) {
            if (entry.getValue() instanceof Duration) {
                // 将 Duration 转换为秒数（double）
                tokenSettingsMap.put(entry.getKey(), ((Duration) entry.getValue()).getSeconds() + 0.0);
            } else {
                // 其他类型直接复制
                tokenSettingsMap.put(entry.getKey(), entry.getValue());
            }
        }
        registeredClientPo.setTokenSettings(tokenSettingsMap);
        return registeredClientPo;
    }

    /**
     * 将RegisteredClientPo转为RegisteredClientVo，前端展示
     *
     * @param registeredClientPo RegisteredClientPo对象实例
     * @return RegisteredClientVo实例
     */
    public RegisteredClientVo convertToRegisteredClientVo(Oauth2RegisteredClientMp registeredClientPo) {
        RegisteredClientVo registeredClientVo = new RegisteredClientVo();
        registeredClientVo.setId(registeredClientPo.getId());
        registeredClientVo.setClientId(registeredClientPo.getClientId());
        registeredClientVo.setClientName(registeredClientPo.getClientName());
        registeredClientVo.setClientIdIssuedAt(registeredClientPo.getClientIdIssuedAt());
        registeredClientVo.setClientSecretExpiresAt(registeredClientPo.getClientSecretExpiresAt());
        
        // 处理 null 情况
        String scopes = registeredClientPo.getScopes();
        registeredClientVo.setScopes(scopes != null && !scopes.trim().isEmpty()
                ? Sets.newHashSet(StringUtils.split(scopes, ","))
                : Sets.newHashSet("read"));
        
        String redirectUris = registeredClientPo.getRedirectUris();
        registeredClientVo.setRedirectUris(redirectUris != null && !redirectUris.trim().isEmpty()
                ? Sets.newHashSet(StringUtils.split(redirectUris, ","))
                : Sets.newHashSet());
        
        String authorizationGrantTypes = registeredClientPo.getAuthorizationGrantTypes();
        registeredClientVo.setAuthorizationGrantTypes(authorizationGrantTypes != null && !authorizationGrantTypes.trim().isEmpty()
                ? Sets.newHashSet(StringUtils.split(authorizationGrantTypes, ","))
                : Sets.newHashSet("client_credentials"));
        
        String clientAuthenticationMethods = registeredClientPo.getClientAuthenticationMethods();
        registeredClientVo.setClientAuthenticationMethods(clientAuthenticationMethods != null && !clientAuthenticationMethods.trim().isEmpty()
                ? Sets.newHashSet(StringUtils.split(clientAuthenticationMethods, ","))
                : Sets.newHashSet("client_secret_basic"));
        
        return registeredClientVo;
    }

}
