package com.scccy.common.base.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import com.scccy.common.base.resolver.CurrentUserArgumentResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Web MVC配置类
 * 只包含通用的WebMvc配置，不包含业务相关的拦截器
 * 排除Gateway服务，因为Gateway使用WebFlux
 * 
 * @author origin
 * @since 2024-12-19
 */
@Configuration
@ConditionalOnClass(WebMvcConfigurer.class)
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private CurrentUserArgumentResolver currentUserArgumentResolver;
    
    /**
     * 默认构造函数
     */
    public WebMvcConfig() {
    }
    
    /**
     * 注册参数解析器
     * <p>
     * 注册 @CurrentUser 注解的参数解析器，支持自动注入用户信息
     *
     * @param resolvers 参数解析器列表
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (!"GET".equalsIgnoreCase(request.getMethod()) || !request.getRequestURI().toString().equals("/favicon.ico")) {
                    return true;
                }
                response.setStatus(HttpStatus.NO_CONTENT.value()); // 设置状态码为204 No Content
                return false;
            }
        }).addPathPatterns("/**");
    }


    /** 不需要拦截地址 */
    public static final String[] excludeUrls = { "/login", "/logout", "/refresh" };



    //开启全局跨域
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedHeaders("*")
                .allowedOriginPatterns("*")
                .allowedMethods("*");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        //自定义配置...

        FastJsonConfig config = new FastJsonConfig();

		config.setDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        config.setJSONB(true);
        config.setReaderFeatures(JSONReader.Feature.FieldBased,
                JSONReader.Feature.SupportArrayToBean,
//                驼峰转换
                JSONReader.Feature.SupportSmartMatch
        );
        // 精简返回JSON，默认不输出为null的字段
		config.setWriterFeatures(
                JSONWriter.Feature.PrettyFormat,
                JSONWriter.Feature.WriteEnumsUsingName,
				JSONWriter.Feature.WriteBigDecimalAsPlain
        );

        converter.setFastJsonConfig(config);
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        // 支持多种 JSON 相关的 MediaType，包括 text/javascript（某些浏览器会发送这个）
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(new MediaType("text", "javascript", StandardCharsets.UTF_8)); // text/javascript
        supportedMediaTypes.add(new MediaType("application", "x-javascript", StandardCharsets.UTF_8)); // application/x-javascript
        supportedMediaTypes.add(new MediaType("text", "json", StandardCharsets.UTF_8)); // text/json
        converter.setSupportedMediaTypes(supportedMediaTypes);
        converters.addFirst(converter);
    }
}


