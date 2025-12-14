package com.scccy.service.system.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scccy.common.modules.domain.mp.system.SysUserMp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户信息表(SysUser)Mapper 接口
 *
 * @author scccy
 * @since 2025-10-22 16:27:00
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserMp> {

    /**
     * 查询用户的所有权限（角色标识和菜单权限）
     * <p>
     * 查询链路：sys_user -> sys_user_role -> sys_role -> sys_role_menu -> sys_menu
     * 返回格式：
     * - 角色标识：ROLE_ADMIN, ROLE_USER（Spring Security 标准格式）
     * - 菜单权限：system:user:list, system:user:add（菜单 perms 字段）
     *
     * @param userName 用户名
     * @return 权限列表
     */
    @Select("SELECT DISTINCT " +
            "    CONCAT('ROLE_', UPPER(r.role_key)) as authority " +
            "FROM sys_user u " +
            "JOIN sys_user_role ur ON u.user_id = ur.user_id " +
            "JOIN sys_role r ON ur.role_id = r.role_id " +
            "WHERE u.user_name = #{userName} " +
            "    AND u.del_flag = 0 " +
            "    AND r.status = '0' " +
            "    AND r.del_flag = 0 " +
            "UNION " +
            "SELECT DISTINCT " +
            "    m.perms as authority " +
            "FROM sys_user u " +
            "JOIN sys_user_role ur ON u.user_id = ur.user_id " +
            "JOIN sys_role r ON ur.role_id = r.role_id " +
            "LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id " +
            "LEFT JOIN sys_menu m ON rm.menu_id = m.menu_id " +
            "WHERE u.user_name = #{userName} " +
            "    AND u.del_flag = 0 " +
            "    AND r.status = '0' " +
            "    AND r.del_flag = 0 " +
            "    AND m.perms IS NOT NULL " +
            "    AND m.perms != '' " +
            "ORDER BY authority")
    List<String> getUserAuthorities(@Param("userName") String userName);
}
