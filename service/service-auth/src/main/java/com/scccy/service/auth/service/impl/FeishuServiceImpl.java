package com.scccy.service.auth.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.scccy.service.base.manager.OkHttpManager;
import com.scccy.service.common.enums.HTTPS;
import com.scccy.service.common.enums.ThirdPath;
import com.scccy.service.auth.service.FeishuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;


import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeishuServiceImpl implements FeishuService {

    private final OkHttpManager okHttpManager;
    private final RedisTemplate<String, Object>  redisTemplate;

    @Override
    public Map<String, String> refreshToken() throws IOException {
        Map<String, Object> param = Map.of(
                "app_id", ThirdPath.FEISHU_APP_ID.getCode(),
                "app_secret", ThirdPath.FEISHU_APP_SECRET.getCode()
        );
        Map<String, Object> headers = Map.of(
                "Content-Type", HTTPS.CONTENT_TYPE.getCode()

        );
        JSONObject post = okHttpManager.post(ThirdPath.FEISHU_LARK_BASE_URL.getCode() + "/auth/v3/app_access_token/internal", param, headers);
        String tenantAccessToken = post.getString("tenant_access_token");
        String appAccessToken = post.getString("app_access_token");
        redisTemplate.opsForValue().set("feishu:token:tenant_access_token",tenantAccessToken, 3600 + 50 * 60, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set("feishu:token:app_access_token",appAccessToken, 3600 + 50 * 60, TimeUnit.SECONDS);

        // 返回新生成的token
        return Map.of(
                "tenant_access_token", tenantAccessToken,
                "app_access_token", appAccessToken
        );
    }


    @Override
    public String getAppAccessToken() throws IOException {
        // 先从Redis中查询app_access_token
        String appAccessToken = (String) redisTemplate.opsForValue().get("feishu:token:app_access_token");
        if (appAccessToken != null && !appAccessToken.isEmpty()) {
            return appAccessToken;
        }
        // 如果Redis中没有，则调用refreshToken方法获取
        return refreshToken().get("app_access_token");

    }

    @Override
    public String getTenantAccessToken() throws IOException {
        // 先从Redis中查询tenant_access_token
        String tenantAccessToken = (String) redisTemplate.opsForValue().get("feishu:token:tenant_access_token");
        if (tenantAccessToken != null && !tenantAccessToken.isEmpty()) {
            return tenantAccessToken;
        }
        // 如果Redis中没有，则调用refreshToken方法获取
        return refreshToken().get("tenant_access_token");

    }




    @Override
    public Map<String, Object> getTokenInfo(Map<String, Object> params) throws IOException   {


        Map<String, Object> param = Map.of(
                "grant_type", "authorization_code",
                "code", ThirdPath.FEISHU_APP_SECRET.getCode()
        );
        Map<String, Object> headers = Map.of(
                "Content-Type", HTTPS.CONTENT_TYPE.getCode(),
                "Authorization", "Bearer " + getAppAccessToken()
        );



        return Map.of();
    }



}
