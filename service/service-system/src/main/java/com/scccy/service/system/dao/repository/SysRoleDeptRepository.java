package com.scccy.service.system.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.scccy.service.system.domain.jpa.SysRoleDeptJpa;
import com.scccy.service.system.domain.jpa.SysRoleDeptIdJpa;

/**
 * 角色和部门关联表 持久层
 *
 * @author scccy
 * @since 2025-11-05 17:55:13
 */
@Repository
public interface SysRoleDeptRepository extends JpaRepository<SysRoleDeptJpa, SysRoleDeptIdJpa> {

}
