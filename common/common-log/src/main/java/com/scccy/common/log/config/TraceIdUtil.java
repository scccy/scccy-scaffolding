package com.scccy.common.log.config;
 
import java.util.UUID;
 
/**
 * traceId生成工具类
 * @author foo
 */
public class TraceIdUtil {
 
    private TraceIdUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
 
    /**
     * 获取traceId
     * @return
     */
    public static String getTraceId() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}