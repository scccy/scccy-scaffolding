package com.scccy.service.wechatwork.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
public  class  WechatworkProperties {

    @Value("${wechatwork.corpID}")
    private  String corpID ;
    @Value("${wechatwork.corSecret}")
    private  String corSecret ;
    @Value("${wechatwork.encodingAESKey}")
    private String appSecret;
    @Value("${wechatwork.baseUrl}")
    private  String baseUrl ;
    @Value("${wechatwork.token}")
    private  String token ;
    private  String encodingAESKey;
    @Value("${wechatwork.coreSecretChat}")
    private  String coreSecretChat;
    @Value("${wechatwork.priKey}")
    private  String priKey;
}


