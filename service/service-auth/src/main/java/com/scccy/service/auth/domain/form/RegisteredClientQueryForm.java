package com.scccy.service.auth.domain.form;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scccy.common.modules.utils.EntityModelConverter;
import com.scccy.service.auth.domain.param.RegisteredClientQueryParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisteredClientQueryForm {
    /**
     * clientId oauth2客户端id
     */
    private String clientId;

    /**
     * clientName oauth2客户端名称
     */
    private String clientName;

    /**
     * 分页参数：当前页数
     */
    private long current = 1L;

    /**
     * 分页参数：每页数量
     */
    private long size = 10L;

    /**
     * 将表单转换成分页查询参数
     * @param clazz 转换的目标类型
     * @return 转换后的查询参数对象
     */
    public RegisteredClientQueryParam toParam(Class<RegisteredClientQueryParam> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        return EntityModelConverter.getInstance().convert(this, clazz);
    }

    /**
     * 获取分页对象
     * @return 分页对象
     */
    public Page<RegisteredClientQueryParam> getPage() {
        return new Page<>(this.current, this.size);
    }
}