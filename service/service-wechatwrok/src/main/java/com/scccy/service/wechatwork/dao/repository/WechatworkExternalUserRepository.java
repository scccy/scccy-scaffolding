package com.scccy.service.wechatwork.dao.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.scccy.service.wechatwork.domain.jpa.WechatworkExternalUserJpa;
import com.scccy.service.wechatwork.domain.jpa.WechatworkExternalUserIdJpa;

/**
 * 好友关系 持久层
 *
 * @author scccy
 * @since 2025-10-24 17:12:06
 */
@Repository
public interface WechatworkExternalUserRepository extends JpaRepository<WechatworkExternalUserJpa, WechatworkExternalUserIdJpa> {

}
