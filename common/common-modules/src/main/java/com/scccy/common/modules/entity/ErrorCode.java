package com.scccy.common.modules.entity;

/**
 * 错误码枚举
 * 定义系统中所有可能的错误码
 */
public enum ErrorCode {
    
    // 成功
    SUCCESS(200, "操作成功"),
    
    // 客户端错误 (4xx)
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
    
    // 服务器错误 (5xx)
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_ERROR(502, "网关错误"),
    BAD_GATEWAY(502, "网关错误"),
    
    // 业务错误 (1000-9999)
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),
    TOKEN_EXPIRED(1005, "令牌已过期"),
    TOKEN_INVALID(1006, "令牌无效"),
    USER_PROFILE_NOT_FOUND(1007, "用户档案不存在"),
    USER_AVATAR_UPLOAD_FAILED(1008, "用户头像上传失败"),
    USER_UPDATE_FAILED(1009, "用户信息更新失败"),
    USER_PERMISSION_DENIED(1010, "用户权限不足"),
    
    // 认证相关错误 (1100-1199)
    AUTH_FAILED(1101, "认证失败"),
    AUTH_TOKEN_MISSING(1102, "认证令牌缺失"),
    AUTH_TOKEN_EXPIRED(1103, "认证令牌已过期"),
    AUTH_TOKEN_INVALID(1104, "认证令牌无效"),
    AUTH_USER_DISABLED(1105, "用户账号已被禁用"),
    AUTH_INVALID_CREDENTIALS(1106, "用户名或密码错误"),
    
    // 核心服务错误码 (2000-3999)
    // 20xx: 核心服务通用错误
    CORE_SERVICE_ERROR(2000, "核心服务错误"),
    CORE_SERVICE_UNAVAILABLE(2001, "核心服务不可用"),
    CORE_SERVICE_TIMEOUT(2002, "核心服务超时"),
    CORE_SERVICE_CONFIG_ERROR(2003, "核心服务配置错误"),
    
    // 21xx: 发布者服务错误 (core-publisher)
    PUBLISHER_TASK_NOT_FOUND(2101, "任务不存在"),
    PUBLISHER_TASK_ALREADY_COMPLETED(2102, "任务已完成"),
    PUBLISHER_TASK_EXPIRED(2103, "任务已过期"),
    PUBLISHER_TASK_STATUS_INVALID(2104, "任务状态无效"),
    PUBLISHER_TASK_REVIEW_NOT_FOUND(2105, "任务审核记录不存在"),
    PUBLISHER_TASK_REVIEW_STATUS_INVALID(2106, "任务审核状态无效"),
    PUBLISHER_TASK_COMPLETION_NOT_FOUND(2107, "任务完成记录不存在"),
    PUBLISHER_TASK_CONFIG_ERROR(2108, "任务配置错误"),
    PUBLISHER_TASK_VALIDATION_FAILED(2109, "任务验证失败"),
    PUBLISHER_TASK_PUBLISH_FAILED(2110, "任务发布失败"),
    PUBLISHER_TASK_UNPUBLISH_FAILED(2111, "任务下架失败"),
    PUBLISHER_TASK_DELETE_FAILED(2112, "任务删除失败"),
    PUBLISHER_TASK_DUPLICATE_SUBMISSION(2113, "任务重复提交"),
    
    // 22xx: 接收者服务错误 (core-receiver) - 预留
    RECEIVER_SERVICE_ERROR(2200, "接收者服务错误"),
    
    // 23xx: 其他核心服务错误 (预留)
    CORE_SERVICE_3_ERROR(2300, "核心服务3错误"),
    
    // 第三方服务错误码 (4000-4999)
    // 40xx: 第三方服务通用错误
    THIRD_PARTY_SERVICE_ERROR(4000, "第三方服务错误"),
    THIRD_PARTY_SERVICE_UNAVAILABLE(4001, "第三方服务不可用"),
    THIRD_PARTY_SERVICE_TIMEOUT(4002, "第三方服务超时"),
    THIRD_PARTY_SERVICE_CONFIG_ERROR(4003, "第三方服务配置错误"),
    
    // 41xx: OSS服务错误 (aliyun-oss)
    OSS_SERVICE_ERROR(4101, "OSS服务错误"),
    OSS_UPLOAD_ERROR(4102, "OSS上传错误"),
    OSS_DOWNLOAD_ERROR(4103, "OSS下载错误"),
    OSS_DELETE_ERROR(4104, "OSS删除错误"),
    OSS_ACCESS_DENIED(4105, "OSS访问被拒绝"),
    OSS_BUCKET_NOT_FOUND(4106, "OSS存储桶不存在"),
    OSS_OBJECT_NOT_FOUND(4107, "OSS对象不存在"),
    OSS_QUOTA_EXCEEDED(4108, "OSS配额超限"),
    OSS_FILE_UPLOAD_FAILED(4109, "OSS文件上传失败"),
    OSS_FILE_NOT_FOUND(4110, "OSS文件不存在"),
    OSS_FILE_SIZE_EXCEEDED(4111, "OSS文件大小超限"),
    OSS_FILE_TYPE_NOT_ALLOWED(4112, "OSS文件类型不允许"),
    OSS_FILE_DELETE_FAILED(4113, "OSS文件删除失败"),
    OSS_FILE_ACCESS_DENIED(4114, "OSS文件访问被拒绝"),
    OSS_FILE_DOWNLOAD_FAILED(4115, "OSS文件下载失败"),
    OSS_FILE_PROCESSING_FAILED(4116, "OSS文件处理失败"),
    OSS_FILE_STORAGE_ERROR(4117, "OSS文件存储错误"),
    
    // 42xx: 企业微信服务错误 (third-party-wechatwork) - 预留
    WECHATWORK_SERVICE_ERROR(4200, "企业微信服务错误"),
    
    // 43xx: 其他第三方服务错误 (预留)
    THIRD_PARTY_SERVICE_3_ERROR(4300, "第三方服务3错误"),
    
    // 44xx: 其他第三方服务错误 (预留)
    THIRD_PARTY_SERVICE_4_ERROR(4400, "第三方服务4错误"),
    
    // 45xx: 其他第三方服务错误 (预留)
    THIRD_PARTY_SERVICE_5_ERROR(4500, "第三方服务5错误"),
    
    // 网关相关错误 (5000-5999)
    GATEWAY_ROUTE_NOT_FOUND(5001, "网关路由不存在"),
    GATEWAY_TIMEOUT(5002, "网关超时"),
    GATEWAY_SERVICE_UNAVAILABLE(5003, "网关服务不可用"),
    GATEWAY_RATE_LIMIT_EXCEEDED(5004, "网关限流"),
    GATEWAY_CORS_ERROR(5005, "网关CORS错误"),
    GATEWAY_FILTER_ERROR(5006, "网关过滤器错误"),
    
    // 企业微信相关错误 (6000-6999)
    WECHATWORK_API_ERROR(6001, "企业微信API错误"),
    WECHATWORK_AUTH_ERROR(6002, "企业微信认证错误"),
    WECHATWORK_CALLBACK_ERROR(6003, "企业微信回调错误"),
    WECHATWORK_USER_NOT_FOUND(6004, "企业微信用户不存在"),
    WECHATWORK_DEPARTMENT_NOT_FOUND(6005, "企业微信部门不存在"),
    
    // 数据库相关错误 (7000-7999)
    DATABASE_CONNECTION_ERROR(7001, "数据库连接错误"),
    DATABASE_QUERY_ERROR(7002, "数据库查询错误"),
    DATABASE_UPDATE_ERROR(7003, "数据库更新错误"),
    DATABASE_DELETE_ERROR(7004, "数据库删除错误"),
    DATABASE_TRANSACTION_ERROR(7005, "数据库事务错误"),
    DATABASE_CONSTRAINT_VIOLATION(7006, "数据库约束违反"),
    
    // 缓存相关错误 (8000-8999)
    CACHE_CONNECTION_ERROR(8001, "缓存连接错误"),
    CACHE_GET_ERROR(8002, "缓存获取错误"),
    CACHE_SET_ERROR(8003, "缓存设置错误"),
    CACHE_DELETE_ERROR(8004, "缓存删除错误"),
    CACHE_KEY_NOT_FOUND(8005, "缓存键不存在"),
    
    // 消息队列相关错误 (9000-9999)
    MQ_CONNECTION_ERROR(9001, "消息队列连接错误"),
    MQ_SEND_ERROR(9002, "消息发送错误"),
    MQ_RECEIVE_ERROR(9003, "消息接收错误"),
    MQ_TOPIC_NOT_FOUND(9004, "消息主题不存在"),
    MQ_CONSUMER_ERROR(9005, "消息消费者错误");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
} 