package com.scccy.service.jackyun.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.jackyun.dao.mapper.WechatworkGroupMapper;
import com.scccy.service.jackyun.domain.mp.WechatworkGroupMp;
import com.scccy.service.jackyun.dao.service.WechatworkGroupMpService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 企微用户群关联表(WechatworkGroup)服务实现类
 *
 * @author scccy
 * @since 2025-10-22 17:26:01
 */
@Service
public class WechatworkGroupMpServiceImpl
        extends ServiceImpl<WechatworkGroupMapper, WechatworkGroupMp>
        implements WechatworkGroupMpService {
    @Override
    public Page<WechatworkGroupMp> pageEq(Integer pageNum, Integer pageSize, WechatworkGroupMp wechatworkGroupMp) {
        Page<WechatworkGroupMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<WechatworkGroupMp> wrapper = new QueryWrapper<>();

        if (wechatworkGroupMp.getWechatworkUserId() != null && !wechatworkGroupMp.getWechatworkUserId().isEmpty()) {
            wrapper.eq("wechatwork_user_id", wechatworkGroupMp.getWechatworkUserId());
        }
        if (wechatworkGroupMp.getWechatworkGroupId() != null && !wechatworkGroupMp.getWechatworkGroupId().isEmpty()) {
            wrapper.eq("wechatwork_group_id", wechatworkGroupMp.getWechatworkGroupId());
        }
        if (wechatworkGroupMp.getWechatworkExternalUnionId() != null && !wechatworkGroupMp.getWechatworkExternalUnionId().isEmpty()) {
            wrapper.eq("wechatwork_external_union_id", wechatworkGroupMp.getWechatworkExternalUnionId());
        }
        if (wechatworkGroupMp.getWechatworkExternalUserId() != null && !wechatworkGroupMp.getWechatworkExternalUserId().isEmpty()) {
            wrapper.eq("wechatwork_external_user_id", wechatworkGroupMp.getWechatworkExternalUserId());
        }
        if (wechatworkGroupMp.getStatus() != null) {
            wrapper.eq("status", wechatworkGroupMp.getStatus());
        }
        if (wechatworkGroupMp.getCreateTime() != null) {
            wrapper.eq("create_time", wechatworkGroupMp.getCreateTime());
        }
        if (wechatworkGroupMp.getUpdateTime() != null) {
            wrapper.eq("update_time", wechatworkGroupMp.getUpdateTime());
        }
        if (wechatworkGroupMp.getCreateBy() != null && !wechatworkGroupMp.getCreateBy().isEmpty()) {
            wrapper.eq("create_by", wechatworkGroupMp.getCreateBy());
        }
        if (wechatworkGroupMp.getUpdateBy() != null && !wechatworkGroupMp.getUpdateBy().isEmpty()) {
            wrapper.eq("update_by", wechatworkGroupMp.getUpdateBy());
        }
        if (wechatworkGroupMp.getDelFlag() != null) {
            wrapper.eq("del_flag", wechatworkGroupMp.getDelFlag());
        }

        return this.page(page, wrapper);
    }


    @Override
    public Page<WechatworkGroupMp> pageLike(Integer pageNum, Integer pageSize, WechatworkGroupMp wechatworkGroupMp) {
        Page<WechatworkGroupMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<WechatworkGroupMp> wrapper = new QueryWrapper<>();

        if (wechatworkGroupMp.getWechatworkUserId() != null && !wechatworkGroupMp.getWechatworkUserId().isEmpty()) {
            wrapper.like("wechatwork_user_id", wechatworkGroupMp.getWechatworkUserId());
        }
        if (wechatworkGroupMp.getWechatworkGroupId() != null && !wechatworkGroupMp.getWechatworkGroupId().isEmpty()) {
            wrapper.like("wechatwork_group_id", wechatworkGroupMp.getWechatworkGroupId());
        }
        if (wechatworkGroupMp.getWechatworkExternalUnionId() != null && !wechatworkGroupMp.getWechatworkExternalUnionId().isEmpty()) {
            wrapper.like("wechatwork_external_union_id", wechatworkGroupMp.getWechatworkExternalUnionId());
        }
        if (wechatworkGroupMp.getWechatworkExternalUserId() != null && !wechatworkGroupMp.getWechatworkExternalUserId().isEmpty()) {
            wrapper.like("wechatwork_external_user_id", wechatworkGroupMp.getWechatworkExternalUserId());
        }
        if (wechatworkGroupMp.getStatus() != null) {
            wrapper.like("status", wechatworkGroupMp.getStatus());
        }
        if (wechatworkGroupMp.getCreateTime() != null) {
            wrapper.like("create_time", wechatworkGroupMp.getCreateTime());
        }
        if (wechatworkGroupMp.getUpdateTime() != null) {
            wrapper.like("update_time", wechatworkGroupMp.getUpdateTime());
        }
        if (wechatworkGroupMp.getCreateBy() != null && !wechatworkGroupMp.getCreateBy().isEmpty()) {
            wrapper.like("create_by", wechatworkGroupMp.getCreateBy());
        }
        if (wechatworkGroupMp.getUpdateBy() != null && !wechatworkGroupMp.getUpdateBy().isEmpty()) {
            wrapper.like("update_by", wechatworkGroupMp.getUpdateBy());
        }
        if (wechatworkGroupMp.getDelFlag() != null) {
            wrapper.like("del_flag", wechatworkGroupMp.getDelFlag());
        }

        return this.page(page, wrapper);
    }

}
