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
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

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

    @Resource
    private JwtDecoder jwtDecoder;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 用户注册接口
     * <p>
     * 接收 JSON 格式的注册请求，创建新用户，注册成功后自动生成 Token
     * <p>
     * 流程：
     * 1. 调用 service-system 创建用户
     * 2. 注册成功后，自动生成 JWT Token
     * 3. 返回 Token 和用户信息
     *
     * @param registerBody 注册请求体（包含用户名、密码等信息）
     * @return 注册结果（包含用户信息和 Token）
     */
    @Operation(
            summary = "用户注册",
            description = "前后端分离的注册接口，接收 JSON 格式的用户名和密码等信息，创建新用户。注册成功后，自动生成 JWT Token 并返回。"
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
     * 接收 JSON 格式的登录请求，验证用户信息并生成 Token
     * <p>
     * 流程：
     * 1. 验证用户凭证
     * 2. 登录成功后，生成 JWT Token
     * 3. 返回 Token 和用户信息
     *
     * @param loginBody 登录请求体（包含用户名和密码）
     * @return 登录结果（包含 Token 和用户信息）
     */
    @Operation(
            summary = "用户登录",
            description = "前后端分离的登录接口，接收 JSON 格式的用户名和密码，验证用户信息并生成 JWT Token。"
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
    public ResultData<LoginResponse> login(@Valid @RequestBody LoginBody loginBody) {
        try {
            // 调用认证服务进行登录并生成 Token
            LoginResponse loginResponse = authService.login(
                    loginBody.getUsername(),
                    loginBody.getPassword()
            );

            log.info("用户登录成功: username={}, userId={}", 
                loginResponse.getUsername(), loginResponse.getUserId());
            return ResultData.ok("登录成功", loginResponse);
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
     * 注意：此接口允许公开访问（permitAll），即使 token 无效也应该允许登出
     * 如果提供了 token，会解析并加入黑名单；如果没有 token，也会返回成功
     *
     * @param request HTTP 请求（用于从请求头提取 token）
     * @return 登出结果
     */
    @Operation(
            summary = "用户登出",
            description = "将当前用户的 JWT Token 加入黑名单，后续 Gateway 会拦截该 Token。即使 token 无效也可以调用此接口。"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "登出成功",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            )
    })
    @PostMapping("/logout")
    @ResponseBody
    public ResultData<String> logout(HttpServletRequest request) {
        try {
            // 从请求头提取 Authorization token
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                
                if (StringUtils.hasText(token)) {
                    try {
                        // 尝试解析 token（即使已过期也能解析）
                        Jwt jwt = jwtDecoder.decode(token);

            // 将 Token 加入黑名单
            tokenBlacklistService.addToBlacklist(jwt);

            String username = jwt.getClaimAsString("username");
            String jti = jwt.getId();
            log.info("用户登出成功: username={}, jti={}", username, jti);
                    } catch (Exception e) {
                        // 如果解析失败（如 token 已过期或格式错误），尝试使用 token 字符串加入黑名单
                        log.debug("解析 token 失败，尝试使用 token 字符串加入黑名单: {}", e.getMessage());
                        
                        // 尝试从 token 中提取过期时间（简单解析 JWT payload）
                        try {
                            String[] parts = token.split("\\.");
                            if (parts.length >= 2) {
                                // Base64 URL 解码 payload（JWT payload 是 UTF-8 编码的 JSON 字符串）
                                String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                                Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
                                Object expObj = claims.get("exp");
                                Long expiresAt = null;
                                if (expObj instanceof Number) {
                                    expiresAt = ((Number) expObj).longValue() * 1000; // 转换为毫秒
                                } else if (expObj instanceof String) {
                                    expiresAt = Long.parseLong((String) expObj) * 1000;
                                }
                                
                                if (expiresAt != null && expiresAt > System.currentTimeMillis()) {
                                    tokenBlacklistService.addToBlacklist(token, expiresAt);
                                } else {
                                    // 已过期的 token 不需要加入黑名单
                                    log.debug("Token 已过期，无需加入黑名单");
                                }
                            }
                        } catch (Exception ex) {
                            log.debug("无法解析 token 过期时间，跳过黑名单: {}", ex.getMessage());
                        }
                    }
                }
            }
            
            // 无论是否有 token，都返回成功（登出接口应该总是允许调用）
            return ResultData.ok("登出成功");
        } catch (Exception e) {
            log.error("用户登出异常: {}", e.getMessage(), e);
            // 即使发生异常，也返回成功（登出接口应该总是允许调用）
            return ResultData.ok("登出成功");
        }
    }
}

