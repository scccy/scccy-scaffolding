package com.scccy.service.auth.service;

import com.alibaba.fastjson2.JSONObject;
import com.scccy.service.auth.dto.FeishuQRLoginDto;

import java.io.IOException;
import java.util.Map;

public interface FeishuService {
    /**
     * 获取应用访问令牌
     */
    Map<String, String>  refreshToken() throws IOException;

//    void getTokenInfo(FeishuQRLoginDto feishuQRLoginDto) throws Exception;
    JSONObject getTokenInfo(FeishuQRLoginDto feishuQRLoginDto) throws Exception;
    JSONObject getUserInfo(String userToken) throws Exception;
    Map<String,Object> qrLogin(FeishuQRLoginDto feishuQRLoginDto) throws Exception;



    String getAppAccessToken() throws IOException ;
    String getTenantAccessToken() throws IOException;
}
