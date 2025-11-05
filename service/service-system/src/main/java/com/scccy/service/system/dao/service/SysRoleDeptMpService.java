package com.scccy.service.system.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scccy.service.system.domain.mp.SysRoleDeptMp;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 角色和部门关联表(SysRoleDept)表服务接口
 *
 * @author scccy
 * @since 2025-11-05 17:55:13
 */
public interface SysRoleDeptMpService extends IService<SysRoleDeptMp> {

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<SysRoleDeptMp> pageLike(Integer pageNum, Integer pageSize, SysRoleDeptMp query);

    /**
     * 分页查询，Service 层负责构造分页和查询条件
     */
    Page<SysRoleDeptMp> pageEq(Integer pageNum, Integer pageSize, SysRoleDeptMp query);

}
