package com.scccy.service.auth.controller;


import com.github.xingfudeshi.knife4j.core.util.StrUtil;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.service.auth.domain.ScopeWithDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Tag(name = "OAuth2 授权", description = "OAuth2 授权相关 API 接口（前后端分离架构）")
public class AuthorizationController {

    @Resource
    private RegisteredClientRepository registeredClientRepository;

    @Resource
    private OAuth2AuthorizationConsentService authorizationConsentService;

    /**
     * 获取授权确认数据（前后端分离）
     * <p>
     * 返回授权确认所需的数据，前端根据这些数据渲染授权确认页面
     *
     * @param principal 认证信息
     * @param clientId  客户端ID
     * @param scope     授权范围
     * @param state     状态信息
     * @param userCode  设备码
     * @return 授权确认数据
     */
    @Operation(
            summary = "获取授权确认数据",
            description = "前后端分离架构：返回 OAuth2 授权确认所需的数据，前端根据这些数据渲染授权确认页面"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "返回授权确认数据", content = @Content),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "401", description = "未认证，需要先登录")
    })
    @GetMapping(value = "/oauth2/consent")
    public ResultData<Map<String, Object>> getConsentData(Principal principal,
                          @Parameter(description = "客户端ID", required = true)
                          @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
                          @Parameter(description = "授权范围，多个范围用空格分隔", required = true)
                          @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
                          @Parameter(description = "状态信息，用于防止 CSRF 攻击", required = true)
                          @RequestParam(OAuth2ParameterNames.STATE) String state,
                          @Parameter(description = "设备码（设备授权流程中使用）")
                          @RequestParam(name = OAuth2ParameterNames.USER_CODE, required = false) String userCode) {
        // 需要授权的scope
        Set<String> scopesToApprove = new HashSet<>();
        // 已授权的scope
        Set<String> previouslyApprovedScopes = new HashSet<>();
        // 查询client信息
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        Assert.notNull(registeredClient, clientId + ":客户端不存在");
        // 查询client授权的scope信息
        OAuth2AuthorizationConsent currentAuthorizationConsent = this.authorizationConsentService.findById(registeredClient.getId(), principal.getName());
        // 已授予的scope
        Set<String> authorizedScopes = Objects.isNull(currentAuthorizationConsent) ? Collections.emptySet() : currentAuthorizationConsent.getScopes();
        //
        for (String requestedScope : StringUtils.delimitedListToStringArray(scope, " ")) {
            if (OidcScopes.OPENID.equals(requestedScope)) {
                continue;
            }
            if (authorizedScopes.contains(requestedScope)) {
                previouslyApprovedScopes.add(requestedScope);
            } else {
                scopesToApprove.add(requestedScope);
            }
        }
        
        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("clientId", clientId);
        data.put("state", state);
        data.put("scopes", withDescription(scopesToApprove));
        data.put("previouslyApprovedScopes", withDescription(previouslyApprovedScopes));
        data.put("principalName", principal.getName());
        data.put("userCode", userCode);
        data.put("requestURI", StringUtils.hasText(userCode) ? "/oauth2/device_verification" : "/oauth2/authorize");
        
        return ResultData.ok(data);
    }

    /**
     * 获取设备验证信息（前后端分离）
     * <p>
     * 返回设备授权验证所需的信息，前端根据这些信息渲染设备验证页面
     *
     * @param userCode 用户码
     * @return 设备验证信息
     */
    @Operation(
            summary = "获取设备验证信息",
            description = "前后端分离架构：返回设备授权验证所需的信息，前端根据这些信息渲染设备验证页面"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "返回设备验证信息"),
            @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    @GetMapping("/oauth2/activate")
    public ResultData<Map<String, Object>> getActivateData(@Parameter(description = "用户码，用于设备授权流程")
                          @RequestParam(value = "user_code", required = false) String userCode) {
        Map<String, Object> data = new HashMap<>();
        data.put("userCode", userCode);
        data.put("verificationUrl", StrUtil.isNotBlank(userCode) 
            ? "/oauth2/device_verification?user_code=" + userCode 
            : "/oauth2/activate");
        return ResultData.ok(data);
    }

    /**
     * 设备验证成功信息（前后端分离）
     *
     * @return 验证成功信息
     */
    @Operation(
            summary = "设备验证成功信息",
            description = "前后端分离架构：返回设备授权验证成功的信息"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "返回设备验证成功信息")
    })
    @GetMapping(value = "/oauth2/activated", params = "success")
    public ResultData<Map<String, Object>> getActivatedData() {
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "设备授权验证成功");
        return ResultData.ok(data);
    }

    private static Set<ScopeWithDescription> withDescription(Set<String> scopes) {
        return scopes.stream().map(ScopeWithDescription::new).collect(Collectors.toSet());
    }
}