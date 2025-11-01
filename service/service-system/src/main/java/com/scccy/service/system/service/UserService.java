package com.scccy.service.system.service;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.system.dao.mp.SysUserMpService;
import com.scccy.service.system.dto.LoginResponse;
import com.scccy.service.system.dto.RegisterBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

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

    // 注意：JWT工具类需要通过依赖注入，这里暂时注释掉，需要在system模块中也添加JWT相关依赖
    // @Autowired
    // private JwtUtils jwtUtils;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /**
     * 用户注册
     *
     * @param registerBody 注册信息
     * @return 注册结果（注意：这里不返回JWT Token，因为JWT工具类在auth模块）
     */
    public ResultData<SysUserMp> register(RegisterBody registerBody) {
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

        log.info("用户注册成功: username={}, userId={}", registerBody.getUsername(), sysUserMp.getUserId());
        return ResultData.ok("注册成功", sysUserMp);
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @return 用户信息
     * @throws BadCredentialsException 认证失败
     */
    public SysUserMp login(String username, String password) {
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

        log.info("用户登录成功: username={}, userId={}", username, user.getUserId());
        return user;
    }
}

