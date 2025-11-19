package com.scccy.service.auth.domain.mp;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.scccy.common.modules.domain.BaseEntity;
import com.scccy.service.auth.domain.vo.RegisteredClientVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * client记录表(Oauth2RegisteredClient)实体类
 *
 * @author scccy
 * @since 2025-11-01 14:03:12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("oauth2_registered_client")
@Schema(name = "Oauth2RegisteredClientMp", description = "client记录表")
public class Oauth2RegisteredClientMp extends BaseEntity<Oauth2RegisteredClientMp, RegisteredClientVo> {
    /**
     * UUID生成
     */
    @Schema(description = "UUID生成")
    @TableId("id")
    private String id;

    /**
     * client_id
     */
    @Schema(description = "client_id")
    @TableField("client_id")
    private String clientId;

    /**
     * client生成时间
     */
    @Schema(description = "client生成时间")
    @TableField("client_id_issued_at")
    private Date clientIdIssuedAt;

    /**
     * client密码
     */
    @Schema(description = "client密码")
    @TableField("client_secret")
    private String clientSecret;

    /**
     * client密码过期时间
     */
    @Schema(description = "client密码过期时间")
    @TableField("client_secret_expires_at")
    private Date clientSecretExpiresAt;

    /**
     * client名称
     */
    @Schema(description = "client名称")
    @TableField("client_name")
    private String clientName;

    /**
     * 客户端支持的authentication_methods如：client_secret_basic、basic等
     */
    @Schema(description = "客户端支持的authentication_methods如：client_secret_basic、basic等")
    @TableField("client_authentication_methods")
    private String clientAuthenticationMethods;

    /**
     * 客户端支持的grant_type如：refresh_token,client_credentials,authorization_code等
     */
    @Schema(description = "客户端支持的grant_type如：refresh_token,client_credentials,authorization_code等")
    @TableField("authorization_grant_types")
    private String authorizationGrantTypes;

    /**
     * 跳转url
     */
    @Schema(description = "跳转url")
    @TableField("redirect_uris")
    private String redirectUris;

    /**
     * 注销url
     */
    @Schema(description = "注销url")
    @TableField("post_logout_redirect_uris")
    private String postLogoutRedirectUris;

    /**
     * client支持的scope如:read、write等
     */
    @Schema(description = "client支持的scope如:read、write等")
    @TableField("scopes")
    private String scopes;

    /**
     * client设置如：过期时间
     */
    @Schema(description = "client设置如：过期时间")
    @TableField(value = "client_settings",typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> clientSettings;

    /**
     * token设置如：过期时间、类型等
     */
    @Schema(description = "token设置如：过期时间、类型等")
    @TableField(value = "token_settings",typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> tokenSettings;



}
