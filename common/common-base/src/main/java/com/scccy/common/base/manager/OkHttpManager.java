package com.scccy.common.base.manager;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * OkHttp3管理器 - 简化版本
 * 提供简洁的GET和POST方法，统一返回JSONObject
 *
 * @author scccy
 */
@Slf4j
public class OkHttpManager {

    private final OkHttpClient okHttpClient;

    public OkHttpManager(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * GET请求 - 返回JSONObject
     */
    public JSONObject get(String url) throws IOException {
        return get(url, null);
    }

    /**
     * GET请求 - 返回JSONObject（带请求头）
     */
    public JSONObject get(String url, Map<String, Object> headers) throws IOException {
        Request.Builder requestBuilder = new Request.Builder().url(url);

        if (headers != null) {
            headers.forEach((key, value) -> requestBuilder.addHeader(key, String.valueOf(value)));
        }

        Request request = requestBuilder.build();
        String responseBody = executeRequest(request);

        return parseResponse(responseBody);
    }

    /**
     * POST请求 - JSON数据，返回JSONObject
     */
    public JSONObject post(String url, Map<String, Object> param) throws IOException {
        return post(url, param, null);
    }

    /**
     * POST请求 - JSON数据，返回JSONObject（带请求头）
     */
    public JSONObject post(String url, Map<String, Object> param, Map<String, Object> headers) throws IOException {
        String jsonData = JSON.toJSONString(param);
        RequestBody requestBody = RequestBody.create(jsonData, MediaType.get("application/json; charset=utf-8"));

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);

        if (headers != null) {
            headers.forEach((key, value) -> requestBuilder.addHeader(key, String.valueOf(value)));
        }

        Request request = requestBuilder.build();
        String responseBody = executeRequest(request);

        return parseResponse(responseBody);
    }


    /**
     * 执行HTTP请求
     */
    private String executeRequest(Request request) throws IOException {
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP请求失败，状态码: " + response.code() + ", 消息: " + response.message());
            }

            ResponseBody responseBody = response.body();
            return responseBody != null ? responseBody.string() : "";
        }
    }

    /**
     * 解析响应数据为JSONObject
     */
    private JSONObject parseResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return new JSONObject();
        }

        try {
            return JSON.parseObject(responseBody);
        } catch (Exception e) {
            log.warn("JSON解析失败，返回原始字符串: {}", e.getMessage());
            JSONObject result = new JSONObject();
            result.put("raw_response", responseBody);
            return result;
        }
    }
} 