package com.scccy.common.modules.domain;

import com.scccy.common.modules.utils.EntityModelConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

@Data
@Schema
public class BaseForm<P> implements Serializable {
    @Schema(
            title = "用户名",
            description = "Form提交时操作人的用户名",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "admin"
    )
    private String username;

    public P toPo(Class<P> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        return (P) EntityModelConverter.getInstance().convert(this, clazz);
    }
}