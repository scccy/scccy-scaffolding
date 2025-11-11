package com.scccy.service.system.domain.mp;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;

/**
 * 角色和菜单关联表(SysRoleMenu)实体类
 *
 * @author scccy
 * @since 2025-11-05 17:55:14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_role_menu")
@Schema(name = "SysRoleMenuMp", description = "角色和菜单关联表")
public class SysRoleMenuMp extends Model<SysRoleMenuMp> implements Serializable {
    @Serial
    private static final long serialVersionUID = 262812794658382413L;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    @TableField("role_id")
    private Long roleId;

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID")
    @TableField("menu_id")
    private Long menuId;


}
