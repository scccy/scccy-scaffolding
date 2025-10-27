package com.scccy.service.jackyun.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.scccy.service.jackyun.domain.jpa.SysUserJpa;

import java.lang.Long;

/**
 * 用户信息表 持久层
 *
 * @author scccy
 * @since 2025-10-22 17:26:22
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUserJpa, Long> {

}
