package com.scccy.service.auth.domain.vo;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
public class RegisteredClientVo  {
    private String id;
    private String clientId;
    private String clientName;
    private Date clientIdIssuedAt;
    private Date clientSecretExpiresAt;
    private Set<String> clientAuthenticationMethods;
    private Set<String> authorizationGrantTypes;
    private Set<String> redirectUris;
    private Set<String> scopes;
}



