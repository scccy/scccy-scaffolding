package com.scccy.service.auth.fegin;

import com.scccy.common.modules.dto.ResultData;
import com.scccy.common.modules.domain.mp.system.SysUserMp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-system", path = "/sysUser") //
public interface SystemUserClient {

    @GetMapping("/{userName}" )
    ResultData<SysUserMp> getByUserName(@PathVariable String userName);
}