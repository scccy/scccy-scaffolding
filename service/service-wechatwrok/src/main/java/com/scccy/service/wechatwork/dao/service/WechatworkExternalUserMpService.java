package com.scccy.service.wechatwork.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scccy.service.wechatwork.domain.mp.WechatworkExternalUserMp;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 好友关系(WechatworkExternalUser)表服务接口
 *
 * @author scccy
 * @since 2025-10-24 17:12:06
 */
public interface WechatworkExternalUserMpService extends IService<WechatworkExternalUserMp> {

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<WechatworkExternalUserMp> pageLike(Integer pageNum, Integer pageSize, WechatworkExternalUserMp query);

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<WechatworkExternalUserMp> pageEq(Integer pageNum, Integer pageSize, WechatworkExternalUserMp query);

}
