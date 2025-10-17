package com.scccy.common.modules.domain.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.scccy.common.modules.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 菜单权限表
 */
@Schema(description="菜单权限表")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_menu")
public class SysMenu extends BaseEntity implements Serializable {
    /**
     * 菜单ID
     */
    @TableId(value = "menu_id", type = IdType.AUTO)
    @Schema(description="菜单ID")
    private Long menuId;

    /**
     * 菜单名称
     */
    @TableField(value = "menu_name")
    @Schema(description="菜单名称")
    private String menuName;

    /**
     * 父菜单ID
     */
    @TableField(value = "parent_id")
    @Schema(description="父菜单ID")
    private Long parentId;

    /**
     * 显示顺序
     */
    @TableField(value = "order_num")
    @Schema(description="显示顺序")
    private Integer orderNum;

    /**
     * 路由地址
     */
    @TableField(value = "`path`")
    @Schema(description="路由地址")
    private String path;

    /**
     * 组件路径
     */
    @TableField(value = "component")
    @Schema(description="组件路径")
    private String component;

    /**
     * 路由参数
     */
    @TableField(value = "query")
    @Schema(description="路由参数")
    private String query;

    /**
     * 路由名称
     */
    @TableField(value = "route_name")
    @Schema(description="路由名称")
    private String routeName;

    /**
     * 是否为外链（0是 1否）
     */
    @TableField(value = "is_frame")
    @Schema(description="是否为外链（0是 1否）")
    private Integer isFrame;

    /**
     * 是否缓存（0缓存 1不缓存）
     */
    @TableField(value = "is_cache")
    @Schema(description="是否缓存（0缓存 1不缓存）")
    private Integer isCache;

    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    @TableField(value = "menu_type")
    @Schema(description="菜单类型（M目录 C菜单 F按钮）")
    private String menuType;

    /**
     * 菜单状态（0显示 1隐藏）
     */
    @TableField(value = "visible")
    @Schema(description="菜单状态（0显示 1隐藏）")
    private String visible;

    /**
     * 菜单状态（0正常 1停用）
     */
    @TableField(value = "`status`")
    @Schema(description="菜单状态（0正常 1停用）")
    private String status;

    /**
     * 权限标识
     */
    @TableField(value = "perms")
    @Schema(description="权限标识")
    private String perms;

    /**
     * 菜单图标
     */
    @TableField(value = "icon")
    @Schema(description="菜单图标")
    private String icon;

    /**
     * 备注
     */
    @TableField(value = "remark")
    @Schema(description="备注")
    private String remark;
}