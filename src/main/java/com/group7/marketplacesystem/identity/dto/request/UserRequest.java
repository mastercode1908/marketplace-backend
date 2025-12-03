package com.group7.marketplacesystem.identity.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {

    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private Boolean gender;
    private String avatarUrl;
    private String role;
    private String userStatus;
}
