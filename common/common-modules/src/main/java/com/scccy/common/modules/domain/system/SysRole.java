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
 * 角色信息表
 */
@Schema(description="角色信息表")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_role")
public class SysRole extends BaseEntity implements Serializable {
    /**
     * 角色ID
     */
    @TableId(value = "role_id", type = IdType.AUTO)
    @Schema(description="角色ID")
    private Long roleId;

    /**
     * 角色名称
     */
    @TableField(value = "role_name")
    @Schema(description="角色名称")
    private String roleName;

    /**
     * 角色权限字符串
     */
    @TableField(value = "role_key")
    @Schema(description="角色权限字符串")
    private String roleKey;

    /**
     * 显示顺序
     */
    @TableField(value = "role_sort")
    @Schema(description="显示顺序")
    private Integer roleSort;

    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
     */
    @TableField(value = "data_scope")
    @Schema(description="数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）")
    private String dataScope;

    /**
     * 菜单树选择项是否关联显示
     */
    @TableField(value = "menu_check_strictly")
    @Schema(description="菜单树选择项是否关联显示")
    private Boolean menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示
     */
    @TableField(value = "dept_check_strictly")
    @Schema(description="部门树选择项是否关联显示")
    private Boolean deptCheckStrictly;

    /**
     * 角色状态（0正常 1停用）
     */
    @TableField(value = "`status`")
    @Schema(description="角色状态（0正常 1停用）")
    private String status;

    /**
     * 备注
     */
    @TableField(value = "remark")
    @Schema(description="备注")
    private String remark;
}