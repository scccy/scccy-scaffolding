package com.scccy.service.auth.oauth2;


import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import com.scccy.service.auth.domain.RegisteredClientConvert;
import com.scccy.service.auth.domain.mp.Oauth2RegisteredClientMp;
import com.scccy.service.auth.service.IOauth2RegisteredClientService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Component
@Primary
public class Oauth2RegisteredClientRepository implements RegisteredClientRepository {

    @Resource
    IOauth2RegisteredClientService oauth2RegisteredClientService;

    @Resource
    RegisteredClientConvert registeredClientConvert;

    @Resource
    private CacheManager cacheManager;

    private Cache<String, RegisteredClient> registeredClientCache;

    private static final String CACHE_NAME = "registered_client_local:";
    private static final Duration CACHE_EXPIRE = Duration.ofMinutes(5);

    @PostConstruct
    public void initCache() {
        QuickConfig qc = QuickConfig.newBuilder(CACHE_NAME)
                .cacheType(CacheType.LOCAL)
                .expire(CACHE_EXPIRE)
                .build();
        registeredClientCache = cacheManager.getOrCreateCache(qc);
        log.info("RegisteredClient 本地缓存初始化完成，name={}，expire={}s", CACHE_NAME, CACHE_EXPIRE.getSeconds());
    }

    @Deprecated
    @Override
    public void save(RegisteredClient registeredClient) {
        log.warn("请使用IOauth2RegisteredClientService相关方法，该实现废弃！");
    }

    @Override
    public RegisteredClient findById(String id) {
        return getCached("id:" + id, () -> {
            Oauth2RegisteredClientMp registeredClientPo = oauth2RegisteredClientService.get(id);
            if (registeredClientPo == null) {
                log.debug("未找到注册客户端: id={}", id);
                return null;
            }
            return registeredClientConvert.convertToRegisteredClient(registeredClientPo);
        });
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return getCached("clientId:" + clientId, () -> {
            Oauth2RegisteredClientMp registeredClientPo = oauth2RegisteredClientService.getByClientId(clientId);
            if (registeredClientPo == null) {
                log.debug("未找到注册客户端: clientId={}", clientId);
                return null;
            }
            return registeredClientConvert.convertToRegisteredClient(registeredClientPo);
        });
    }

    private RegisteredClient getCached(String key, Supplier<RegisteredClient> supplier) {
        if (registeredClientCache == null) {
            return supplier.get();
        }
        RegisteredClient cached = registeredClientCache.get(key);
        if (cached != null) {
            return cached;
        }
        RegisteredClient value = supplier.get();
        if (value != null) {
            registeredClientCache.put(key, value);
        }
        return value;
    }
}