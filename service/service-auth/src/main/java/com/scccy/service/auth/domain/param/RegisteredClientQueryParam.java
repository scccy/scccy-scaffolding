package com.scccy.service.auth.domain.param;


import com.scccy.common.modules.domain.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisteredClientQueryParam extends BaseParam {
    /**
     * clientId oauth2客户端id
     */
    private String clientId;
    /**
     * clientName oauth2客户端名称
     */
    private String clientName;
}
