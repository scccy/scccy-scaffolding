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
 * 通知公告表
 */
@Schema(description="通知公告表")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_notice")
public class SysNotice extends BaseEntity implements Serializable {
    /**
     * 公告ID
     */
    @TableId(value = "notice_id", type = IdType.AUTO)
    @Schema(description="公告ID")
    private Integer noticeId;

    /**
     * 公告标题
     */
    @TableField(value = "notice_title")
    @Schema(description="公告标题")
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    @TableField(value = "notice_type")
    @Schema(description="公告类型（1通知 2公告）")
    private String noticeType;

    /**
     * 公告内容
     */
    @TableField(value = "notice_content")
    @Schema(description="公告内容")
    private byte[] noticeContent;

    /**
     * 公告状态（0正常 1关闭）
     */
    @TableField(value = "`status`")
    @Schema(description="公告状态（0正常 1关闭）")
    private String status;

    /**
     * 备注
     */
    @TableField(value = "remark")
    @Schema(description="备注")
    private String remark;
}