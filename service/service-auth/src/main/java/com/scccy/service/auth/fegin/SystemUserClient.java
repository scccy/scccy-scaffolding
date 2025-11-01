package com.scccy.service.auth.fegin;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "service-system", path = "/sysUser", fallbackFactory = SystemUserClientFallback.class)
public interface SystemUserClient {

    @GetMapping("/userName")
    ResultData<SysUserMp> getByUserName(@RequestParam String userName);
}