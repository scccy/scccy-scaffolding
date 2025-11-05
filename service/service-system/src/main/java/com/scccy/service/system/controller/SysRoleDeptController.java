package com.scccy.service.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scccy.service.system.dao.service.SysRoleDeptMpService;
import com.scccy.service.system.domain.mp.SysRoleDeptMp;
import com.scccy.common.modules.dto.ResultData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import com.scccy.service.system.domain.jpa.SysRoleDeptJpa;
import com.scccy.service.system.domain.jpa.SysRoleDeptIdJpa;
import com.scccy.service.system.dao.repository.SysRoleDeptRepository;

/**
 * 角色和部门关联表 控制器（联合主键，使用 JPA Repository 进行部分 CRUD；分页/列表使用 MyBatis-Plus）
 *
 * @author scccy
 * @date 2025-11-05 17:55:13
 */
@RestController
@RequestMapping("/sysRoleDept")
public class SysRoleDeptController {

    @Autowired
    private SysRoleDeptRepository sysRoleDeptRepository;

    @Autowired
    private SysRoleDeptMpService sysRoleDeptMpServiceImpl;

    /**
     * 新增（请求使用 Mp 实体 -> 转换为 JPA 实体后保存）
     */
    @PostMapping
    public ResultData<?> save(@RequestBody SysRoleDeptMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空");
        // 构造复合主键对象
        SysRoleDeptIdJpa id = new SysRoleDeptIdJpa();
        id.setRoleId(mp.getRoleId());
        id.setDeptId(mp.getDeptId());

        // 构造 JPA 实体并拷贝非主键字段
        SysRoleDeptJpa entity = new SysRoleDeptJpa();
        entity.setId(id);

        SysRoleDeptJpa saved = sysRoleDeptRepository.save(entity);
        return ResultData.ok(saved);
    }

    /**
     * 根据复合主键逻辑删除（请求使用 Mp 实体，或只需在 body 中带主键字段）
     */
    @DeleteMapping
    public ResultData<?> delete(@RequestBody SysRoleDeptMp mp) {
        if (mp == null) {
            return ResultData.fail("请求体不能为空");
        }

        // 构造 LambdaQueryWrapper
        LambdaQueryWrapper<SysRoleDeptMp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleDeptMp::getRoleId, mp.getRoleId());
        wrapper.eq(SysRoleDeptMp::getDeptId, mp.getDeptId());

        boolean success = sysRoleDeptMpServiceImpl.remove(wrapper); // 逻辑删除
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
    public ResultData<?> deleteBatch(@RequestBody List<SysRoleDeptMp> mps) {
        if (mps == null || mps.isEmpty()) return ResultData.fail("主键列表为空");
        mps.forEach(mp -> {
            SysRoleDeptIdJpa id = new SysRoleDeptIdJpa();
            id.setRoleId(mp.getRoleId());
            id.setDeptId(mp.getDeptId());
            sysRoleDeptRepository.deleteById(id);
        });
        return ResultData.ok("批量删除成功");
    }

    /**
     * 修改（请求使用 Mp 实体 -> 转换为 JPA 实体后保存）
     */
    @PutMapping
    public ResultData<?> update(@RequestBody SysRoleDeptMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空");
        SysRoleDeptIdJpa id = new SysRoleDeptIdJpa();
        id.setRoleId(mp.getRoleId());
        id.setDeptId(mp.getDeptId());

        SysRoleDeptJpa entity = new SysRoleDeptJpa();
        entity.setId(id);

        SysRoleDeptJpa saved = sysRoleDeptRepository.save(entity);
        return ResultData.ok(saved);
    }

    /**
     * 根据复合主键查询（请求使用 Mp 实体或只在 body 中带主键字段）
     */
    @PostMapping("/getById")
    public ResultData<SysRoleDeptJpa> getById(@RequestBody SysRoleDeptMp mp) {
        if (mp == null) return ResultData.fail("请求体不能为空");
        SysRoleDeptIdJpa id = new SysRoleDeptIdJpa();
        id.setRoleId(mp.getRoleId());
        id.setDeptId(mp.getDeptId());
        return sysRoleDeptRepository.findById(id)
                .map(ResultData::ok)
                .orElseGet(() -> ResultData.fail("未查询到数据"));
    }

    /**
     * 分页查询（支持所有字段的 like 查询）
     */
    @PostMapping("/pageLike")
    public ResultData<Page<SysRoleDeptMp>> pageLike(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            SysRoleDeptMp sysRoleDeptMp) {

        // 分页逻辑已移至 ServiceImpl，控制器仅负责参数传递
        Page<SysRoleDeptMp> resultPage = sysRoleDeptMpServiceImpl.pageLike(pageNum, pageSize, sysRoleDeptMp);
        return ResultData.ok(resultPage);
    }

    /**
     * 列表查询（等值）
     */
    @PostMapping("/pageEq")
    public ResultData<Page<SysRoleDeptMp>> pageEq(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            SysRoleDeptMp sysRoleDeptMp) {
        // 分页逻辑已移至 ServiceImpl，控制器仅负责参数传递
        Page<SysRoleDeptMp> resultPage = sysRoleDeptMpServiceImpl.pageEq(pageNum, pageSize, sysRoleDeptMp);
        return ResultData.ok(resultPage);
    }

    /**
     * 获取所有数据（无条件）
     */
    @GetMapping("/all")
    public ResultData<List<SysRoleDeptMp>> all() {
        List<SysRoleDeptMp> list = sysRoleDeptMpServiceImpl.list();
        return ResultData.ok(list);
    }
}

