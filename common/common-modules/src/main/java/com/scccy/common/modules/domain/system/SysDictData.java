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
 * 字典数据表
 */
@Schema(description="字典数据表")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_dict_data")
public class SysDictData extends BaseEntity implements Serializable {
    /**
     * 字典编码
     */
    @TableId(value = "dict_code", type = IdType.AUTO)
    @Schema(description="字典编码")
    private Long dictCode;

    /**
     * 字典排序
     */
    @TableField(value = "dict_sort")
    @Schema(description="字典排序")
    private Integer dictSort;

    /**
     * 字典标签
     */
    @TableField(value = "dict_label")
    @Schema(description="字典标签")
    private String dictLabel;

    /**
     * 字典键值
     */
    @TableField(value = "dict_value")
    @Schema(description="字典键值")
    private String dictValue;

    /**
     * 字典类型
     */
    @TableField(value = "dict_type")
    @Schema(description="字典类型")
    private String dictType;

    /**
     * 样式属性（其他样式扩展）
     */
    @TableField(value = "css_class")
    @Schema(description="样式属性（其他样式扩展）")
    private String cssClass;

    /**
     * 表格回显样式
     */
    @TableField(value = "list_class")
    @Schema(description="表格回显样式")
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    @TableField(value = "is_default")
    @Schema(description="是否默认（Y是 N否）")
    private String isDefault;

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