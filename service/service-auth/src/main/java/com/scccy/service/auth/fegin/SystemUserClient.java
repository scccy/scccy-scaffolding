package com.scccy.service.auth.fegin;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.auth.dto.LoginBody;
import com.scccy.service.auth.dto.RegisterBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "service-system", path = "/sysUser", fallbackFactory = SystemUserClientFallback.class)
public interface SystemUserClient {

    @GetMapping("/userName")
    ResultData<SysUserMp> getByUserName(@RequestParam String userName);

    /**
     * 用户注册
     *
     * @param registerBody 注册信息（包含明文密码）
     * @return 注册结果（包含用户信息）
     */
    @PostMapping("/register")
    ResultData<SysUserMp> register(@RequestBody RegisterBody registerBody);

    /**
     * 用户登录
     *
     * @param loginBody 登录信息（用户名和明文密码）
     * @return 用户信息
     */
    @PostMapping("/login")
    ResultData<SysUserMp> login(@RequestBody LoginBody loginBody);

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
    ResultData<List<String>> getUserAuthorities(@RequestParam String userName);
}