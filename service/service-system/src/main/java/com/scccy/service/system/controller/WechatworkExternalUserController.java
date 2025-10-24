package com.scccy.service.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.system.dao.repository.WechatworkExternalUserRepository;
import com.scccy.service.system.domain.jpa.WechatworkExternalUserIdJpa;
import com.scccy.service.system.domain.jpa.WechatworkExternalUserJpa;
import com.scccy.service.system.domain.mp.WechatworkExternalUserMp;
import com.scccy.service.system.dao.mp.WechatworkExternalUserMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友关系 控制器（联合主键，使用 JPA Repository 进行部分 CRUD；分页/列表使用 MyBatis-Plus）
 *
 * @author scccy
 * @date 2025-10-22 16:37:01
 */
@RestController
@RequestMapping("/wechatworkExternalUser" )
public class WechatworkExternalUserController {

    @Autowired
    private WechatworkExternalUserRepository wechatworkExternalUserRepository;

    @Autowired
    private WechatworkExternalUserMpService wechatworkExternalUserMpServiceImpl;

    /**
     * 新增（请求使用 Mp 实体 -> 转换为 JPA 实体后保存）
     */
    @PostMapping
    public ResultData<?> save(@RequestBody WechatworkExternalUserMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空" );
        // 构造复合主键对象
        WechatworkExternalUserIdJpa id = new WechatworkExternalUserIdJpa();
        id.setWechatworkUserId(mp.getWechatworkUserId());
        id.setWechatworkExternalUserid(mp.getWechatworkExternalUserid());

        // 构造 JPA 实体并拷贝非主键字段
        WechatworkExternalUserJpa entity = new WechatworkExternalUserJpa();
        entity.setId(id);
        entity.setWechatworkUnionId(mp.getWechatworkUnionId());
        entity.setUserId(mp.getUserId());
        entity.setStatus(mp.getStatus());
        entity.setCreateTime(mp.getCreateTime());
        entity.setUpdateTime(mp.getUpdateTime());
        entity.setCreateBy(mp.getCreateBy());
        entity.setUpdateBy(mp.getUpdateBy());
        entity.setDelFlag(mp.getDelFlag());

        WechatworkExternalUserJpa saved = wechatworkExternalUserRepository.save(entity);
        return ResultData.ok(saved);
    }

    /**
     * 根据复合主键逻辑删除（请求使用 Mp 实体，或只需在 body 中带主键字段）
     */
    @DeleteMapping
    public ResultData<?> delete(@RequestBody WechatworkExternalUserMp mp) {
        if (mp == null) {
            return ResultData.fail("请求体不能为空" );
        }

        // 构造 LambdaQueryWrapper
        LambdaQueryWrapper<WechatworkExternalUserMp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WechatworkExternalUserMp::getWechatworkUserId, mp.getWechatworkUserId());
        wrapper.eq(WechatworkExternalUserMp::getWechatworkExternalUserid, mp.getWechatworkExternalUserid());

        boolean success = wechatworkExternalUserMpServiceImpl.remove(wrapper); // 逻辑删除
        if (success) {
            return ResultData.ok("删除成功" );
        } else {
            return ResultData.fail("删除失败或记录不存在" );
        }
    }

    /**
     * 批量删除（JPA）—— 接收 Mp 实体列表（从每个 Mp 中抽取主键并删除）
     */
    @DeleteMapping("/batch" )
    public ResultData<?> deleteBatch(@RequestBody List<WechatworkExternalUserMp> mps) {
        if (mps == null || mps.isEmpty()) return ResultData.fail("主键列表为空" );
        mps.forEach(mp -> {
            WechatworkExternalUserIdJpa id = new WechatworkExternalUserIdJpa();
            id.setWechatworkUserId(mp.getWechatworkUserId());
            id.setWechatworkExternalUserid(mp.getWechatworkExternalUserid());
            wechatworkExternalUserRepository.deleteById(id);
        });
        return ResultData.ok("批量删除成功" );
    }

    /**
     * 修改（请求使用 Mp 实体 -> 转换为 JPA 实体后保存）
     */
    @PutMapping
    public ResultData<?> update(@RequestBody WechatworkExternalUserMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空" );
        WechatworkExternalUserIdJpa id = new WechatworkExternalUserIdJpa();
        id.setWechatworkUserId(mp.getWechatworkUserId());
        id.setWechatworkExternalUserid(mp.getWechatworkExternalUserid());

        WechatworkExternalUserJpa entity = new WechatworkExternalUserJpa();
        entity.setId(id);
        entity.setWechatworkUnionId(mp.getWechatworkUnionId());
        entity.setUserId(mp.getUserId());
        entity.setStatus(mp.getStatus());
        entity.setCreateTime(mp.getCreateTime());
        entity.setUpdateTime(mp.getUpdateTime());
        entity.setCreateBy(mp.getCreateBy());
        entity.setUpdateBy(mp.getUpdateBy());
        entity.setDelFlag(mp.getDelFlag());

        WechatworkExternalUserJpa saved = wechatworkExternalUserRepository.save(entity);
        return ResultData.ok(saved);
    }

    /**
     * 根据复合主键查询（请求使用 Mp 实体或只在 body 中带主键字段）
     */
    @PostMapping("/getById" )
    public ResultData<WechatworkExternalUserJpa> getById(@RequestBody WechatworkExternalUserMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空" );
        WechatworkExternalUserIdJpa id = new WechatworkExternalUserIdJpa();
        id.setWechatworkUserId(mp.getWechatworkUserId());
        id.setWechatworkExternalUserid(mp.getWechatworkExternalUserid());
        return wechatworkExternalUserRepository.findById(id)
                .map(ResultData::ok)
                .orElseGet(() -> ResultData.fail("未查询到数据" ));
    }

    /**
     * 分页查询（支持所有字段的 like 查询）
     */
    @PostMapping("/pageLike" )
    public ResultData<Page<WechatworkExternalUserMp>> pageLike(
            @RequestParam(defaultValue = "1" ) Integer pageNum,
            @RequestParam(defaultValue = "10" ) Integer pageSize,
            WechatworkExternalUserMp wechatworkExternalUserMp) {

        // 分页逻辑已移至 ServiceImpl，控制器仅负责参数传递
        Page<WechatworkExternalUserMp> resultPage = wechatworkExternalUserMpServiceImpl.pageLike(pageNum, pageSize, wechatworkExternalUserMp);
        return ResultData.ok(resultPage);
    }

    /**
     * 列表查询（等值）
     */
    @PostMapping("/pageEq" )
    public ResultData<Page<WechatworkExternalUserMp>> pageEq(
            @RequestParam(defaultValue = "1" ) Integer pageNum,
            @RequestParam(defaultValue = "10" ) Integer pageSize,
            WechatworkExternalUserMp wechatworkExternalUserMp) {
        // 分页逻辑已移至 ServiceImpl，控制器仅负责参数传递
        Page<WechatworkExternalUserMp> resultPage = wechatworkExternalUserMpServiceImpl.pageEq(pageNum, pageSize, wechatworkExternalUserMp);
        return ResultData.ok(resultPage);
    }

    /**
     * 获取所有数据（无条件）
     */
    @GetMapping("/all" )
    public ResultData<List<WechatworkExternalUserMp>> all() {
        List<WechatworkExternalUserMp> list = wechatworkExternalUserMpServiceImpl.list();
        return ResultData.ok(list);
    }
}

