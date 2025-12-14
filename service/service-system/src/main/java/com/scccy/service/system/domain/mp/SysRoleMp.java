package com.scccy.service.system.domain.mp;

import java.util.Date;
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
 * 角色信息表(SysRole)实体类
 *
 * @author scccy
 * @since 2025-11-06 15:49:52
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_role")
@Schema(name = "SysRoleMp", description = "角色信息表")
public class SysRoleMp extends Model<SysRoleMp> implements Serializable {
    @Serial
    private static final long serialVersionUID = 170147702554898217L;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    @TableId("role_id")
    private Long roleId;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    @TableField("role_name")
    private String roleName;

    /**
     * 角色权限字符串
     */
    @Schema(description = "角色权限字符串")
    @TableField("role_key")
    private String roleKey;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    @TableField("role_sort")
    private Integer roleSort;

    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
     */
    @Schema(description = "数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）")
    @TableField("data_scope")
    private String dataScope;

    /**
     * 菜单树选择项是否关联显示
     */
    @Schema(description = "菜单树选择项是否关联显示")
    @TableField("menu_check_strictly")
    private Integer menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示
     */
    @Schema(description = "部门树选择项是否关联显示")
    @TableField("dept_check_strictly")
    private Integer deptCheckStrictly;

    /**
     * 角色状态（0正常 1停用）
     */
    @Schema(description = "角色状态（0正常 1停用）")
    @TableField("status")
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @Schema(description = "删除标志（0代表存在 2代表删除）")
    @TableField("del_flag")
    private String delFlag;

    /**
     * 创建者
     */
    @Schema(description = "创建者")
    @TableField("create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新者
     */
    @Schema(description = "更新者")
    @TableField("update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    /**
     * 备注
     */
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;


}
