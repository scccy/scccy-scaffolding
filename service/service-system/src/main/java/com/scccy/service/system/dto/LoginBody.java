package com.scccy.service.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户登录对象
 *
 * @author scccy
 */
@Data
public class LoginBody {
    /**
     * 用户名
     */
    @NotNull(message = "用户名不能为空")
    private String username;

    /**
     * 用户密码
     */
    @NotNull(message = "密码不能为空")
    private String password;
}

