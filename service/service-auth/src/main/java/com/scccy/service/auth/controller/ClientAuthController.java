package com.scccy.service.auth.controller;

import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.auth.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

/**
 * 客户端认证控制器
 * <p>
 * 处理三方服务（client_id）的登录和登出功能
 * <p>
 * 职责：
 * - 客户端登录：通过 OAuth2 client_credentials 流程获取 Token
 * - 客户端登出：将 JWT Token 加入黑名单（撤销 Token）
 * - Token 撤销：遵循 OAuth2 Token Revocation 规范（RFC 7009）
 *
 * @author scccy
 */
@Slf4j
@RestController
@Tag(name = "客户端认证", description = "三方服务（client_id）登录登出 API 接口")
public class ClientAuthController {

    @Resource
    private TokenBlacklistService tokenBlacklistService;

    /**
     * 客户端登出接口
     * <p>
     * 将当前客户端的 JWT Token 加入黑名单，后续 Gateway 会拦截该 Token
     * <p>
     * 注意：此接口需要携带有效的 JWT Token（Authorization: Bearer {token}）
     * 适用于 client_credentials 模式的客户端
     *
     * @param authentication 认证信息（包含 JWT Token）
     * @return 登出结果
     */
    @Operation(
            summary = "客户端登出",
            description = "将当前客户端的 JWT Token 加入黑名单，后续 Gateway 会拦截该 Token。需要携带有效的 JWT Token（client_credentials 模式）。"
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
    @PostMapping("/api/client/logout")
    @ResponseBody
    public ResultData<String> logout(Authentication authentication) {
        try {
            // 检查认证信息
            if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
                log.warn("客户端登出失败：未提供有效的 JWT Token");
                return ResultData.fail("未提供有效的 JWT Token");
            }

            // 获取 JWT Token
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtAuth.getToken();

            // 将 Token 加入黑名单
            tokenBlacklistService.addToBlacklist(jwt);

            String clientId = jwt.getClaimAsString("client_id");
            String jti = jwt.getId();
            log.info("客户端登出成功: clientId={}, jti={}", clientId, jti);

            return ResultData.ok("登出成功");
        } catch (Exception e) {
            log.error("客户端登出异常: {}", e.getMessage(), e);
            return ResultData.fail("登出失败: " + e.getMessage());
        }
    }

    /**
     * OAuth2 Token 撤销接口
     * <p>
     * 遵循 OAuth2 Token Revocation 规范（RFC 7009）
     * 将指定的 Token 加入黑名单
     * <p>
     * 支持两种方式：
     * 1. 从请求参数中获取 token（OAuth2 标准方式，暂不支持，需要解析 JWT）
     * 2. 从 Authorization header 中获取 token（如果请求已认证，推荐）
     * <p>
     * 注意：此接口主要用于三方服务撤销 Token
     *
     * @param token Token 字符串（可选，如果未提供则从请求中提取）
     * @param authentication 认证信息（可选，如果提供了 token 参数则不需要）
     * @return 撤销结果
     */
    @Operation(
            summary = "OAuth2 Token 撤销",
            description = "遵循 OAuth2 Token Revocation 规范（RFC 7009），将指定的 Token 加入黑名单。支持从请求参数或 Authorization header 中获取 token。主要用于三方服务撤销 Token。"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token 撤销成功",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "请求参数错误，未提供 token",
                    content = @Content(schema = @Schema(implementation = ResultData.class))
            )
    })
    @PostMapping("/oauth2/revoke")
    @ResponseBody
    public ResultData<String> revokeToken(
            @Parameter(description = "要撤销的 Token（可选，如果未提供则从请求中提取）")
            @RequestParam(name = "token", required = false) String token,
            Authentication authentication) {
        try {
            Jwt jwt = null;

            // 方式1：从请求参数中获取 token（需要解析 JWT token 字符串）
            if (token != null && !token.trim().isEmpty()) {
                // 注意：这里需要解析 JWT token 字符串
                // 由于 Spring Authorization Server 的 Token 是标准的 JWT 格式，
                // 可以使用 JwtDecoder 解析，但为了简化，暂时不支持
                log.warn("从请求参数中获取 token 的方式暂不支持，请使用 Authorization header");
                return ResultData.fail("暂不支持从请求参数中获取 token，请使用 Authorization header");
            }

            // 方式2：从认证信息中获取 token（推荐）
            if (authentication != null && authentication instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
                jwt = jwtAuth.getToken();
            }

            // 检查是否获取到 token
            if (jwt == null) {
                log.warn("Token 撤销失败：未提供有效的 Token");
                return ResultData.fail("未提供有效的 Token");
            }

            // 将 Token 加入黑名单
            tokenBlacklistService.addToBlacklist(jwt);

            String clientId = jwt.getClaimAsString("client_id");
            String jti = jwt.getId();
            log.info("Token 撤销成功: clientId={}, jti={}", clientId, jti);

            return ResultData.ok("Token 撤销成功");
        } catch (Exception e) {
            log.error("Token 撤销异常: {}", e.getMessage(), e);
            return ResultData.fail("Token 撤销失败: " + e.getMessage());
        }
    }
}

