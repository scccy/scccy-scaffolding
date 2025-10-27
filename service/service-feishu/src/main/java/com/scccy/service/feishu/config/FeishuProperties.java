package com.scccy.service.feishu.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
public  class FeishuProperties {



    @Value("${feishu.corpID}")
    private  String appId ;
    @Value("${feishu.corSecret}")
    private  String appSecret ;
    @Value("${feishu.baseUrl}")
    private  String larkBaseUrl ;
    @Value("${feishu.token}")
    private  String token ;
    private  String encodingAESKey;
    @Value("${feishu.coreSecretChat}")
    private  String coreSecretChat;
    @Value("${feishu.priKey}")
    private  String priKey;
}


