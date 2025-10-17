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
 * 参数配置表
 */
@Schema(description="参数配置表")
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_config")
public class SysConfig extends BaseEntity implements Serializable {
    /**
     * 参数主键
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    @Schema(description="参数主键")
    private Integer configId;

    /**
     * 参数名称
     */
    @TableField(value = "config_name")
    @Schema(description="参数名称")
    private String configName;

    /**
     * 参数键名
     */
    @TableField(value = "config_key")
    @Schema(description="参数键名")
    private String configKey;

    /**
     * 参数键值
     */
    @TableField(value = "config_value")
    @Schema(description="参数键值")
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    @TableField(value = "config_type")
    @Schema(description="系统内置（Y是 N否）")
    private String configType;

    /**
     * 备注
     */
    @TableField(value = "remark")
    @Schema(description="备注")
    private String remark;
}