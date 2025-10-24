package com.scccy.service.demo.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scccy.service.demo.domain.mp.SysUserMp;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息表(SysUser)Mapper 接口
 *
 * @author scccy
 * @since 2025-10-22 17:26:22
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserMp> {

}
