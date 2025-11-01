package com.scccy.service.auth.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scccy.service.auth.domain.mp.Oauth2AuthorizationMp;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * token记录表(Oauth2Authorization)表服务接口
 *
 * @author scccy
 * @since 2025-11-01 15:18:16
 */
public interface Oauth2AuthorizationMpService extends IService<Oauth2AuthorizationMp> {

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<Oauth2AuthorizationMp> pageLike(Integer pageNum, Integer pageSize, Oauth2AuthorizationMp query);

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<Oauth2AuthorizationMp> pageEq(Integer pageNum, Integer pageSize, Oauth2AuthorizationMp query);

}
