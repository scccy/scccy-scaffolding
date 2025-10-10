package com.scccy.common.base.config;

import com.scccy.common.base.manager.OkHttpManager;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp3配置类
 * 
 * @author scccy
 */
@Configuration
public class OkHttpConfig {
    
    /**
     * 创建OkHttpClient Bean
     */
    @Bean
    public OkHttpClient okHttpClient() {
        // 创建日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        
        // 创建连接池
        ConnectionPool connectionPool = new ConnectionPool(
            5, // 最大空闲连接数
            5, // 保持连接时间（分钟）
            TimeUnit.MINUTES
        );
        
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10)) // 连接超时
                .readTimeout(Duration.ofSeconds(30))    // 读取超时
                .writeTimeout(Duration.ofSeconds(30))   // 写入超时
                .connectionPool(connectionPool)         // 连接池
                .addInterceptor(loggingInterceptor)     // 日志拦截器
                .retryOnConnectionFailure(true)         // 连接失败时重试
                .build();
    }
    
    /**
     * 创建OkHttpManager Bean
     */
    @Bean
    public OkHttpManager okHttpManager(OkHttpClient okHttpClient) {
        return new OkHttpManager(okHttpClient);
    }
} 