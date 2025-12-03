package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthUserInfo {
    private Integer id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String avatarUrl;
}
