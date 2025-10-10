package com.scccy.feishu.dto;

import lombok.Data;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Data
public class FeishuQRLoginDto {
    private String loginTime;
    private String redirect_uri;
    private String url;

    // 获取 code
    public String getCode() {
        return getQueryParam("code");
    }

    // 获取 state
    public String getState() {
        return getQueryParam("state");
    }

    // 通用方法：从 url 查询参数里提取 key 对应的值
    private String getQueryParam(String key) {
        if (url == null || !url.contains("?")) {
            return null;
        }
        String query = url.substring(url.indexOf("?") + 1);
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map.get(key);
    }
}
