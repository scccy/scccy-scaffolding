package com.scccy.common.base.resolver;

import com.scccy.common.modules.annotation.CurrentUser;
import com.scccy.common.modules.constant.UserHeaderConstants;
import com.scccy.common.modules.dto.CurrentUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 当前用户参数解析器
 * <p>
 * 自动从请求头中提取用户信息，注入到 Controller 方法参数中
 * <p>
 * 支持的请求头：
 * <ul>
 *     <li>X-User-Id: 用户ID</li>
 *     <li>X-Username: 用户名</li>
 *     <li>X-Authorities: 权限列表（逗号分隔）</li>
 * </ul>
 *
 * @author scccy
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) 
            && CurrentUserInfo.class.isAssignableFrom(parameter.getParameterType());
    }
    
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
        
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }
        
        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        boolean required = annotation != null && annotation.required();
        
        // 从请求头提取用户信息（Gateway 内部添加的请求头，前端不会获取到）
        String userIdStr = request.getHeader(UserHeaderConstants.HEADER_USER_ID);
        String username = request.getHeader(UserHeaderConstants.HEADER_USERNAME);
        String authoritiesStr = request.getHeader(UserHeaderConstants.HEADER_AUTHORITIES);
        
        // 如果必需且没有用户信息，返回 null（框架会处理）
        if (required && (userIdStr == null || username == null)) {
            return null;
        }
        
        // 构建 CurrentUserInfo 对象
        CurrentUserInfo userInfo = new CurrentUserInfo();
        
        // 解析用户ID
        if (userIdStr != null && !userIdStr.trim().isEmpty()) {
            try {
                userInfo.setUserId(Long.parseLong(userIdStr));
            } catch (NumberFormatException e) {
                // 忽略解析错误
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
        
        return userInfo;
    }
}

