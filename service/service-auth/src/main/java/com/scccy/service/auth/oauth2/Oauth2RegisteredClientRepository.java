package com.scccy.service.auth.oauth2;


import com.scccy.service.auth.domain.RegisteredClientConvert;
import com.scccy.service.auth.domain.mp.Oauth2RegisteredClientMp;
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
        Oauth2RegisteredClientMp registeredClientPo = oauth2RegisteredClientService.get(id);
        if (registeredClientPo == null) {
            log.debug("未找到注册客户端: id={}", id);
            return null;
        }
        return registeredClientConvert.convertToRegisteredClient(registeredClientPo);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        Oauth2RegisteredClientMp registeredClientPo = oauth2RegisteredClientService.getByClientId(clientId);
        if (registeredClientPo == null) {
            log.debug("未找到注册客户端: clientId={}", clientId);
            return null;
        }
        return registeredClientConvert.convertToRegisteredClient(registeredClientPo);
    }
}