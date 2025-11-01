package com.scccy.service.auth.oauth2;


import com.scccy.service.auth.domain.RegisteredClientConvert;
import com.scccy.service.auth.service.IOauth2RegisteredClientService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Slf4j
@Component
@Primary
public class Oauth2RegisteredClientRepository implements RegisteredClientRepository {

    @Resource
    IOauth2RegisteredClientService oauth2RegisteredClientService;

    @Resource
    RegisteredClientConvert registeredClientConvert;

    @Deprecated
    @Override
    public void save(RegisteredClient registeredClient) {
        log.warn("请使用IOauth2RegisteredClientService相关方法，该实现废弃！");
    }

    @Override
    public RegisteredClient findById(String id) {
        return registeredClientConvert.convertToRegisteredClient(oauth2RegisteredClientService.get(id));
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return registeredClientConvert.convertToRegisteredClient(oauth2RegisteredClientService.getByClientId(clientId));
    }
}