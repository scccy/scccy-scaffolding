package com.scccy.common.modules.domain;

import com.scccy.common.modules.enums.ErrorType;
import lombok.Getter;

/**
 * 错误码枚举
 * 定义系统中所有可能的错误码
 */
@Getter
public enum ErrorCode implements ErrorType {
    
    // 成功
    SUCCESS(200, "操作成功"),
    
    //客户端错误xx)
    PARAM_ERROR(400, "参数错误"),
    PARAMS_ERROR(400, "参数错误"), // 别名，保持兼容性
    INVALID_PARAMETER(400, "无效参数"), // 新增，用于测试兼容性
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "方法不允许"),
    CONFLICT(409, "资源冲突"),
    REQUEST_TIMEOUT(408, "请求超时"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    
    //服务器错误xx)
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_ERROR(502, "网关错误"),
    BAD_GATEWAY(502, "网关错误");
    

    

    


    private final Integer code;
    private final String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}