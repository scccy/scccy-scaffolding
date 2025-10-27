package com.scccy.service.wechatwork.dao.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.scccy.service.wechatwork.domain.jpa.WechatworkGroupJpa;
import com.scccy.service.wechatwork.domain.jpa.WechatworkGroupIdJpa;

/**
 * 企微用户群关联表 持久层
 *
 * @author scccy
 * @since 2025-10-24 18:09:45
 */
@Repository
public interface WechatworkGroupRepository extends JpaRepository<WechatworkGroupJpa, WechatworkGroupIdJpa> {

}
