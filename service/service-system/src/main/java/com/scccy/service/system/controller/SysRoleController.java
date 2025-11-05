package com.scccy.service.system.controller;

import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scccy.service.system.dao.service.SysRoleMpService;
import com.scccy.service.system.domain.mp.SysRoleMp;
import com.scccy.common.modules.dto.ResultData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import com.scccy.service.system.domain.mp.SysRoleMp;

/**
 * 角色信息表 控制器（单主键，使用 MyBatis-Plus Service 完整 CRUD）
 *
 * @author scccy
 * @date 2025-11-05 17:55:12
 */
@RestController
@RequestMapping("/sysRole")
public class SysRoleController {


    @Autowired
    private SysRoleMpService sysRoleMpServiceImpl;

    /**
     * 新增
     */
    @PostMapping
    public ResultData<?> save(@RequestBody SysRoleMp sysRoleMp) {
        boolean result = sysRoleMpServiceImpl.save(sysRoleMp);
        return result ? ResultData.ok("新增成功") : ResultData.fail("新增失败");
    }

    /**
     * 根据ID删除
     */
    @DeleteMapping("/{id}")
    public ResultData<?> delete(@PathVariable Long id) {
        boolean result = sysRoleMpServiceImpl.removeById(id);
        return result ? ResultData.ok("删除成功") : ResultData.fail("删除失败");
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    public ResultData<?> deleteBatch(@RequestBody List<Long> ids) {
        boolean result = sysRoleMpServiceImpl.removeByIds(ids);
        return result ? ResultData.ok("批量删除成功") : ResultData.fail("批量删除失败");
    }

    /**
     * 修改
     */
    @PutMapping
    public ResultData<?> update(@RequestBody SysRoleMp sysRoleMp) {
        boolean result = sysRoleMpServiceImpl.updateById(sysRoleMp);
        return result ? ResultData.ok("修改成功") : ResultData.fail("修改失败");
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public ResultData<SysRoleMp> getById(@PathVariable Long id) {
        SysRoleMp entity = sysRoleMpServiceImpl.getById(id);
        return ResultData.ok(entity);
    }

    /**
     * 分页查询（支持所有字段的 like 查询）
     */
    @PostMapping("/pageLike")
    public ResultData<Page<SysRoleMp>> pageLike(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            SysRoleMp sysRoleMp) {

        // 分页逻辑已移至 ServiceImpl，控制器仅负责参数传递
        Page<SysRoleMp> resultPage = sysRoleMpServiceImpl.pageLike(pageNum, pageSize, sysRoleMp);
        return ResultData.ok(resultPage);
    }

    /**
     * 列表查询（等值）
     */
    @PostMapping("/pageEq")
    public ResultData<Page<SysRoleMp>> pageEq(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            SysRoleMp sysRoleMp) {
        // 分页逻辑已移至 ServiceImpl，控制器仅负责参数传递
        Page<SysRoleMp> resultPage = sysRoleMpServiceImpl.pageEq(pageNum, pageSize, sysRoleMp);
        return ResultData.ok(resultPage);
    }

    /**
     * 获取所有数据（无条件）
     */
    @GetMapping("/all")
    public ResultData<List<SysRoleMp>> all() {
        List<SysRoleMp> list = sysRoleMpServiceImpl.list();
        return ResultData.ok(list);
    }
}

