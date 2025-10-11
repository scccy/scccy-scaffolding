package com.scccy.service.auth.controller;


import com.scccy.service.auth.dto.FeishuQRLoginDto;
import com.scccy.service.auth.service.FeishuService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class TokenController {

    @Resource
    FeishuService feishuServiceImpl;



    @PostMapping("/qrLogin")
    public Map<String,Object> QrLogin(@RequestBody(required = false) FeishuQRLoginDto feishuQRLoginDto) throws Exception {
        if(feishuQRLoginDto.getCode() == null){
            return  null;
        }else{
            Map<String, Object> response = feishuServiceImpl.qrLogin(feishuQRLoginDto);
            response.put("code",0);
            return response;
        }
    }
}
