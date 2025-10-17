package com.scccy.service.system.controller;


import cn.idev.excel.FastExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scccy.common.excel.listener.GenericExcelListener;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.system.domain.SysUser;
import com.scccy.service.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

/**
 * 用户信息
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/sys/user")
@Tag(name = "用户相关")
public class SysUserController {

    @Autowired
    SysUserService SysUerServiceImpl;
    @Autowired
    private SysUserService sysUserService;


    /**
     * 获取用户列表
     */
    //    @RequiresPermissions("system:user:list")
    @Operation(
            summary = "批量获取用户"
    )
    @PostMapping("/list")
    public IPage<SysUser> list(@RequestBody SysUser user, Integer pageNum, Integer pageSize) {
        // 分页对象
        Page<SysUser> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(SysUser::getUserName, user.getUserName())
                .or().eq(SysUser::getPhonenumber, user.getPhonenumber());


        // 分页查询
        return SysUerServiceImpl.page(page, queryWrapper);
    }

    //
    //    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    //    @RequiresPermissions("system:user:export")
    @PostMapping("/export")
    @Operation(summary = "导出excel")
    public void export(HttpServletResponse response, SysUser user) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("demo", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        List<SysUser> list = SysUerServiceImpl.list();
        FastExcel.write(response.getOutputStream(), SysUser.class)
                .sheet("Sheet1")
                .doWrite(list);

    }

    //    //    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
//    //    @RequiresPermissions("system:user:import")
    @Operation(summary="上传excel")
    @PostMapping("/importData")
    public ResultData<?> importData(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return ResultData.fail("请选择一个文件上传！");
        }
        // 读取 Excel 并处理 SysUser
        FastExcel.read(file.getInputStream(), SysUser.class,
                        new GenericExcelListener<SysUser>(list -> {
                            // 批量保存到数据库
                            SysUerServiceImpl.saveBatch(list);
                        }))
                .sheet()
                .doRead();
        return ResultData.ok("文件上传并处理成功！");
    }


    /**
     * 获取当前用户信息
     */
//    @InnerAuth
    @GetMapping("/info/{username}")
    public ResultData<?> info(@PathVariable("username") String username)
    {
        SysUser sysUser = SysUerServiceImpl.lambdaQuery().eq(SysUser::getUserName, username).one();

        // 角色集合
        Set<String> roles = permissionService.getRolePermission(sysUser);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(sysUser);
        LoginUser sysUserVo = new LoginUser();
        sysUserVo.setSysUser(sysUser);
        sysUserVo.setRoles(roles);
        sysUserVo.setPermissions(permissions);
        return ResultData.ok(sysUserVo);
    }
//
    /**
     * 注册用户信息
     */
//    @InnerAuth
//    @PostMapping("/register")
//    public ResultData<Boolean> register(@RequestBody SysUser sysUser)
//    {
//        String username = sysUser.getUserName();
//        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
//        {
//            return ResultData.fail("当前系统没有开启注册功能！");
//        }
//        if (!userService.checkUserNameUnique(sysUser))
//        {
//            return R.fail("保存用户'" + username + "'失败，注册账号已存在");
//        }
//        return ResultData.ok(userService.registerUser(sysUser));
//    }
//
//    /**
//     *记录用户登录IP地址和登录时间
//     */
//    @InnerAuth
//    @PutMapping("/recordlogin")
//    public ResultData<Boolean> recordlogin(@RequestBody SysUser sysUser)
//    {
//        return ResultData.ok(userService.updateLoginInfo(sysUser));
//    }
//
//    /**
//     * 获取用户信息
//     *
//     * @return 用户信息
//     */
//    @GetMapping("getInfo")
//    public ResultData<?> getInfo()
//    {
//        LoginUser loginUser = SecurityUtils.getLoginUser();
//        SysUser user = loginUser.getSysUser();
//        // 角色集合
//        Set<String> roles = permissionService.getRolePermission(user);
//        // 权限集合
//        Set<String> permissions = permissionService.getMenuPermission(user);
//        if (!loginUser.getPermissions().equals(permissions))
//        {
//            loginUser.setPermissions(permissions);
//            tokenService.refreshToken(loginUser);
//        }
//        ResultData<?> ajax = ResultData<?>.success();
//        ajax.put("user", user);
//        ajax.put("roles", roles);
//        ajax.put("permissions", permissions);
//        ajax.put("isDefaultModifyPwd", initPasswordIsModify(user.getPwdUpdateDate()));
//        ajax.put("isPasswordExpired", passwordIsExpiration(user.getPwdUpdateDate()));
//        return ajax;
//    }
//
//    // 检查初始密码是否提醒修改
//    public boolean initPasswordIsModify(Date pwdUpdateDate)
//    {
//        Integer initPasswordModify = Convert.toInt(configService.selectConfigByKey("sys.account.initPasswordModify"));
//        return initPasswordModify != null && initPasswordModify == 1 && pwdUpdateDate == null;
//    }
//
//    // 检查密码是否过期
//    public boolean passwordIsExpiration(Date pwdUpdateDate)
//    {
//        Integer passwordValidateDays = Convert.toInt(configService.selectConfigByKey("sys.account.passwordValidateDays"));
//        if (passwordValidateDays != null && passwordValidateDays > 0)
//        {
//            if (StringUtils.isNull(pwdUpdateDate))
//            {
//                // 如果从未修改过初始密码，直接提醒过期
//                return true;
//            }
//            Date nowDate = DateUtils.getNowDate();
//            return DateUtils.differentDaysByMillisecond(nowDate, pwdUpdateDate) > passwordValidateDays;
//        }
//        return false;
//    }
//
//    /**
//     * 根据用户编号获取详细信息
//     */
//    //    @RequiresPermissions("system:user:query")
//    @GetMapping(value = { "/", "/{userId}" })
//    public ResultData<?> getInfo(@PathVariable(value = "userId", required = false) Long userId)
//    {
//        ResultData<?> ajax = ResultData<?>.success();
//        if (StringUtils.isNotNull(userId))
//        {
//            userService.checkUserDataScope(userId);
//            SysUser sysUser = userService.selectUserById(userId);
//            ajax.put(ResultData<?>.DATA_TAG, sysUser);
//            ajax.put("postIds", postService.selectPostListByUserId(userId));
//            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
//        }
//        List<SysRole> roles = roleService.selectRoleAll();
//        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
//        ajax.put("posts", postService.selectPostAll());
//        return ajax;
//    }
//
//    /**
//     * 新增用户
//     */
////    //    @RequiresPermissions("system:user:add")
////    //    @Log(title = "用户管理", businessType = BusinessType.INSERT)
//    @PostMapping
//    public ResultData<?> add(@Validated @RequestBody SysUser user)
//    {
//        deptService.checkDeptDataScope(user.getDeptId());
//        roleService.checkRoleDataScope(user.getRoleIds());
//        if (!userService.checkUserNameUnique(user))
//        {
//            return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
//        }
//        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
//        {
//            return error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
//        }
//        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
//        {
//            return error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
//        }
//        user.setCreateBy(SecurityUtils.getUsername());
//        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
//        return toAjax(userService.insertUser(user));
//    }
//
//    /**
//     * 修改用户
//     */
//    //    @RequiresPermissions("system:user:edit")
//    //    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public ResultData<?> edit(@Validated @RequestBody SysUser user)
//    {
//        userService.checkUserAllowed(user);
//        userService.checkUserDataScope(user.getUserId());
//        deptService.checkDeptDataScope(user.getDeptId());
//        roleService.checkRoleDataScope(user.getRoleIds());
//        if (!userService.checkUserNameUnique(user))
//        {
//            return error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
//        }
//        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
//        {
//            return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
//        }
//        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
//        {
//            return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
//        }
//        user.setUpdateBy(SecurityUtils.getUsername());
//        return toAjax(userService.updateUser(user));
//    }
//
//    /**
//     * 删除用户
//     */
//    //    @RequiresPermissions("system:user:remove")
//    //    @Log(title = "用户管理", businessType = BusinessType.DELETE)
//    @DeleteMapping("/{userIds}")
//    public ResultData<?> remove(@PathVariable Long[] userIds)
//    {
//        if (ArrayUtils.contains(userIds, SecurityUtils.getUserId()))
//        {
//            return error("当前用户不能删除");
//        }
//        return toAjax(userService.deleteUserByIds(userIds));
//    }
//
//    /**
//     * 重置密码
//     */
//    //    @RequiresPermissions("system:user:edit")
//    //    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
//    @PutMapping("/resetPwd")
//    public ResultData<?> resetPwd(@RequestBody SysUser user)
//    {
//        userService.checkUserAllowed(user);
//        userService.checkUserDataScope(user.getUserId());
//        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
//        user.setUpdateBy(SecurityUtils.getUsername());
//        return toAjax(userService.resetPwd(user));
//    }
//
//    /**
//     * 状态修改
//     */
//    //    @RequiresPermissions("system:user:edit")
//    //    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
//    @PutMapping("/changeStatus")
//    public ResultData<?> changeStatus(@RequestBody SysUser user)
//    {
//        userService.checkUserAllowed(user);
//        userService.checkUserDataScope(user.getUserId());
//        user.setUpdateBy(SecurityUtils.getUsername());
//        return toAjax(userService.updateUserStatus(user));
//    }
//
//    /**
//     * 根据用户编号获取授权角色
//     */
//    //    @RequiresPermissions("system:user:query")
//    @GetMapping("/authRole/{userId}")
//    public ResultData<?> authRole(@PathVariable("userId") Long userId)
//    {
//        ResultData<?> ajax = ResultData<?>.success();
//        SysUser user = userService.selectUserById(userId);
//        List<SysRole> roles = roleService.selectRolesByUserId(userId);
//        ajax.put("user", user);
//        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
//        return ajax;
//    }
//
//    /**
//     * 用户授权角色
//     */
//    //    @RequiresPermissions("system:user:edit")
//    //    @Log(title = "用户管理", businessType = BusinessType.GRANT)
//    @PutMapping("/authRole")
//    public ResultData<?> insertAuthRole(Long userId, Long[] roleIds)
//    {
//        userService.checkUserDataScope(userId);
//        roleService.checkRoleDataScope(roleIds);
//        userService.insertUserAuth(userId, roleIds);
//        return success();
//    }
//
//    /**
//     * 获取部门树列表
//     */
//    //    @RequiresPermissions("system:user:list")
//    @GetMapping("/deptTree")
//    public ResultData<?> deptTree(SysDept dept)
//    {
//        return success(deptService.selectDeptTreeList(dept));
//    }
}
