package com.scccy.service.wechatwork.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scccy.service.wechatwork.domain.mp.WechatworkGroupMp;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 企微用户群关联表(WechatworkGroup)表服务接口
 *
 * @author scccy
 * @since 2025-10-24 17:12:29
 */
public interface WechatworkGroupMpService extends IService<WechatworkGroupMp> {

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<WechatworkGroupMp> pageLike(Integer pageNum, Integer pageSize, WechatworkGroupMp query);

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<WechatworkGroupMp> pageEq(Integer pageNum, Integer pageSize, WechatworkGroupMp query);

}
