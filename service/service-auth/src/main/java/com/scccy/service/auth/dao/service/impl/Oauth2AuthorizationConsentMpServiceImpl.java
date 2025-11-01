package com.scccy.service.auth.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.auth.dao.mapper.Oauth2AuthorizationConsentMapper;
import com.scccy.service.auth.domain.mp.Oauth2AuthorizationConsentMp;
import com.scccy.service.auth.dao.service.Oauth2AuthorizationConsentMpService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 授权记录(Oauth2AuthorizationConsent)服务实现类
 *
 * @author scccy
 * @since 2025-11-01 15:18:41
 */
@Service
public class Oauth2AuthorizationConsentMpServiceImpl
        extends ServiceImpl<Oauth2AuthorizationConsentMapper, Oauth2AuthorizationConsentMp>
        implements Oauth2AuthorizationConsentMpService {
    @Override
    public Page<Oauth2AuthorizationConsentMp> pageEq(Integer pageNum, Integer pageSize, Oauth2AuthorizationConsentMp oauth2AuthorizationConsentMp) {
        Page<Oauth2AuthorizationConsentMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Oauth2AuthorizationConsentMp> wrapper = new QueryWrapper<>();

        if (oauth2AuthorizationConsentMp.getRegisteredClientId() != null && !oauth2AuthorizationConsentMp.getRegisteredClientId().isEmpty()) {
            wrapper.eq("registered_client_id", oauth2AuthorizationConsentMp.getRegisteredClientId());
        }
        if (oauth2AuthorizationConsentMp.getPrincipalName() != null && !oauth2AuthorizationConsentMp.getPrincipalName().isEmpty()) {
            wrapper.eq("principal_name", oauth2AuthorizationConsentMp.getPrincipalName());
        }
        if (oauth2AuthorizationConsentMp.getAuthorities() != null && !oauth2AuthorizationConsentMp.getAuthorities().isEmpty()) {
            wrapper.eq("authorities", oauth2AuthorizationConsentMp.getAuthorities());
        }

        return this.page(page, wrapper);
    }


    @Override
    public Page<Oauth2AuthorizationConsentMp> pageLike(Integer pageNum, Integer pageSize, Oauth2AuthorizationConsentMp oauth2AuthorizationConsentMp) {
        Page<Oauth2AuthorizationConsentMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Oauth2AuthorizationConsentMp> wrapper = new QueryWrapper<>();

        if (oauth2AuthorizationConsentMp.getRegisteredClientId() != null && !oauth2AuthorizationConsentMp.getRegisteredClientId().isEmpty()) {
            wrapper.like("registered_client_id", oauth2AuthorizationConsentMp.getRegisteredClientId());
        }
        if (oauth2AuthorizationConsentMp.getPrincipalName() != null && !oauth2AuthorizationConsentMp.getPrincipalName().isEmpty()) {
            wrapper.like("principal_name", oauth2AuthorizationConsentMp.getPrincipalName());
        }
        if (oauth2AuthorizationConsentMp.getAuthorities() != null && !oauth2AuthorizationConsentMp.getAuthorities().isEmpty()) {
            wrapper.like("authorities", oauth2AuthorizationConsentMp.getAuthorities());
        }

        return this.page(page, wrapper);
    }

}
