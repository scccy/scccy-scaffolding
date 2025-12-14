package com.scccy.common.base.config;

import com.scccy.common.modules.annotation.Anonymous;
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
 * 动态收集 {@code @Anonymous} 注解标记的 URL 路径
 * <p>
 * 在应用启动时扫描所有 Controller，收集标记了 {@code @Anonymous} 注解的方法和类的 URL 路径，
 * 用于 Spring Security 配置中自动放行这些路径。
 * <p>
 * 使用方式：
 * <pre>
 * &#64;Anonymous
 * &#64;GetMapping("/public")
 * public ResultData&lt;?&gt; publicEndpoint() {
 *     // 此接口会自动被放行，无需手动配置路径
 * }
 * </pre>
 * <p>
 * 注意：
 * <ul>
 *     <li>支持方法级别和类级别的 {@code @Anonymous} 注解</li>
 *     <li>路径变量（如 {@code {id}}）会被替换为 {@code *}，以支持路径匹配</li>
 *     <li>在 Spring Security 配置中注入此 Bean，使用 {@code getUrls()} 获取所有匿名访问路径</li>
 * </ul>
 *
 * @author scccy
 * @since 2025-01-XX
 */
@Slf4j
@Component
public class PermitAllUrlProperties implements InitializingBean, ApplicationContextAware {

    /**
     * 路径变量模式：匹配 {variable} 格式
     */
    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{(.*?)\\}");

    /**
     * 路径变量替换为通配符
     */
    private static final String ASTERISK = "*";

    /**
     * Spring 应用上下文
     */
    private ApplicationContext applicationContext;

    /**
     * 收集到的匿名访问 URL 列表
     */
    @Getter
    private final List<String> urls = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {
        try {
            // 明确指定使用 requestMappingHandlerMapping，避免与 Actuator 的 controllerEndpointHandlerMapping 冲突
            RequestMappingHandlerMapping mapping = applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();

            log.info("开始扫描 @Anonymous 注解标记的 URL 路径...");

            handlerMethods.forEach((info, handlerMethod) -> {
                // 检查方法级别的 @Anonymous 注解
                Anonymous methodAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Anonymous.class);
                if (methodAnnotation != null && methodAnnotation.value()) {
                    // 获取路径模式（Spring Boot 3.x 使用 PathPatternsRequestCondition）
                    info.getPathPatternsCondition().getPatterns().forEach(pattern -> {
                        String url = pattern.getPatternString();
                        // 将路径变量 {id} 替换为 *，以支持路径匹配
                        String processedUrl = PATH_VARIABLE_PATTERN.matcher(url).replaceAll(ASTERISK);
                        urls.add(processedUrl);
                        log.debug("发现 @Anonymous 方法: {} -> {}", handlerMethod.getMethod().getName(), processedUrl);
                    });
                }

                // 检查类级别的 @Anonymous 注解
                Anonymous controllerAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Anonymous.class);
                if (controllerAnnotation != null && controllerAnnotation.value()) {
                    // 获取路径模式
                    info.getPathPatternsCondition().getPatterns().forEach(pattern -> {
                        String url = pattern.getPatternString();
                        // 将路径变量 {id} 替换为 *，以支持路径匹配
                        String processedUrl = PATH_VARIABLE_PATTERN.matcher(url).replaceAll(ASTERISK);
                        urls.add(processedUrl);
                        log.debug("发现 @Anonymous 类: {} -> {}", handlerMethod.getBeanType().getSimpleName(), processedUrl);
                    });
                }
            });

            log.info("扫描完成，共收集到 {} 个匿名访问路径", urls.size());
            if (log.isDebugEnabled() && !urls.isEmpty()) {
                log.debug("匿名访问路径列表: {}", urls);
            }
        } catch (Exception e) {
            log.error("扫描 @Anonymous 注解时发生错误", e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

