package com.scccy.service.auth.domain.mp;

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

/**
 * 授权记录(Oauth2AuthorizationConsent)实体类
 *
 * @author scccy
 * @since 2025-11-01 15:18:41
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("oauth2_authorization_consent")
@Schema(name = "Oauth2AuthorizationConsentMp", description = "授权记录")
public class Oauth2AuthorizationConsentMp extends Model<Oauth2AuthorizationConsentMp> implements Serializable {
    @Serial
    private static final long serialVersionUID = 987733782240595919L;

    /**
     * client_id
     */
    @Schema(description = "client_id")
    @TableId("registered_client_id")
    private String registeredClientId;

    /**
     * 身份信息，一般为clientId
     */
    @Schema(description = "身份信息，一般为clientId")
    @TableField("principal_name")
    private String principalName;

    /**
     * 授权记录
     */
    @Schema(description = "授权记录")
    @TableField("authorities")
    private String authorities;


}
