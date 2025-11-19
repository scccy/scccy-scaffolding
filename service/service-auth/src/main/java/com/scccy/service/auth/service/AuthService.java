package com.scccy.service.auth.service;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.auth.dto.LoginResponse;
import com.scccy.service.auth.dto.RegisterBody;
import com.scccy.service.auth.fegin.SystemUserClient;
import jakarta.annotation.Resource;
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

    @Autowired
    private SystemUserCacheService systemUserCacheService;

    @Resource
    private UserTokenGenerationService userTokenGenerationService;

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

        // 1. 从缓存封装获取用户信息（内部仍通过 Feign，如缓存未命中时会远程查询）
        SysUserMp user = systemUserCacheService.getUserByUserName(userName);
        if (user == null) {
            log.warn("用户不存在: userName={}", userName);
            throw new BadCredentialsException("用户名或密码错误");
        }

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
     * 通过 Feign 调用 service-system 创建用户，注册成功后自动生成 Token
     * <p>
     * 流程：
     * 1. 调用 service-system 创建用户
     * 2. 注册成功后，自动生成 JWT Token
     * 3. 返回用户信息和 Token
     *
     * @param registerBody 注册信息（包含明文密码）
     * @return 注册结果（包含用户信息和 Token）
     */
    public ResultData<LoginResponse> register(RegisterBody registerBody) {
        log.info("用户注册: username={}", registerBody.getUsername());

        try {
            // 1. 通过 Feign 调用 service-system 创建用户
            ResultData<SysUserMp> result = systemUserClient.register(registerBody);

            if (result == null || !result.isSuccess()) {
                log.warn("用户注册失败: username={}, message={}", 
                    registerBody.getUsername(), 
                    result != null ? result.getMessage() : "未知错误");
                return ResultData.fail(result != null ? result.getMessage() : "注册失败");
            }

            SysUserMp user = result.getData();
            if (user == null) {
                log.warn("用户注册失败: username={}, 返回数据为空", registerBody.getUsername());
                return ResultData.fail("注册失败");
            }

            log.info("用户注册成功: username={}, userId={}", 
                registerBody.getUsername(), 
                user.getUserId());

            // 2. 注册成功后，自动生成 Token
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setUserId(user.getUserId());
            loginResponse.setUsername(user.getUserName());
            loginResponse.setNickName(user.getNickName());
            
            try {
                LoginResponse tokenResponse = userTokenGenerationService.generateUserToken(registerBody.getUsername());
                loginResponse.setToken(tokenResponse.getToken());
                loginResponse.setExpireTime(tokenResponse.getExpireTime());
                log.info("注册后自动生成 Token 成功: username={}", registerBody.getUsername());
            } catch (Exception e) {
                log.error("注册后自动生成 Token 失败: username={}, error={}", 
                    registerBody.getUsername(), e.getMessage(), e);
                // Token 生成失败不影响注册结果，但不返回 Token
                loginResponse.setToken(null);
                loginResponse.setExpireTime(null);
            }
            
            return ResultData.ok("注册成功", loginResponse);
        } catch (Exception e) {
            log.error("用户注册异常: username={}, error={}",
                registerBody.getUsername(), e.getMessage(), e);
            return ResultData.fail("注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录并生成 Token
     * <p>
     * 验证用户凭证，登录成功后生成 JWT Token
     * <p>
     * 流程：
     * 1. 验证用户凭证
     * 2. 登录成功后，生成 JWT Token
     * 3. 返回 Token 和用户信息
     *
     * @param userName 用户名
     * @param password 密码（明文）
     * @return 登录响应（包含 Token 和用户信息）
     * @throws BadCredentialsException 认证失败
     */
    public LoginResponse login(String userName, String password) {
        log.info("用户登录: userName={}", userName);

        // 1. 验证用户凭证
        Authentication authentication = authenticate(userName, password);

        // 2. 登录成功后，生成 JWT Token
        try {
            LoginResponse loginResponse = userTokenGenerationService.generateUserToken(userName);
            log.info("用户登录成功并生成 Token: username={}, userId={}", 
                userName, loginResponse.getUserId());
            return loginResponse;
        } catch (Exception e) {
            log.error("生成 Token 失败: username={}, error={}", userName, e.getMessage(), e);
            throw new RuntimeException("生成 Token 失败: " + e.getMessage(), e);
        }
    }
}

