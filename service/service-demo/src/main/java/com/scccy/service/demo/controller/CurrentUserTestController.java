package com.scccy.service.demo.controller;

import com.scccy.common.modules.annotation.CurrentUser;
import com.scccy.common.modules.dto.CurrentUserInfo;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.common.modules.utils.UserContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 当前用户测试 Controller
 * <p>
 * 用于测试 @CurrentUser 注解的两种使用方式：
 * <ul>
 *     <li>方式一：方法注解 + UserContextUtils（AOP 环绕）</li>
 *     <li>方式二：参数注解（HandlerMethodArgumentResolver）</li>
 * </ul>
 *
 * @author scccy
 */
@Slf4j
@Tag(name = "当前用户测试", description = "测试 @CurrentUser 注解的两种使用方式")
@RequestMapping("/demo/current-user")
@RestController
public class CurrentUserTestController {

    /**
     * 测试1：使用方法注解（AOP 环绕方式）
     * <p>
     * 使用 @CurrentUser 注解在方法上，通过 UserContextUtils 获取用户信息
     */
    @CurrentUser
    @Operation(summary = "方法注解方式", description = "使用 @CurrentUser 方法注解，通过 UserContextUtils 获取用户信息")
    @GetMapping("/method-annotation")
    public ResultData<Map<String, Object>> methodAnnotation() {
        // 通过工具类获取用户信息
        CurrentUserInfo user = UserContextUtils.getCurrentUser();
        
        log.info("方法注解方式 - 用户: {}", user != null ? user.getUsername() : "unknown");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "使用方法注解方式获取用户信息");
        data.put("user", user);
        data.put("userId", UserContextUtils.getCurrentUserId());
        data.put("username", UserContextUtils.getCurrentUsername());
        data.put("authorities", UserContextUtils.getCurrentAuthorities());
        data.put("hasQueryPermi", UserContextUtils.hasPermi("system:demo:query"));
        data.put("hasAdminRole", UserContextUtils.hasRole("ROLE_ADMIN"));
        data.put("timestamp", System.currentTimeMillis());
        
        return ResultData.ok(data);
    }

    /**
     * 测试2：使用参数注解（HandlerMethodArgumentResolver 方式）
     * <p>
     * 使用 @CurrentUser 注解在方法参数上，直接注入用户信息
     */
    @Operation(summary = "参数注解方式", description = "使用 @CurrentUser 参数注解，直接注入用户信息")
    @GetMapping("/parameter-annotation")
    public ResultData<Map<String, Object>> parameterAnnotation(@CurrentUser CurrentUserInfo user) {
        log.info("参数注解方式 - 用户: {}", user != null ? user.getUsername() : "unknown");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "使用参数注解方式获取用户信息");
        data.put("user", user);
        data.put("userId", user != null ? user.getUserId() : null);
        data.put("username", user != null ? user.getUsername() : null);
        data.put("authorities", user != null ? user.getAuthorities() : null);
        data.put("hasQueryPermi", user != null && user.hasPermi("system:demo:query"));
        data.put("hasAdminRole", user != null && user.hasRole("ROLE_ADMIN"));
        data.put("timestamp", System.currentTimeMillis());
        
        return ResultData.ok(data);
    }

    /**
     * 测试3：方法注解 + 可选用户信息（required = false）
     */
    @CurrentUser(required = false)
    @Operation(summary = "方法注解（可选）", description = "使用 @CurrentUser 方法注解，用户信息可选")
    @GetMapping("/method-annotation-optional")
    public ResultData<Map<String, Object>> methodAnnotationOptional() {
        CurrentUserInfo user = UserContextUtils.getCurrentUser();
        
        log.info("方法注解（可选）方式 - 用户: {}", user != null ? user.getUsername() : "null");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "方法注解方式（可选用户信息）");
        data.put("user", user);
        data.put("hasUser", user != null);
        data.put("timestamp", System.currentTimeMillis());
        
        return ResultData.ok(data);
    }

    /**
     * 测试4：参数注解 + 可选用户信息（required = false）
     */
    @Operation(summary = "参数注解（可选）", description = "使用 @CurrentUser 参数注解，用户信息可选")
    @GetMapping("/parameter-annotation-optional")
    public ResultData<Map<String, Object>> parameterAnnotationOptional(
            @CurrentUser(required = false) CurrentUserInfo user) {
        log.info("参数注解（可选）方式 - 用户: {}", user != null ? user.getUsername() : "null");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "参数注解方式（可选用户信息）");
        data.put("user", user);
        data.put("hasUser", user != null);
        data.put("timestamp", System.currentTimeMillis());
        
        return ResultData.ok(data);
    }

    /**
     * 测试5：混合使用（方法注解 + 参数注解）
     * <p>
     * 注意：两种方式可以同时使用，但推荐只使用一种方式
     */
    @CurrentUser
    @Operation(summary = "混合使用", description = "同时使用方法注解和参数注解（不推荐）")
    @GetMapping("/mixed")
    public ResultData<Map<String, Object>> mixedUsage(@CurrentUser CurrentUserInfo userParam) {
        // 通过工具类获取（方法注解方式）
        CurrentUserInfo userFromContext = UserContextUtils.getCurrentUser();
        
        // 通过参数获取（参数注解方式）
        CurrentUserInfo userFromParam = userParam;
        
        log.info("混合使用 - 上下文用户: {}, 参数用户: {}", 
            userFromContext != null ? userFromContext.getUsername() : "null",
            userFromParam != null ? userFromParam.getUsername() : "null");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "混合使用两种方式（不推荐）");
        data.put("userFromContext", userFromContext);
        data.put("userFromParam", userFromParam);
        data.put("isSame", userFromContext != null && userFromParam != null 
            && userFromContext.getUserId().equals(userFromParam.getUserId()));
        data.put("timestamp", System.currentTimeMillis());
        
        return ResultData.ok(data);
    }
}

