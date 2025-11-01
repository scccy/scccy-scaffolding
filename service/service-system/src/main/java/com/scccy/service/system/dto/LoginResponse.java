package com.scccy.service.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应体
 *
 * @author scccy
 */
@Data
@Schema(description = "登录响应体")
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * JWT Token
     */
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "管理员")
    private String nickName;

    /**
     * Token过期时间（时间戳，毫秒）
     */
    @Schema(description = "Token过期时间（时间戳，毫秒）", example = "1699123456789")
    private Long expireTime;
}

