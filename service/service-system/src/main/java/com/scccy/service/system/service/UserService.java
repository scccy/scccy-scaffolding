package com.scccy.service.system.service;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.system.dao.mapper.SysUserMapper;
import com.scccy.service.system.dao.mp.SysUserMpService;
import com.scccy.service.system.dto.LoginResponse;
import com.scccy.service.system.dto.RegisterBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

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
    private SysUserMapper sysUserMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /**
     * 用户注册
     * <p>
     * 在 OAuth2 架构中，Token 应该由 Authorization Server 统一生成
     * 此接口只负责用户注册，不返回 Token
     * 客户端需要单独调用 Authorization Server 获取 Token
     *
     * @param registerBody 注册信息
     * @return 注册结果（包含用户信息，不包含 Token）
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

        // 4. 构建登录响应（不包含 Token）
        // 在 OAuth2 架构中，Token 应该由 Authorization Server 统一生成
        // 客户端需要单独调用 Authorization Server 获取 Token
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(null);  // 不再返回 Token
        loginResponse.setUserId(sysUserMp.getUserId());
        loginResponse.setUsername(sysUserMp.getUserName());
        loginResponse.setNickName(sysUserMp.getNickName());
        loginResponse.setExpireTime(null);  // 不再返回过期时间

        log.info("用户注册成功: username={}, userId={}", registerBody.getUsername(), sysUserMp.getUserId());
        return ResultData.ok("注册成功", loginResponse);
    }

    /**
     * 用户登录
     * <p>
     * 在 OAuth2 架构中，Token 应该由 Authorization Server 统一生成
     * 此接口只负责验证用户身份，不返回 Token
     * 客户端需要单独调用 Authorization Server 获取 Token
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @return 登录响应（包含用户信息，不包含 Token）
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

        // 4. 构建登录响应（不包含 Token）
        // 在 OAuth2 架构中，Token 应该由 Authorization Server 统一生成
        // 客户端需要单独调用 Authorization Server 获取 Token
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(null);  // 不再返回 Token
        loginResponse.setUserId(user.getUserId());
        loginResponse.setUsername(user.getUserName());
        loginResponse.setNickName(user.getNickName());
        loginResponse.setExpireTime(null);  // 不再返回过期时间

        log.info("用户登录成功: username={}, userId={}", username, user.getUserId());
        return loginResponse;
    }

    /**
     * 获取用户权限列表
     * <p>
     * 查询用户 → 角色 → 菜单权限的完整链路
     * 返回权限列表，包含：
     * - 角色标识：ROLE_ADMIN, ROLE_USER（Spring Security 标准格式）
     * - 菜单权限：system:user:list, system:user:add（菜单 perms 字段）
     *
     * @param userName 用户名
     * @return 权限列表
     */
    public List<String> getUserAuthorities(String userName) {
        log.debug("获取用户权限: userName={}", userName);
        try {
            List<String> authorities = sysUserMapper.getUserAuthorities(userName);
            log.debug("用户权限查询成功: userName={}, authorities={}", userName, authorities);
            return authorities != null ? authorities : Collections.emptyList();
        } catch (Exception e) {
            log.error("获取用户权限失败: userName={}, error={}", userName, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

}

