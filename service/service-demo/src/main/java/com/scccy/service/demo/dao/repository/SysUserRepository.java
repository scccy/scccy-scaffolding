package com.scccy.service.demo.dao.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.scccy.service.demo.domain.jpa.SysUserJpa;

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
