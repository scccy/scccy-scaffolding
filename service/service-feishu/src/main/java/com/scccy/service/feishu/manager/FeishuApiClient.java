package com.scccy.service.feishu.manager;

import com.alibaba.fastjson2.JSONObject;
import com.lark.oapi.Client;
import com.lark.oapi.core.response.BaseResponse;
import com.scccy.service.feishu.config.FeishuProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 飞书API客户端
 * 提供基础的Client连接管理
 * 作为Spring Bean进行管理
 * 
 * @author scccy
 * @since 2025-10-10
 */
@Component
@Slf4j
public class FeishuApiClient {
    
    private Client client;
    private FeishuProperties feishuProperties;

    /**
     * 初始化飞书客户端
     */
    @PostConstruct
    public void init() {
        String appId = feishuProperties.getAppId();
        String appSecret = feishuProperties.getAppSecret();
        
        this.client = Client.newBuilder(appId, appSecret).build();
        log.info("飞书API客户端初始化完成，AppId: {}", appId);
    }
    
    /**
     * 获取飞书客户端实例
     * 
     * @return Client 飞书客户端实例
     */
    public Client getClient() {
        if (client == null) {
            log.error("飞书客户端未初始化，请检查配置");
            throw new IllegalStateException("飞书客户端未初始化");
        }
        return client;
    }
    
    /**
     * 检查客户端是否已初始化
     * 
     * @return boolean 是否已初始化
     */
    public boolean isInitialized() {
        return client != null;
    }
    
    /**
     * 处理飞书API响应错误
     * 检查响应是否成功，如果失败则返回错误对象
     * 
     * @param resp 飞书API响应对象
     * @return JSONObject 如果请求失败返回错误对象，成功返回null
     */
    @Nullable
    public JSONObject handleResponse(BaseResponse<?> resp) {
        if (!resp.success()) {
            // 将响应体转换成字符串
            String rawBody = new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8);
            log.error("飞书API调用失败: {}", rawBody);
            
            // 使用 fastjson2 解析为 JSONObject
            return JSONObject.parseObject(rawBody);
        }
        return null;
    }
    
    /**
     * 将响应数据转换为JSONObject
     * 
     * @param data 响应数据对象
     * @return JSONObject 转换后的JSON对象
     */
    public JSONObject convertToJsonObject(Object data) {
        return JSONObject.parseObject(JSONObject.toJSONString(data));
    }
}
