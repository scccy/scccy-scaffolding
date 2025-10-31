package com.scccy.service.wechatwork.domain.mp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 好友关系(WechatworkExternalUser)实体类
 *
 * @author scccy
 * @since 2025-10-24 17:12:06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wechatwork_external_user")
@Schema(name = "WechatworkExternalUserMp", description = "好友关系")
public class WechatworkExternalUserMp extends Model<WechatworkExternalUserMp> implements Serializable {
    @Serial
    private static final long serialVersionUID = -14683513622400071L;

    /**
     * 企微id
     */
    @Schema(description = "企微id")
    @TableId("wechatwork_user_id")
    private String wechatworkUserId;

    /**
     * 客户标识
     */
    @Schema(description = "客户标识")
    @TableField("wechatwork_union_id")
    private String wechatworkUnionId;

    /**
     * 客户企微id
     */
    @Schema(description = "客户企微id")
    @TableField("wechatwork_external_userid")
    private String wechatworkExternalUserid;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    @TableField("user_id")
    private String userId;

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
