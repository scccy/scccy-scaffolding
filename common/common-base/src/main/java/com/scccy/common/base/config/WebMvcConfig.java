package com.scccy.common.base.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**

 * 只包含通用的WebMvc配置，不包含业务相关的拦截器
 * 排除Gateway服务，因为Gateway使用WebFlux
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnClass(WebMvcConfigurer.class)
public class WebMvcConfig implements WebMvcConfigurer {

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
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        converters.addFirst(converter);
    }
}


