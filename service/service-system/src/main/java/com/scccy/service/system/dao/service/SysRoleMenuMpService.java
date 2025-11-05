package com.scccy.service.system.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scccy.service.system.domain.mp.SysRoleMenuMp;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 角色和菜单关联表(SysRoleMenu)表服务接口
 *
 * @author scccy
 * @since 2025-11-05 17:55:14
 */
public interface SysRoleMenuMpService extends IService<SysRoleMenuMp> {

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<SysRoleMenuMp> pageLike(Integer pageNum, Integer pageSize, SysRoleMenuMp query);

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<SysRoleMenuMp> pageEq(Integer pageNum, Integer pageSize, SysRoleMenuMp query);

}
