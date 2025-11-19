package com.scccy.service.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 在服务启动阶段预热内部服务客户端配置，
 * 避免首次请求时多次查询 oauth2_registered_client 表。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisteredClientWarmupRunner implements ApplicationRunner {

    private final RegisteredClientRepository registeredClientRepository;

    @Value("${scccy.internal-token.client-id:}")
    private String internalServiceClientId;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(internalServiceClientId)) {
            log.warn("未配置 scccy.internal-token.client-id，跳过 RegisteredClient 预热");
            return;
        }

        RegisteredClient client = registeredClientRepository.findByClientId(internalServiceClientId);
        if (client == null) {
            String message = String.format(
                    "RegisteredClient 预热失败，未找到配置的 clientId=%s，请先在 oauth2_registered_client 中创建记录",
                    internalServiceClientId);
            log.error(message);
            throw new IllegalStateException(message);
        }

        long ttlSeconds = client.getTokenSettings().getAccessTokenTimeToLive().toSeconds();
        log.info("RegisteredClient 预热成功: clientId={}, accessTokenTTL={}s", internalServiceClientId, ttlSeconds);
    }
}

