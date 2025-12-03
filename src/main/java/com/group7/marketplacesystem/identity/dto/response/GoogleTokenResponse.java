package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoogleTokenResponse {
    private String access_token;
    private String expires_in;
    private String scope;
    private String token_type;
    private String id_token;
}
