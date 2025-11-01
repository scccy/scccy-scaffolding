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
}