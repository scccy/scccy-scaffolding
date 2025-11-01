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
import java.util.Date;

/**
 * token记录表(Oauth2Authorization)实体类
 *
 * @author scccy
 * @since 2025-11-01 15:18:15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("oauth2_authorization")
@Schema(name = "Oauth2AuthorizationMp", description = "token记录表")
public class Oauth2AuthorizationMp extends Model<Oauth2AuthorizationMp> implements Serializable {
    @Serial
    private static final long serialVersionUID = 144136690644201970L;

    /**
     * UUID生成
     */
    @Schema(description = "UUID生成")
    @TableId("id")
    private String id;

    /**
     * clientId
     */
    @Schema(description = "clientId")
    @TableField("registered_client_id")
    private String registeredClientId;

    /**
     * 身份信息，一般为clientId
     */
    @Schema(description = "身份信息，一般为clientId")
    @TableField("principal_name")
    private String principalName;

    /**
     * 客户端支持的grant_type如：refresh_token,client_credentials,authorization_code等
     */
    @Schema(description = "客户端支持的grant_type如：refresh_token,client_credentials,authorization_code等")
    @TableField("authorization_grant_type")
    private String authorizationGrantType;

    /**
     * 客户
     */
    @Schema(description = "客户")
    @TableField("authorized_scopes")
    private String authorizedScopes;

    /**
     * 其他属性
     */
    @Schema(description = "其他属性")
    @TableField("attributes")
    private byte[] attributes;

    /**
     * token状态
     */
    @Schema(description = "token状态")
    @TableField("state")
    private String state;

    /**
     * 预授权码值
     */
    @Schema(description = "预授权码值")
    @TableField("authorization_code_value")
    private byte[] authorizationCodeValue;

    /**
     * 预授权码生成时间
     */
    @Schema(description = "预授权码生成时间")
    @TableField("authorization_code_issued_at")
    private Date authorizationCodeIssuedAt;

    /**
     * 预授权码过期时间
     */
    @Schema(description = "预授权码过期时间")
    @TableField("authorization_code_expires_at")
    private Date authorizationCodeExpiresAt;

    /**
     * 预授权码原数据，java实例
     */
    @Schema(description = "预授权码原数据，java实例")
    @TableField("authorization_code_metadata")
    private byte[] authorizationCodeMetadata;

    /**
     * access_token值
     */
    @Schema(description = "access_token值")
    @TableField("access_token_value")
    private byte[] accessTokenValue;

    /**
     * access_token生成时间
     */
    @Schema(description = "access_token生成时间")
    @TableField("access_token_issued_at")
    private Date accessTokenIssuedAt;

    /**
     * access_token过期时间
     */
    @Schema(description = "access_token过期时间")
    @TableField("access_token_expires_at")
    private Date accessTokenExpiresAt;

    /**
     * access_token原数据，java实例
     */
    @Schema(description = "access_token原数据，java实例")
    @TableField("access_token_metadata")
    private byte[] accessTokenMetadata;

    /**
     * access_token类型如：Bearer
     */
    @Schema(description = "access_token类型如：Bearer")
    @TableField("access_token_type")
    private String accessTokenType;

    /**
     * access_token scopes如:read、write等
     */
    @Schema(description = "access_token scopes如:read、write等")
    @TableField("access_token_scopes")
    private String accessTokenScopes;

    /**
     * oidc_id_token值
     */
    @Schema(description = "oidc_id_token值")
    @TableField("oidc_id_token_value")
    private byte[] oidcIdTokenValue;

    /**
     * oidc_id_token生成时间
     */
    @Schema(description = "oidc_id_token生成时间")
    @TableField("oidc_id_token_issued_at")
    private Date oidcIdTokenIssuedAt;

    /**
     * oidc_id_token过期时间
     */
    @Schema(description = "oidc_id_token过期时间")
    @TableField("oidc_id_token_expires_at")
    private Date oidcIdTokenExpiresAt;

    /**
     * oidc_id_token元数据
     */
    @Schema(description = "oidc_id_token元数据")
    @TableField("oidc_id_token_metadata")
    private byte[] oidcIdTokenMetadata;

    /**
     * refresh_token元数据，java实例
     */
    @Schema(description = "refresh_token元数据，java实例")
    @TableField("refresh_token_value")
    private byte[] refreshTokenValue;

    /**
     * refresh_token生成时间
     */
    @Schema(description = "refresh_token生成时间")
    @TableField("refresh_token_issued_at")
    private Date refreshTokenIssuedAt;

    /**
     * refresh_token过期时间
     */
    @Schema(description = "refresh_token过期时间")
    @TableField("refresh_token_expires_at")
    private Date refreshTokenExpiresAt;

    /**
     * refresh_token元数据，java实例
     */
    @Schema(description = "refresh_token元数据，java实例")
    @TableField("refresh_token_metadata")
    private byte[] refreshTokenMetadata;

    /**
     * 用户授权码
     */
    @Schema(description = "用户授权码")
    @TableField("user_code_value")
    private byte[] userCodeValue;

    /**
     * 用户授权码生成时间
     */
    @Schema(description = "用户授权码生成时间")
    @TableField("user_code_issued_at")
    private Date userCodeIssuedAt;

    /**
     * 用户授权码过期时间
     */
    @Schema(description = "用户授权码过期时间")
    @TableField("user_code_expires_at")
    private Date userCodeExpiresAt;

    /**
     * 用户授权码元数据
     */
    @Schema(description = "用户授权码元数据")
    @TableField("user_code_metadata")
    private byte[] userCodeMetadata;

    /**
     * 设备授权码
     */
    @Schema(description = "设备授权码")
    @TableField("device_code_value")
    private byte[] deviceCodeValue;

    /**
     * 设备授权码生成时间
     */
    @Schema(description = "设备授权码生成时间")
    @TableField("device_code_issued_at")
    private Date deviceCodeIssuedAt;

    /**
     * 设备授权码过期时间
     */
    @Schema(description = "设备授权码过期时间")
    @TableField("device_code_expires_at")
    private Date deviceCodeExpiresAt;

    /**
     * 设备授权码元数据
     */
    @Schema(description = "设备授权码元数据")
    @TableField("device_code_metadata")
    private byte[] deviceCodeMetadata;


}
