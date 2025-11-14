package com.scccy.common.base.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Feign 拦截器相关配置
 * <p>
 * 控制内部令牌注入的启用状态以及跳过的客户端列表。
 */
@Data
@ConfigurationProperties(prefix = "scccy.internal-token.feign")
public class InternalTokenFeignProperties {

    /**
     * 是否启用 Feign 拦截器自动注入 Authorization 头
     */
    private boolean enabled = true;

    /**
     * 需要跳过内部令牌注入的 Feign 客户端名称
     * <p>
     * 匹配 {@code @FeignClient(name = "...")} 中的 name/contextId
     */
    private List<String> skipClients = new ArrayList<>();
}

