package com.scccy.service.system.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.scccy.service.system.domain.jpa.SysRoleMenuJpa;
import com.scccy.service.system.domain.jpa.SysRoleMenuIdJpa;

/**
 * 角色和菜单关联表 持久层
 *
 * @author scccy
 * @since 2025-11-05 17:55:14
 */
@Repository
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenuJpa, SysRoleMenuIdJpa> {

}
