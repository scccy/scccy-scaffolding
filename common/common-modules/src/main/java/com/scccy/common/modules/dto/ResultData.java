package com.scccy.common.modules.dto;


import com.scccy.common.modules.enums.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 统一响应数据类 - 核心响应数据结构
 * 遵循单一职责原则，只负责响应数据的管理
 * 
 * @author origin
 * @since 2024-12-19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ResultData<T> {
    
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;

    // ==================== 成功响应方法 ====================
    
    /**
     * 成功响应
     * 
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> ResultData<T> ok() {
        return new ResultData<T>()
            .setCode(200)
            .setMessage("SUCCESS");
    }
    
    /**
     * 成功响应（带数据）
     * 
     * @param <T> 数据类型
     * @param data 响应数据
     * @return 成功响应结果
     */
    public static <T> ResultData<T> ok(T data) {
        return new ResultData<T>()
            .setCode(200)
            .setMessage("SUCCESS")
            .setData(data);
    }
    
    /**
     * 成功响应（带消息和数据）
     * 
     * @param <T> 数据类型
     * @param message 响应消息
     * @param data 响应数据
     * @return 成功响应结果
     */
    public static <T> ResultData<T> ok(String message, T data) {
        return new ResultData<T>()
            .setCode(200)
            .setMessage(message)
            .setData(data);
    }


    // ==================== 失败响应方法 ====================
    
    /**
     * 失败响应
     * 
     * @param <T> 数据类型
     * @return 失败响应结果
     */
    public static <T> ResultData<T> fail() {
        return new ResultData<T>()
            .setCode(500)
            .setMessage("FAIL");
    }
    
    /**
     * 失败响应（带消息）
     * 
     * @param <T> 数据类型
     * @param message 错误消息
     * @return 失败响应结果
     */
    public static <T> ResultData<T> fail(String message) {
        return new ResultData<T>()
            .setCode(500)
            .setMessage(message);
    }
    
    /**
     * 失败响应（带码和消息）
     * 
     * @param <T> 数据类型
     * @param code 错误码
     * @param message 错误消息
     * @return 失败响应结果
     */
    public static <T> ResultData<T> fail(Integer code, String message) {
        return new ResultData<T>()
            .setCode(code)
            .setMessage(message);
    }
    
    /**
     * 失败响应（带码、消息和数据）
     * 
     * @param <T> 数据类型
     * @param code 错误码
     * @param message 错误消息
     * @param data 错误数据
     * @return 失败响应结果
     */
    public static <T> ResultData<T> fail(Integer code, String message, T data) {
        return new ResultData<T>()
            .setCode(code)
            .setMessage(message)
            .setData(data);
    }

    /**
     * 失败响应（使用ErrorCode和自定义消息）
     * 
     * @param <T> 数据类型
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     * @return 失败响应结果
     */
    public static <T> ResultData<T> fail(ErrorType errorCode, String message) {
        return new ResultData<T>()
            .setCode(errorCode.getCode())
            .setMessage(message);
    }


}
