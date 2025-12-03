package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoogleUserInfoResponse {
    private String sub;
    private String email;
    private String name;
    private String picture;
    private boolean email_verified;
}
