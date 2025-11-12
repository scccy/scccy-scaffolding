package com.scccy.service.auth.controller;

import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.auth.dto.LoginBody;
import com.scccy.service.auth.dto.LoginResponse;
import com.scccy.service.auth.dto.RegisterBody;
import com.scccy.service.auth.service.AuthService;
import com.scccy.service.auth.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 * <p>
 * 处理普通用户的注册、登录和登出功能
 * <p>
 * 职责：
 * - 用户注册：创建新用户（通过 Feign 调用 system 服务）
 * - 用户登录：用户名密码认证
 * - 用户登出：将 JWT Token 加入黑名单
 *
 * @author scccy
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户认证", description = "普通用户注册、登录、登出 API 接口")
public class UserAuthController {

    @Resource
    private AuthService authService;

    @Resource
    private TokenBlacklistService tokenBlacklistService;

    /**
     * 用户注册接口
     * <p>
     * 接收 JSON 格式的注册请求，创建新用户
     * <p>
     * 注意：在 OAuth2 架构中，Token 应该由 Authorization Server 统一生成
     * 此接口不返回 Token，客户端需要单独调用 Authorization Server 获取 Token
     * <p>
     * 注册成功后，客户端可以调用登录接口获取 Token
     *
     * @param registerBody 注册请求体（包含用户名、密码等信息）
     * @return 注册结果（包含用户信息，不包含 Token）
     */
    @Operation(
            summary = "用户注册",
            description = "前后端分离的注册接口，接收 JSON 格式的用户名和密码等信息，创建新用户。注册成功后，客户端需要单独调用登录接口获取 Token。"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "注册成功",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "注册失败，用户名已存在或参数错误",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            )
    })
    @PostMapping("/register")
    @ResponseBody
    public ResultData<LoginResponse> register(@Valid @RequestBody RegisterBody registerBody) {
        return authService.register(registerBody);
    }

    /**
     * 用户登录接口
     * <p>
     * 接收 JSON 格式的登录请求，验证用户信息并返回结果
     * <p>
     * 注意：此接口返回的是登录结果，实际 Token 需要通过 OAuth2 授权流程获取
     *
     * @param loginBody 登录请求体（包含用户名和密码）
     * @return 登录结果
     */
    @Operation(
            summary = "用户登录",
            description = "前后端分离的登录接口，接收 JSON 格式的用户名和密码，返回登录结果。实际 Token 需要通过 OAuth2 授权流程获取。"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "登录成功",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "登录失败，用户名或密码错误",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            )
    })
    @PostMapping("/login")
    @ResponseBody
    public ResultData<String> login(@Valid @RequestBody LoginBody loginBody) {
        try {
            // 调用认证服务进行登录
            org.springframework.security.core.Authentication authentication = authService.authenticate(
                    loginBody.getUsername(),
                    loginBody.getPassword()
            );

            // 登录成功，返回用户名
            log.info("用户登录成功: username={}", authentication.getName());
            return ResultData.ok("登录成功", authentication.getName());
        } catch (BadCredentialsException e) {
            // 登录失败
            log.warn("用户登录失败: username={}, error={}", loginBody.getUsername(), e.getMessage());
            return ResultData.fail("用户名或密码错误");
        } catch (Exception e) {
            log.error("用户登录异常: username={}, error={}", loginBody.getUsername(), e.getMessage(), e);
            return ResultData.fail("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出接口
     * <p>
     * 将当前用户的 JWT Token 加入黑名单，后续 Gateway 会拦截该 Token
     * <p>
     * 注意：此接口需要携带有效的 JWT Token（Authorization: Bearer {token}）
     *
     * @param authentication 认证信息（包含 JWT Token）
     * @return 登出结果
     */
    @Operation(
            summary = "用户登出",
            description = "将当前用户的 JWT Token 加入黑名单，后续 Gateway 会拦截该 Token。需要携带有效的 JWT Token。"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "登出成功",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "未认证，需要携带有效的 JWT Token",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            )
    })
    @PostMapping("/logout")
    @ResponseBody
    public ResultData<String> logout(Authentication authentication) {
        try {
            // 检查认证信息
            if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
                log.warn("用户登出失败：未提供有效的 JWT Token");
                return ResultData.fail("未提供有效的 JWT Token");
            }

            // 获取 JWT Token
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtAuth.getToken();

            // 将 Token 加入黑名单
            tokenBlacklistService.addToBlacklist(jwt);

            String username = jwt.getClaimAsString("username");
            String jti = jwt.getId();
            log.info("用户登出成功: username={}, jti={}", username, jti);

            return ResultData.ok("登出成功");
        } catch (Exception e) {
            log.error("用户登出异常: {}", e.getMessage(), e);
            return ResultData.fail("登出失败: " + e.getMessage());
        }
    }
}

