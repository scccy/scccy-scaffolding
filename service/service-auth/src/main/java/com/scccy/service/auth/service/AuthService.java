package com.scccy.service.auth.service;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
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
}

