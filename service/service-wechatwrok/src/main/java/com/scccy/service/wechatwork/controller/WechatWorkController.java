package com.scccy.service.wechatwork.controller;


import com.scccy.service.wechatwork.aes.AesException;
import com.scccy.service.wechatwork.service.WechatworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Tag(name = "企业微信", description = "企业微信相关接口")
@RestController
@RequestMapping(value="wechatwork")
@Async
public class WechatWorkController {
    @Autowired
    WechatworkService wechatWorkServiceImpl;
    


    // 创建一个专门的线程池来处理异步任务
    private final Executor asyncExecutor = Executors.newFixedThreadPool(10);

    @GetMapping("/callBack")
    @Operation(summary = "企业微信回调验证", description = "企业微信回调URL验证接口")
    public String callBackGet(
             @RequestParam String msg_signature,
             @RequestParam String nonce,
             @RequestParam String timestamp,
             @RequestParam String echostr) throws AesException {
        return  wechatWorkServiceImpl.callBackGet(msg_signature,nonce,timestamp,echostr);
    }


    @PostMapping(path = "/callBack")
    @Operation(summary = "企业微信回调处理", description = "企业微信回调消息处理接口")
    public Integer callBackPost(
             @RequestParam String msg_signature,
             @RequestParam String nonce,
             @RequestParam String timestamp,
             @RequestBody String xmlBody) throws AesException {

        // 异步处理业务逻辑
        CompletableFuture.runAsync(() -> {

            try {
                wechatWorkServiceImpl.callBackPost(msg_signature, nonce, timestamp, xmlBody);
            } catch (AesException e) {
                throw new RuntimeException(e);
            }

        }, asyncExecutor);

        return 200;
    }



}
