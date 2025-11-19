package com.scccy.service.demo.controller;

import com.scccy.common.modules.annotation.Anonymous;
import com.scccy.common.modules.annotation.CurrentUser;
import com.scccy.common.modules.dto.CurrentUserInfo;
import com.scccy.common.modules.dto.ResultData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 权限控制测试 Controller
 * <p>
 * 用于测试 @PreAuthorize 和 @CurrentUser 注解的功能
 * <p>
 * 测试场景：
 * 1. 使用 @PreAuthorize("@ss.hasPermi('...')") 进行权限控制
 * 2. 使用 @CurrentUser CurrentUserInfo user 获取用户信息
 * 3. 组合使用两者
 *
 * @author scccy
 */
@Slf4j
@Tag(name = "权限测试", description = "权限控制和用户信息注入测试")
@RequestMapping("/demo/permission")
@RestController
public class PermissionTestController {

    /**
     * 测试1：公开接口（不需要权限）
     * <p>
     * 可以不带 Token 访问，用于测试 Gateway 是否正确放行
     * <p>
     * 使用 @Anonymous 注解标记为公开接口，Gateway 会根据路径模式（包含 /public）自动放行
     */
    @Anonymous
    @Operation(summary = "公开接口", description = "不需要权限，可以不带 Token 访问")
    @GetMapping("/public")
    public ResultData<Map<String, Object>> publicEndpoint() {
        log.info("访问公开接口");
        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是一个公开接口，不需要权限");
        data.put("timestamp", System.currentTimeMillis());
        return ResultData.ok(data);
    }

    /**
     * 测试2：需要认证但不需要权限检查
     * <p>
     * 需要带 Token，但不需要特定权限
     */
    @Operation(summary = "需要认证的接口", description = "需要带 Token，但不需要特定权限")
    @GetMapping("/authenticated")
    public ResultData<Map<String, Object>> authenticatedEndpoint(@CurrentUser CurrentUserInfo user) {
        log.info("访问需要认证的接口，用户: {}", user != null ? user.getUsername() : "unknown");
        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是一个需要认证的接口");
        data.put("user", user);
        data.put("timestamp", System.currentTimeMillis());
        return ResultData.ok(data);
    }

    /**
     * 测试3：需要特定权限（使用 @PreAuthorize）
     * <p>
     * 需要 system:demo:query 权限
     */
    @Operation(summary = "需要查询权限", description = "需要 system:demo:query 权限")
    @PreAuthorize("@ss.hasPermi('system:demo:query')")
    @GetMapping("/query")
    public ResultData<Map<String, Object>> queryEndpoint(@CurrentUser CurrentUserInfo user) {
        log.info("用户 {} (ID: {}) 访问查询接口", user != null ? user.getUsername() : "unknown", user != null ? user.getUserId() : null);
        Map<String, Object> data = new HashMap<>();
        data.put("message", "查询成功");
        data.put("user", user);
        data.put("permissions", user != null ? user.getAuthorities() : null);
        data.put("timestamp", System.currentTimeMillis());
        return ResultData.ok(data);
    }

    /**
     * 测试4：需要添加权限（使用 @PreAuthorize）
     * <p>
     * 需要 system:demo:add 权限
     */
    @Operation(summary = "需要添加权限", description = "需要 system:demo:add 权限")
    @PreAuthorize("@ss.hasPermi('system:demo:add')")
    @PostMapping("/add")
    public ResultData<Map<String, Object>> addEndpoint(@RequestBody Map<String, Object> request, @CurrentUser CurrentUserInfo user) {
        log.info("用户 {} (ID: {}) 访问添加接口", user != null ? user.getUsername() : "unknown", user != null ? user.getUserId() : null);
        Map<String, Object> data = new HashMap<>();
        data.put("message", "添加成功");
        data.put("request", request);
        data.put("user", user);
        data.put("timestamp", System.currentTimeMillis());
        return ResultData.ok(data);
    }

    /**
     * 测试5：需要编辑权限（使用 @PreAuthorize）
     * <p>
     * 需要 system:demo:edit 权限
     */
    @Operation(summary = "需要编辑权限", description = "需要 system:demo:edit 权限")
    @PreAuthorize("@ss.hasPermi('system:demo:edit')")
    @PutMapping("/edit/{id}")
    public ResultData<Map<String, Object>> editEndpoint(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @CurrentUser CurrentUserInfo user) {
        log.info("用户 {} (ID: {}) 编辑资源: {}", user != null ? user.getUsername() : "unknown", user != null ? user.getUserId() : null, id);
        Map<String, Object> data = new HashMap<>();
        data.put("message", "编辑成功");
        data.put("id", id);
        data.put("request", request);
        data.put("user", user);
        data.put("timestamp", System.currentTimeMillis());
        return ResultData.ok(data);
    }

    /**
     * 测试6：需要删除权限（使用 @PreAuthorize）
     * <p>
     * 需要 system:demo:remove 权限
     */
    @Operation(summary = "需要删除权限", description = "需要 system:demo:remove 权限")
    @PreAuthorize("@ss.hasPermi('system:demo:remove')")
    @DeleteMapping("/remove/{id}")
    public ResultData<Map<String, Object>> removeEndpoint(@PathVariable Long id, @CurrentUser CurrentUserInfo user) {
        log.info("用户 {} (ID: {}) 删除资源: {}", user != null ? user.getUsername() : "unknown", user != null ? user.getUserId() : null, id);
        Map<String, Object> data = new HashMap<>();
        data.put("message", "删除成功");
        data.put("id", id);
        data.put("user", user);
        data.put("timestamp", System.currentTimeMillis());
        return ResultData.ok(data);
    }

    /**
     * 测试7：需要角色权限（使用 @PreAuthorize）
     * <p>
     * 需要 ROLE_ADMIN 角色
     */
    @Operation(summary = "需要管理员角色", description = "需要 ROLE_ADMIN 角色")
    @PreAuthorize("@ss.hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public ResultData<Map<String, Object>> adminEndpoint(@CurrentUser CurrentUserInfo user) {
        log.info("管理员 {} (ID: {}) 访问管理接口", user != null ? user.getUsername() : "unknown", user != null ? user.getUserId() : null);
        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是管理员专用接口");
        data.put("user", user);
        data.put("roles", user != null ? user.getAuthorities() : null);
        data.put("timestamp", System.currentTimeMillis());
        return ResultData.ok(data);
    }

    /**
     * 测试8：需要多个权限中的任意一个（使用 @PreAuthorize）
     * <p>
     * 需要 system:demo:query 或 system:demo:add 权限
     */
    @Operation(summary = "需要多个权限中的任意一个", description = "需要 system:demo:query 或 system:demo:add 权限")
    @PreAuthorize("@ss.hasAnyPermi('system:demo:query', 'system:demo:add')")
    @GetMapping("/any")
    public ResultData<Map<String, Object>> anyPermissionEndpoint(@CurrentUser CurrentUserInfo user) {
        log.info("用户 {} (ID: {}) 访问任意权限接口", user != null ? user.getUsername() : "unknown", user != null ? user.getUserId() : null);
        Map<String, Object> data = new HashMap<>();
        data.put("message", "您有查询或添加权限");
        data.put("user", user);
        data.put("permissions", user != null ? user.getAuthorities() : null);
        data.put("timestamp", System.currentTimeMillis());
        return ResultData.ok(data);
    }

    /**
     * 测试9：获取当前用户信息（不需要权限检查）
     * <p>
     * 仅用于测试 @CurrentUser 注解的功能
     */
    @Operation(summary = "获取当前用户信息", description = "不需要权限，仅用于测试 @CurrentUser 注解")
    @GetMapping("/current-user")
    public ResultData<Map<String, Object>> currentUserEndpoint(@CurrentUser CurrentUserInfo user) {
        log.info("获取当前用户信息: {}", user != null ? user.getUsername() : "unknown");
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("hasQueryPermi", user != null && user.hasPermi("system:demo:query"));
        data.put("hasAddPermi", user != null && user.hasPermi("system:demo:add"));
        data.put("hasAdminRole", user != null && user.hasRole("ROLE_ADMIN"));
        data.put("timestamp", System.currentTimeMillis());
        return ResultData.ok(data);
    }

    /**
     * 测试10：可选用户信息（required = false）
     * <p>
     * 即使没有用户信息也能访问，用于测试可选参数
     */
    @Operation(summary = "可选用户信息", description = "即使没有用户信息也能访问")
    @GetMapping("/optional-user")
    public ResultData<Map<String, Object>> optionalUserEndpoint(@CurrentUser(required = false) CurrentUserInfo user) {
        log.info("访问可选用户信息接口，用户: {}", user != null ? user.getUsername() : "null");
        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是可选用户信息接口");
        data.put("user", user);
        data.put("hasUser", user != null);
        data.put("timestamp", System.currentTimeMillis());
        return ResultData.ok(data);
    }
}

