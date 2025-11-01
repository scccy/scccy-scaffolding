package com.scccy.service.auth.fegin;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * SystemUserClient 降级服务工厂
 * 当 service-system 服务不可用时，返回明确的错误信息
 * 使用 FallbackFactory 可以获取异常信息，便于排查问题
 *
 * @author scccy
 */
@Slf4j
@Component
public class SystemUserClientFallback implements FallbackFactory<SystemUserClient> {

    @Override
    public SystemUserClient create(Throwable cause) {
        return new SystemUserClient() {
            @Override
            public ResultData<SysUserMp> getByUserName(String userName) {
                log.error("service-system 服务不可用，无法获取用户信息: userName={}, 错误原因: {}", 
                        userName, cause != null ? cause.getMessage() : "未知错误", cause);
                return ResultData.fail("service-system 服务不可用，无法获取用户信息: " + 
                        (cause != null ? cause.getMessage() : "服务调用失败"));
            }
        };
    }
}

