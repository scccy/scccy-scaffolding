package com.scccy.service.system.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scccy.service.system.domain.mp.SysUserMp;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息表(SysUser)Mapper 接口
 *
 * @author scccy
 * @since 2025-10-22 16:27:00
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserMp> {

}
