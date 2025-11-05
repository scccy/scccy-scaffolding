package com.scccy.common.base.config;

import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import feign.Logger;
import feign.Request;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OpenFeign配置类
 * 
 * @author origin
 * @since 2024-12-19
 */
@Configuration
public class OpenFeignConfig {
    
    /**
     * 默认构造函数
     */
    public OpenFeignConfig() {
    }

    /**
     * 配置日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * 配置请求超时时间
     * 
     * 注意：在Feign 13.x版本中，Request.Options构造函数已被标记为过时
     * 但这是当前版本支持的正确用法，直到升级到更新的版本
     * 
     * @return Request.Options 配置对象，包含连接超时、读取超时和是否跟随重定向的配置
     */
    @Bean
    @SuppressWarnings("deprecation")
    public Request.Options options() {
        // Feign 13.x版本的正确用法
        return new Request.Options(
            10000,  // 连接超时（毫秒）
            60000,  // 读取超时（毫秒）
            false   // 是否跟随重定向
        );
    }

    /**
     * 配置FastJSON2消息转换器
     * 
     * @return HttpMessageConverters 包含FastJSON2消息转换器的转换器集合
     */
    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        // 配置FastJSON2
        com.alibaba.fastjson2.support.config.FastJsonConfig config = new com.alibaba.fastjson2.support.config.FastJsonConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        config.setCharset(StandardCharsets.UTF_8);
        
        // 设置序列化特性
        config.setWriterFeatures(
            com.alibaba.fastjson2.JSONWriter.Feature.WriteEnumsUsingName,
            com.alibaba.fastjson2.JSONWriter.Feature.WriteBigDecimalAsPlain
        );
        
        // 设置反序列化特性
        config.setReaderFeatures(
            com.alibaba.fastjson2.JSONReader.Feature.FieldBased,
            com.alibaba.fastjson2.JSONReader.Feature.SupportArrayToBean,
            com.alibaba.fastjson2.JSONReader.Feature.SupportSmartMatch
        );
        
        converter.setFastJsonConfig(config);
        
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(converter);
        return new HttpMessageConverters(converters);
    }

    /**
     * 配置编码器 - 使用FastJSON2
     * 
     * @param messageConverters 消息转换器工厂对象
     * @return Encoder Feign编码器实例，支持表单和JSON编码
     */
    @Bean
    public Encoder feignEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    /**
     * 配置解码器 - 使用FastJSON2
     * 
     * @param messageConverters 消息转换器工厂对象
     * @return Decoder Feign解码器实例，支持JSON解码
     */
    @Bean
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringDecoder(messageConverters);
    }
}