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
 * 字典类型表
 */
@Schema(description="字典类型表")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_dict_type")
public class SysDictType extends BaseEntity implements Serializable {
    /**
     * 字典主键
     */
    @TableId(value = "dict_id", type = IdType.AUTO)
    @Schema(description="字典主键")
    private Long dictId;

    /**
     * 字典名称
     */
    @TableField(value = "dict_name")
    @Schema(description="字典名称")
    private String dictName;

    /**
     * 字典类型
     */
    @TableField(value = "dict_type")
    @Schema(description="字典类型")
    private String dictType;

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