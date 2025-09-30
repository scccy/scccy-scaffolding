package com.origin.banyu.coreflink.common.example;

import com.origin.banyu.coreflink.common.config.RedisConfig;
import com.origin.banyu.coreflink.common.redis.RedisWriter;

/**
 * RedisWriter 使用示例
 * 
 * 展示如何使用重构后的 RedisWriter
 */
public class RedisWriterExample {

    public static void main(String[] args) {
        try {
            // 打印配置信息
            System.out.println("Redis 配置:");
            System.out.println("  Host: " + RedisConfig.HOST);
            System.out.println("  Port: " + RedisConfig.PORT);
            System.out.println("  Database: " + RedisConfig.DATABASE);
            System.out.println("  Password: ***");
            System.out.println("  Default TTL: " + RedisConfig.DEFAULT_TTL_SECONDS + " seconds");
            
            // 示例1: 设置带过期时间的键值对
            String key1 = "user:session:12345";
            String value1 = "{\"userId\":12345,\"loginTime\":\"" + System.currentTimeMillis() + "\"}";
            RedisWriter.set(key1, value1, 3600); // 1小时过期
            System.out.println("设置带过期时间的键值对: " + key1);
            
            // 示例2: 设置无过期时间的键值对
            String key2 = "user:profile:12345";
            String value2 = "{\"userId\":12345,\"name\":\"张三\",\"email\":\"zhangsan@example.com\"}";
            RedisWriter.set(key2, value2);
            System.out.println("设置无过期时间的键值对: " + key2);
            
            // 示例3: 使用默认过期时间
            String key3 = "user:cache:12345";
            String value3 = "{\"userId\":12345,\"cachedData\":\"some data\"}";
            RedisWriter.set(key3, value3, RedisConfig.DEFAULT_TTL_SECONDS);
            System.out.println("使用默认过期时间设置键值对: " + key3);
            
            System.out.println("所有操作完成!");
            
        } catch (Exception e) {
            System.err.println("Redis 操作失败: " + e.getMessage());
        }
    }
}
