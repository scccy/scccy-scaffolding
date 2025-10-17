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
 * 部门表
 */
@Schema(description="部门表")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_dept")
public class SysDept extends BaseEntity implements Serializable {
    /**
     * 部门id
     */
    @TableId(value = "dept_id", type = IdType.AUTO)
    @Schema(description="部门id")
    private Long deptId;

    /**
     * 父部门id
     */
    @TableField(value = "parent_id")
    @Schema(description="父部门id")
    private Long parentId;

    /**
     * 祖级列表
     */
    @TableField(value = "ancestors")
    @Schema(description="祖级列表")
    private String ancestors;

    /**
     * 部门名称
     */
    @TableField(value = "dept_name")
    @Schema(description="部门名称")
    private String deptName;

    /**
     * 显示顺序
     */
    @TableField(value = "order_num")
    @Schema(description="显示顺序")
    private Integer orderNum;

    /**
     * 负责人
     */
    @TableField(value = "leader")
    @Schema(description="负责人")
    private String leader;

    /**
     * 联系电话
     */
    @TableField(value = "phone")
    @Schema(description="联系电话")
    private String phone;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    @Schema(description="邮箱")
    private String email;

    /**
     * 部门状态（0正常 1停用）
     */
    @TableField(value = "`status`")
    @Schema(description="部门状态（0正常 1停用）")
    private String status;
}