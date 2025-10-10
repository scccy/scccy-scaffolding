package com.scccy.feishu.controller;

import com.scccy.feishu.dto.FeishuQRLoginDto;
import com.scccy.feishu.service.FeishuService;
import com.scccy.service.common.dto.ResultData;
import jakarta.annotation.Resource;
import org.apache.poi.ss.formula.functions.T;
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

    @GetMapping("/groupChatSend")
    public ResultData<T> groupChatSend(@RequestBody(required = false) String chatData){
        feishuServiceImpl.groupChatSend(chatData);
        return null;
    }

}
