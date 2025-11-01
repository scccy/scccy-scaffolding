package com.scccy.service.auth.oauth2;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scccy.service.auth.dao.service.Oauth2AuthorizationConsentMpService;
import com.scccy.service.auth.domain.AuthorizationConsentConvert;
import com.scccy.service.auth.domain.mp.Oauth2AuthorizationConsentMp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Component;

/**
 * OAuth2AuthorizationConsentService 实现类
 * <p>
 * 基于 MyBatis-Plus 的 OAuth2AuthorizationConsentService 实现
 * 使用数据库存储授权同意记录
 *
 * @author scccy
 */
@Slf4j
@Component
@Primary
public class Oauth2AuthorizationConsentService implements OAuth2AuthorizationConsentService {

    @Resource
    private Oauth2AuthorizationConsentMpService oauth2AuthorizationConsentMpService;

    @Resource
    private AuthorizationConsentConvert authorizationConsentConvert;

    @Override
    public void save(OAuth2AuthorizationConsent authorizationConsent) {
        log.debug("保存授权同意记录: registeredClientId={}, principalName={}",
                authorizationConsent.getRegisteredClientId(),
                authorizationConsent.getPrincipalName());

        Oauth2AuthorizationConsentMp mp = authorizationConsentConvert.convertToMp(authorizationConsent);
        // 使用 saveOrUpdate 方法，因为主键是复合主键 (registeredClientId, principalName)
        oauth2AuthorizationConsentMpService.saveOrUpdate(mp);
    }

    @Override
    public void remove(OAuth2AuthorizationConsent authorizationConsent) {
        log.debug("删除授权同意记录: registeredClientId={}, principalName={}",
                authorizationConsent.getRegisteredClientId(),
                authorizationConsent.getPrincipalName());

        QueryWrapper<Oauth2AuthorizationConsentMp> wrapper = new QueryWrapper<>();
        wrapper.eq("registered_client_id", authorizationConsent.getRegisteredClientId())
                .eq("principal_name", authorizationConsent.getPrincipalName());

        oauth2AuthorizationConsentMpService.remove(wrapper);
    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        log.debug("查找授权同意记录: registeredClientId={}, principalName={}",
                registeredClientId, principalName);

        QueryWrapper<Oauth2AuthorizationConsentMp> wrapper = new QueryWrapper<>();
        wrapper.eq("registered_client_id", registeredClientId)
                .eq("principal_name", principalName);

        Oauth2AuthorizationConsentMp mp = oauth2AuthorizationConsentMpService.getOne(wrapper);
        return authorizationConsentConvert.convertToOAuth2(mp);
    }
}

