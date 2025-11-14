package com.scccy.common.base.feign;

import com.scccy.common.base.annotation.SkipInternalToken;
import com.scccy.common.base.config.properties.InternalTokenFeignProperties;
import com.scccy.common.base.manager.InternalTokenManager;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Target;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Feign 请求拦截器：为内部调用自动附加 Authorization 头
 */
@Slf4j
public class InternalTokenFeignRequestInterceptor implements RequestInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SKIP_HEADER = "X-Internal-Token-Skip";

    private final InternalTokenManager internalTokenManager;
    private final Set<String> skipClients;

    public InternalTokenFeignRequestInterceptor(InternalTokenManager internalTokenManager,
                                                InternalTokenFeignProperties feignProperties) {
        this.internalTokenManager = internalTokenManager;
        this.skipClients = normalize(feignProperties.getSkipClients());
    }

    @Override
    public void apply(RequestTemplate template) {
        if (template == null || shouldSkip(template)) {
            return;
        }

        if (template.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
            // 已有认证头，尊重显式配置
            return;
        }

        try {
            String token = internalTokenManager.getToken();
            if (StringUtils.hasText(token)) {
                template.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token);
            } else {
                log.warn("内部令牌为空，跳过注入，target={}, url={}",
                    getTargetName(template.feignTarget()), template.url());
            }
        } catch (Exception ex) {
            log.error("获取内部服务令牌失败，target={}, url={}, error={}",
                getTargetName(template.feignTarget()), template.url(), ex.getMessage(), ex);
        }
    }

    private boolean shouldSkip(RequestTemplate template) {
        if (template.headers().containsKey(SKIP_HEADER)) {
            // 移除内部控制头，避免传递到下游
            template.removeHeader(SKIP_HEADER);
            return true;
        }
        Target<?> target = template.feignTarget();
        if (target == null) {
            return false;
        }
        if (StringUtils.hasText(target.name()) &&
            skipClients.contains(target.name().toLowerCase(Locale.ROOT))) {
            return true;
        }
        Class<?> type = target.type();
        return type != null && type.isAnnotationPresent(SkipInternalToken.class);
    }

    private Set<String> normalize(Collection<String> values) {
        Set<String> normalized = new HashSet<>();
        if (values == null) {
            return normalized;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                normalized.add(value.trim().toLowerCase(Locale.ROOT));
            }
        }
        return normalized;
    }

    private String getTargetName(Target<?> target) {
        if (target == null) {
            return "unknown";
        }
        if (StringUtils.hasText(target.name())) {
            return target.name();
        }
        Class<?> type = target.type();
        return type != null ? type.getSimpleName() : "unknown";
    }
}

