package com.scccy.service.system.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scccy.service.system.domain.mp.SysRoleDeptMp;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色和部门关联表(SysRoleDept)Mapper 接口
 *
 * @author scccy
 * @since 2025-11-05 17:55:13
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDeptMp> {

}
