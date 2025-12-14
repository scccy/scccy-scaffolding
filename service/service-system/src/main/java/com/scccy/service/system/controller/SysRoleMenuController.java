package com.scccy.service.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scccy.service.system.dao.service.SysRoleMenuMpService;
import com.scccy.service.system.domain.mp.SysRoleMenuMp;
import com.scccy.common.modules.dto.ResultData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import com.scccy.service.system.domain.jpa.SysRoleMenuJpa;
import com.scccy.service.system.domain.jpa.SysRoleMenuIdJpa;
import com.scccy.service.system.dao.repository.SysRoleMenuRepository;

/**
 * 角色和菜单关联表 控制器（联合主键，使用 JPA Repository 进行部分 CRUD；分页/列表使用 MyBatis-Plus）
 *
 * @author scccy
 * @date 2025-11-05 17:55:14
 */
@RestController
@RequestMapping("/sysRoleMenu")
public class SysRoleMenuController {

    @Autowired
    private SysRoleMenuRepository sysRoleMenuRepository;

    @Autowired
    private SysRoleMenuMpService sysRoleMenuMpServiceImpl;

    /**
     * 新增（请求使用 Mp 实体 -> 转换为 JPA 实体后保存）
     */
    @PostMapping
    public ResultData<?> save(@RequestBody SysRoleMenuMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空");
        // 构造复合主键对象
        SysRoleMenuIdJpa id = new SysRoleMenuIdJpa();
        id.setRoleId(mp.getRoleId());
        id.setMenuId(mp.getMenuId());

        // 构造 JPA 实体并拷贝非主键字段
        SysRoleMenuJpa entity = new SysRoleMenuJpa();
        entity.setId(id);

        SysRoleMenuJpa saved = sysRoleMenuRepository.save(entity);
        return ResultData.ok(saved);
    }

    /**
     * 根据复合主键逻辑删除（请求使用 Mp 实体，或只需在 body 中带主键字段）
     */
    @DeleteMapping
    public ResultData<?> delete(@RequestBody SysRoleMenuMp mp) {
        if (mp == null) {
            return ResultData.fail("请求体不能为空");
        }

        // 构造 LambdaQueryWrapper
        LambdaQueryWrapper<SysRoleMenuMp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenuMp::getRoleId, mp.getRoleId());
        wrapper.eq(SysRoleMenuMp::getMenuId, mp.getMenuId());

        boolean success = sysRoleMenuMpServiceImpl.remove(wrapper); // 逻辑删除
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
    public ResultData<?> deleteBatch(@RequestBody List<SysRoleMenuMp> mps) {
        if (mps == null || mps.isEmpty()) return ResultData.fail("主键列表为空");
        mps.forEach(mp -> {
            SysRoleMenuIdJpa id = new SysRoleMenuIdJpa();
            id.setRoleId(mp.getRoleId());
            id.setMenuId(mp.getMenuId());
            sysRoleMenuRepository.deleteById(id);
        });
        return ResultData.ok("批量删除成功");
    }

    /**
     * 修改（请求使用 Mp 实体 -> 转换为 JPA 实体后保存）
     */
    @PutMapping
    public ResultData<?> update(@RequestBody SysRoleMenuMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空");
        SysRoleMenuIdJpa id = new SysRoleMenuIdJpa();
        id.setRoleId(mp.getRoleId());
        id.setMenuId(mp.getMenuId());

        SysRoleMenuJpa entity = new SysRoleMenuJpa();
        entity.setId(id);

        SysRoleMenuJpa saved = sysRoleMenuRepository.save(entity);
        return ResultData.ok(saved);
    }

    /**
     * 根据复合主键查询（请求使用 Mp 实体或只在 body 中带主键字段）
     */
    @PostMapping("/getById")
    public ResultData<SysRoleMenuJpa> getById(@RequestBody SysRoleMenuMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空");
        SysRoleMenuIdJpa id = new SysRoleMenuIdJpa();
        id.setRoleId(mp.getRoleId());
        id.setMenuId(mp.getMenuId());
        return sysRoleMenuRepository.findById(id)
                .map(ResultData::ok)
                .orElseGet(() -> ResultData.fail("未查询到数据"));
    }

    /**
     * 分页查询（支持所有字段的 like 查询）
     */
    @PostMapping("/pageLike")
    public ResultData<Page<SysRoleMenuMp>> pageLike(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            SysRoleMenuMp sysRoleMenuMp) {

        // 分页逻辑已移至 ServiceImpl，控制器仅负责参数传递
        Page<SysRoleMenuMp> resultPage = sysRoleMenuMpServiceImpl.pageLike(pageNum, pageSize, sysRoleMenuMp);
        return ResultData.ok(resultPage);
    }

    /**
     * 列表查询（等值）
     */
    @PostMapping("/pageEq")
    public ResultData<Page<SysRoleMenuMp>> pageEq(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            SysRoleMenuMp sysRoleMenuMp) {
        // 分页逻辑已移至 ServiceImpl，控制器仅负责参数传递
        Page<SysRoleMenuMp> resultPage = sysRoleMenuMpServiceImpl.pageEq(pageNum, pageSize, sysRoleMenuMp);
        return ResultData.ok(resultPage);
    }

    /**
     * 获取所有数据（无条件）
     */
    @GetMapping("/all")
    public ResultData<List<SysRoleMenuMp>> all() {
        List<SysRoleMenuMp> list = sysRoleMenuMpServiceImpl.list();
        return ResultData.ok(list);
    }
}

