package com.scccy.service.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.service.system.dao.service.SysUserMpService;
import com.scccy.service.system.dto.LoginBody;
import com.scccy.service.system.dto.RegisterBody;
import com.scccy.service.system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户信息表 控制器（单主键，使用 MyBatis-Plus Service 完整 CRUD）
 *
 * @author scccy
 * @date 2025-10-22 16:27:00
 */
@Slf4j
@RestController
@RequestMapping("/sysUser" )
public class SysUserController {


    @Autowired
    private SysUserMpService sysUserMpServiceImpl;

    @Autowired
    private UserService userService;

    /**
     * 新增
     */
    @PostMapping
    public ResultData<?> save(@RequestBody SysUserMp sysUserMp) {
        boolean result = sysUserMpServiceImpl.save(sysUserMp);
        return result ? ResultData.ok("新增成功" ) : ResultData.fail("新增失败" );
    }

    /**
     * 用户注册（内部接口）
     * <p>
     * 专门用于用户注册的接口，包含注册相关的验证逻辑
     * 接收明文密码，自动加密后保存
     * <p>
     * 注意：此接口为内部接口，仅供 Auth 服务通过 Feign 调用
     * 不对外暴露，客户端应该调用 Auth 服务的 /api/user/register 接口
     * <p>
     * 此接口返回用户信息（SysUserMp），Token 由 Auth 服务生成
     *
     * @param registerBody 注册信息（包含明文密码）
     * @return 注册结果（包含用户信息，不包含 Token）
     */
    @PostMapping("/register")
    public ResultData<SysUserMp> register(@Valid @RequestBody RegisterBody registerBody) {
        return userService.register(registerBody);
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

    /**
     * 获取用户权限列表
     * <p>
     * 查询用户 → 角色 → 菜单权限的完整链路
     * 返回权限列表，包含：
     * - 角色标识：ROLE_ADMIN, ROLE_USER（Spring Security 标准格式）
     * - 菜单权限：system:user:list, system:user:add（菜单 perms 字段）
     *
     * @param userName 用户名
     * @return 权限列表
     */
    @GetMapping("/authorities")
    public ResultData<List<String>> getUserAuthorities(@RequestParam String userName) {
        List<String> authorities = userService.getUserAuthorities(userName);
        return ResultData.ok(authorities);
    }
}

