package com.scccy.service.auth.domain;

import com.scccy.service.auth.domain.mp.Oauth2AuthorizationConsentMp;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OAuth2AuthorizationConsent 转换器
 * <p>
 * 用于 OAuth2AuthorizationConsent 和 Oauth2AuthorizationConsentMp 之间的转换
 *
 * @author scccy
 */
@Component
public class AuthorizationConsentConvert {

    /**
     * 将 OAuth2AuthorizationConsent 转换为 Oauth2AuthorizationConsentMp
     *
     * @param authorizationConsent OAuth2AuthorizationConsent 对象
     * @return Oauth2AuthorizationConsentMp 对象
     */
    public Oauth2AuthorizationConsentMp convertToMp(OAuth2AuthorizationConsent authorizationConsent) {
        if (authorizationConsent == null) {
            return null;
        }
        Oauth2AuthorizationConsentMp mp = new Oauth2AuthorizationConsentMp();
        mp.setRegisteredClientId(authorizationConsent.getRegisteredClientId());
        mp.setPrincipalName(authorizationConsent.getPrincipalName());
        // 将 scopes 集合转换为逗号分隔的字符串
        Set<String> scopes = authorizationConsent.getScopes();
        if (scopes != null && !scopes.isEmpty()) {
            mp.setAuthorities(String.join(",", scopes));
        }
        return mp;
    }

    /**
     * 将 Oauth2AuthorizationConsentMp 转换为 OAuth2AuthorizationConsent
     *
     * @param mp Oauth2AuthorizationConsentMp 对象
     * @return OAuth2AuthorizationConsent 对象
     */
    public OAuth2AuthorizationConsent convertToOAuth2(Oauth2AuthorizationConsentMp mp) {
        if (mp == null) {
            return null;
        }
        // 将 authorities 字符串转换为 scopes 集合
        Set<String> scopes = Collections.emptySet();
        if (StringUtils.isNotBlank(mp.getAuthorities())) {
            scopes = Arrays.stream(StringUtils.split(mp.getAuthorities(), ","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
        }
        
        // 使用 Builder 构建 OAuth2AuthorizationConsent
        OAuth2AuthorizationConsent.Builder builder = OAuth2AuthorizationConsent.withId(
                mp.getRegisteredClientId(),
                mp.getPrincipalName());
        
        // 添加 scopes（使用 scope 方法逐个添加）
        for (String scope : scopes) {
            builder.scope(scope);
        }
        
        return builder.build();
    }
}

