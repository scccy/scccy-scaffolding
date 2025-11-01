package com.scccy.service.auth.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scccy.service.auth.domain.mp.Oauth2RegisteredClientMp;
import org.apache.ibatis.annotations.Mapper;

/**
 * client记录表(Oauth2RegisteredClient)Mapper 接口
 *
 * @author scccy
 * @since 2025-11-01 14:03:12
 */
@Mapper
public interface Oauth2RegisteredClientMapper extends BaseMapper<Oauth2RegisteredClientMp> {

}
