package com.scccy.common.base.aspect;

import com.scccy.common.modules.annotation.CurrentUser;
import com.scccy.common.modules.constant.UserHeaderConstants;
import com.scccy.common.modules.dto.CurrentUserInfo;
import com.scccy.common.modules.utils.UserContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 当前用户切面
 * <p>
 * 使用 AOP 环绕方式，自动从请求头提取用户信息并存储到 ThreadLocal
 * <p>
 * 使用方式：
 * <pre>
 * &#064;CurrentUser
 * &#064;GetMapping("/
 * public ResultData<?> getById(@PathVariable Long id) {
 *     CurrentUserInfo user = UserContextUtils.getCurrentUser();
 *     // ...
 * }
 * </pre>
 *
 * @author scccy
 */
@Slf4j
@Aspect
@Component
public class CurrentUserAspect {
    
    /**
     * 切点：匹配标注了 @CurrentUser 注解的方法
     */
    @Pointcut("@annotation(com.scccy.common.modules.annotation.CurrentUser)")
    public void currentUserPointcut() {
    }
    
    /**
     * 环绕通知：提取用户信息并存储到 ThreadLocal
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 执行异常
     */
    @Around("currentUserPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法上的 @CurrentUser 注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        CurrentUser annotation = signature.getMethod().getAnnotation(CurrentUser.class);
        boolean required = annotation != null && annotation.required();
        
        CurrentUserInfo userInfo = null;
        
        try {
            // 从请求头提取用户信息
            userInfo = extractUserInfo(required);
            
            // 存储到 ThreadLocal
            UserContextUtils.setCurrentUser(userInfo);
            
            // 执行方法
            return joinPoint.proceed();
        } finally {
            // 清理 ThreadLocal，防止内存泄漏
            UserContextUtils.clear();
        }
    }
    
    /**
     * 从请求头提取用户信息
     *
     * @param required 是否必需
     * @return 用户信息，如果未找到且 required=false 则返回 null
     */
    private CurrentUserInfo extractUserInfo(boolean required) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                if (required) {
                    log.warn("无法获取请求上下文，且用户信息是必需的");
                }
                return null;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // 从请求头提取用户信息（Gateway 内部添加的请求头，前端不会获取到）
            String userIdStr = request.getHeader(UserHeaderConstants.HEADER_USER_ID);
            String username = request.getHeader(UserHeaderConstants.HEADER_USERNAME);
            String authoritiesStr = request.getHeader(UserHeaderConstants.HEADER_AUTHORITIES);
            
            // 如果必需且没有用户信息，返回 null
            if (required && (userIdStr == null || username == null)) {
                log.warn("请求头中缺少用户信息，且用户信息是必需的");
                return null;
            }
            
            // 构建 CurrentUserInfo 对象
            CurrentUserInfo userInfo = new CurrentUserInfo();
            
            // 解析用户ID
            if (userIdStr != null && !userIdStr.trim().isEmpty()) {
                try {
                    userInfo.setUserId(Long.parseLong(userIdStr));
                } catch (NumberFormatException e) {
                    log.warn("解析用户ID失败: {}", userIdStr);
                }
            }
            
            // 设置用户名
            userInfo.setUsername(username);
            
            // 解析权限列表
            List<String> authorities = Collections.emptyList();
            if (authoritiesStr != null && !authoritiesStr.trim().isEmpty()) {
                authorities = Arrays.asList(authoritiesStr.split(","));
            }
            userInfo.setAuthorities(authorities);
            
            log.debug("提取用户信息成功: userId={}, username={}, authorities={}",
                userInfo.getUserId(), userInfo.getUsername(), userInfo.getAuthorities());
            
            return userInfo;
        } catch (Exception e) {
            log.error("提取用户信息失败", e);
            if (required) {
                log.warn("用户信息是必需的，但提取失败");
            }
            return null;
        }
    }
}

