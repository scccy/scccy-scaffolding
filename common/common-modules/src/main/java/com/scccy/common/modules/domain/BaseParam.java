package com.scccy.common.modules.domain;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.io.Serializable;
import java.util.Date;

@Data  // Lombok注解，自动生成getter、setter、equals、hashCode和toString方法
public class BaseParam implements Serializable {

    @Schema(
        title = "开始时间",
        description = "查询条件创建记录的开始时间",
        requiredMode = RequiredMode.REQUIRED,
        example = "2020-05-06 12:23:23"
    )
    private Date createdTimeStart;

    @Schema(
        title = "结束时间",
        description = "查询条件创建记录的结束时间",
        requiredMode = RequiredMode.REQUIRED,
        example = "2020-05-12 12:23:23"
    )
    private Date createdTimeEnd;

}