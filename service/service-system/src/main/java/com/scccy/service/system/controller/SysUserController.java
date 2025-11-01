package com.scccy.service.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.service.system.dao.mp.SysUserMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户信息表 控制器（单主键，使用 MyBatis-Plus Service 完整 CRUD）
 *
 * @author scccy
 * @date 2025-10-22 16:27:00
 */
@RestController
@RequestMapping("/sysUser" )
public class SysUserController {


    @Autowired
    private SysUserMpService sysUserMpServiceImpl;

    /**
     * 新增
     */
    @PostMapping
    public ResultData<?> save(@RequestBody SysUserMp sysUserMp) {
        boolean result = sysUserMpServiceImpl.save(sysUserMp);
        return result ? ResultData.ok("新增成功" ) : ResultData.fail("新增失败" );
    }

    /**
     * 根据ID删除
     */
    @DeleteMapping("/{id}" )
    public ResultData<?> delete(@PathVariable Long id) {
        boolean result = sysUserMpServiceImpl.removeById(id);
        return result ? ResultData.ok("删除成功" ) : ResultData.fail("删除失败" );
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch" )
    public ResultData<?> deleteBatch(@RequestBody List<Long> ids) {
        boolean result = sysUserMpServiceImpl.removeByIds(ids);
        return result ? ResultData.ok("批量删除成功" ) : ResultData.fail("批量删除失败" );
    }

    /**
     * 修改
     */
    @PutMapping
    public ResultData<?> update(@RequestBody SysUserMp sysUserMp) {
        boolean result = sysUserMpServiceImpl.updateById(sysUserMp);
        return result ? ResultData.ok("修改成功" ) : ResultData.fail("修改失败" );
    }

    /**
     * 根据ID查询（路径变量必须是数字）
     */
    @GetMapping("/id/{id}" )
    public ResultData<SysUserMp> getById(@PathVariable Long id) {
        SysUserMp entity = sysUserMpServiceImpl.getById(id);
        return ResultData.ok(entity);
    }


    /**
     * 根据user_name查询（支持路径变量，路径必须包含 userName）
     * 注意：此方法用于支持 /sysUser/userName/xxx 形式的请求
     */
    @GetMapping("/userName" )
    public ResultData<SysUserMp> getByUserName(@RequestParam String userName) {
        SysUserMp entity = sysUserMpServiceImpl.lambdaQuery().eq(SysUserMp::getUserName, userName).one();
        return ResultData.ok(entity);
    }

    /**
     * 分页查询（支持所有字段的 like 查询）
     */
    @PostMapping("/pageLike" )
    public ResultData<Page<SysUserMp>> pageLike(
            @RequestParam(defaultValue = "1" ) Integer pageNum,
            @RequestParam(defaultValue = "10" ) Integer pageSize,
            SysUserMp sysUserMp) {

        // 分页逻辑已移至 ServiceImpl，控制器仅负责参数传递
        Page<SysUserMp> resultPage = sysUserMpServiceImpl.pageLike(pageNum, pageSize, sysUserMp);
        return ResultData.ok(resultPage);
    }

    /**
     * 列表查询（等值）
     */
    @PostMapping("/pageEq" )
    public ResultData<Page<SysUserMp>> pageEq(
            @RequestParam(defaultValue = "1" ) Integer pageNum,
            @RequestParam(defaultValue = "10" ) Integer pageSize,
            SysUserMp sysUserMp) {
        // 分页逻辑已移至 ServiceImpl，控制器仅负责参数传递
        Page<SysUserMp> resultPage = sysUserMpServiceImpl.pageEq(pageNum, pageSize, sysUserMp);
        return ResultData.ok(resultPage);
    }

    /**
     * 获取所有数据（无条件）
     */
    @GetMapping("/all" )
    public ResultData<List<SysUserMp>> all() {
        List<SysUserMp> list = sysUserMpServiceImpl.list();
        return ResultData.ok(list);
    }
}

