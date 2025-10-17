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
 * 系统访问记录
 */
@Schema(description="系统访问记录")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_logininfor")
public class SysLogininfor extends BaseEntity implements Serializable {
    /**
     * 访问ID
     */
    @TableId(value = "info_id", type = IdType.AUTO)
    @Schema(description="访问ID")
    private Long infoId;

    /**
     * 用户账号
     */
    @TableField(value = "user_name")
    @Schema(description="用户账号")
    private String userName;

    /**
     * 登录IP地址
     */
    @TableField(value = "ipaddr")
    @Schema(description="登录IP地址")
    private String ipaddr;

    /**
     * 登录状态（0成功 1失败）
     */
    @TableField(value = "`status`")
    @Schema(description="登录状态（0成功 1失败）")
    private String status;

    /**
     * 提示信息
     */
    @TableField(value = "msg")
    @Schema(description="提示信息")
    private String msg;

    /**
     * 访问时间
     */
    @TableField(value = "access_time")
    @Schema(description="访问时间")
    private Date accessTime;
}