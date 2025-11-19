package com.scccy.service.system.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.system.dao.mapper.SysRoleMenuMapper;
import com.scccy.service.system.domain.mp.SysRoleMenuMp;
import com.scccy.service.system.dao.service.SysRoleMenuMpService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 角色和菜单关联表(SysRoleMenu)服务实现类
 *
 * @author scccy
 * @since 2025-11-05 17:55:14
 */
@Service
public class SysRoleMenuMpServiceImpl
        extends ServiceImpl<SysRoleMenuMapper, SysRoleMenuMp>
        implements SysRoleMenuMpService {
    @Override
    public Page<SysRoleMenuMp> pageEq(Integer pageNum, Integer pageSize, SysRoleMenuMp sysRoleMenuMp) {
        Page<SysRoleMenuMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysRoleMenuMp> wrapper = new QueryWrapper<>();

        if (sysRoleMenuMp.getRoleId() != null) {
            wrapper.eq("role_id", sysRoleMenuMp.getRoleId());
        }
        if (sysRoleMenuMp.getMenuId() != null) {
            wrapper.eq("menu_id", sysRoleMenuMp.getMenuId());
        }

        return this.page(page, wrapper);
    }


    @Override
    public Page<SysRoleMenuMp> pageLike(Integer pageNum, Integer pageSize, SysRoleMenuMp sysRoleMenuMp) {
        Page<SysRoleMenuMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysRoleMenuMp> wrapper = new QueryWrapper<>();

        if (sysRoleMenuMp.getRoleId() != null) {
            wrapper.like("role_id", sysRoleMenuMp.getRoleId());
        }
        if (sysRoleMenuMp.getMenuId() != null) {
            wrapper.like("menu_id", sysRoleMenuMp.getMenuId());
        }

        return this.page(page, wrapper);
    }

}
