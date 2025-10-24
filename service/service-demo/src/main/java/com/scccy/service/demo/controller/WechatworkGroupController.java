package com.scccy.service.demo.controller;

import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scccy.service.demo.dao.service.WechatworkGroupMpService;
import com.scccy.service.demo.domain.mp.WechatworkGroupMp;
import com.scccy.common.modules.dto.ResultData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import com.scccy.service.demo.domain.jpa.WechatworkGroupJpa;
import com.scccy.service.demo.domain.jpa.WechatworkGroupIdJpa;
import com.scccy.service.demo.dao.repository.WechatworkGroupRepository;

/**
 * 企微用户群关联表 控制器（联合主键，使用 JPA Repository 进行部分 CRUD；分页/列表使用 MyBatis-Plus）
 *
 * @author scccy
 * @date 2025-10-22 17:26:01
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
     * 根据复合主键逻辑删除（请求使用 Mp 实体，或只需在 body 中带主键字段）
     */
    @DeleteMapping
    public ResultData<?> delete(@RequestBody WechatworkGroupMp mp) {
        if (mp == null) {
            return ResultData.fail("请求体不能为空");
        }

        // 构造 LambdaQueryWrapper
        LambdaQueryWrapper<WechatworkGroupMp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WechatworkGroupMp::getWechatworkGroupId, mp.getWechatworkGroupId());
        wrapper.eq(WechatworkGroupMp::getWechatworkExternalUserId, mp.getWechatworkExternalUserId());

        boolean success = wechatworkGroupMpServiceImpl.remove(wrapper); // 逻辑删除
        if (success) {
            return ResultData.ok("删除成功");
        } else {
            return ResultData.fail("删除失败或记录不存在");
        }
    }

    /**
     * 批量删除（JPA）—— 接收 Mp 实体列表（从每个 Mp 中抽取主键并删除）
     */
    @DeleteMapping("/batch")
    public ResultData<?> deleteBatch(@RequestBody List<WechatworkGroupMp> mps) {
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
     * 分页查询（支持所有字段的 like 查询）
     */
    @PostMapping("/pageLike")
    public ResultData<Page<WechatworkGroupMp>> pageLike(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            WechatworkGroupMp wechatworkGroupMp) {

        // 分页逻辑已移至 ServiceImpl，控制器仅负责参数传递
        Page<WechatworkGroupMp> resultPage = wechatworkGroupMpServiceImpl.pageLike(pageNum, pageSize, wechatworkGroupMp);
        return ResultData.ok(resultPage);
    }

    /**
     * 列表查询（等值）
     */
    @PostMapping("/pageEq")
    public ResultData<Page<WechatworkGroupMp>> pageEq(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            WechatworkGroupMp wechatworkGroupMp) {
        // 分页逻辑已移至 ServiceImpl，控制器仅负责参数传递
        Page<WechatworkGroupMp> resultPage = wechatworkGroupMpServiceImpl.pageEq(pageNum, pageSize, wechatworkGroupMp);
        return ResultData.ok(resultPage);
    }

    /**
     * 获取所有数据（无条件）
     */
    @GetMapping("/all")
    public ResultData<List<WechatworkGroupMp>> all() {
        List<WechatworkGroupMp> list = wechatworkGroupMpServiceImpl.list();
        return ResultData.ok(list);
    }
}

