package com.scccy.service.auth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scccy.service.auth.domain.mp.Oauth2RegisteredClientMp;
import com.scccy.service.auth.domain.param.RegisteredClientQueryParam;
import com.scccy.service.auth.domain.vo.RegisteredClientVo;


public interface IOauth2RegisteredClientService {
    /**
     * 新增
     *
     * @param oauth2RegisteredClientMp Client表单对象
     */
    boolean add(Oauth2RegisteredClientMp oauth2RegisteredClientMp);

    /**
     * 修改客户端信息
     *
     * @param oauth2RegisteredClientMp Client表单对象，须有id字段
     */
    boolean update(Oauth2RegisteredClientMp oauth2RegisteredClientMp);

    /**
     * 查询所有Client列表
     *
     * @param registeredClientQueryParam 查询参数
     * @return IPage<RegisteredClientVo> Client列表
     */
    IPage<RegisteredClientVo> query(Page page, RegisteredClientQueryParam registeredClientQueryParam);

    /**
     * 根据clientId获取对象
     *
     * @param clientId clientId
     * @return RegisteredClientPo
     */
    Oauth2RegisteredClientMp getByClientId(String clientId);

    /**
     * 根据id获取对象
     *
     * @param id 唯一id
     * @return RegisteredClientPo
     */
    Oauth2RegisteredClientMp get(String id);

    /**
     * 失效client
     *
     * @param id 唯一id
     */
    boolean disable(String id);
}
