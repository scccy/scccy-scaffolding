package com.scccy.service.auth.controller;

import com.scccy.common.modules.domain.mp.system.SysUserMp;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.auth.dto.LoginBody;
import com.scccy.service.auth.dto.LoginResponse;
import com.scccy.service.auth.dto.RegisterBody;
import com.scccy.service.auth.fegin.SystemUserClient;
import com.scccy.service.auth.service.TokenBlacklistService;
import com.scccy.service.auth.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 * <p>
 * 处理普通用户的注册、登录、登出等功能
 * 与OAuth2第三方授权功能分离
 *
 * @author scccy
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户认证", description = "普通用户注册、登录、登出等认证相关接口")
public class UserAuthController {

    @Resource
    private SystemUserClient systemUserClient;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private TokenBlacklistService tokenBlacklistService;

    /**
     * 用户注册
     *
     * @param registerBody 注册信息
     * @return 注册结果（包含JWT Token）
     */
    @Operation(
            summary = "用户注册",
            description = "新用户注册接口，注册成功后自动返回JWT Token，可直接用于登录"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "注册成功",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "注册失败（用户名已存在、参数验证失败等）",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            )
    })
    @PostMapping("/register")
    public ResultData<LoginResponse> register(@Valid @RequestBody RegisterBody registerBody) {
        log.info("用户注册请求: username={}", registerBody.getUsername());
        try {
            // 1. 调用 service-system 进行注册
            ResultData<SysUserMp> registerResult = systemUserClient.register(registerBody);
            if (registerResult == null || !registerResult.isSuccess() || registerResult.getData() == null) {
                return ResultData.fail(registerResult != null ? registerResult.getMsg() : "注册失败");
            }

            SysUserMp user = registerResult.getData();

            // 2. 生成JWT Token
            String token = generateUserToken(user);

            // 3. 构建登录响应
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setUserId(user.getUserId());
            loginResponse.setUsername(user.getUserName());
            loginResponse.setNickName(user.getNickName());
            Long expireTime = jwtUtils.getExpirationDate(token).getTime();
            loginResponse.setExpireTime(expireTime);

            return ResultData.ok("注册成功", loginResponse);
        } catch (Exception e) {
            log.error("用户注册异常: username={}, error={}", registerBody.getUsername(), e.getMessage(), e);
            return ResultData.fail("注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     *
     * @param loginBody 登录信息（用户名和密码）
     * @return 登录结果（包含JWT Token和用户信息）
     */
    @Operation(
            summary = "用户登录",
            description = "用户登录接口，验证用户名和密码后返回JWT Token"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "登录成功",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "登录失败（用户名或密码错误、账户被停用等）",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            )
    })
    @PostMapping("/login")
    public ResultData<LoginResponse> login(@Valid @RequestBody LoginBody loginBody) {
        log.info("用户登录请求: username={}", loginBody.getUsername());
        try {
            // 1. 调用 service-system 进行登录验证
            ResultData<SysUserMp> loginResult = systemUserClient.login(loginBody);
            if (loginResult == null || !loginResult.isSuccess() || loginResult.getData() == null) {
                return ResultData.fail(loginResult != null ? loginResult.getMsg() : "登录失败");
            }

            SysUserMp user = loginResult.getData();

            // 2. 生成JWT Token
            String token = generateUserToken(user);

            // 3. 构建登录响应
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setUserId(user.getUserId());
            loginResponse.setUsername(user.getUserName());
            loginResponse.setNickName(user.getNickName());
            Long expireTime = jwtUtils.getExpirationDate(token).getTime();
            loginResponse.setExpireTime(expireTime);

            return ResultData.ok("登录成功", loginResponse);
        } catch (Exception e) {
            log.error("用户登录异常: username={}, error={}", loginBody.getUsername(), e.getMessage(), e);
            return ResultData.fail("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @return 登出结果
     */
    @Operation(
            summary = "用户登出",
            description = "用户登出接口，将当前Token加入黑名单（如果实现了Token黑名单功能）"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "登出成功",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "未提供有效的Token",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            )
    })
    @PostMapping("/logout")
    public ResultData<String> logout(HttpServletRequest request) {
        // 从请求头提取Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            log.warn("登出请求未提供Token");
            return ResultData.fail("未提供认证Token");
        }

        try {
            String token = jwtUtils.extractTokenFromHeader(authHeader);
            if (token == null || token.trim().isEmpty()) {
                return ResultData.fail("无效的Token格式");
            }

            // 验证Token有效性
            if (!jwtUtils.validateToken(token)) {
                return ResultData.fail("Token无效或已过期");
            }

            // 检查Token是否已在黑名单中（防止重复登出）
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Token已在黑名单中: userId={}", jwtUtils.getUserId(token));
                return ResultData.ok("该Token已失效");
            }

            // 将Token加入黑名单
            Long expireTime = jwtUtils.getExpirationDate(token).getTime();
            tokenBlacklistService.addToBlacklist(token, expireTime);

            log.info("用户登出成功，Token已加入黑名单: userId={}", jwtUtils.getUserId(token));
            return ResultData.ok("登出成功");
        } catch (Exception e) {
            log.error("用户登出异常: error={}", e.getMessage(), e);
            return ResultData.fail("登出失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     * <p>
     * 从JWT Token中解析用户信息并返回
     *
     * @param request HTTP请求
     * @return 当前用户信息
     */
    @Operation(
            summary = "获取当前用户信息",
            description = "从JWT Token中解析并返回当前登录用户的信息"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token无效或已过期",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            )
    })
    @GetMapping("/info")
    public ResultData<LoginResponse> getCurrentUserInfo(
            @Parameter(description = "JWT Token（可选，如果不提供则从请求头Authorization中获取）")
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        try {
            // 优先从参数获取，否则从请求头获取
            if (authHeader == null || authHeader.trim().isEmpty()) {
                authHeader = request.getHeader("Authorization");
            }

            if (authHeader == null || authHeader.trim().isEmpty()) {
                return ResultData.fail("未提供认证Token");
            }

            String token = jwtUtils.extractTokenFromHeader(authHeader);
            if (token == null || token.trim().isEmpty()) {
                return ResultData.fail("无效的Token格式");
            }

            // 验证Token有效性
            if (!jwtUtils.validateToken(token)) {
                return ResultData.fail("Token无效或已过期");
            }

            // 检查Token是否在黑名单中（已登出）
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Token已在黑名单中，无法获取用户信息");
                return ResultData.fail("Token已失效，请重新登录");
            }

            // 从Token中解析用户信息
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setUserId(jwtUtils.getUserId(token));
            loginResponse.setUsername(jwtUtils.getUsername(token));
            loginResponse.setNickName(jwtUtils.getNickName(token));
            Long expireTime = jwtUtils.getExpirationDate(token).getTime();
            loginResponse.setExpireTime(expireTime);

            return ResultData.ok(loginResponse);
        } catch (Exception e) {
            log.error("获取用户信息异常: error={}", e.getMessage(), e);
            return ResultData.fail("获取用户信息失败: " + e.getMessage());
        }
    }
}

