package com.scccy.service.auth.service.impl;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.auth.dao.mapper.Oauth2RegisteredClientMapper;
import com.scccy.service.auth.domain.RegisteredClientConvert;
import com.scccy.service.auth.domain.mp.Oauth2RegisteredClientMp;
import com.scccy.service.auth.domain.param.RegisteredClientQueryParam;
import com.scccy.service.auth.domain.vo.RegisteredClientVo;
import com.scccy.service.auth.service.IOauth2RegisteredClientService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class Oauth2RegisteredClientService extends ServiceImpl<Oauth2RegisteredClientMapper, Oauth2RegisteredClientMp> implements IOauth2RegisteredClientService {
    /**
     * cache prefix key
     */
    private static final String CACHE_PREFIX_KEY = "client:";

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    RegisteredClientConvert registeredClientConvert;

    @Override
    public boolean add(Oauth2RegisteredClientMp oauth2RegisteredClientMp) {
        //密码不为空，表示重新设置了密码，保存密码
        if (StringUtils.isNotBlank(oauth2RegisteredClientMp.getClientSecret()))
            oauth2RegisteredClientMp.setClientSecret(passwordEncoder.encode(oauth2RegisteredClientMp.getClientSecret()));
        //保存
        return this.save(oauth2RegisteredClientMp);
    }

    @Override
    @CacheInvalidate(name = CACHE_PREFIX_KEY, key = "#registeredClientPo.id")
    public boolean update(Oauth2RegisteredClientMp oauth2RegisteredClientMp) {
        //密码不为空，表示重新设置了密码，保存密码
        if (StringUtils.isNotBlank(oauth2RegisteredClientMp.getClientSecret()))
            oauth2RegisteredClientMp.setClientSecret(passwordEncoder.encode(oauth2RegisteredClientMp.getClientSecret()));
        //更新
        return this.updateById(oauth2RegisteredClientMp);
    }

    @Override
    public IPage<RegisteredClientVo> query(Page page, RegisteredClientQueryParam registeredClientQueryParam) {
        QueryWrapper<Oauth2RegisteredClientMp> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(registeredClientQueryParam.getClientId()), "client_id", registeredClientQueryParam.getClientId());
        queryWrapper.eq(StringUtils.isNotBlank(registeredClientQueryParam.getClientName()), "client_name", registeredClientQueryParam.getClientName());
        IPage<Oauth2RegisteredClientMp> iPage = this.page(page, queryWrapper);
        return iPage.convert(registeredClientConvert::convertToRegisteredClientVo);
    }

    @Override
    @Cached(name = CACHE_PREFIX_KEY, key = "#id", cacheType = CacheType.BOTH)
    public Oauth2RegisteredClientMp get(String id) {
        return this.getById(id);
    }

    @Override
    @Cached(name = CACHE_PREFIX_KEY, key = "#clientId", cacheType = CacheType.BOTH)
    public Oauth2RegisteredClientMp getByClientId(String clientId) {
        QueryWrapper<Oauth2RegisteredClientMp> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("client_id", clientId);
        return this.getOne(queryWrapper);
    }

    @Override
    @CacheInvalidate(name = CACHE_PREFIX_KEY, key = "#id")
    public boolean disable(String id) {
        return this.removeById(id);
    }
}