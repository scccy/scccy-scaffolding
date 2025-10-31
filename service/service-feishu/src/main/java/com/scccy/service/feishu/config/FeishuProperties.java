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



    @Value("${feishu.appId}")
    private  String appId ;
    @Value("${feishu.appSecret}")
    private  String appSecret ;
    @Value("${feishu.baseUrl}")
    private  String larkBaseUrl ;


}


