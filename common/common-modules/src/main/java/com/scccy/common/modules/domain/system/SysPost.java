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
 * 岗位信息表
 */
@Schema(description="岗位信息表")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_post")
public class SysPost extends BaseEntity implements Serializable {
    /**
     * 岗位ID
     */
    @TableId(value = "post_id", type = IdType.AUTO)
    @Schema(description="岗位ID")
    private Long postId;

    /**
     * 岗位编码
     */
    @TableField(value = "post_code")
    @Schema(description="岗位编码")
    private String postCode;

    /**
     * 岗位名称
     */
    @TableField(value = "post_name")
    @Schema(description="岗位名称")
    private String postName;

    /**
     * 显示顺序
     */
    @TableField(value = "post_sort")
    @Schema(description="显示顺序")
    private Integer postSort;

    /**
     * 状态（0正常 1停用）
     */
    @TableField(value = "`status`")
    @Schema(description="状态（0正常 1停用）")
    private String status;

    /**
     * 备注
     */
    @TableField(value = "remark")
    @Schema(description="备注")
    private String remark;
}