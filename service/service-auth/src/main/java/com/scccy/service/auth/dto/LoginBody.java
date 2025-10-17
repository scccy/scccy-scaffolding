package com.scccy.service.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户登录对象
 * 
 * @author scccy
 */
@Data
public class LoginBody
{
    /**
     * 用户名
     */
    @NotNull
    private String username;

    /**
     * 用户密码
     */
    @NotNull
    private String password;


}
