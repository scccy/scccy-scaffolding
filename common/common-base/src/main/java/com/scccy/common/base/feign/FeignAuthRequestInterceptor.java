package com.scccy.common.base.feign;

import com.scccy.common.base.annotation.SkipInternalToken;
import com.scccy.common.base.config.properties.InternalTokenFeignProperties;
import com.scccy.common.base.service.AuthTokenService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Target;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Feign 调用自动注入内部 JWT 的拦截器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(RequestInterceptor.class)
@ConditionalOnProperty(prefix = "scccy.internal-token.feign", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FeignAuthRequestInterceptor implements RequestInterceptor {

    private final AuthTokenService authTokenService;
    private final InternalTokenFeignProperties feignProperties;

    @Override
    public void apply(RequestTemplate template) {
        if (shouldSkip(template)) {
            return;
        }
        if (template.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
            return;
        }

        String token = authTokenService.getServiceToken();
        template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private boolean shouldSkip(RequestTemplate template) {
        Target<?> target = template.feignTarget();
        if (target == null) {
            return false;
        }

        String name = target.name();
        if (feignProperties.getSkipClients().contains(name)) {
            log.debug("Feign 客户端 {} 配置跳过内部 token 注入", name);
            return true;
        }

        Class<?> type = target.type();
        if (type != null && type.isAnnotationPresent(SkipInternalToken.class)) {
            log.debug("Feign 客户端 {} 使用 @SkipInternalToken 跳过内部 token 注入", name);
            return true;
        }
        return false;
    }
}


