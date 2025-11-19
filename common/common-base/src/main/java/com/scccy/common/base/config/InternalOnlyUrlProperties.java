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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 收集 {@link InternalOnly} 注解标记的接口及其 scope 信息，供 ResourceServerConfig 使用。
 */
@Slf4j
@Component
public class InternalOnlyUrlProperties implements InitializingBean, ApplicationContextAware {

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{(.*?)\\}");
    private static final String ASTERISK = "*";

    private ApplicationContext applicationContext;

    @Getter
    private final List<InternalEndpointDefinition> endpoints = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {
        try {
            RequestMappingHandlerMapping mapping = applicationContext.getBean(
                "requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();

            log.info("开始扫描 @InternalOnly 注解标记的 URL 路径...");

            Set<InternalEndpointDefinition> deduplicated = new LinkedHashSet<>();

            handlerMethods.forEach((info, handlerMethod) -> {
                InternalOnly methodAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), InternalOnly.class);
                InternalOnly classAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), InternalOnly.class);

                if (methodAnnotation == null && classAnnotation == null) {
                    return;
                }

                String scope = methodAnnotation != null ? methodAnnotation.scope() : classAnnotation.scope();
                info.getPathPatternsCondition().getPatterns().forEach(pattern -> {
                    String raw = pattern.getPatternString();
                    String processed = PATH_VARIABLE_PATTERN.matcher(raw).replaceAll(ASTERISK);
                    deduplicated.add(new InternalEndpointDefinition(processed, scope));
                    log.debug("发现内部接口: {} -> scope={}", processed, scope);
                });
            });

            endpoints.addAll(deduplicated);
            log.info("扫描完成，共记录 {} 条内部接口规则", endpoints.size());
        } catch (Exception e) {
            log.error("扫描 @InternalOnly 注解时发生错误", e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 内部接口定义，包含匹配路径和所需 scope。
     */
    public record InternalEndpointDefinition(String pattern, String scope) {
    }
}


