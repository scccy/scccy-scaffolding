package com.scccy.service.feishu.service;

import com.alibaba.fastjson2.JSONObject;
import com.scccy.service.feishu.dto.FeishuQRLoginDto;


import java.io.IOException;
import java.util.Map;

public interface FeishuService {
    /**
     * 获取应用访问令牌
     */
    Map<String, String>  refreshToken() throws IOException;


    JSONObject getTokenInfo(FeishuQRLoginDto feishuQRLoginDto) throws Exception;
    JSONObject getUserInfo(String userToken) throws Exception;

//    单点登入
    Map<String,Object> qrLogin(FeishuQRLoginDto feishuQRLoginDto) throws Exception;



    String getAppAccessToken() throws IOException ;
    String getTenantAccessToken() throws IOException;

//    群聊消息发送
    Boolean groupChatSend(String chatId, String openId, String chatData)  throws Exception;
    Boolean groupChatSend(String chatId, String chatData)  throws Exception;
}
