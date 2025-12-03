package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Integer id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Boolean gender;
    private String avatarUrl;
    private String role;
    private String userStatus;
    private Instant createdAt;
}
