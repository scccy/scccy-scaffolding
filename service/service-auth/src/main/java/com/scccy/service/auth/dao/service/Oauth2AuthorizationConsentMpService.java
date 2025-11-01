package com.scccy.service.auth.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scccy.service.auth.domain.mp.Oauth2AuthorizationConsentMp;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 授权记录(Oauth2AuthorizationConsent)表服务接口
 *
 * @author scccy
 * @since 2025-11-01 15:18:41
 */
public interface Oauth2AuthorizationConsentMpService extends IService<Oauth2AuthorizationConsentMp> {

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<Oauth2AuthorizationConsentMp> pageLike(Integer pageNum, Integer pageSize, Oauth2AuthorizationConsentMp query);

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<Oauth2AuthorizationConsentMp> pageEq(Integer pageNum, Integer pageSize, Oauth2AuthorizationConsentMp query);

}
