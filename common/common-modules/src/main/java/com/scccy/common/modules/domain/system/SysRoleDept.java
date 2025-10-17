package com.scccy.common.modules.domain.system;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 角色和部门关联表
 */
@Schema(description="角色和部门关联表")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_role_dept")
public class SysRoleDept extends BaseEntity implements Serializable {
    /**
     * 角色ID
     */
    @TableId(value = "role_id", type = IdType.AUTO)
    @Schema(description="角色ID")
    private Long roleId;

    /**
     * 部门ID
     */
    @TableId(value = "dept_id", type = IdType.AUTO)
    @Schema(description="部门ID")
    private Long deptId;
}