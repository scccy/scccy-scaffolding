package com.scccy.service.system.service;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.system.dao.mp.SysUserMpService;
import com.scccy.service.system.dto.LoginResponse;
import com.scccy.service.system.dto.RegisterBody;
import com.scccy.service.system.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务
 * <p>
 * 处理用户注册、登录等业务逻辑
 * 直接操作本地数据库，不通过远程调用
 *
 * @author scccy
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private SysUserMpService sysUserMpService;

    @Autowired
    private JwtUtils jwtUtils;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /**
     * 用户注册
     *
     * @param registerBody 注册信息
     * @return 注册结果（包含JWT Token）
     */
    public ResultData<LoginResponse> register(RegisterBody registerBody) {
        log.info("用户注册: username={}", registerBody.getUsername());

        // 1. 验证用户名是否已存在
        SysUserMp existingUser = sysUserMpService.lambdaQuery()
                .eq(SysUserMp::getUserName, registerBody.getUsername())
                .one();
        if (existingUser != null) {
            log.warn("用户名已存在: username={}", registerBody.getUsername());
            return ResultData.fail("用户名已存在");
        }

        // 2. 构建用户对象
        SysUserMp sysUserMp = new SysUserMp();
        sysUserMp.setUserName(registerBody.getUsername());
        sysUserMp.setPassword(passwordEncoder.encode(registerBody.getPassword()));  // 加密密码
        sysUserMp.setNickName(registerBody.getNickName() != null ? registerBody.getNickName() : registerBody.getUsername());
        sysUserMp.setEmail(registerBody.getEmail());
        sysUserMp.setPhonenumber(registerBody.getPhone());
        sysUserMp.setStatus(0);  // 默认正常状态
        sysUserMp.setDelFlag(0);  // 默认未删除
        sysUserMp.setUserType("00");  // 系统用户
        sysUserMp.setCreateTime(new Date());

        // 3. 保存用户
        boolean result = sysUserMpService.save(sysUserMp);
        if (!result) {
            log.error("用户注册失败: username={}", registerBody.getUsername());
            return ResultData.fail("注册失败");
        }

        // 4. 生成JWT Token
        String token = generateUserToken(sysUserMp);

        // 5. 构建登录响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setUserId(sysUserMp.getUserId());
        loginResponse.setUsername(sysUserMp.getUserName());
        loginResponse.setNickName(sysUserMp.getNickName());
        loginResponse.setExpireTime(jwtUtils.getExpirationDate(token).getTime());

        log.info("用户注册成功: username={}, userId={}", registerBody.getUsername(), sysUserMp.getUserId());
        return ResultData.ok("注册成功", loginResponse);
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @return 登录响应（包含JWT Token和用户信息）
     * @throws BadCredentialsException 认证失败
     */
    public LoginResponse login(String username, String password) {
        log.info("用户登录: username={}", username);

        // 1. 查询用户信息
        SysUserMp user = sysUserMpService.lambdaQuery()
                .eq(SysUserMp::getUserName, username)
                .one();

        if (user == null) {
            log.warn("用户不存在: username={}", username);
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 2. 验证用户状态
        if (user.getStatus() != null && user.getStatus() != 0) {
            log.warn("用户账户已被停用: username={}", username);
            throw new BadCredentialsException("用户账户已被停用");
        }

        if (user.getDelFlag() != null && user.getDelFlag() != 0) {
            log.warn("用户账户已被删除: username={}", username);
            throw new BadCredentialsException("用户账户已被删除");
        }

        // 3. 验证密码
        String encodedPassword = user.getPassword();
        if (encodedPassword == null || !passwordEncoder.matches(password, encodedPassword)) {
            log.warn("密码验证失败: username={}", username);
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 4. 生成JWT Token
        String token = generateUserToken(user);

        // 5. 构建登录响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setUserId(user.getUserId());
        loginResponse.setUsername(user.getUserName());
        loginResponse.setNickName(user.getNickName());
        loginResponse.setExpireTime(jwtUtils.getExpirationDate(token).getTime());

        log.info("用户登录成功: username={}, userId={}", username, user.getUserId());
        return loginResponse;
    }

    /**
     * 生成用户JWT Token
     * 将用户基础信息写入JWT
     *
     * @param user 用户信息
     * @return JWT Token
     */
    private String generateUserToken(SysUserMp user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("nickName", user.getNickName());
        claims.put("status", user.getStatus());
        claims.put("email", user.getEmail());
        claims.put("phonenumber", user.getPhonenumber());

        return jwtUtils.generateToken(user.getUserId(), user.getUserName(), claims);
    }
}

