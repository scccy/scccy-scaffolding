package com.scccy.service.system.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.system.dao.mapper.SysRoleMapper;
import com.scccy.service.system.domain.mp.SysRoleMp;
import com.scccy.service.system.dao.service.SysRoleMpService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 角色信息表(SysRole)服务实现类
 *
 * @author scccy
 * @since 2025-11-05 17:55:12
 */
@Service
public class SysRoleMpServiceImpl
        extends ServiceImpl<SysRoleMapper, SysRoleMp>
        implements SysRoleMpService {
    @Override
    public Page<SysRoleMp> pageEq(Integer pageNum, Integer pageSize, SysRoleMp sysRoleMp) {
        Page<SysRoleMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysRoleMp> wrapper = new QueryWrapper<>();

        if (sysRoleMp.getRoleId() != null) {
            wrapper.eq("role_id", sysRoleMp.getRoleId());
        }
        if (sysRoleMp.getRoleName() != null && !sysRoleMp.getRoleName().isEmpty()) {
            wrapper.eq("role_name", sysRoleMp.getRoleName());
        }
        if (sysRoleMp.getRoleKey() != null && !sysRoleMp.getRoleKey().isEmpty()) {
            wrapper.eq("role_key", sysRoleMp.getRoleKey());
        }
        if (sysRoleMp.getRoleSort() != null) {
            wrapper.eq("role_sort", sysRoleMp.getRoleSort());
        }
        if (sysRoleMp.getDataScope() != null && !sysRoleMp.getDataScope().isEmpty()) {
            wrapper.eq("data_scope", sysRoleMp.getDataScope());
        }
        if (sysRoleMp.getMenuCheckStrictly() != null) {
            wrapper.eq("menu_check_strictly", sysRoleMp.getMenuCheckStrictly());
        }
        if (sysRoleMp.getDeptCheckStrictly() != null) {
            wrapper.eq("dept_check_strictly", sysRoleMp.getDeptCheckStrictly());
        }
        if (sysRoleMp.getStatus() != null && !sysRoleMp.getStatus().isEmpty()) {
            wrapper.eq("status", sysRoleMp.getStatus());
        }
        if (sysRoleMp.getDelFlag() != null && !sysRoleMp.getDelFlag().isEmpty()) {
            wrapper.eq("del_flag", sysRoleMp.getDelFlag());
        }
        if (sysRoleMp.getCreateBy() != null && !sysRoleMp.getCreateBy().isEmpty()) {
            wrapper.eq("create_by", sysRoleMp.getCreateBy());
        }
        if (sysRoleMp.getCreateTime() != null) {
            wrapper.eq("create_time", sysRoleMp.getCreateTime());
        }
        if (sysRoleMp.getUpdateBy() != null && !sysRoleMp.getUpdateBy().isEmpty()) {
            wrapper.eq("update_by", sysRoleMp.getUpdateBy());
        }
        if (sysRoleMp.getUpdateTime() != null) {
            wrapper.eq("update_time", sysRoleMp.getUpdateTime());
        }
        if (sysRoleMp.getRemark() != null && !sysRoleMp.getRemark().isEmpty()) {
            wrapper.eq("remark", sysRoleMp.getRemark());
        }

        return this.page(page, wrapper);
    }


    @Override
    public Page<SysRoleMp> pageLike(Integer pageNum, Integer pageSize, SysRoleMp sysRoleMp) {
        Page<SysRoleMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysRoleMp> wrapper = new QueryWrapper<>();

        if (sysRoleMp.getRoleId() != null) {
            wrapper.like("role_id", sysRoleMp.getRoleId());
        }
        if (sysRoleMp.getRoleName() != null && !sysRoleMp.getRoleName().isEmpty()) {
            wrapper.like("role_name", sysRoleMp.getRoleName());
        }
        if (sysRoleMp.getRoleKey() != null && !sysRoleMp.getRoleKey().isEmpty()) {
            wrapper.like("role_key", sysRoleMp.getRoleKey());
        }
        if (sysRoleMp.getRoleSort() != null) {
            wrapper.like("role_sort", sysRoleMp.getRoleSort());
        }
        if (sysRoleMp.getDataScope() != null && !sysRoleMp.getDataScope().isEmpty()) {
            wrapper.like("data_scope", sysRoleMp.getDataScope());
        }
        if (sysRoleMp.getMenuCheckStrictly() != null) {
            wrapper.like("menu_check_strictly", sysRoleMp.getMenuCheckStrictly());
        }
        if (sysRoleMp.getDeptCheckStrictly() != null) {
            wrapper.like("dept_check_strictly", sysRoleMp.getDeptCheckStrictly());
        }
        if (sysRoleMp.getStatus() != null && !sysRoleMp.getStatus().isEmpty()) {
            wrapper.like("status", sysRoleMp.getStatus());
        }
        if (sysRoleMp.getDelFlag() != null && !sysRoleMp.getDelFlag().isEmpty()) {
            wrapper.like("del_flag", sysRoleMp.getDelFlag());
        }
        if (sysRoleMp.getCreateBy() != null && !sysRoleMp.getCreateBy().isEmpty()) {
            wrapper.like("create_by", sysRoleMp.getCreateBy());
        }
        if (sysRoleMp.getCreateTime() != null) {
            wrapper.like("create_time", sysRoleMp.getCreateTime());
        }
        if (sysRoleMp.getUpdateBy() != null && !sysRoleMp.getUpdateBy().isEmpty()) {
            wrapper.like("update_by", sysRoleMp.getUpdateBy());
        }
        if (sysRoleMp.getUpdateTime() != null) {
            wrapper.like("update_time", sysRoleMp.getUpdateTime());
        }
        if (sysRoleMp.getRemark() != null && !sysRoleMp.getRemark().isEmpty()) {
            wrapper.like("remark", sysRoleMp.getRemark());
        }

        return this.page(page, wrapper);
    }

}
