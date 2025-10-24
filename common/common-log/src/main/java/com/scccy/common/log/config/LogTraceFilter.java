package com.scccy.common.log.config;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
 
/**
 * @author: foo
 * @create: 2020-09-04 11:02
 */
public class LogTraceFilter extends OncePerRequestFilter {
 
 
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        preHandle(request);
        filterChain.doFilter(request, response);
        afterCompletion();
    }
 
    /**
     * 过滤前处理
     * @param request 请求
     * @return
     */
    public void preHandle(HttpServletRequest request) {
        //如果有上层调用就用上层的ID
        String traceId = request.getHeader(Constants.TRACE_ID);
        if (traceId == null) {
            traceId = TraceIdUtil.getTraceId();
        }
 
        MDC.put(Constants.TRACE_ID, traceId);
    }
 
    /**
     * 过滤后处理
     */
    public void afterCompletion() {
        //调用结束后删除
        MDC.remove(Constants.TRACE_ID);
    }
}