package com.scccy.service.demo.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.demo.dao.mapper.SysUserMapper;
import com.scccy.service.demo.domain.mp.SysUserMp;
import com.scccy.service.demo.dao.service.SysUserMpService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 用户信息表(SysUser)服务实现类
 *
 * @author scccy
 * @since 2025-10-22 17:26:22
 */
@Service
public class SysUserMpServiceImpl
        extends ServiceImpl<SysUserMapper, SysUserMp>
        implements SysUserMpService {
    @Override
    public Page<SysUserMp> pageEq(Integer pageNum, Integer pageSize, SysUserMp sysUserMp) {
        Page<SysUserMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysUserMp> wrapper = new QueryWrapper<>();

        if (sysUserMp.getUserId() != null) {
            wrapper.eq("user_id", sysUserMp.getUserId());
        }
        if (sysUserMp.getDeptId() != null) {
            wrapper.eq("dept_id", sysUserMp.getDeptId());
        }
        if (sysUserMp.getUserName() != null && !sysUserMp.getUserName().isEmpty()) {
            wrapper.eq("user_name", sysUserMp.getUserName());
        }
        if (sysUserMp.getNickName() != null && !sysUserMp.getNickName().isEmpty()) {
            wrapper.eq("nick_name", sysUserMp.getNickName());
        }
        if (sysUserMp.getUserType() != null && !sysUserMp.getUserType().isEmpty()) {
            wrapper.eq("user_type", sysUserMp.getUserType());
        }
        if (sysUserMp.getEmail() != null && !sysUserMp.getEmail().isEmpty()) {
            wrapper.eq("email", sysUserMp.getEmail());
        }
        if (sysUserMp.getPhonenumber() != null && !sysUserMp.getPhonenumber().isEmpty()) {
            wrapper.eq("phonenumber", sysUserMp.getPhonenumber());
        }
        if (sysUserMp.getSex() != null && !sysUserMp.getSex().isEmpty()) {
            wrapper.eq("sex", sysUserMp.getSex());
        }
        if (sysUserMp.getAvatar() != null && !sysUserMp.getAvatar().isEmpty()) {
            wrapper.eq("avatar", sysUserMp.getAvatar());
        }
        if (sysUserMp.getPassword() != null && !sysUserMp.getPassword().isEmpty()) {
            wrapper.eq("password", sysUserMp.getPassword());
        }
        if (sysUserMp.getStatus() != null) {
            wrapper.eq("status", sysUserMp.getStatus());
        }
        if (sysUserMp.getDelFlag() != null) {
            wrapper.eq("del_flag", sysUserMp.getDelFlag());
        }
        if (sysUserMp.getLoginIp() != null && !sysUserMp.getLoginIp().isEmpty()) {
            wrapper.eq("login_ip", sysUserMp.getLoginIp());
        }
        if (sysUserMp.getLoginDate() != null) {
            wrapper.eq("login_date", sysUserMp.getLoginDate());
        }
        if (sysUserMp.getPwdUpdateDate() != null) {
            wrapper.eq("pwd_update_date", sysUserMp.getPwdUpdateDate());
        }
        if (sysUserMp.getCreateBy() != null && !sysUserMp.getCreateBy().isEmpty()) {
            wrapper.eq("create_by", sysUserMp.getCreateBy());
        }
        if (sysUserMp.getCreateTime() != null) {
            wrapper.eq("create_time", sysUserMp.getCreateTime());
        }
        if (sysUserMp.getUpdateBy() != null && !sysUserMp.getUpdateBy().isEmpty()) {
            wrapper.eq("update_by", sysUserMp.getUpdateBy());
        }
        if (sysUserMp.getUpdateTime() != null) {
            wrapper.eq("update_time", sysUserMp.getUpdateTime());
        }
        if (sysUserMp.getRemark() != null && !sysUserMp.getRemark().isEmpty()) {
            wrapper.eq("remark", sysUserMp.getRemark());
        }

        return this.page(page, wrapper);
    }


    @Override
    public Page<SysUserMp> pageLike(Integer pageNum, Integer pageSize, SysUserMp sysUserMp) {
        Page<SysUserMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysUserMp> wrapper = new QueryWrapper<>();

        if (sysUserMp.getUserId() != null) {
            wrapper.like("user_id", sysUserMp.getUserId());
        }
        if (sysUserMp.getDeptId() != null) {
            wrapper.like("dept_id", sysUserMp.getDeptId());
        }
        if (sysUserMp.getUserName() != null && !sysUserMp.getUserName().isEmpty()) {
            wrapper.like("user_name", sysUserMp.getUserName());
        }
        if (sysUserMp.getNickName() != null && !sysUserMp.getNickName().isEmpty()) {
            wrapper.like("nick_name", sysUserMp.getNickName());
        }
        if (sysUserMp.getUserType() != null && !sysUserMp.getUserType().isEmpty()) {
            wrapper.like("user_type", sysUserMp.getUserType());
        }
        if (sysUserMp.getEmail() != null && !sysUserMp.getEmail().isEmpty()) {
            wrapper.like("email", sysUserMp.getEmail());
        }
        if (sysUserMp.getPhonenumber() != null && !sysUserMp.getPhonenumber().isEmpty()) {
            wrapper.like("phonenumber", sysUserMp.getPhonenumber());
        }
        if (sysUserMp.getSex() != null && !sysUserMp.getSex().isEmpty()) {
            wrapper.like("sex", sysUserMp.getSex());
        }
        if (sysUserMp.getAvatar() != null && !sysUserMp.getAvatar().isEmpty()) {
            wrapper.like("avatar", sysUserMp.getAvatar());
        }
        if (sysUserMp.getPassword() != null && !sysUserMp.getPassword().isEmpty()) {
            wrapper.like("password", sysUserMp.getPassword());
        }
        if (sysUserMp.getStatus() != null) {
            wrapper.like("status", sysUserMp.getStatus());
        }
        if (sysUserMp.getDelFlag() != null) {
            wrapper.like("del_flag", sysUserMp.getDelFlag());
        }
        if (sysUserMp.getLoginIp() != null && !sysUserMp.getLoginIp().isEmpty()) {
            wrapper.like("login_ip", sysUserMp.getLoginIp());
        }
        if (sysUserMp.getLoginDate() != null) {
            wrapper.like("login_date", sysUserMp.getLoginDate());
        }
        if (sysUserMp.getPwdUpdateDate() != null) {
            wrapper.like("pwd_update_date", sysUserMp.getPwdUpdateDate());
        }
        if (sysUserMp.getCreateBy() != null && !sysUserMp.getCreateBy().isEmpty()) {
            wrapper.like("create_by", sysUserMp.getCreateBy());
        }
        if (sysUserMp.getCreateTime() != null) {
            wrapper.like("create_time", sysUserMp.getCreateTime());
        }
        if (sysUserMp.getUpdateBy() != null && !sysUserMp.getUpdateBy().isEmpty()) {
            wrapper.like("update_by", sysUserMp.getUpdateBy());
        }
        if (sysUserMp.getUpdateTime() != null) {
            wrapper.like("update_time", sysUserMp.getUpdateTime());
        }
        if (sysUserMp.getRemark() != null && !sysUserMp.getRemark().isEmpty()) {
            wrapper.like("remark", sysUserMp.getRemark());
        }

        return this.page(page, wrapper);
    }

}
