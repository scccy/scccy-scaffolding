package com.scccy.service.system.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.system.dao.mapper.SysRoleDeptMapper;
import com.scccy.service.system.domain.mp.SysRoleDeptMp;
import com.scccy.service.system.dao.service.SysRoleDeptMpService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 角色和部门关联表(SysRoleDept)服务实现类
 *
 * @author scccy
 * @since 2025-11-05 17:55:13
 */
@Service
public class SysRoleDeptMpServiceImpl
        extends ServiceImpl<SysRoleDeptMapper, SysRoleDeptMp>
        implements SysRoleDeptMpService {
    @Override
    public Page<SysRoleDeptMp> pageEq(Integer pageNum, Integer pageSize, SysRoleDeptMp sysRoleDeptMp) {
        Page<SysRoleDeptMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysRoleDeptMp> wrapper = new QueryWrapper<>();

        if (sysRoleDeptMp.getRoleId() != null) {
            wrapper.eq("role_id", sysRoleDeptMp.getRoleId());
        }
        if (sysRoleDeptMp.getDeptId() != null) {
            wrapper.eq("dept_id", sysRoleDeptMp.getDeptId());
        }

        return this.page(page, wrapper);
    }


    @Override
    public Page<SysRoleDeptMp> pageLike(Integer pageNum, Integer pageSize, SysRoleDeptMp sysRoleDeptMp) {
        Page<SysRoleDeptMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysRoleDeptMp> wrapper = new QueryWrapper<>();

        if (sysRoleDeptMp.getRoleId() != null) {
            wrapper.like("role_id", sysRoleDeptMp.getRoleId());
        }
        if (sysRoleDeptMp.getDeptId() != null) {
            wrapper.like("dept_id", sysRoleDeptMp.getDeptId());
        }

        return this.page(page, wrapper);
    }

}
