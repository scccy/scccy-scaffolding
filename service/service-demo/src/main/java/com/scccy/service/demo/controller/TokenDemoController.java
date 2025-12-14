package com.scccy.service.demo.controller;

import com.scccy.common.modules.annotation.Anonymous;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.demo.fegin.AuthTokenClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Token 获取测试 Controller
 * <p>
 * 用于测试和模拟获取 OAuth2 Token
 * <p>
 * 注意：此接口仅用于测试，实际生产环境应该由前端直接调用 Authorization Server
 *
 * @author scccy
 */
@Slf4j
@Tag(name = "Token 测试", description = "OAuth2 Token 获取测试接口")
@RequestMapping("/demo/token")
@RestController
public class TokenDemoController {

    @Autowired
    private AuthTokenClient authTokenClient;

    /**
     * 系统内置客户端凭证（用于内部用户 Token 模拟）
     * <p>
     * 注意：这些凭证应该配置在配置文件中，不要硬编码
     * 仅用于内部系统、测试环境使用
     */
    @Value("${demo.token.internal-client-id:internal_user_client}")
    private String internalClientId;

    @Value("${demo.token.internal-client-secret:internal_user_secret}")
    private String internalClientSecret;

    /**
     * 构建 Basic 认证头
     *
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥
     * @return Basic 认证头（格式：Basic base64(client_id:client_secret)）
     */
    private String buildBasicAuth(String clientId, String clientSecret) {
        String credentials = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 使用客户端凭证模式获取 Token
     * <p>
     * 这是 OAuth2 客户端凭证模式的简化封装，用于测试
     *
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥
     * @return Token 响应
     */
    @Anonymous
    @Operation(summary = "获取 Token（客户端凭证模式）", description = "使用客户端凭证模式获取 OAuth2 Token")
    @PostMapping("/client-credentials")
    public ResultData<Map<String, Object>> getTokenByClientCredentials(
            @Parameter(description = "客户端ID", required = true) @RequestParam String clientId,
            @Parameter(description = "客户端密钥", required = true) @RequestParam String clientSecret) {
        
        log.info("获取 Token - 客户端凭证模式: clientId={}", clientId);
        
        try {
            // 构建 Basic 认证头
            String basicAuth = buildBasicAuth(clientId, clientSecret);
            
            // 使用 Feign 调用 Token 端点
            Map<String, Object> tokenResponse = authTokenClient.getTokenByClientCredentials(basicAuth, "client_credentials");
            
            // 检查是否有错误
            if (tokenResponse.containsKey("error")) {
                log.error("Token 获取失败: clientId={}, error={}", clientId, tokenResponse.get("error_description"));
                return ResultData.fail(500, "Token 获取失败: " + tokenResponse.get("error_description"), tokenResponse);
            }
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", tokenResponse);
            data.put("access_token", tokenResponse.get("access_token"));
            data.put("token_type", tokenResponse.get("token_type"));
            data.put("expires_in", tokenResponse.get("expires_in"));
            data.put("message", "Token 获取成功");
            
            log.info("Token 获取成功: clientId={}, expiresIn={}", clientId, tokenResponse.get("expires_in"));
            
            return ResultData.ok("Token 获取成功", data);
        } catch (Exception e) {
            log.error("Token 获取异常: clientId={}, error={}", clientId, e.getMessage(), e);
            return ResultData.fail("Token 获取异常: " + e.getMessage());
        }
    }

    /**
     * 使用用户名密码模式获取 Token（如果支持）
     * <p>
     * 注意：此接口需要 Authorization Server 支持密码模式
     *
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥
     * @param username     用户名
     * @param password     密码
     * @return Token 响应
     */
    @Anonymous
    @Operation(summary = "获取 Token（密码模式）", description = "使用用户名密码模式获取 OAuth2 Token（需要 Authorization Server 支持）")
    @PostMapping("/password")
    public ResultData<Map<String, Object>> getTokenByPassword(
            @Parameter(description = "客户端ID", required = true) @RequestParam String clientId,
            @Parameter(description = "客户端密钥", required = true) @RequestParam String clientSecret,
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "密码", required = true) @RequestParam String password) {
        
        log.info("获取 Token - 密码模式: clientId={}, username={}", clientId, username);
        
        try {
            // 构建 Basic 认证头
            String basicAuth = buildBasicAuth(clientId, clientSecret);
            
            // 使用 Feign 调用 Token 端点
            Map<String, Object> tokenResponse = authTokenClient.getTokenByPassword(basicAuth, "password", username, password);
            
            // 检查是否有错误
            if (tokenResponse.containsKey("error")) {
                log.error("Token 获取失败: clientId={}, username={}, error={}", 
                    clientId, username, tokenResponse.get("error_description"));
                return ResultData.fail(500, "Token 获取失败: " + tokenResponse.get("error_description"), tokenResponse);
            }
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", tokenResponse);
            data.put("access_token", tokenResponse.get("access_token"));
            data.put("token_type", tokenResponse.get("token_type"));
            data.put("expires_in", tokenResponse.get("expires_in"));
            data.put("refresh_token", tokenResponse.get("refresh_token"));
            data.put("message", "Token 获取成功");
            
            log.info("Token 获取成功: clientId={}, username={}, expiresIn={}", 
                clientId, username, tokenResponse.get("expires_in"));
            
            return ResultData.ok("Token 获取成功", data);
        } catch (Exception e) {
            log.error("Token 获取异常: clientId={}, username={}, error={}", clientId, username, e.getMessage(), e);
            return ResultData.fail("Token 获取异常: " + e.getMessage());
        }
    }

    /**
     * 刷新 Token
     * <p>
     * 使用 refresh_token 刷新 Access Token
     *
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥
     * @param refreshToken 刷新 Token
     * @return Token 响应
     */
    @Anonymous
    @Operation(summary = "刷新 Token", description = "使用 refresh_token 刷新 Access Token")
    @PostMapping("/refresh")
    public ResultData<Map<String, Object>> refreshToken(
            @Parameter(description = "客户端ID", required = true) @RequestParam String clientId,
            @Parameter(description = "客户端密钥", required = true) @RequestParam String clientSecret,
            @Parameter(description = "刷新 Token", required = true) @RequestParam String refreshToken) {
        
        log.info("刷新 Token: clientId={}", clientId);
        
        try {
            // 构建 Basic 认证头
            String basicAuth = buildBasicAuth(clientId, clientSecret);
            
            // 使用 Feign 调用 Token 端点
            Map<String, Object> tokenResponse = authTokenClient.refreshToken(basicAuth, "refresh_token", refreshToken);
            
            // 检查是否有错误
            if (tokenResponse.containsKey("error")) {
                log.error("Token 刷新失败: clientId={}, error={}", clientId, tokenResponse.get("error_description"));
                return ResultData.fail(500, "Token 刷新失败: " + tokenResponse.get("error_description"), tokenResponse);
            }
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", tokenResponse);
            data.put("access_token", tokenResponse.get("access_token"));
            data.put("token_type", tokenResponse.get("token_type"));
            data.put("expires_in", tokenResponse.get("expires_in"));
            data.put("refresh_token", tokenResponse.get("refresh_token"));
            data.put("message", "Token 刷新成功");
            
            log.info("Token 刷新成功: clientId={}, expiresIn={}", clientId, tokenResponse.get("expires_in"));
            
            return ResultData.ok("Token 刷新成功", data);
        } catch (Exception e) {
            log.error("Token 刷新异常: clientId={}, error={}", clientId, e.getMessage(), e);
            return ResultData.fail("Token 刷新异常: " + e.getMessage());
        }
    }

    /**
     * 内部用户 Token 模拟（无需 client_id 和 client_secret）
     * <p>
     * 使用系统内置客户端凭证，仅需用户名和密码即可获取 Token
     * <p>
     * 适用于：
     * - 内部系统用户登录
     * - 测试环境快速获取 Token
     * - 开发环境模拟用户登录
     * <p>
     * 注意：此接口仅用于内部使用，不对外暴露 client_id 和 client_secret
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @return Token 响应
     */
    @Anonymous
    @Operation(summary = "内部用户 Token 模拟", description = "使用系统内置客户端凭证，仅需用户名和密码即可获取 Token（无需 client_id 和 client_secret）")
    @PostMapping("/internal/user")
    public ResultData<Map<String, Object>> getTokenByInternalUser(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "密码（明文）", required = true) @RequestParam String password) {
        
        log.info("内部用户 Token 模拟: username={}, internalClientId={}", username, internalClientId);
        
        try {
            // 使用系统内置客户端凭证构建 Basic 认证头
            String basicAuth = buildBasicAuth(internalClientId, internalClientSecret);
            
            // 使用 Feign 调用 Token 端点
            Map<String, Object> tokenResponse = authTokenClient.getTokenByPassword(basicAuth, "password", username, password);
            
            // 检查是否有错误
            if (tokenResponse.containsKey("error")) {
                log.error("内部用户 Token 获取失败: username={}, error={}", 
                    username, tokenResponse.get("error_description"));
                
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("error", tokenResponse.get("error"));
                errorData.put("error_description", tokenResponse.get("error_description"));
                errorData.put("message", "Token 获取失败，请检查用户名和密码是否正确");
                
                return ResultData.fail(500, "Token 获取失败: " + tokenResponse.get("error_description"), errorData);
            }
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", tokenResponse);
            data.put("access_token", tokenResponse.get("access_token"));
            data.put("token_type", tokenResponse.get("token_type"));
            data.put("expires_in", tokenResponse.get("expires_in"));
            data.put("refresh_token", tokenResponse.get("refresh_token"));
            data.put("message", "Token 获取成功（内部用户模式）");
            data.put("note", "使用系统内置客户端凭证，无需传递 client_id 和 client_secret");
            
            log.info("内部用户 Token 获取成功: username={}, expiresIn={}", 
                username, tokenResponse.get("expires_in"));
            
            return ResultData.ok("Token 获取成功", data);
        } catch (Exception e) {
            log.error("内部用户 Token 获取异常: username={}, error={}", username, e.getMessage(), e);
            return ResultData.fail("Token 获取异常: " + e.getMessage());
        }
    }

    /**
     * 获取 Token 使用说明
     *
     * @return 使用说明
     */
    @Anonymous
    @Operation(summary = "Token 获取说明", description = "获取 Token 的使用说明")
    @GetMapping("/help")
    public ResultData<Map<String, Object>> getTokenHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("title", "OAuth2 Token 获取说明");
        help.put("authServerService", "service-auth");
        help.put("endpoints", Map.of(
            "internalUser", "/demo/token/internal/user",
            "clientCredentials", "/demo/token/client-credentials",
            "password", "/demo/token/password",
            "refresh", "/demo/token/refresh"
        ));
        help.put("examples", Map.of(
            "internalUser", Map.of(
                "method", "POST",
                "url", "/demo/token/internal/user",
                "description", "内部用户 Token 模拟（推荐，无需 client_id 和 client_secret）",
                "params", Map.of(
                    "username", "admin",
                    "password", "password"
                ),
                "curl", "curl -X POST http://localhost:30000/demo/token/internal/user?username=admin&password=password"
            ),
            "clientCredentials", Map.of(
                "method", "POST",
                "url", "/demo/token/client-credentials",
                "params", Map.of(
                    "clientId", "test_1",
                    "clientSecret", "1234567"
                ),
                "curl", "curl -X POST http://localhost:30000/demo/token/client-credentials?clientId=test_1&clientSecret=1234567"
            ),
            "password", Map.of(
                "method", "POST",
                "url", "/demo/token/password",
                "params", Map.of(
                    "clientId", "test_1",
                    "clientSecret", "1234567",
                    "username", "admin",
                    "password", "password"
                )
            ),
            "refresh", Map.of(
                "method", "POST",
                "url", "/demo/token/refresh",
                "params", Map.of(
                    "clientId", "test_1",
                    "clientSecret", "1234567",
                    "refreshToken", "your_refresh_token"
                )
            )
        ));
        help.put("notes", Map.of(
            "internalUser", "内部用户 Token 模拟接口，使用系统内置客户端凭证，仅需用户名和密码，适合内部系统和测试环境使用",
            "other", "其他接口仅用于测试，实际生产环境应该由前端直接调用 Authorization Server"
        ));
        
        return ResultData.ok(help);
    }
}

