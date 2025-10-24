package com.scccy.service.system.dao.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.scccy.service.system.domain.jpa.WechatworkExternalUserJpa;
import com.scccy.service.system.domain.jpa.WechatworkExternalUserIdJpa;

/**
 * 好友关系 持久层
 *
 * @author scccy
 * @since 2025-10-22 16:37:01
 */
@Repository
public interface WechatworkExternalUserRepository extends JpaRepository<WechatworkExternalUserJpa, WechatworkExternalUserIdJpa> {

}
