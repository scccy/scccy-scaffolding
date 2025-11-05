package com.scccy.service.system.dao.mp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.system.dao.mapper.WechatworkExternalUserMapper;
import com.scccy.service.system.dao.mp.WechatworkExternalUserMpService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 好友关系(WechatworkExternalUser)服务实现类
 *
 * @author scccy
 * @since 2025-10-22 16:37:01
 */
@Service
public class WechatworkExternalUserMpServiceImpl
        extends ServiceImpl<WechatworkExternalUserMapper, WechatworkExternalUserMp>
        implements WechatworkExternalUserMpService {
    @Override
    public Page<WechatworkExternalUserMp> pageEq(Integer pageNum, Integer pageSize, WechatworkExternalUserMp wechatworkExternalUserMp) {
        Page<WechatworkExternalUserMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<WechatworkExternalUserMp> wrapper = new QueryWrapper<>();

        if (wechatworkExternalUserMp.getWechatworkUserId() != null && !wechatworkExternalUserMp.getWechatworkUserId().isEmpty()) {
            wrapper.eq("wechatwork_user_id" , wechatworkExternalUserMp.getWechatworkUserId());
        }
        if (wechatworkExternalUserMp.getWechatworkUnionId() != null && !wechatworkExternalUserMp.getWechatworkUnionId().isEmpty()) {
            wrapper.eq("wechatwork_union_id" , wechatworkExternalUserMp.getWechatworkUnionId());
        }
        if (wechatworkExternalUserMp.getWechatworkExternalUserid() != null && !wechatworkExternalUserMp.getWechatworkExternalUserid().isEmpty()) {
            wrapper.eq("wechatwork_external_userid" , wechatworkExternalUserMp.getWechatworkExternalUserid());
        }
        if (wechatworkExternalUserMp.getUserId() != null && !wechatworkExternalUserMp.getUserId().isEmpty()) {
            wrapper.eq("user_id" , wechatworkExternalUserMp.getUserId());
        }
        if (wechatworkExternalUserMp.getStatus() != null) {
            wrapper.eq("status" , wechatworkExternalUserMp.getStatus());
        }
        if (wechatworkExternalUserMp.getCreateTime() != null) {
            wrapper.eq("create_time" , wechatworkExternalUserMp.getCreateTime());
        }
        if (wechatworkExternalUserMp.getUpdateTime() != null) {
            wrapper.eq("update_time" , wechatworkExternalUserMp.getUpdateTime());
        }
        if (wechatworkExternalUserMp.getCreateBy() != null && !wechatworkExternalUserMp.getCreateBy().isEmpty()) {
            wrapper.eq("create_by" , wechatworkExternalUserMp.getCreateBy());
        }
        if (wechatworkExternalUserMp.getUpdateBy() != null && !wechatworkExternalUserMp.getUpdateBy().isEmpty()) {
            wrapper.eq("update_by" , wechatworkExternalUserMp.getUpdateBy());
        }
        if (wechatworkExternalUserMp.getDelFlag() != null) {
            wrapper.eq("del_flag" , wechatworkExternalUserMp.getDelFlag());
        }

        return this.page(page, wrapper);
    }


    @Override
    public Page<WechatworkExternalUserMp> pageLike(Integer pageNum, Integer pageSize, WechatworkExternalUserMp wechatworkExternalUserMp) {
        Page<WechatworkExternalUserMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<WechatworkExternalUserMp> wrapper = new QueryWrapper<>();

        if (wechatworkExternalUserMp.getWechatworkUserId() != null && !wechatworkExternalUserMp.getWechatworkUserId().isEmpty()) {
            wrapper.like("wechatwork_user_id" , wechatworkExternalUserMp.getWechatworkUserId());
        }
        if (wechatworkExternalUserMp.getWechatworkUnionId() != null && !wechatworkExternalUserMp.getWechatworkUnionId().isEmpty()) {
            wrapper.like("wechatwork_union_id" , wechatworkExternalUserMp.getWechatworkUnionId());
        }
        if (wechatworkExternalUserMp.getWechatworkExternalUserid() != null && !wechatworkExternalUserMp.getWechatworkExternalUserid().isEmpty()) {
            wrapper.like("wechatwork_external_userid" , wechatworkExternalUserMp.getWechatworkExternalUserid());
        }
        if (wechatworkExternalUserMp.getUserId() != null && !wechatworkExternalUserMp.getUserId().isEmpty()) {
            wrapper.like("user_id" , wechatworkExternalUserMp.getUserId());
        }
        if (wechatworkExternalUserMp.getStatus() != null) {
            wrapper.like("status" , wechatworkExternalUserMp.getStatus());
        }
        if (wechatworkExternalUserMp.getCreateTime() != null) {
            wrapper.like("create_time" , wechatworkExternalUserMp.getCreateTime());
        }
        if (wechatworkExternalUserMp.getUpdateTime() != null) {
            wrapper.like("update_time" , wechatworkExternalUserMp.getUpdateTime());
        }
        if (wechatworkExternalUserMp.getCreateBy() != null && !wechatworkExternalUserMp.getCreateBy().isEmpty()) {
            wrapper.like("create_by" , wechatworkExternalUserMp.getCreateBy());
        }
        if (wechatworkExternalUserMp.getUpdateBy() != null && !wechatworkExternalUserMp.getUpdateBy().isEmpty()) {
            wrapper.like("update_by" , wechatworkExternalUserMp.getUpdateBy());
        }
        if (wechatworkExternalUserMp.getDelFlag() != null) {
            wrapper.like("del_flag" , wechatworkExternalUserMp.getDelFlag());
        }

        return this.page(page, wrapper);
    }

}
