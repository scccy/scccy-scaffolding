package com.scccy.service.jackyun.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scccy.service.jackyun.domain.mp.SysUserMp;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 用户信息表(SysUser)表服务接口
 *
 * @author scccy
 * @since 2025-10-22 17:26:22
 */
public interface SysUserMpService extends IService<SysUserMp> {

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<SysUserMp> pageLike(Integer pageNum, Integer pageSize, SysUserMp query);

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<SysUserMp> pageEq(Integer pageNum, Integer pageSize, SysUserMp query);

}
