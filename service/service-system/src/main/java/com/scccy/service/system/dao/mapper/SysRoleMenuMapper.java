package com.scccy.service.system.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scccy.service.system.domain.mp.SysRoleMenuMp;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色和菜单关联表(SysRoleMenu)Mapper 接口
 *
 * @author scccy
 * @since 2025-11-05 17:55:14
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenuMp> {

}
