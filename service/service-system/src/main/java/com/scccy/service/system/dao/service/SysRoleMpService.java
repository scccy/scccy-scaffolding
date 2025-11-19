package com.scccy.service.system.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scccy.service.system.domain.mp.SysRoleMp;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 角色信息表(SysRole)表服务接口
 *
 * @author scccy
 * @since 2025-11-06 15:49:52
 */
public interface SysRoleMpService extends IService<SysRoleMp> {

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<SysRoleMp> pageLike(Integer pageNum, Integer pageSize, SysRoleMp query);

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<SysRoleMp> pageEq(Integer pageNum, Integer pageSize, SysRoleMp query);

}
