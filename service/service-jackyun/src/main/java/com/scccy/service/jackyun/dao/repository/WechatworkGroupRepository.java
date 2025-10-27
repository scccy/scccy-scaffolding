package com.scccy.service.jackyun.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.scccy.service.jackyun.domain.jpa.WechatworkGroupJpa;
import com.scccy.service.jackyun.domain.jpa.WechatworkGroupIdJpa;

/**
 * 企微用户群关联表 持久层
 *
 * @author scccy
 * @since 2025-10-22 17:26:01
 */
@Repository
public interface WechatworkGroupRepository extends JpaRepository<WechatworkGroupJpa, WechatworkGroupIdJpa> {

}
