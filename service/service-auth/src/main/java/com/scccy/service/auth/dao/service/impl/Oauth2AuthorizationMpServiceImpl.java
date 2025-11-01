package com.scccy.service.auth.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.auth.dao.mapper.Oauth2AuthorizationMapper;
import com.scccy.service.auth.domain.mp.Oauth2AuthorizationMp;
import com.scccy.service.auth.dao.service.Oauth2AuthorizationMpService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * token记录表(Oauth2Authorization)服务实现类
 *
 * @author scccy
 * @since 2025-11-01 15:18:16
 */
@Service
public class Oauth2AuthorizationMpServiceImpl
        extends ServiceImpl<Oauth2AuthorizationMapper, Oauth2AuthorizationMp>
        implements Oauth2AuthorizationMpService {
    @Override
    public Page<Oauth2AuthorizationMp> pageEq(Integer pageNum, Integer pageSize, Oauth2AuthorizationMp oauth2AuthorizationMp) {
        Page<Oauth2AuthorizationMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Oauth2AuthorizationMp> wrapper = new QueryWrapper<>();

        if (oauth2AuthorizationMp.getId() != null && !oauth2AuthorizationMp.getId().isEmpty()) {
            wrapper.eq("id", oauth2AuthorizationMp.getId());
        }
        if (oauth2AuthorizationMp.getRegisteredClientId() != null && !oauth2AuthorizationMp.getRegisteredClientId().isEmpty()) {
            wrapper.eq("registered_client_id", oauth2AuthorizationMp.getRegisteredClientId());
        }
        if (oauth2AuthorizationMp.getPrincipalName() != null && !oauth2AuthorizationMp.getPrincipalName().isEmpty()) {
            wrapper.eq("principal_name", oauth2AuthorizationMp.getPrincipalName());
        }
        if (oauth2AuthorizationMp.getAuthorizationGrantType() != null && !oauth2AuthorizationMp.getAuthorizationGrantType().isEmpty()) {
            wrapper.eq("authorization_grant_type", oauth2AuthorizationMp.getAuthorizationGrantType());
        }
        if (oauth2AuthorizationMp.getAuthorizedScopes() != null && !oauth2AuthorizationMp.getAuthorizedScopes().isEmpty()) {
            wrapper.eq("authorized_scopes", oauth2AuthorizationMp.getAuthorizedScopes());
        }
        if (oauth2AuthorizationMp.getAttributes() != null) {
            wrapper.eq("attributes", oauth2AuthorizationMp.getAttributes());
        }
        if (oauth2AuthorizationMp.getState() != null && !oauth2AuthorizationMp.getState().isEmpty()) {
            wrapper.eq("state", oauth2AuthorizationMp.getState());
        }
        if (oauth2AuthorizationMp.getAuthorizationCodeValue() != null) {
            wrapper.eq("authorization_code_value", oauth2AuthorizationMp.getAuthorizationCodeValue());
        }
        if (oauth2AuthorizationMp.getAuthorizationCodeIssuedAt() != null) {
            wrapper.eq("authorization_code_issued_at", oauth2AuthorizationMp.getAuthorizationCodeIssuedAt());
        }
        if (oauth2AuthorizationMp.getAuthorizationCodeExpiresAt() != null) {
            wrapper.eq("authorization_code_expires_at", oauth2AuthorizationMp.getAuthorizationCodeExpiresAt());
        }
        if (oauth2AuthorizationMp.getAuthorizationCodeMetadata() != null) {
            wrapper.eq("authorization_code_metadata", oauth2AuthorizationMp.getAuthorizationCodeMetadata());
        }
        if (oauth2AuthorizationMp.getAccessTokenValue() != null) {
            wrapper.eq("access_token_value", oauth2AuthorizationMp.getAccessTokenValue());
        }
        if (oauth2AuthorizationMp.getAccessTokenIssuedAt() != null) {
            wrapper.eq("access_token_issued_at", oauth2AuthorizationMp.getAccessTokenIssuedAt());
        }
        if (oauth2AuthorizationMp.getAccessTokenExpiresAt() != null) {
            wrapper.eq("access_token_expires_at", oauth2AuthorizationMp.getAccessTokenExpiresAt());
        }
        if (oauth2AuthorizationMp.getAccessTokenMetadata() != null) {
            wrapper.eq("access_token_metadata", oauth2AuthorizationMp.getAccessTokenMetadata());
        }
        if (oauth2AuthorizationMp.getAccessTokenType() != null && !oauth2AuthorizationMp.getAccessTokenType().isEmpty()) {
            wrapper.eq("access_token_type", oauth2AuthorizationMp.getAccessTokenType());
        }
        if (oauth2AuthorizationMp.getAccessTokenScopes() != null && !oauth2AuthorizationMp.getAccessTokenScopes().isEmpty()) {
            wrapper.eq("access_token_scopes", oauth2AuthorizationMp.getAccessTokenScopes());
        }
        if (oauth2AuthorizationMp.getOidcIdTokenValue() != null) {
            wrapper.eq("oidc_id_token_value", oauth2AuthorizationMp.getOidcIdTokenValue());
        }
        if (oauth2AuthorizationMp.getOidcIdTokenIssuedAt() != null) {
            wrapper.eq("oidc_id_token_issued_at", oauth2AuthorizationMp.getOidcIdTokenIssuedAt());
        }
        if (oauth2AuthorizationMp.getOidcIdTokenExpiresAt() != null) {
            wrapper.eq("oidc_id_token_expires_at", oauth2AuthorizationMp.getOidcIdTokenExpiresAt());
        }
        if (oauth2AuthorizationMp.getOidcIdTokenMetadata() != null) {
            wrapper.eq("oidc_id_token_metadata", oauth2AuthorizationMp.getOidcIdTokenMetadata());
        }
        if (oauth2AuthorizationMp.getRefreshTokenValue() != null) {
            wrapper.eq("refresh_token_value", oauth2AuthorizationMp.getRefreshTokenValue());
        }
        if (oauth2AuthorizationMp.getRefreshTokenIssuedAt() != null) {
            wrapper.eq("refresh_token_issued_at", oauth2AuthorizationMp.getRefreshTokenIssuedAt());
        }
        if (oauth2AuthorizationMp.getRefreshTokenExpiresAt() != null) {
            wrapper.eq("refresh_token_expires_at", oauth2AuthorizationMp.getRefreshTokenExpiresAt());
        }
        if (oauth2AuthorizationMp.getRefreshTokenMetadata() != null) {
            wrapper.eq("refresh_token_metadata", oauth2AuthorizationMp.getRefreshTokenMetadata());
        }
        if (oauth2AuthorizationMp.getUserCodeValue() != null) {
            wrapper.eq("user_code_value", oauth2AuthorizationMp.getUserCodeValue());
        }
        if (oauth2AuthorizationMp.getUserCodeIssuedAt() != null) {
            wrapper.eq("user_code_issued_at", oauth2AuthorizationMp.getUserCodeIssuedAt());
        }
        if (oauth2AuthorizationMp.getUserCodeExpiresAt() != null) {
            wrapper.eq("user_code_expires_at", oauth2AuthorizationMp.getUserCodeExpiresAt());
        }
        if (oauth2AuthorizationMp.getUserCodeMetadata() != null) {
            wrapper.eq("user_code_metadata", oauth2AuthorizationMp.getUserCodeMetadata());
        }
        if (oauth2AuthorizationMp.getDeviceCodeValue() != null) {
            wrapper.eq("device_code_value", oauth2AuthorizationMp.getDeviceCodeValue());
        }
        if (oauth2AuthorizationMp.getDeviceCodeIssuedAt() != null) {
            wrapper.eq("device_code_issued_at", oauth2AuthorizationMp.getDeviceCodeIssuedAt());
        }
        if (oauth2AuthorizationMp.getDeviceCodeExpiresAt() != null) {
            wrapper.eq("device_code_expires_at", oauth2AuthorizationMp.getDeviceCodeExpiresAt());
        }
        if (oauth2AuthorizationMp.getDeviceCodeMetadata() != null) {
            wrapper.eq("device_code_metadata", oauth2AuthorizationMp.getDeviceCodeMetadata());
        }

        return this.page(page, wrapper);
    }


    @Override
    public Page<Oauth2AuthorizationMp> pageLike(Integer pageNum, Integer pageSize, Oauth2AuthorizationMp oauth2AuthorizationMp) {
        Page<Oauth2AuthorizationMp> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Oauth2AuthorizationMp> wrapper = new QueryWrapper<>();

        if (oauth2AuthorizationMp.getId() != null && !oauth2AuthorizationMp.getId().isEmpty()) {
            wrapper.like("id", oauth2AuthorizationMp.getId());
        }
        if (oauth2AuthorizationMp.getRegisteredClientId() != null && !oauth2AuthorizationMp.getRegisteredClientId().isEmpty()) {
            wrapper.like("registered_client_id", oauth2AuthorizationMp.getRegisteredClientId());
        }
        if (oauth2AuthorizationMp.getPrincipalName() != null && !oauth2AuthorizationMp.getPrincipalName().isEmpty()) {
            wrapper.like("principal_name", oauth2AuthorizationMp.getPrincipalName());
        }
        if (oauth2AuthorizationMp.getAuthorizationGrantType() != null && !oauth2AuthorizationMp.getAuthorizationGrantType().isEmpty()) {
            wrapper.like("authorization_grant_type", oauth2AuthorizationMp.getAuthorizationGrantType());
        }
        if (oauth2AuthorizationMp.getAuthorizedScopes() != null && !oauth2AuthorizationMp.getAuthorizedScopes().isEmpty()) {
            wrapper.like("authorized_scopes", oauth2AuthorizationMp.getAuthorizedScopes());
        }
        if (oauth2AuthorizationMp.getAttributes() != null) {
            wrapper.like("attributes", oauth2AuthorizationMp.getAttributes());
        }
        if (oauth2AuthorizationMp.getState() != null && !oauth2AuthorizationMp.getState().isEmpty()) {
            wrapper.like("state", oauth2AuthorizationMp.getState());
        }
        if (oauth2AuthorizationMp.getAuthorizationCodeValue() != null) {
            wrapper.like("authorization_code_value", oauth2AuthorizationMp.getAuthorizationCodeValue());
        }
        if (oauth2AuthorizationMp.getAuthorizationCodeIssuedAt() != null) {
            wrapper.like("authorization_code_issued_at", oauth2AuthorizationMp.getAuthorizationCodeIssuedAt());
        }
        if (oauth2AuthorizationMp.getAuthorizationCodeExpiresAt() != null) {
            wrapper.like("authorization_code_expires_at", oauth2AuthorizationMp.getAuthorizationCodeExpiresAt());
        }
        if (oauth2AuthorizationMp.getAuthorizationCodeMetadata() != null) {
            wrapper.like("authorization_code_metadata", oauth2AuthorizationMp.getAuthorizationCodeMetadata());
        }
        if (oauth2AuthorizationMp.getAccessTokenValue() != null) {
            wrapper.like("access_token_value", oauth2AuthorizationMp.getAccessTokenValue());
        }
        if (oauth2AuthorizationMp.getAccessTokenIssuedAt() != null) {
            wrapper.like("access_token_issued_at", oauth2AuthorizationMp.getAccessTokenIssuedAt());
        }
        if (oauth2AuthorizationMp.getAccessTokenExpiresAt() != null) {
            wrapper.like("access_token_expires_at", oauth2AuthorizationMp.getAccessTokenExpiresAt());
        }
        if (oauth2AuthorizationMp.getAccessTokenMetadata() != null) {
            wrapper.like("access_token_metadata", oauth2AuthorizationMp.getAccessTokenMetadata());
        }
        if (oauth2AuthorizationMp.getAccessTokenType() != null && !oauth2AuthorizationMp.getAccessTokenType().isEmpty()) {
            wrapper.like("access_token_type", oauth2AuthorizationMp.getAccessTokenType());
        }
        if (oauth2AuthorizationMp.getAccessTokenScopes() != null && !oauth2AuthorizationMp.getAccessTokenScopes().isEmpty()) {
            wrapper.like("access_token_scopes", oauth2AuthorizationMp.getAccessTokenScopes());
        }
        if (oauth2AuthorizationMp.getOidcIdTokenValue() != null) {
            wrapper.like("oidc_id_token_value", oauth2AuthorizationMp.getOidcIdTokenValue());
        }
        if (oauth2AuthorizationMp.getOidcIdTokenIssuedAt() != null) {
            wrapper.like("oidc_id_token_issued_at", oauth2AuthorizationMp.getOidcIdTokenIssuedAt());
        }
        if (oauth2AuthorizationMp.getOidcIdTokenExpiresAt() != null) {
            wrapper.like("oidc_id_token_expires_at", oauth2AuthorizationMp.getOidcIdTokenExpiresAt());
        }
        if (oauth2AuthorizationMp.getOidcIdTokenMetadata() != null) {
            wrapper.like("oidc_id_token_metadata", oauth2AuthorizationMp.getOidcIdTokenMetadata());
        }
        if (oauth2AuthorizationMp.getRefreshTokenValue() != null) {
            wrapper.like("refresh_token_value", oauth2AuthorizationMp.getRefreshTokenValue());
        }
        if (oauth2AuthorizationMp.getRefreshTokenIssuedAt() != null) {
            wrapper.like("refresh_token_issued_at", oauth2AuthorizationMp.getRefreshTokenIssuedAt());
        }
        if (oauth2AuthorizationMp.getRefreshTokenExpiresAt() != null) {
            wrapper.like("refresh_token_expires_at", oauth2AuthorizationMp.getRefreshTokenExpiresAt());
        }
        if (oauth2AuthorizationMp.getRefreshTokenMetadata() != null) {
            wrapper.like("refresh_token_metadata", oauth2AuthorizationMp.getRefreshTokenMetadata());
        }
        if (oauth2AuthorizationMp.getUserCodeValue() != null) {
            wrapper.like("user_code_value", oauth2AuthorizationMp.getUserCodeValue());
        }
        if (oauth2AuthorizationMp.getUserCodeIssuedAt() != null) {
            wrapper.like("user_code_issued_at", oauth2AuthorizationMp.getUserCodeIssuedAt());
        }
        if (oauth2AuthorizationMp.getUserCodeExpiresAt() != null) {
            wrapper.like("user_code_expires_at", oauth2AuthorizationMp.getUserCodeExpiresAt());
        }
        if (oauth2AuthorizationMp.getUserCodeMetadata() != null) {
            wrapper.like("user_code_metadata", oauth2AuthorizationMp.getUserCodeMetadata());
        }
        if (oauth2AuthorizationMp.getDeviceCodeValue() != null) {
            wrapper.like("device_code_value", oauth2AuthorizationMp.getDeviceCodeValue());
        }
        if (oauth2AuthorizationMp.getDeviceCodeIssuedAt() != null) {
            wrapper.like("device_code_issued_at", oauth2AuthorizationMp.getDeviceCodeIssuedAt());
        }
        if (oauth2AuthorizationMp.getDeviceCodeExpiresAt() != null) {
            wrapper.like("device_code_expires_at", oauth2AuthorizationMp.getDeviceCodeExpiresAt());
        }
        if (oauth2AuthorizationMp.getDeviceCodeMetadata() != null) {
            wrapper.like("device_code_metadata", oauth2AuthorizationMp.getDeviceCodeMetadata());
        }

        return this.page(page, wrapper);
    }

}
