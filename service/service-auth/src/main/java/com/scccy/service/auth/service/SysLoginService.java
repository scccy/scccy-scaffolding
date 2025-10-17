package com.scccy.service.auth.service;

import com.scccy.service.auth.dto.LoginUser;
import jakarta.validation.constraints.NotNull;

public interface SysLoginService {
    LoginUser login(@NotNull String username, @NotNull String password);
}
