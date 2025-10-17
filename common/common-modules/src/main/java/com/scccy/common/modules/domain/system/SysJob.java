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
 * 定时任务调度表
 */
@Schema(description="定时任务调度表")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_job")
public class SysJob extends BaseEntity implements Serializable {
    /**
     * 任务ID
     */
    @TableId(value = "job_id", type = IdType.AUTO)
    @Schema(description="任务ID")
    private Long jobId;

    /**
     * 任务名称
     */
    @TableId(value = "job_name", type = IdType.AUTO)
    @Schema(description="任务名称")
    private String jobName;

    /**
     * 任务组名
     */
    @TableId(value = "job_group", type = IdType.AUTO)
    @Schema(description="任务组名")
    private String jobGroup;

    /**
     * 调用目标字符串
     */
    @TableField(value = "invoke_target")
    @Schema(description="调用目标字符串")
    private String invokeTarget;

    /**
     * cron执行表达式
     */
    @TableField(value = "cron_expression")
    @Schema(description="cron执行表达式")
    private String cronExpression;

    /**
     * 计划执行错误策略（1立即执行 2执行一次 3放弃执行）
     */
    @TableField(value = "misfire_policy")
    @Schema(description="计划执行错误策略（1立即执行 2执行一次 3放弃执行）")
    private String misfirePolicy;

    /**
     * 是否并发执行（0允许 1禁止）
     */
    @TableField(value = "concurrent")
    @Schema(description="是否并发执行（0允许 1禁止）")
    private String concurrent;

    /**
     * 状态（0正常 1暂停）
     */
    @TableField(value = "`status`")
    @Schema(description="状态（0正常 1暂停）")
    private String status;

    /**
     * 备注信息
     */
    @TableField(value = "remark")
    @Schema(description="备注信息")
    private String remark;
}