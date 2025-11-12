package com.scccy.service.auth.service;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.auth.dto.LoginResponse;
import com.scccy.service.auth.dto.RegisterBody;
import com.scccy.service.auth.fegin.SystemUserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 认证服务
 * <p>
 * 处理用户登录认证逻辑
 *
 * @author scccy
 */
@Slf4j
@Service
public class AuthService {

    @Autowired
    private SystemUserClient systemUserClient;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /**
     * 用户登录认证
     *
     * @param userName 用户名
     * @param password 密码（明文）
     * @return 认证对象
     * @throws BadCredentialsException 认证失败
     */
    public Authentication authenticate(String userName, String password) {
        log.info("用户登录认证: userName={}", userName);

        // 1. 通过 Feign 调用 service-system 获取用户信息
        ResultData<SysUserMp> result = systemUserClient.getByUserName(userName);
        
        if (result == null || result.getData() == null) {
            log.warn("用户不存在: userName={}", userName);
            throw new BadCredentialsException("用户名或密码错误");
        }

        SysUserMp user = result.getData();

        // 2. 验证用户状态
        if (user.getStatus() != null && user.getStatus() != 0) {
            log.warn("用户账户已被停用: userName={}", userName);
            throw new BadCredentialsException("用户账户已被停用");
        }

        if (user.getDelFlag() != null && user.getDelFlag() != 0) {
            log.warn("用户账户已被删除: userName={}", userName);
            throw new BadCredentialsException("用户账户已被删除");
        }

        // 3. 验证密码
        String encodedPassword = user.getPassword();
        if (encodedPassword == null || !passwordEncoder.matches(password, encodedPassword)) {
            log.warn("密码验证失败: userName={}", userName);
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 4. 创建认证对象
        // 注意：这里使用用户名作为 principal，Spring Security 会自动处理
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userName,  // principal（主体）
                null,     // credentials（凭证，认证成功后设为 null）
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))  // authorities（权限）
        );

        // 5. 保存到 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("用户登录成功: userName={}", userName);
        return authentication;
    }

    /**
     * 用户注册
     * <p>
     * 通过 Feign 调用 service-system 创建用户
     * <p>
     * 注意：在 OAuth2 架构中，Token 应该由 Authorization Server 统一生成
     * 此方法只负责用户注册，不返回 Token
     * 客户端需要单独调用 Authorization Server 获取 Token
     *
     * @param registerBody 注册信息（包含明文密码）
     * @return 注册结果（包含用户信息，不包含 Token）
     */
    public ResultData<LoginResponse> register(RegisterBody registerBody) {
        log.info("用户注册: username={}", registerBody.getUsername());

        try {
            // 通过 Feign 调用 service-system 创建用户
            ResultData<LoginResponse> result = systemUserClient.register(registerBody);

            if (result == null || !result.isSuccess()) {
                log.warn("用户注册失败: username={}, message={}", 
                    registerBody.getUsername(), 
                    result != null ? result.getMessage() : "未知错误");
                return ResultData.fail(result != null ? result.getMessage() : "注册失败");
            }

            log.info("用户注册成功: username={}, userId={}", 
                registerBody.getUsername(), 
                result.getData() != null ? result.getData().getUserId() : null);
            
            return result;
        } catch (Exception e) {
            log.error("用户注册异常: username={}, error={}", 
                registerBody.getUsername(), e.getMessage(), e);
            return ResultData.fail("注册失败: " + e.getMessage());
        }
    }
}

