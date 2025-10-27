package com.scccy.service.jackyun.domain.mp;

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
 * 企微用户群关联表(WechatworkGroup)实体类
 *
 * @author scccy
 * @since 2025-10-22 17:26:01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wechatwork_group")
@Schema(name = "WechatworkGroupMp", description = "企微用户群关联表")
public class WechatworkGroupMp extends Model<WechatworkGroupMp> implements Serializable {
    @Serial
    private static final long serialVersionUID = 791005980552431792L;

    /**
     * 企微id
     */
    @Schema(description = "企微id")
    @TableField("wechatwork_user_id")
    private String wechatworkUserId;

    /**
     * 群id
     */
    @Schema(description = "群id")
    @TableId("wechatwork_group_id")
    private String wechatworkGroupId;

    /**
     * 客户标识
     */
    @Schema(description = "客户标识")
    @TableField("wechatwork_external_union_id")
    private String wechatworkExternalUnionId;

    /**
     * 客户企微id
     */
    @Schema(description = "客户企微id")
    @TableId("wechatwork_external_user_id")
    private String wechatworkExternalUserId;

    /**
     * 状态 1 启用  0 禁用
     */
    @Schema(description = "状态 1 启用  0 禁用")
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    @TableField("create_by")
    private String createBy;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    @TableField("update_by")
    private String updateBy;

    /**
     * 1 未删除 0 已删除
     */
    @Schema(description = "1 未删除 0 已删除")
    @TableField("del_flag")
    private Integer delFlag;


}
