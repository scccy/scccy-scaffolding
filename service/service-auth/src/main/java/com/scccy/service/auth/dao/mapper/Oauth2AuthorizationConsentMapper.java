package com.scccy.service.auth.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scccy.service.auth.domain.mp.Oauth2AuthorizationConsentMp;
import org.apache.ibatis.annotations.Mapper;

/**
 * 授权记录(Oauth2AuthorizationConsent)Mapper 接口
 *
 * @author scccy
 * @since 2025-11-01 15:18:41
 */
@Mapper
public interface Oauth2AuthorizationConsentMapper extends BaseMapper<Oauth2AuthorizationConsentMp> {

}
