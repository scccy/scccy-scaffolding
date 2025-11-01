package com.scccy.service.auth.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scccy.service.auth.domain.mp.Oauth2AuthorizationMp;
import org.apache.ibatis.annotations.Mapper;

/**
 * token记录表(Oauth2Authorization)Mapper 接口
 *
 * @author scccy
 * @since 2025-11-01 15:18:16
 */
@Mapper
public interface Oauth2AuthorizationMapper extends BaseMapper<Oauth2AuthorizationMp> {

}
