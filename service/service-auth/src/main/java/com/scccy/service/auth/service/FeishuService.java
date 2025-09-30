package com.scccy.service.auth.service;

import java.io.IOException;
import java.util.Map;

public interface FeishuService {
    /**
     * 获取应用访问令牌
     */
    Map<String, String>  refreshToken() throws IOException;

    Map<String, Object> getTokenInfo(Map<String, Object> params) throws IOException;


    String getAppAccessToken() throws IOException ;
    String getTenantAccessToken() throws IOException;
}
