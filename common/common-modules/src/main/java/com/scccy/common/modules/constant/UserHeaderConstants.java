package com.scccy.common.modules.constant;

/**
 * 用户信息请求头常量
 * <p>
 * Gateway 从 Token 中提取用户信息后，会将这些信息添加到请求头中传递给后端服务
 * <p>
 * 重要说明：
 * <ul>
 *     <li>这些请求头是 Gateway 内部添加的，前端不会获取到</li>
 *     <li>前端不应该发送这些请求头，这些是 Gateway 自动添加的</li>
 *     <li>这些请求头不会暴露给前端，只在 Gateway 和后端服务之间传递</li>
 * </ul>
 *
 * @author scccy
 */
public class UserHeaderConstants {
    
    /**
     * 用户ID请求头
     * <p>
     * Gateway 从 Token 中提取 userId 后，添加到请求头中
     * 后端服务从请求头中读取此值
     */
    public static final String HEADER_USER_ID = "X-User-Id";
    
    /**
     * 用户名请求头
     * <p>
     * Gateway 从 Token 中提取 username 后，添加到请求头中
     * 后端服务从请求头中读取此值
     */
    public static final String HEADER_USERNAME = "X-Username";
    
    /**
     * 权限列表请求头
     * <p>
     * Gateway 从 Token 中提取 authorities 后，以逗号分隔的字符串形式添加到请求头中
     * 后端服务从请求头中读取此值，并解析为权限列表
     * <p>
     * 格式：ROLE_ADMIN,ROLE_USER,system:user:list,system:user:add
     */
    public static final String HEADER_AUTHORITIES = "X-Authorities";
    
    /**
     * 私有构造函数，防止实例化
     */
    private UserHeaderConstants() {
        throw new UnsupportedOperationException("常量类不能被实例化");
    }
}

