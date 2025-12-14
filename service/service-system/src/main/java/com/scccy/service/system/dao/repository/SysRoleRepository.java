package com.scccy.service.system.dao.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.scccy.service.system.domain.jpa.SysRoleJpa;

import java.lang.Long;

/**
 * 角色信息表 持久层
 *
 * @author scccy
 * @since 2025-11-05 17:55:12
 */
@Repository
public interface SysRoleRepository extends JpaRepository<SysRoleJpa, Long> {

}
