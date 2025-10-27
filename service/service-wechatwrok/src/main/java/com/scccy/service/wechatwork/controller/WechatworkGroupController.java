package com.scccy.service.wechatwork.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.wechatwork.dao.repository.WechatworkGroupRepository;
import com.scccy.service.wechatwork.dao.service.WechatworkGroupMpService;
import com.scccy.service.wechatwork.domain.jpa.WechatworkGroupIdJpa;
import com.scccy.service.wechatwork.domain.jpa.WechatworkGroupJpa;
import com.scccy.service.wechatwork.domain.mp.WechatworkGroupMp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 企微用户群关联表 控制器（联合主键，使用 JPA Repository 进行部分 CRUD；分页/列表使用 MyBatis-Plus）
 *
 * @author scccy
 * @date 2025-10-24 18:09:45
 */
@RestController
@RequestMapping("/wechatworkGroup")
public class WechatworkGroupController {

    @Autowired
    private WechatworkGroupRepository wechatworkGroupRepository;

    @Autowired
    private WechatworkGroupMpService wechatworkGroupMpServiceImpl;

    /**
     * 新增（请求使用 Mp 实体 -> 转换为 JPA 实体后保存）
     */
    @PostMapping
    public ResultData<?> save(@RequestBody WechatworkGroupMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空");
        // 构造复合主键对象
        WechatworkGroupIdJpa id = new WechatworkGroupIdJpa();
        id.setWechatworkGroupId(mp.getWechatworkGroupId());
        id.setWechatworkExternalUserId(mp.getWechatworkExternalUserId());

        // 构造 JPA 实体并拷贝非主键字段
        WechatworkGroupJpa entity = new WechatworkGroupJpa();
        entity.setId(id);
        entity.setWechatworkUserId(mp.getWechatworkUserId());
        entity.setWechatworkExternalUnionId(mp.getWechatworkExternalUnionId());
        entity.setStatus(mp.getStatus());
        entity.setCreateTime(mp.getCreateTime());
        entity.setUpdateTime(mp.getUpdateTime());
        entity.setCreateBy(mp.getCreateBy());
        entity.setUpdateBy(mp.getUpdateBy());
        entity.setDelFlag(mp.getDelFlag());

        WechatworkGroupJpa saved = wechatworkGroupRepository.save(entity);
        return ResultData.ok(saved);
    }

    /**
     * 根据复合主键删除（请求使用 Mp 实体，或只需在 body 中带主键字段）
     */
    @DeleteMapping
    public ResultData<?> delete(@RequestBody WechatworkGroupMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空");
        WechatworkGroupIdJpa id = new WechatworkGroupIdJpa();
        id.setWechatworkGroupId(mp.getWechatworkGroupId());
        id.setWechatworkExternalUserId(mp.getWechatworkExternalUserId());
        wechatworkGroupRepository.deleteById(id);
        return ResultData.ok("删除成功");
    }

    /**
     * 批量删除（JPA）—— 接收 Mp 实体列表（从每个 Mp 中抽取主键并删除）
     */
    @DeleteMapping("/batch")
    public ResultData<?> deleteBatch(@RequestBody java.util.List<WechatworkGroupMp> mps) {
        if (mps == null || mps.isEmpty()) return ResultData.fail("主键列表为空");
        mps.forEach(mp -> {
            WechatworkGroupIdJpa id = new WechatworkGroupIdJpa();
            id.setWechatworkGroupId(mp.getWechatworkGroupId());
            id.setWechatworkExternalUserId(mp.getWechatworkExternalUserId());
            wechatworkGroupRepository.deleteById(id);
        });
        return ResultData.ok("批量删除成功");
    }

    /**
     * 修改（请求使用 Mp 实体 -> 转换为 JPA 实体后保存）
     */
    @PutMapping
    public ResultData<?> update(@RequestBody WechatworkGroupMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空");
        WechatworkGroupIdJpa id = new WechatworkGroupIdJpa();
        id.setWechatworkGroupId(mp.getWechatworkGroupId());
        id.setWechatworkExternalUserId(mp.getWechatworkExternalUserId());

        WechatworkGroupJpa entity = new WechatworkGroupJpa();
        entity.setId(id);
        entity.setWechatworkUserId(mp.getWechatworkUserId());
        entity.setWechatworkExternalUnionId(mp.getWechatworkExternalUnionId());
        entity.setStatus(mp.getStatus());
        entity.setCreateTime(mp.getCreateTime());
        entity.setUpdateTime(mp.getUpdateTime());
        entity.setCreateBy(mp.getCreateBy());
        entity.setUpdateBy(mp.getUpdateBy());
        entity.setDelFlag(mp.getDelFlag());

        WechatworkGroupJpa saved = wechatworkGroupRepository.save(entity);
        return ResultData.ok(saved);
    }

    /**
     * 根据复合主键查询（请求使用 Mp 实体或只在 body 中带主键字段）
     */
    @PostMapping("/getById")
    public ResultData<WechatworkGroupJpa> getById(@RequestBody WechatworkGroupMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空");
        WechatworkGroupIdJpa id = new WechatworkGroupIdJpa();
        id.setWechatworkGroupId(mp.getWechatworkGroupId());
        id.setWechatworkExternalUserId(mp.getWechatworkExternalUserId());
        return wechatworkGroupRepository.findById(id)
                .map(ResultData::ok)
                .orElseGet(() -> ResultData.fail("未查询到数据"));
    }

    /**
     * 分页查询（MyBatis-Plus）—— 支持所有字段的 like 查询（字符串）和 eq（非字符串）
     */
    @GetMapping("/page")
    public ResultData<Page<WechatworkGroupMp>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            WechatworkGroupMp wechatworkGroupMp) {

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
            wrapper.eq("status", wechatworkGroupMp.getStatus());
        }
        if (wechatworkGroupMp.getCreateTime() != null) {
            wrapper.eq("create_time", wechatworkGroupMp.getCreateTime());
        }
        if (wechatworkGroupMp.getUpdateTime() != null) {
            wrapper.eq("update_time", wechatworkGroupMp.getUpdateTime());
        }
        if (wechatworkGroupMp.getCreateBy() != null && !wechatworkGroupMp.getCreateBy().isEmpty()) {
            wrapper.like("create_by", wechatworkGroupMp.getCreateBy());
        }
        if (wechatworkGroupMp.getUpdateBy() != null && !wechatworkGroupMp.getUpdateBy().isEmpty()) {
            wrapper.like("update_by", wechatworkGroupMp.getUpdateBy());
        }
        if (wechatworkGroupMp.getDelFlag() != null) {
            wrapper.eq("del_flag", wechatworkGroupMp.getDelFlag());
        }

        Page<WechatworkGroupMp> resultPage = wechatworkGroupMpServiceImpl.page(page, wrapper);
        return ResultData.ok(resultPage);
    }

    /**
     * 列表查询（MyBatis-Plus）
     */
    @GetMapping("/list")
    public ResultData<java.util.List<WechatworkGroupMp>> list(WechatworkGroupMp wechatworkGroupMp) {
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
            wrapper.eq("status", wechatworkGroupMp.getStatus());
        }
        if (wechatworkGroupMp.getCreateTime() != null) {
            wrapper.eq("create_time", wechatworkGroupMp.getCreateTime());
        }
        if (wechatworkGroupMp.getUpdateTime() != null) {
            wrapper.eq("update_time", wechatworkGroupMp.getUpdateTime());
        }
        if (wechatworkGroupMp.getCreateBy() != null && !wechatworkGroupMp.getCreateBy().isEmpty()) {
            wrapper.like("create_by", wechatworkGroupMp.getCreateBy());
        }
        if (wechatworkGroupMp.getUpdateBy() != null && !wechatworkGroupMp.getUpdateBy().isEmpty()) {
            wrapper.like("update_by", wechatworkGroupMp.getUpdateBy());
        }
        if (wechatworkGroupMp.getDelFlag() != null) {
            wrapper.eq("del_flag", wechatworkGroupMp.getDelFlag());
        }

        java.util.List<WechatworkGroupMp> list = wechatworkGroupMpServiceImpl.list(wrapper);
        return ResultData.ok(list);
    }

    /**
     * 获取所有数据（无条件）
     */
    @GetMapping("/all")
    public ResultData<java.util.List<WechatworkGroupMp>> all() {
        java.util.List<WechatworkGroupMp> list = wechatworkGroupMpServiceImpl.list();
        return ResultData.ok(list);
    }
}

