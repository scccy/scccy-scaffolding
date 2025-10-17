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
import java.util.Date;

/**
 * 操作日志记录
 */
@Schema(description="操作日志记录")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_oper_log")
public class SysOperLog extends BaseEntity implements Serializable {
    /**
     * 日志主键
     */
    @TableId(value = "oper_id", type = IdType.AUTO)
    @Schema(description="日志主键")
    private Long operId;

    /**
     * 模块标题
     */
    @TableField(value = "title")
    @Schema(description="模块标题")
    private String title;

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    @TableField(value = "business_type")
    @Schema(description="业务类型（0其它 1新增 2修改 3删除）")
    private Integer businessType;

    /**
     * 方法名称
     */
    @TableField(value = "`method`")
    @Schema(description="方法名称")
    private String method;

    /**
     * 请求方式
     */
    @TableField(value = "request_method")
    @Schema(description="请求方式")
    private String requestMethod;

    /**
     * 操作类别（0其它 1后台用户 2手机端用户）
     */
    @TableField(value = "operator_type")
    @Schema(description="操作类别（0其它 1后台用户 2手机端用户）")
    private Integer operatorType;

    /**
     * 操作人员
     */
    @TableField(value = "oper_name")
    @Schema(description="操作人员")
    private String operName;

    /**
     * 部门名称
     */
    @TableField(value = "dept_name")
    @Schema(description="部门名称")
    private String deptName;

    /**
     * 请求URL
     */
    @TableField(value = "oper_url")
    @Schema(description="请求URL")
    private String operUrl;

    /**
     * 主机地址
     */
    @TableField(value = "oper_ip")
    @Schema(description="主机地址")
    private String operIp;

    /**
     * 操作地点
     */
    @TableField(value = "oper_location")
    @Schema(description="操作地点")
    private String operLocation;

    /**
     * 请求参数
     */
    @TableField(value = "oper_param")
    @Schema(description="请求参数")
    private String operParam;

    /**
     * 返回参数
     */
    @TableField(value = "json_result")
    @Schema(description="返回参数")
    private String jsonResult;

    /**
     * 操作状态（0正常 1异常）
     */
    @TableField(value = "`status`")
    @Schema(description="操作状态（0正常 1异常）")
    private Integer status;

    /**
     * 错误消息
     */
    @TableField(value = "error_msg")
    @Schema(description="错误消息")
    private String errorMsg;

    /**
     * 操作时间
     */
    @TableField(value = "oper_time")
    @Schema(description="操作时间")
    private Date operTime;

    /**
     * 消耗时间
     */
    @TableField(value = "cost_time")
    @Schema(description="消耗时间")
    private Long costTime;
}