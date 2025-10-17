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
 * 定时任务调度日志表
 */
@Schema(description="定时任务调度日志表")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_job_log")
public class SysJobLog extends BaseEntity implements Serializable {
    /**
     * 任务日志ID
     */
    @TableId(value = "job_log_id", type = IdType.AUTO)
    @Schema(description="任务日志ID")
    private Long jobLogId;

    /**
     * 任务名称
     */
    @TableField(value = "job_name")
    @Schema(description="任务名称")
    private String jobName;

    /**
     * 任务组名
     */
    @TableField(value = "job_group")
    @Schema(description="任务组名")
    private String jobGroup;

    /**
     * 调用目标字符串
     */
    @TableField(value = "invoke_target")
    @Schema(description="调用目标字符串")
    private String invokeTarget;

    /**
     * 日志信息
     */
    @TableField(value = "job_message")
    @Schema(description="日志信息")
    private String jobMessage;

    /**
     * 执行状态（0正常 1失败）
     */
    @TableField(value = "`status`")
    @Schema(description="执行状态（0正常 1失败）")
    private String status;

    /**
     * 异常信息
     */
    @TableField(value = "exception_info")
    @Schema(description="异常信息")
    private String exceptionInfo;
}