package com.scccy.service.feishu.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.lark.oapi.Client;
import com.lark.oapi.core.request.RequestOptions;
import com.lark.oapi.service.authen.v1.model.CreateOidcAccessTokenReq;
import com.lark.oapi.service.authen.v1.model.CreateOidcAccessTokenReqBody;
import com.lark.oapi.service.authen.v1.model.CreateOidcAccessTokenResp;
import com.lark.oapi.service.authen.v1.model.GetUserInfoResp;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import com.scccy.common.base.manager.OkHttpManager;
import com.scccy.common.modules.enums.HTTPS;
import com.scccy.service.feishu.config.FeishuProperties;
import com.scccy.service.feishu.dto.FeishuQRLoginDto;
import com.scccy.service.feishu.manager.FeishuApiClient;
import com.scccy.service.feishu.service.FeishuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeishuServiceImpl implements FeishuService {

    private final OkHttpManager okHttpManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FeishuApiClient feishuApiClient;
    private final FeishuProperties feishuProperties;

    @Override
    public Map<String, String> refreshToken() throws IOException {
        Map<String, Object> param = Map.of(
                "app_id", feishuProperties.getAppId(),
                "app_secret", feishuProperties.getAppSecret()
        );
        Map<String, Object> headers = Map.of(
                "Content-Type", HTTPS.CONTENT_TYPE.getCode()

        );
        JSONObject post = okHttpManager.post(feishuProperties.getLarkBaseUrl()+ "/auth/v3/app_access_token/internal", param, headers);
        String tenantAccessToken = post.getString("tenant_access_token");
        String appAccessToken = post.getString("app_access_token");
        Integer expire = post.getInteger("expire");
        redisTemplate.opsForValue().set("feishu:token:tenant_access_token", tenantAccessToken, expire, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set("feishu:token:app_access_token", appAccessToken, expire, TimeUnit.SECONDS);

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
    public Boolean groupChatSend(String chatId, String openId, String chatData) throws Exception {
        // 使用注入的 client
        Client client = feishuApiClient.getClient();

        // 拼接 content
        String content = String.format("{\"elements\":[{\"tag\":\"markdown\",\"content\":\"%s\\n\\n <at id=\\\"%s\\\"></at>\"}]}", chatData, openId);

        // 创建请求对象
        CreateMessageReq req = CreateMessageReq.newBuilder()
                .receiveIdType("chat_id")
                .createMessageReqBody(CreateMessageReqBody.newBuilder()
                        .receiveId(chatId)
                        .msgType("interactive")
                        .content(content)
                        .build())
                .build();

        // 发起请求
        CreateMessageResp resp = client.im().v1().message().create(req);


        // 处理服务端错误
        JSONObject errorJson = feishuApiClient.handleResponse(resp);
        // 有错误返回 false
        return errorJson == null;

    }

    @Override
    public Boolean groupChatSend(String chatId,  String chatData) throws Exception {
        // 使用注入的 client
        Client client = feishuApiClient.getClient();

        // 拼接 content
        String content = String.format("{\"elements\":[{\"tag\":\"markdown\",\"content\":\"%s\"}]}", chatData);

        // 创建请求对象
        CreateMessageReq req = CreateMessageReq.newBuilder()
                .receiveIdType("chat_id")
                .createMessageReqBody(CreateMessageReqBody.newBuilder()
                        .receiveId(chatId)
                        .msgType("interactive")
                        .content(content)
                        .build())
                .build();

        // 发起请求
        CreateMessageResp resp = client.im().v1().message().create(req);


        // 处理服务端错误
        JSONObject errorJson = feishuApiClient.handleResponse(resp);
        // 有错误返回 false
        return errorJson == null;

    }

    @Override
    public JSONObject getTokenInfo(FeishuQRLoginDto feishuQRLoginDto) throws Exception {
        Client client = feishuApiClient.getClient();
        // 创建请求对象
        CreateOidcAccessTokenReq req = CreateOidcAccessTokenReq.newBuilder()
                .createOidcAccessTokenReqBody(CreateOidcAccessTokenReqBody.newBuilder()
                        .grantType("authorization_code")
                        .code(feishuQRLoginDto.getCode())
                        .build())
                .build();

        // 发起请求
        CreateOidcAccessTokenResp resp = client.authen().v1().oidcAccessToken().create(req);

        // 处理服务端错误
        JSONObject x = feishuApiClient.handleResponse(resp);
        if (x != null) return x;

        // 业务数据处理
        return feishuApiClient.convertToJsonObject(resp.getData());
    }

    @Override
    public JSONObject getUserInfo(String userToken) throws Exception {
        // 使用注入的 client
        Client client = feishuApiClient.getClient();
        // 创建请求对象
        // 发起请求
        GetUserInfoResp resp = client.authen().v1().userInfo().get(RequestOptions.newBuilder()
                .userAccessToken(userToken)
                .build());

        JSONObject x = feishuApiClient.handleResponse(resp);
        if (x != null) return x;
        // 业务数据处理
        return feishuApiClient.convertToJsonObject(resp.getData());

    }


    //---------------单点登入
    @Override
    public Map<String, Object> qrLogin(FeishuQRLoginDto feishuQRLoginDto) throws Exception {

        JSONObject tokenInfo = getTokenInfo(feishuQRLoginDto);
        String accessToken = tokenInfo.getString("access_token");
        String refreshToken = tokenInfo.getString("refresh_token");

        Integer expiresIn = tokenInfo.getInteger("expires_in");
        Integer refreshExpiresIn = tokenInfo.getInteger("refresh_expires_in");
        String scope = tokenInfo.getString("scope");

        JSONObject userInfo = getUserInfo(accessToken);
        String openId = userInfo.getString("open_id");
        String userId = userInfo.getString("user_id");
        String name = userInfo.getString("name");

        if (
                redisTemplate.opsForValue().get("feishu:" + openId + ":accessToken") == null
        ) {
            redisTemplate.opsForValue().set("feishu:" + openId + ":accessToken", accessToken, expiresIn, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set("feishu:" + openId + ":refreshToken", refreshToken, refreshExpiresIn, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set("feishu:" + openId + ":scope", scope, expiresIn, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set("feishu:" + openId + ":name", name);
            redisTemplate.opsForValue().set("feishu:" + openId + ":userId", userId);
        }

        return new HashMap<>(Map.of(
                "tokenInfo", tokenInfo,
                "qrUserInfo", userInfo)
        );
    }
}
