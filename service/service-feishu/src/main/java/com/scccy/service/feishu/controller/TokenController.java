package com.scccy.service.feishu.controller;

import com.scccy.service.feishu.dto.FeishuQRLoginDto;
import com.scccy.service.feishu.service.FeishuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Feishu Auth", description = "飞书认证相关接口")
@RequestMapping("/feishu")
public class TokenController {

    @Resource
    FeishuService feishuServiceImpl;

    @PostMapping("/qrLogin")
    @Operation(summary = "二维码登录", description = "使用飞书回调的授权 code 进行登录换取令牌")
    public Map<String,Object> QrLogin(@RequestBody(required = false) FeishuQRLoginDto feishuQRLoginDto) throws Exception {
        if(feishuQRLoginDto.getCode() == null){
            return  null;
        }else{
            Map<String, Object> response = feishuServiceImpl.qrLogin(feishuQRLoginDto);
            response.put("code",0);
            return response;
        }
    }

//    @GetMapping("/groupChatSend")
//    public ResultData<T> groupChatSend(@RequestBody(required = false) String chatData){
//        feishuServiceImpl.groupChatSend(chatData);
//        return null;
//    }

}
