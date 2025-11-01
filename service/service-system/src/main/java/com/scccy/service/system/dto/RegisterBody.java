package com.scccy.service.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求体
 *
 * @author scccy
 */
@Data
@Schema(description = "用户注册请求体")
public class RegisterBody {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    @Schema(description = "用户名", required = true, example = "admin")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
    @Schema(description = "密码", required = true, example = "123456")
    private String password;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "管理员")
    private String nickName;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
}

