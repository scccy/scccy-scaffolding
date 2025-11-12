package com.scccy.service.auth.fegin;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.auth.dto.LoginBody;
import com.scccy.service.auth.dto.LoginResponse;
import com.scccy.service.auth.dto.RegisterBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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

            @Override
            public ResultData<LoginResponse> register(RegisterBody registerBody) {
                log.error("service-system 服务不可用，无法注册用户: username={}, 错误原因: {}", 
                        registerBody != null ? registerBody.getUsername() : "unknown", 
                        cause != null ? cause.getMessage() : "未知错误", cause);
                return ResultData.fail("service-system 服务不可用，无法注册用户: " + 
                        (cause != null ? cause.getMessage() : "服务调用失败"));
            }

            @Override
            public ResultData<SysUserMp> login(LoginBody loginBody) {
                log.error("service-system 服务不可用，无法登录: username={}, 错误原因: {}", 
                        loginBody != null ? loginBody.getUsername() : "unknown", 
                        cause != null ? cause.getMessage() : "未知错误", cause);
                return ResultData.fail("service-system 服务不可用，无法登录: " + 
                        (cause != null ? cause.getMessage() : "服务调用失败"));
            }

            @Override
            public ResultData<List<String>> getUserAuthorities(String userName) {
                log.error("service-system 服务不可用，无法获取用户权限: userName={}, 错误原因: {}", 
                        userName, cause != null ? cause.getMessage() : "未知错误", cause);
                // 返回空列表，避免影响 Token 生成
                return ResultData.ok(Collections.emptyList());
            }
        };
    }
}

