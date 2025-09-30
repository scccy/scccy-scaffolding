package com.scccy.service.auth.controller;


import com.scccy.service.common.dto.ResultData;
import com.scccy.service.auth.service.FeishuService;
import jakarta.annotation.Resource;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class TokenController {

    @Resource
    FeishuService feishuServiceImpl;

    @GetMapping("/FsQrLogin")
    public ResultData<T> QrLogin() throws IOException {
        feishuServiceImpl.getAppAccessToken();

        return null;
    }
}
