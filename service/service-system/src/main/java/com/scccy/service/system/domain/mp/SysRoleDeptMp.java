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
 * 角色和部门关联表(SysRoleDept)实体类
 *
 * @author scccy
 * @since 2025-11-05 17:55:13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_role_dept")
@Schema(name = "SysRoleDeptMp", description = "角色和部门关联表")
public class SysRoleDeptMp extends Model<SysRoleDeptMp> implements Serializable {
    @Serial
    private static final long serialVersionUID = 587538666529168319L;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    @TableId("role_id")
    private Long roleId;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    @TableId("dept_id")
    private Long deptId;


}
