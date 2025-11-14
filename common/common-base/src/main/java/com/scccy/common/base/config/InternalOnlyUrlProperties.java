package com.scccy.common.base.config;

import com.scccy.common.modules.annotation.InternalOnly;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 收集 {@link InternalOnly} 注解标记的 URL 及其 Scope 要求
 */
@Slf4j
@Component
public class InternalOnlyUrlProperties implements InitializingBean, ApplicationContextAware {

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{(.*?)\\}");
    private static final String ASTERISK = "*";

    @Getter
    private final List<InternalEndpointDefinition> endpoints = new ArrayList<>();

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() {
        try {
            RequestMappingHandlerMapping mapping = applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();

            log.info("开始扫描 @InternalOnly 注解标记的 URL 路径...");

            handlerMethods.forEach((info, handlerMethod) -> {
                InternalOnly methodAnno = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), InternalOnly.class);
                if (methodAnno != null) {
                    registerEndpoints(info, methodAnno.scope(), handlerMethod.getMethod().getName());
                    return; // 方法级优先生效
                }

                InternalOnly typeAnno = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), InternalOnly.class);
                if (typeAnno != null) {
                    registerEndpoints(info, typeAnno.scope(), handlerMethod.getBeanType().getSimpleName());
                }
            });

            log.info("扫描完成，共收集到 {} 条内部接口规则", endpoints.size());
            if (log.isDebugEnabled() && !endpoints.isEmpty()) {
                log.debug("内部接口规则: {}", endpoints);
            }
        } catch (Exception e) {
            log.error("扫描 @InternalOnly 注解时发生错误", e);
        }
    }

    private void registerEndpoints(RequestMappingInfo info, String scope, String source) {
        if (info.getPathPatternsCondition() == null) {
            return;
        }
        info.getPathPatternsCondition().getPatterns().forEach(pattern -> {
            String url = PATH_VARIABLE_PATTERN.matcher(pattern.getPatternString()).replaceAll(ASTERISK);
            endpoints.add(new InternalEndpointDefinition(url, scope));
            log.debug("发现 @InternalOnly: {} -> {} (scope={})", source, url, scope);
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 内部端点定义
     */
    public record InternalEndpointDefinition(String pattern, String scope) {}
}

